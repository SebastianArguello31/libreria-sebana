package py.una.sebana.dto;

import py.una.sebana.entity.Usuario;
import java.time.OffsetDateTime;

public record UsuarioResponse(
        Integer id,
        String email,
        String nombreCompleto,
        String rol,
        boolean activo,
        OffsetDateTime creadoEn
) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombreCompleto(),
                usuario.getRol().getNombre(),
                usuario.isActivo(),
                usuario.getCreadoEn()
        );
    }
}