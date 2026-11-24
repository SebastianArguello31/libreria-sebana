package py.una.sebana.dto;

import py.una.sebana.entity.Libro;
import java.time.OffsetDateTime;

public record LibroResponse(
        Integer id,
        String titulo,
        String autor,
        String isbn,
        String editorial,
        Short anioPublicacion,
        String descripcion,
        Integer stock,
        boolean activo,
        OffsetDateTime creadoEn
) {
    public static LibroResponse from(Libro libro) {
        return new LibroResponse(
                libro.getId(),
                libro.getTitulo(),
                libro.getAutor(),
                libro.getIsbn(),
                libro.getEditorial(),
                libro.getAnioPublicacion(),
                libro.getDescripcion(),
                libro.getStock(),
                libro.isActivo(),
                libro.getCreadoEn()
        );
    }
}