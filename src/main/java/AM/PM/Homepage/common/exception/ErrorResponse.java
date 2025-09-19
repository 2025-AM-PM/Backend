package AM.PM.Homepage.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorResponse {
    private final String code;
    private final String message;
    private final HttpStatus status;

    public ErrorResponse(ErrorCode exception) {
        this.code = exception.getCode();
        this.message = exception.getMessage();
        this.status = exception.getStatus();
    }
}
