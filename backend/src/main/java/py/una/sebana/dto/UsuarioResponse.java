package py.una.sebana.dto;

import py.una.sebana.entity.Usuario;

public record UsuarioResponse(
        Integer id,
        String email,
        String nombreCompleto,
        String rol
) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombreCompleto(),
                usuario.getRol().getNombre()
        );
    }
}