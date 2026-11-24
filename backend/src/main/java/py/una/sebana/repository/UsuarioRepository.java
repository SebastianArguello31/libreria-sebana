package py.una.sebana.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import py.una.sebana.entity.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer>, JpaSpecificationExecutor<Usuario> {
    @EntityGraph(attributePaths = "rol")
    Optional<Usuario> findByEmail(String email);

    @EntityGraph(attributePaths = "rol")
    Optional<Usuario> findById(Integer id);

    @EntityGraph(attributePaths = "rol")
    Page<Usuario> findAll(Specification<Usuario> spec, Pageable pageable);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Integer id);

    long countByRol_NombreAndActivoTrue(String nombreRol);
}