package py.una.sebana.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import py.una.sebana.dto.*;
import py.una.sebana.service.UsuarioService;

import java.net.URI;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Usuarios", description = "Gestión de usuarios (solo administradores)")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "El usuario no es administrador",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Filtra por texto (nombre o email), rol y estado. Todos los filtros son opcionales.")
    public Page<UsuarioResponse> listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) Boolean activo,
            @ParameterObject @PageableDefault(size = 10, sort = "nombreCompleto") Pageable pageable) {
        return usuarioService.listar(texto, rol, activo, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario")
    @ApiResponse(responseCode = "404", description = "No existe el usuario",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public UsuarioResponse obtener(@PathVariable Integer id) {
        return usuarioService.obtener(id);
    }

    @PostMapping
    @Operation(summary = "Crear usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El email ya está registrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCrearRequest request) {
        UsuarioResponse creado = usuarioService.crear(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar usuario", description = "Modifica email, nombre y rol. No cambia la contraseña ni el estado.")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe el usuario",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email duplicado o regla de negocio violada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UsuarioResponse actualizar(@PathVariable Integer id,
                                      @Valid @RequestBody UsuarioActualizarRequest request,
                                      @AuthenticationPrincipal Jwt jwt) {
        return usuarioService.actualizar(id, request, jwt.getSubject());
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar usuario", description = "Un usuario desactivado no puede iniciar sesión.")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "No existe el usuario",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Intento de desactivarse a sí mismo o al último admin",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UsuarioResponse cambiarEstado(@PathVariable Integer id,
                                         @Valid @RequestBody UsuarioEstadoRequest request,
                                         @AuthenticationPrincipal Jwt jwt) {
        return usuarioService.cambiarEstado(id, request.activo(), jwt.getSubject());
    }

    @PutMapping("/{id}/password")
    @Operation(summary = "Restablecer contraseña", description = "El administrador asigna una contraseña nueva sin conocer la anterior.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contraseña actualizada"),
            @ApiResponse(responseCode = "400", description = "Contraseña inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe el usuario",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> restablecerPassword(@PathVariable Integer id,
                                                    @Valid @RequestBody PasswordRestablecerRequest request) {
        usuarioService.restablecerPassword(id, request);
        return ResponseEntity.noContent().build();
    }
}