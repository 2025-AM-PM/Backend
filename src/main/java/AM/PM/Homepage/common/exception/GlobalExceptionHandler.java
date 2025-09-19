package AM.PM.Homepage.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        log.error("[Exception] code={}, message={}, status={}, detail={}",
                errorCode.getCode(), errorCode.getMessage(), errorCode.getStatus().toString(), exception.getDetail());
        ErrorResponse response = new ErrorResponse(errorCode);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
