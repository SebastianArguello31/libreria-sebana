package py.una.sebana.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PrestamoCrearRequest(
        @NotNull @Positive
        Integer libroId,

        @NotNull @Min(7) @Max(30)
        Integer duracionDias
) {}