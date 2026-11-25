package py.una.sebana.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import py.una.sebana.dto.PrestamoActualizarRequest;
import py.una.sebana.dto.PrestamoCrearRequest;
import py.una.sebana.dto.PrestamoResponse;
import py.una.sebana.service.PrestamoService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PrestamoController {
    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    // Rutas para que el usuario autenticado gestione sus préstamos

    @PostMapping("/prestamos")
    @PreAuthorize("hasAnyRole('ADMIN', 'USUARIO')")
    public ResponseEntity<PrestamoResponse> crearMiPrestamo(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PrestamoCrearRequest request
    ) {
        PrestamoResponse respuesta =
                prestamoService.crearParaUsuario(jwt.getSubject(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping("/prestamos/mios/actuales")
    @PreAuthorize("hasAnyRole('ADMIN', 'USUARIO')")
    public List<PrestamoResponse> listarMisPrestamosActuales(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return prestamoService.listarMisActuales(jwt.getSubject());
    }

    @GetMapping("/prestamos/mios/historial")
    @PreAuthorize("hasAnyRole('ADMIN', 'USUARIO')")
    public List<PrestamoResponse> listarMiHistorial(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return prestamoService.listarMiHistorial(jwt.getSubject());
    }

    @PutMapping("/prestamos/{prestamoId}/renovar")
    @PreAuthorize("hasAnyRole('ADMIN', 'USUARIO')")
    public PrestamoResponse renovarMiPrestamo(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer prestamoId,
            @Valid @RequestBody PrestamoActualizarRequest request
    ) {
        return prestamoService.renovarMio(
                jwt.getSubject(),
                prestamoId,
                request
        );
    }

    // Rutas administrativas

    @GetMapping("/admin/prestamos")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PrestamoResponse> listarTodos() {
        return prestamoService.listar();
    }

    @GetMapping("/admin/prestamos/{prestamoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public PrestamoResponse obtener(
            @PathVariable Integer prestamoId
    ) {
        return prestamoService.obtener(prestamoId);
    }

    @PostMapping("/admin/usuarios/{usuarioId}/prestamos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PrestamoResponse> registrarPresencial(
            @PathVariable Integer usuarioId,
            @Valid @RequestBody PrestamoCrearRequest request
    ) {
        PrestamoResponse respuesta =
                prestamoService.crearParaAdmin(usuarioId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PutMapping("/admin/prestamos/{prestamoId}/renovar")
    @PreAuthorize("hasRole('ADMIN')")
    public PrestamoResponse renovarComoAdmin(
            @PathVariable Integer prestamoId,
            @Valid @RequestBody PrestamoActualizarRequest request
    ) {
        return prestamoService.renovarComoAdmin(prestamoId, request);
    }

    @PostMapping("/admin/prestamos/{prestamoId}/devolver")
    @PreAuthorize("hasRole('ADMIN')")
    public PrestamoResponse devolver(
            @PathVariable Integer prestamoId
    ) {
        return prestamoService.devolver(prestamoId);
    }

    @DeleteMapping("/admin/prestamos/{prestamoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> anular(
            @PathVariable Integer prestamoId
    ) {
        prestamoService.anular(prestamoId);
        return ResponseEntity.noContent().build();
    }
}