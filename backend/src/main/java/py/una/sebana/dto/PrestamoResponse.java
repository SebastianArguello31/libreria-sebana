package py.una.sebana.dto;

import py.una.sebana.entity.EstadoPrestamo;
import py.una.sebana.entity.Prestamo;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record PrestamoResponse(
        Integer id,
        Integer usuarioId,
        String nombreUsuario,
        String emailUsuario,
        Integer libroId,
        String tituloLibro,
        String isbnLibro,
        Short duracionDias,
        LocalDate fechaPrestamo,
        LocalDate fechaVencimiento,
        LocalDate fechaDevolucion,
        EstadoPrestamo estado,
        OffsetDateTime creadoEn
) {
    public static PrestamoResponse from(Prestamo prestamo) {
        return new PrestamoResponse(
                prestamo.getId(),
                prestamo.getUsuario().getId(),
                prestamo.getUsuario().getNombreCompleto(),
                prestamo.getUsuario().getEmail(),
                prestamo.getLibro().getId(),
                prestamo.getLibro().getTitulo(),
                prestamo.getLibro().getIsbn(),
                prestamo.getDuracionDias(),
                prestamo.getFechaPrestamo(),
                prestamo.getFechaVencimiento(),
                prestamo.getFechaDevolucion(),
                prestamo.getEstado(),
                prestamo.getCreadoEn()
        );
    }
}