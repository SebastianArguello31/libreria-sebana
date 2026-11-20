package py.una.sebana.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String mensaje,
        String path,
        Map<String, String> campos
) {
    public static ErrorResponse of(int status, String error, String mensaje, String path) {
        return new ErrorResponse(Instant.now(), status, error, mensaje, path, null);
    }
}