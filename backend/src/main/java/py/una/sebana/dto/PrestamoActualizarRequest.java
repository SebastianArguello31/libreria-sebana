package py.una.sebana.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PrestamoActualizarRequest(
        @NotNull @Min(7) @Max(30)
        Integer duracionTotalDias
) {}