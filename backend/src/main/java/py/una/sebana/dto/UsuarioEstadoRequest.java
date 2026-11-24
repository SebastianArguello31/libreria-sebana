package py.una.sebana.dto;

import jakarta.validation.constraints.NotNull;

public record UsuarioEstadoRequest(
        @NotNull(message = "El campo activo es obligatorio")
        Boolean activo
) { }