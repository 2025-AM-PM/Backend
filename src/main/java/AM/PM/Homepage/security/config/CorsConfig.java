package AM.PM.Homepage.security.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    private static final List<String> CORS_ALLOW_ORIGINS =
            Arrays.asList(
                    "http://localhost:3000",
                    "http://localhost:7000",
                    "http://localhost:8080",
                    "https://frontend-phi-ten-65.vercel.app/", // 테스트
                    "https://frontend-phi-ten-65.vercel.app/", // 메인
                    "https://ampm-test.duckdns.org",
                    "https://ampm-main.duckdns.org"
            );

    private static final List<String> CORS_ALLOW_METHODS =
            Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

    private static final List<String> CORS_ALLOW_HEADERS =
            Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin", "Location");

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration conf = new CorsConfiguration();
        conf.setAllowCredentials(true);
        conf.setAllowedOrigins(CORS_ALLOW_ORIGINS);
        conf.setAllowedMethods(CORS_ALLOW_METHODS);
        conf.setAllowedHeaders(CORS_ALLOW_HEADERS);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", conf);
        return source;
    }
}