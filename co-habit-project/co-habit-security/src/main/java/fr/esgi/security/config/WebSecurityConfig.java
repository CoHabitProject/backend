package fr.esgi.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration keycloak = ClientRegistration.withRegistrationId("keycloak")
                                                        .clientId("co-habit-confidential")
                                                        .clientSecret("secret")
                                                        .scope("openid","profile","email")
                                                        .authorizationUri("http://localhost:8080/realms/co-habit/protocol/openid-connect/auth")
                                                        .tokenUri("http://localhost:8080/realms/co-habit/protocol/openid-connect/token")
                                                        .jwkSetUri("http://localhost:8080/realms/co-habit/protocol/openid-connect/certs")
                                                        .userInfoUri("http://localhost:8080/realms/co-habit/protocol/openid-connect/userinfo")
                                                        .issuerUri("http://localhost:8080/realms/co-habit")
                                                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                                        .redirectUri("http://localhost:8081/login/oauth2/code/keycloak")
                                                        .userNameAttributeName(IdTokenClaimNames.SUB)
                                                        .build();
        return new InMemoryClientRegistrationRepository(keycloak);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws
                                                              Exception {
        http.authorizeHttpRequests(authz -> authz
                    .requestMatchers("/", "/index", "/css/**", "/js/**")
                    .permitAll()
                    .requestMatchers("/customers/**")
                    .authenticated()
            )
            /*.oauth2Login(oauth2 -> oauth2
                    .userInfoEndpoint(userInfo -> userInfo
                            .userAuthoritiesMapper(this.userAuthoritiesMapper())
                    )
            )*/
            .oauth2Login(withDefaults())
            .logout(logout -> logout
                    .logoutSuccessUrl("/")
            );

        return http.build();
    }

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return authorities -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                if (authority instanceof OidcUserAuthority oidcUserAuthority) {
                    mappedAuthorities.addAll(extractKeycloakAuthorities(oidcUserAuthority.getIdToken()
                                                                                         .getClaims()));
                } else if (authority instanceof OAuth2UserAuthority oauth2UserAuthority) {
                    mappedAuthorities.addAll(extractKeycloakAuthorities(oauth2UserAuthority.getAttributes()));
                }
            });

            return mappedAuthorities;
        };
    }

    private Collection<GrantedAuthority> extractKeycloakAuthorities(Map<String, Object> claims) {
        Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
        if (realmAccess == null) {
            return Set.of();
        }

        Collection<String> roles = (Collection<String>) realmAccess.get("roles");
        if (roles == null) {
            return Set.of();
        }

        return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toSet());
    }
}
