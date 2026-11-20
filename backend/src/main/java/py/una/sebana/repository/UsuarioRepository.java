package py.una.sebana.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import py.una.sebana.entity.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    @EntityGraph(attributePaths = "rol")
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}