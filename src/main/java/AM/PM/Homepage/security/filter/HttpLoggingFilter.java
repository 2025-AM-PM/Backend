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
        try {
            filterChain.doFilter(request, response);
        } finally {
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            String uri = request.getRequestURI()
                    + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
            int status = response.getStatus();
            if (HttpStatusCode.valueOf(status).isError()) {
                log.error("[HTTP] {} {} -> res={}, tookMs={}ms", request.getMethod(), uri, status, tookMs);
            } else {
                log.info("[HTTP] {} {} -> res={}, tookMs={}ms", request.getMethod(), uri, status, tookMs);
            }
        }
    }
}
