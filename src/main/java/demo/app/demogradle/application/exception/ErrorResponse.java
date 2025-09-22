package demo.app.demogradle.application.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponse {
    private String mensaje;
    private int codigo;
    private LocalDateTime timestamp;
    private String path;
}
