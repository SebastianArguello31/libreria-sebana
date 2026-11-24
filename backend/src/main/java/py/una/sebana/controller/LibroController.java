package py.una.sebana.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import py.una.sebana.dto.LibroRequest;
import py.una.sebana.dto.LibroResponse;
import py.una.sebana.service.LibroService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/libros")
@PreAuthorize("hasRole('ADMIN')")
public class LibroController {
    private final LibroService libroService;

    public LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    @GetMapping
    public List<LibroResponse> listar() {
        return libroService.listar();
    }

    @GetMapping("/{id}")
    public LibroResponse obtener(@PathVariable Integer id) {
        return libroService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<LibroResponse> crear(
            @Valid @RequestBody LibroRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(libroService.crear(request));
    }

    @PutMapping("/{id}")
    public LibroResponse actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody LibroRequest request
    ) {
        return libroService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Integer id) {
        libroService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }
}