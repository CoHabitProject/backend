package fr.esgi.rest.rest;

import fr.esgi.rest.dto.auth.LoginReqDto;
import fr.esgi.rest.dto.auth.RefreshReqDto;
import fr.esgi.rest.dto.auth.RegisterReqDto;
import fr.esgi.security.service.KeycloakAuthService;
import fr.esgi.security.service.KeycloakRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API pour gérer l'authentification des utilisateurs")
public class AuthRest {

    private final KeycloakRegistrationService regService;
    private final KeycloakAuthService         authService;

    public AuthRest(KeycloakRegistrationService regService,
                    KeycloakAuthService authService) {
        this.regService = regService;
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Inscription utilisateur", 
               description = "Permet de créer un nouveau compte utilisateur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Utilisateur créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Requête invalide"),
        @ApiResponse(responseCode = "409", description = "Utilisateur déjà existant")
    })
    public ResponseEntity<Void> register(@RequestBody RegisterReqDto dto) throws
                                                                          KeycloakRegistrationService.UserAlreadyExistsException {
        regService.register(dto.getUsername(), dto.getEmail(), dto.getPassword());
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion utilisateur", 
               description = "Authentifie un utilisateur et retourne des tokens d'accès et de rafraîchissement")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authentification réussie", 
                     content = @Content(schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "401", description = "Identifiants incorrects")
    })
    public Mono<ResponseEntity<Map<String,Object>>> login(@RequestBody LoginReqDto dto) {
        return authService.login(dto.getUsername(), dto.getPassword())
                          .map(tokens -> ResponseEntity.ok(tokens));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchir le token", 
               description = "Génère un nouveau token d'accès à partir d'un token de rafraîchissement valide")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token rafraîchi avec succès", 
                     content = @Content(schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "401", description = "Token de rafraîchissement invalide ou expiré")
    })
    public Mono<ResponseEntity<Map<String,Object>>> refresh(@RequestBody RefreshReqDto dto) {
        return authService.refresh(dto.getRefreshToken())
                          .map(tokens -> ResponseEntity.ok(tokens));
    }
}



