package py.una.sebana.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import py.una.sebana.dto.LoginRequest;
import py.una.sebana.dto.LoginResponse;
import py.una.sebana.dto.UsuarioResponse;
import py.una.sebana.dto.PasswordCambiarRequest;
import py.una.sebana.exception.RecursoNoEncontradoException;
import py.una.sebana.exception.CampoInvalidoException;
import py.una.sebana.repository.UsuarioRepository;
import py.una.sebana.security.TokenService;
import py.una.sebana.entity.Usuario;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager, TokenService tokenService, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));
        TokenService.TokenGenerado token = tokenService.generar(auth);
        UsuarioResponse usuario = buscarPorEmail(auth.getName());

        return new LoginResponse(token.token(), "Bearer", token.expiraEn(), usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .map(UsuarioResponse::from)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + email));
    }

    @Transactional
    public void cambiarPassword(String email, PasswordCambiarRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + email));

        if (!passwordEncoder.matches(request.passwordActual(), usuario.getPasswordHash())) {
            throw new CampoInvalidoException("passwordActual", "La contraseña actual es incorrecta");
        }
        if (passwordEncoder.matches(request.passwordNueva(), usuario.getPasswordHash())) {
            throw new CampoInvalidoException("passwordNueva", "La nueva contraseña debe ser distinta de la actual");
        }

        usuario.setPasswordHash(passwordEncoder.encode(request.passwordNueva()));
    }
}