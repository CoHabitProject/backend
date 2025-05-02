package fr.esgi.rest;

import fr.esgi.dto.auth.LoginReqDto;
import fr.esgi.dto.auth.RefreshReqDto;
import fr.esgi.dto.auth.RegisterReqDto;
import fr.esgi.service.KeycloakAuthService;
import fr.esgi.service.KeycloakRegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthRest {

    private final KeycloakRegistrationService regService;
    private final KeycloakAuthService         authService;

    public AuthRest(KeycloakRegistrationService regService,
                    KeycloakAuthService authService) {
        this.regService = regService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterReqDto dto) throws
                                                                          KeycloakRegistrationService.UserAlreadyExistsException {
        regService.register(dto.getUsername(), dto.getEmail(), dto.getPassword());
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String,Object>>> login(@RequestBody LoginReqDto dto) {
        return authService.login(dto.getUsername(), dto.getPassword())
                          .map(tokens -> ResponseEntity.ok(tokens));
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<Map<String,Object>>> refresh(@RequestBody RefreshReqDto dto) {
        return authService.refresh(dto.getRefreshToken())
                          .map(tokens -> ResponseEntity.ok(tokens));
    }
}



