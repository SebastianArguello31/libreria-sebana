package py.una.sebana.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import py.una.sebana.dto.ErrorResponse;
import py.una.sebana.dto.LoginRequest;
import py.una.sebana.dto.LoginResponse;
import py.una.sebana.dto.UsuarioResponse;
import py.una.sebana.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Inicio de sesión y datos del usuario autenticado")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Iniciar sesión", description = "Valida email y contraseña y devuelve un JWT válido por 2 horas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login correcto"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Email o contraseña incorrectos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Usuario actual", description = "Devuelve los datos del usuario dueño del token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario autenticado"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) {
        return authService.buscarPorEmail(jwt.getSubject());
    }

    @PutMapping("/me/password")
    @Operation(summary = "Cambiar mi contraseña", description = "Requiere la contraseña actual.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contraseña actualizada"),
            @ApiResponse(responseCode = "400", description = "Contraseña actual incorrecta o nueva inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> cambiarPassword(@AuthenticationPrincipal Jwt jwt,
                                                @Valid @RequestBody PasswordCambiarRequest request) {
        authService.cambiarPassword(jwt.getSubject(), request);
        return ResponseEntity.noContent().build();
    }
}