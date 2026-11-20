package py.una.sebana.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @Valid @NotNull Jwt jwt,
        @Valid @NotNull Cors cors,
        @Valid @NotNull Seed seed
) {

    public record Jwt(
            @NotBlank String secret,
            @NotNull Duration expiration
    ) { }

    public record Cors(
            @NotBlank String allowedOrigin
    ) { }

    public record Seed(
            @NotBlank String adminPassword,
            @NotBlank String usuarioPassword
    ) { }
}