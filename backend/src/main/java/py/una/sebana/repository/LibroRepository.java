package py.una.sebana.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import py.una.sebana.entity.Libro;

import java.util.List;

public interface LibroRepository extends JpaRepository<Libro, Integer> {
    List<Libro> findAllByOrderByTituloAsc();

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Integer id);
}