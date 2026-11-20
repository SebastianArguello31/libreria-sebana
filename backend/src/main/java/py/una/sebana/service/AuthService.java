package py.una.sebana.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import py.una.sebana.dto.LoginRequest;
import py.una.sebana.dto.LoginResponse;
import py.una.sebana.dto.UsuarioResponse;
import py.una.sebana.exception.RecursoNoEncontradoException;
import py.una.sebana.repository.UsuarioRepository;
import py.una.sebana.security.TokenService;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    public AuthService(AuthenticationManager authenticationManager, TokenService tokenService, UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
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
}