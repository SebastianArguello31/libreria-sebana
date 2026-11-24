package py.una.sebana.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import py.una.sebana.entity.EstadoPrestamo;
import py.una.sebana.entity.Prestamo;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PrestamoRepository extends JpaRepository<Prestamo, Integer> {
    @EntityGraph(attributePaths = {"usuario", "libro"})
    List<Prestamo> findAllByOrderByFechaPrestamoDescIdDesc();

    @EntityGraph(attributePaths = {"usuario", "libro"})
    List<Prestamo> findAllByUsuario_IdAndEstadoOrderByFechaPrestamoDescIdDesc(
            Integer usuarioId,
            EstadoPrestamo estado
    );

    @EntityGraph(attributePaths = {"usuario", "libro"})
    List<Prestamo> findAllByUsuario_IdAndEstadoInOrderByFechaPrestamoDescIdDesc(
            Integer usuarioId,
            Collection<EstadoPrestamo> estados
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Prestamo p WHERE p.id = :id")
    Optional<Prestamo> findByIdForUpdate(@Param("id") Integer id);

    @Query("""
            SELECT MIN(p.fechaVencimiento)
            FROM Prestamo p
            WHERE p.libro.id = :libroId
              AND p.estado = :estado
            """)
    Optional<LocalDate> buscarProximaFechaDisponible(
            @Param("libroId") Integer libroId,
            @Param("estado") EstadoPrestamo estado
    );
}