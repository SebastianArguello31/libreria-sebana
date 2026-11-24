package py.una.sebana.repository;

import org.springframework.data.jpa.domain.Specification;
import py.una.sebana.entity.Usuario;

public final class UsuarioSpecs {
    private UsuarioSpecs() { }

    public static Specification<Usuario> filtrar(String texto, String rol, Boolean activo) {
        Specification<Usuario> spec = Specification.unrestricted();

        if (texto != null && !texto.isBlank()) {
            String patron = "%" + texto.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("nombreCompleto")), patron),
                    cb.like(cb.lower(root.get("email")), patron)
            ));
        }
        if (rol != null && !rol.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("rol").get("nombre"), rol));
        }
        if (activo != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("activo"), activo));
        }
        return spec;
    }
}