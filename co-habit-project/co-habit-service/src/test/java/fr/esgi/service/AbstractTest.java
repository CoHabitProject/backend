package fr.esgi.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AbstractTest {

    /**
     * Initializes the security context with a placeholder user for testing purposes.
     * This method sets up a mock authentication token with predefined user details.
     * <br>
     * <br>
     * List of claims :
     * <ol>
     *     <li>sub: User ID => 123</li>
     *     <li>preferred_username: Username => johndoe</li>
     *     <li>email : "john@example.com"</li>
     * </ol>
     */
    protected void initSecurityContextPlaceHolder() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USR1"));
        authorities.add(new SimpleGrantedAuthority("ROLE_USR2"));

        final String        TEST_USER_ID    = "123";
        final String        TEST_USERNAME   = "johndoe";
        final String        TEST_EMAIL      = "john@example.com";
        final LocalDateTime TEST_CREATED_AT = LocalDateTime.now();

        Jwt jwt = Jwt.withTokenValue("token")
                     .claim("sub", TEST_USER_ID)
                     .claim("preferred_username", TEST_USERNAME)
                     .claim("email", TEST_EMAIL)
                     .claim("email_verified", true)
                     .claim("realm_access", Map.of("roles", List.of("view-profil")))
                     .header("alg", "none")
                     .build();

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, authorities);
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext()
                             .setAuthentication(authentication);
    }

    ;
}
