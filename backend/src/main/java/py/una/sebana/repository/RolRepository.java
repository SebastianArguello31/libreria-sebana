package py.una.sebana.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import py.una.sebana.entity.Rol;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByNombre(String nombre);
}