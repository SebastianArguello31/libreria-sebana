package py.una.sebana.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import py.una.sebana.dto.LibroRequest;
import py.una.sebana.dto.LibroResponse;
import py.una.sebana.entity.Libro;
import py.una.sebana.exception.RecursoNoEncontradoException;
import py.una.sebana.repository.LibroRepository;

import java.util.List;

@Service
public class LibroService {
    private final LibroRepository libroRepository;

    public LibroService(LibroRepository libroRepository) {
        this.libroRepository = libroRepository;
    }

    @Transactional(readOnly = true)
    public List<LibroResponse> listar() {
        return libroRepository.findAllByOrderByTituloAsc()
                .stream()
                .map(LibroResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public LibroResponse obtener(Integer id) {
        return LibroResponse.from(buscarLibro(id));
    }

    @Transactional
    public LibroResponse crear(LibroRequest request) {
        String isbn = normalizarOpcional(request.isbn());
        validarIsbnDisponible(isbn, null);

        Libro libro = new Libro(
                request.titulo().trim(),
                request.autor().trim(),
                isbn,
                normalizarOpcional(request.editorial()),
                request.anioPublicacion(),
                normalizarOpcional(request.descripcion()),
                request.stock()
        );

        return LibroResponse.from(libroRepository.save(libro));
    }

    @Transactional
    public LibroResponse actualizar(Integer id, LibroRequest request) {
        Libro libro = buscarLibro(id);
        String isbn = normalizarOpcional(request.isbn());
        validarIsbnDisponible(isbn, id);

        libro.setTitulo(request.titulo().trim());
        libro.setAutor(request.autor().trim());
        libro.setIsbn(isbn);
        libro.setEditorial(normalizarOpcional(request.editorial()));
        libro.setAnioPublicacion(request.anioPublicacion());
        libro.setDescripcion(normalizarOpcional(request.descripcion()));
        libro.setStock(request.stock());

        return LibroResponse.from(libroRepository.save(libro));
    }

    @Transactional
    public void darDeBaja(Integer id) {
        Libro libro = buscarLibro(id);
        libro.setActivo(false);
        libroRepository.save(libro);
    }

    private Libro buscarLibro(Integer id) {
        return libroRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException("No se encontró el libro con ID " + id));
    }

    private void validarIsbnDisponible(String isbn, Integer idActual) {
        if (isbn == null) {
            return;
        }

        boolean duplicado = idActual == null
                ? libroRepository.existsByIsbn(isbn)
                : libroRepository.existsByIsbnAndIdNot(isbn, idActual);

        if (duplicado) {
            throw new DataIntegrityViolationException("El ISBN ya está registrado");
        }
    }

    private String normalizarOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}