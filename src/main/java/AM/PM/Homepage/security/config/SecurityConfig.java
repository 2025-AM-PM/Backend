package AM.PM.Homepage.security.config;

import AM.PM.Homepage.member.student.domain.Student;
import AM.PM.Homepage.member.student.repository.RefreshTokenRepository;
import AM.PM.Homepage.member.student.repository.StudentRepository;
import AM.PM.Homepage.member.student.service.RefreshTokenService;
import AM.PM.Homepage.member.student.service.StudentService;
import AM.PM.Homepage.security.UserAuth;
import AM.PM.Homepage.security.filter.JwtFilter;
import AM.PM.Homepage.security.filter.StudentLoginFilter;
import AM.PM.Homepage.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collection;
import java.util.Collections;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtUtil jwtUtil;
    private final StudentService studentService;
    private final RefreshTokenService refreshTokenService;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JwtUtil jwtUtil, StudentService studentService, RefreshTokenService refreshTokenService) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
        this.studentService = studentService;
        this.refreshTokenService = refreshTokenService;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {

                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                        CorsConfiguration configuration = new CorsConfiguration();

                        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                        configuration.setAllowedMethods(Collections.singletonList("*"));
                        configuration.setAllowCredentials(true);
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setMaxAge(3600L);

                        configuration.setExposedHeaders(Collections.singletonList("Set-Cookie"));
                        configuration.setExposedHeaders(Collections.singletonList("access"));

                        return configuration;
                    }
                }));

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
                        .requestMatchers("/login", "/", "/join").permitAll()
                        .requestMatchers("/reissue").permitAll()
                        .anyRequest().authenticated());
        http
                .addFilterAt(new StudentLoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, refreshTokenService, studentService), UsernamePasswordAuthenticationFilter.class);
        http
                .addFilterBefore(new JwtFilter(jwtUtil), StudentLoginFilter.class);




        //세션 설정
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
/*
    @Bean
    public UserDetailsService users(BCryptPasswordEncoder encoder) {
        Student student =  Student.builder()
                .studentName("김덕수")
                .studentNumber("202117072")
                .password(encoder.encode("990713"))
                .studentRole("ROLE_USER")
                .build();

        UserAuth userAuth = new UserAuth(student);

        return new InMemoryUserDetailsManager(userAuth);
    }*/

}
