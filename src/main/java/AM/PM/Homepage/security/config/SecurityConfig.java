package AM.PM.Homepage.security.config;

import AM.PM.Homepage.member.student.service.RefreshTokenService;
import AM.PM.Homepage.security.filter.JwtFilter;
import AM.PM.Homepage.security.filter.StudentLoginFilter;
import AM.PM.Homepage.security.handler.LoginFailureHandler;
import AM.PM.Homepage.security.handler.LoginSuccessHandler;
import AM.PM.Homepage.security.handler.LogoutHandlerImpl;
import AM.PM.Homepage.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final CorsConfigurationSource corsConfigurationSource;
    private final LogoutHandlerImpl logoutHandler;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public LoginSuccessHandler loginSuccessHandler(JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
        return new LoginSuccessHandler(jwtUtil, refreshTokenService);
    }

    @Bean
    public LoginFailureHandler loginFailureHandler() {
        return new LoginFailureHandler();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(conf -> conf.configurationSource(corsConfigurationSource));

        //csrf disable
        http
                .csrf(AbstractHttpConfigurer::disable);

        //From 로그인 방식 disable
        http
                .formLogin(AbstractHttpConfigurer::disable);

        //http basic 인증 방식 disable
        http
                .httpBasic(AbstractHttpConfigurer::disable);

        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(
                                "/**", // 임시로 전체 허용
                                "/",
                                "/login",
                                "/join",
                                "/api/reissue",
                                "/health",
                                "/static/**"
                        ).permitAll()
                        .requestMatchers(
                                "/temp" // 나중에 ADMIN 생기면 설정
                        ).hasRole("ADMIN")
                        .anyRequest().authenticated());

        StudentLoginFilter studentLoginFilter = new StudentLoginFilter(
                authenticationManager(authenticationConfiguration));
        studentLoginFilter.setFilterProcessesUrl("/api/student/login");
        studentLoginFilter.setSuccessHandler(loginSuccessHandler(jwtUtil, refreshTokenService));
        studentLoginFilter.setFailureHandler(loginFailureHandler());

        http
                .addFilterAt(studentLoginFilter,
                        UsernamePasswordAuthenticationFilter.class);
        http
                .addFilterBefore(new JwtFilter(jwtUtil), StudentLoginFilter.class);

        //세션 설정
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http
                .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin));

        http
                .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/api/student/login")
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler((request, response, authentication) ->
                        response.setStatus(HttpServletResponse.SC_OK)) // 로그아웃 성공 시 200 OK 응답
                );

        return http.build();
    }
}
