package py.una.sebana.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import py.una.sebana.entity.Rol;
import py.una.sebana.entity.Usuario;
import py.una.sebana.repository.RolRepository;
import py.una.sebana.repository.UsuarioRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties props;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           RolRepository rolRepository,
                           PasswordEncoder passwordEncoder,
                           AppProperties props) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.props = props;
    }

    @Override
    @Transactional
    public void run(String... args) {
        crearSiNoExiste("admin", props.seed().adminPassword(), "Administrador Sebana", Rol.ADMIN);
        crearSiNoExiste("lector", props.seed().usuarioPassword(), "Lector de Prueba", Rol.USUARIO);
    }

    private void crearSiNoExiste(String username, String password, String nombreCompleto, String nombreRol) {
        if (usuarioRepository.existsByUsername(username)) {
            return;
        }

        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new IllegalStateException("No existe el rol " + nombreRol + ". ¿Se aplicó la migración V1?"));

        String hash = passwordEncoder.encode(password);
        usuarioRepository.save(new Usuario(username, hash, nombreCompleto, rol));

        log.info("Usuario inicial creado: {} ({})", username, nombreRol);
    }
}