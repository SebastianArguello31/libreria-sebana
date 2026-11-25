package py.una.sebana.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import py.una.sebana.dto.LibroResponse;
import py.una.sebana.service.LibroService;

import java.util.List;

@RestController
@RequestMapping("/api/libros")
@PreAuthorize("hasAnyRole('ADMIN', 'USUARIO')")
public class LibroCatalogoController {
    private final LibroService libroService;

    public LibroCatalogoController(LibroService libroService) {
        this.libroService = libroService;
    }

    @GetMapping
    public List<LibroResponse> listarActivos() {
        return libroService.listarActivos();
    }
}