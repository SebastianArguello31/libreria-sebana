package py.una.sebana.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import py.una.sebana.dto.PrestamoActualizarRequest;
import py.una.sebana.dto.PrestamoCrearRequest;
import py.una.sebana.dto.PrestamoResponse;
import py.una.sebana.entity.EstadoPrestamo;
import py.una.sebana.entity.Libro;
import py.una.sebana.entity.Prestamo;
import py.una.sebana.entity.Usuario;
import py.una.sebana.exception.CampoInvalidoException;
import py.una.sebana.exception.ConflictoException;
import py.una.sebana.exception.RecursoNoEncontradoException;
import py.una.sebana.repository.LibroRepository;
import py.una.sebana.repository.PrestamoRepository;
import py.una.sebana.repository.UsuarioRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PrestamoService {
    private final PrestamoRepository prestamoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;

    public PrestamoService(
            PrestamoRepository prestamoRepository,
            UsuarioRepository usuarioRepository,
            LibroRepository libroRepository
    ) {
        this.prestamoRepository = prestamoRepository;
        this.usuarioRepository = usuarioRepository;
        this.libroRepository = libroRepository;
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponse> listar() {
        return prestamoRepository.findAllByOrderByFechaPrestamoDescIdDesc()
                .stream()
                .map(PrestamoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PrestamoResponse obtener(Integer id) {
        return PrestamoResponse.from(buscarPrestamo(id));
    }

    @Transactional
    public PrestamoResponse crearParaUsuario(
            String emailAutenticado,
            PrestamoCrearRequest request
    ) {
        Usuario usuario = buscarUsuarioPorEmail(emailAutenticado);
        return crearPrestamo(usuario, request);
    }

    @Transactional
    public PrestamoResponse crearParaAdmin(
            Integer usuarioId,
            PrestamoCrearRequest request
    ) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el usuario con ID " + usuarioId));

        return crearPrestamo(usuario, request);
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponse> listarMisActuales(String emailAutenticado) {
        Usuario usuario = buscarUsuarioPorEmail(emailAutenticado);

        return prestamoRepository
                .findAllByUsuario_IdAndEstadoOrderByFechaPrestamoDescIdDesc(
                        usuario.getId(),
                        EstadoPrestamo.PRESTADO
                )
                .stream()
                .map(PrestamoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponse> listarMiHistorial(String emailAutenticado) {
        Usuario usuario = buscarUsuarioPorEmail(emailAutenticado);

        return prestamoRepository
                .findAllByUsuario_IdAndEstadoInOrderByFechaPrestamoDescIdDesc(
                        usuario.getId(),
                        List.of(EstadoPrestamo.DEVUELTO, EstadoPrestamo.ANULADO)
                )
                .stream()
                .map(PrestamoResponse::from)
                .toList();
    }

    @Transactional
    public PrestamoResponse renovarMio(
            String emailAutenticado,
            Integer prestamoId,
            PrestamoActualizarRequest request
    ) {
        Usuario usuario = buscarUsuarioPorEmail(emailAutenticado);
        Prestamo prestamo = buscarPrestamoConBloqueo(prestamoId);

        if (!prestamo.getUsuario().getId().equals(usuario.getId())) {
            throw new AccessDeniedException(
                    "No tenés permiso para renovar este préstamo."
            );
        }

        return renovar(prestamo, request);
    }

    @Transactional
    public PrestamoResponse renovarComoAdmin(
            Integer prestamoId,
            PrestamoActualizarRequest request
    ) {
        return renovar(buscarPrestamoConBloqueo(prestamoId), request);
    }

    @Transactional
    public PrestamoResponse devolver(Integer prestamoId) {
        Prestamo prestamo = buscarPrestamoConBloqueo(prestamoId);

        if (prestamo.getEstado() != EstadoPrestamo.PRESTADO) {
            throw new ConflictoException(
                    "El préstamo ya fue devuelto o anulado."
            );
        }

        Libro libro = buscarLibroConBloqueo(prestamo.getLibro().getId());
        prestamo.marcarDevuelto(LocalDate.now());
        libro.setStock(libro.getStock() + 1);

        return PrestamoResponse.from(prestamoRepository.save(prestamo));
    }

    @Transactional
    public void anular(Integer prestamoId) {
        Prestamo prestamo = buscarPrestamoConBloqueo(prestamoId);

        if (prestamo.getEstado() != EstadoPrestamo.PRESTADO) {
            throw new ConflictoException(
                    "Solo se puede anular un préstamo que siga prestado."
            );
        }

        Libro libro = buscarLibroConBloqueo(prestamo.getLibro().getId());
        prestamo.anular();
        libro.setStock(libro.getStock() + 1);

        prestamoRepository.save(prestamo);
    }

    private PrestamoResponse crearPrestamo(
            Usuario usuario,
            PrestamoCrearRequest request
    ) {
        if (!usuario.isActivo()) {
            throw new CampoInvalidoException(
                    "usuarioId",
                    "No se pueden crear préstamos para un usuario inactivo."
            );
        }

        Libro libro = buscarLibroConBloqueo(request.libroId());

        if (!libro.isActivo()) {
            throw new CampoInvalidoException(
                    "libroId",
                    "No se pueden prestar libros dados de baja."
            );
        }

        if (libro.getStock() <= 0) {
            throw new ConflictoException(mensajeSinDisponibilidad(libro));
        }

        libro.setStock(libro.getStock() - 1);

        Prestamo prestamo = new Prestamo(
                usuario,
                libro,
                request.duracionDias()
        );

        return PrestamoResponse.from(prestamoRepository.save(prestamo));
    }

    private PrestamoResponse renovar(
            Prestamo prestamo,
            PrestamoActualizarRequest request
    ) {
        if (prestamo.getEstado() != EstadoPrestamo.PRESTADO) {
            throw new ConflictoException(
                    "Solo se puede renovar un préstamo que siga prestado."
            );
        }

        if (request.duracionTotalDias() <= prestamo.getDuracionDias()) {
            throw new CampoInvalidoException(
                    "duracionTotalDias",
                    "La nueva duración debe superar la duración actual."
            );
        }

        prestamo.renovar(request.duracionTotalDias());
        return PrestamoResponse.from(prestamoRepository.save(prestamo));
    }

    private Usuario buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el usuario autenticado."
                ));
    }

    private Prestamo buscarPrestamo(Integer id) {
        return prestamoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el préstamo con ID " + id
                ));
    }

    private Prestamo buscarPrestamoConBloqueo(Integer id) {
        return prestamoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el préstamo con ID " + id
                ));
    }

    private Libro buscarLibroConBloqueo(Integer id) {
        return libroRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el libro con ID " + id
                ));
    }

    private String mensajeSinDisponibilidad(Libro libro) {
        return prestamoRepository.buscarProximaFechaDisponible(
                        libro.getId(),
                        EstadoPrestamo.PRESTADO
                )
                .map(fecha -> "No hay ejemplares disponibles. "
                        + "La fecha de devolución más próxima es "
                        + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        + ".")
                .orElse("No hay ejemplares disponibles actualmente.");
    }
}