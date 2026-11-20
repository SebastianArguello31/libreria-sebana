package py.una.sebana.dto;

import java.time.Instant;

public record LoginResponse(
        String token,
        String tipo,
        Instant expiraEn,
        UsuarioResponse usuario
) { }