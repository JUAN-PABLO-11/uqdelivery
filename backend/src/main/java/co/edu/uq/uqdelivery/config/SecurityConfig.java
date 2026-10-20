package co.edu.uq.uqdelivery.config;

import co.edu.uq.uqdelivery.security.JwtAuthFilter;
import co.edu.uq.uqdelivery.security.SecurityErrorHandlers;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtFilter,
                                           SecurityErrorHandlers errorHandlers) throws Exception {
        return http
                .csrf(c -> c.disable())                      // API sin cookies: usa JWT
                .cors(Customizer.withDefaults())             // usa el bean de CorsConfig
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(errorHandlers)   // 401
                        .accessDeniedHandler(errorHandlers))       // 403
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/repartidor/**").hasRole("REPARTIDOR")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * JwtAuthFilter es @Component y Spring Boot lo registraría también como filtro "normal".
     * Eso lo ejecuta fuera de Spring Security y el usuario quedaría sin autenticar (401).
     * Aquí se desactiva ese registro extra: solo corre dentro de la cadena de seguridad.
     */
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilterRegistration(JwtAuthFilter filter) {
        FilterRegistrationBean<JwtAuthFilter> registro = new FilterRegistrationBean<>(filter);
        registro.setEnabled(false);
        return registro;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
