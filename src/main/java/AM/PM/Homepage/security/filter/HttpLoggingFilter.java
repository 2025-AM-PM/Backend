package AM.PM.Homepage.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class HttpLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long start = System.nanoTime();

        String uri = request.getRequestURI();
        String query = request.getQueryString();
        if (query != null) {
            uri += "?" + query;
        }

        String method = request.getMethod();
        String clientIp = resolveClientIp(request);

        try {
            // 실제 요청 처리
            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            // 예외 발생한 경우 한 번 로깅 (예외 타입 + 스택)
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            int status = safeStatus(response);

            log.error(
                    "[HTTP-EX] {} {} ip={} -> res={}, tookMs={}ms, ex={}",
                    method, uri, clientIp, status, tookMs, ex.getClass().getSimpleName(),
                    ex
            );

            // 꼭 다시 던져줘야 스프링이 에러 응답(500 등)을 정상적으로 만들어줌
            throw ex;

        } finally {
            // 정상/에러 상관없이 항상 최종 응답 상태 한 번 더 로그
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            int status = safeStatus(response);

            logByStatus(method, uri, clientIp, status, tookMs);
        }
    }

    private void logByStatus(String method, String uri,
                             String clientIp, int status, long tookMs) {

        HttpStatusCode httpStatus;
        try {
            httpStatus = HttpStatusCode.valueOf(status);
        } catch (IllegalArgumentException e) {
            // 알 수 없는 상태 코드인 경우 (거의 없겠지만 방어 코드)
            log.warn(
                    "[HTTP] {} {} ip={} -> res={}, tookMs={}ms",
                    method, uri, clientIp, status, tookMs
            );
            return;
        }

        if (httpStatus.is5xxServerError()) {
            log.error(
                    "[HTTP] {} {} ip={} -> res={}, tookMs={}ms",
                    method, uri, clientIp, status, tookMs
            );
        } else if (httpStatus.is4xxClientError()) {
            log.warn(
                    "[HTTP] {} {} ip={} -> res={}, tookMs={}ms",
                    method, uri, clientIp, status, tookMs
            );
        } else {
            log.info(
                    "[HTTP] {} {} ip={} -> res={}, tookMs={}ms",
                    method, uri, clientIp, status, tookMs
            );
        }
    }

    private int safeStatus(HttpServletResponse response) {
        int status = response.getStatus();
        // 아직 명시적으로 status가 안 세팅된 경우(0) 방어
        return status == 0 ? HttpServletResponse.SC_OK : status;
    }

    private String resolveClientIp(HttpServletRequest request) {
        // 프록시 환경 고려: X-Forwarded-For 있으면 첫 값 사용
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
