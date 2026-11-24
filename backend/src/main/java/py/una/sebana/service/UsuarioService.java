package py.una.sebana.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import py.una.sebana.dto.UsuarioActualizarRequest;
import py.una.sebana.dto.UsuarioCrearRequest;
import py.una.sebana.dto.UsuarioResponse;
import py.una.sebana.entity.Rol;
import py.una.sebana.entity.Usuario;
import py.una.sebana.exception.ConflictoException;
import py.una.sebana.exception.RecursoNoEncontradoException;
import py.una.sebana.repository.RolRepository;
import py.una.sebana.repository.UsuarioRepository;
import py.una.sebana.repository.UsuarioSpecs;

import java.util.Locale;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(String texto, String rol, Boolean activo, Pageable pageable) {
        return usuarioRepository.findAll(UsuarioSpecs.filtrar(texto, rol, activo), pageable)
                .map(UsuarioResponse::from);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Integer id) {
        return UsuarioResponse.from(buscar(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioCrearRequest request) {
        String email = normalizarEmail(request.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new ConflictoException("Ya existe un usuario con el email " + email);
        }

        Usuario usuario = new Usuario(
                email,
                passwordEncoder.encode(request.password()),
                request.nombreCompleto().trim(),
                buscarRol(request.rol())
        );
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Integer id, UsuarioActualizarRequest request, String emailActual) {
        Usuario usuario = buscar(id);
        boolean esElMismo = usuario.getEmail().equals(emailActual);
        String email = normalizarEmail(request.email());

        if (usuarioRepository.existsByEmailAndIdNot(email, id)) {
            throw new ConflictoException("Ya existe otro usuario con el email " + email);
        }
        if (esElMismo && !email.equals(usuario.getEmail())) {
            throw new ConflictoException("No podés cambiar tu propio email: tu sesión está asociada a él");
        }

        if (!usuario.getRol().getNombre().equals(request.rol())) {
            if (esElMismo) {
                throw new ConflictoException("No podés cambiar tu propio rol");
            }
            if (esUltimoAdminActivo(usuario)) {
                throw new ConflictoException("No se puede quitar el rol al último administrador activo");
            }
            usuario.setRol(buscarRol(request.rol()));
        }

        usuario.setEmail(email);
        usuario.setNombreCompleto(request.nombreCompleto().trim());
        return UsuarioResponse.from(usuario);
    }

    @Transactional
    public UsuarioResponse cambiarEstado(Integer id, boolean activo, String emailActual) {
        Usuario usuario = buscar(id);

        if (!activo) {
            if (usuario.getEmail().equals(emailActual)) {
                throw new ConflictoException("No podés desactivar tu propio usuario");
            }
            if (esUltimoAdminActivo(usuario)) {
                throw new ConflictoException("No se puede desactivar al último administrador activo");
            }
        }

        usuario.setActivo(activo);
        return UsuarioResponse.from(usuario);
    }

    private boolean esUltimoAdminActivo(Usuario usuario) {
        return usuario.isActivo()
                && Rol.ADMIN.equals(usuario.getRol().getNombre())
                && usuarioRepository.countByRol_NombreAndActivoTrue(Rol.ADMIN) <= 1;
    }

    private Usuario buscar(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id " + id));
    }

    private Rol buscarRol(String nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el rol " + nombre));
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}