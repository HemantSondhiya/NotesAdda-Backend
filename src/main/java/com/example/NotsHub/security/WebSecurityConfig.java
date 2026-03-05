package com.example.NotsHub.security;

import com.example.NotsHub.security.jwt.AuthEntryPointJwt;
import com.example.NotsHub.security.jwt.AuthTokenFilter;
import com.example.NotsHub.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;
    @Autowired
    private RateLimitFilter rateLimitFilter;

    @Value("${app.security.expose-dev-endpoints:false}")
    private boolean exposeDevEndpoints;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {
                })
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers(HttpMethod.GET, "/api/auth/users").hasAnyRole("UNIVERSITY_ADMIN", "SUPER_ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/auth/user").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/auth/username").authenticated()
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/public/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/universities/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/programs/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/notes").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/notes/search").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/notes/*/download").permitAll()
                    .requestMatchers("/actuator/health/**").permitAll()
                    .requestMatchers("/actuator/info").permitAll()
                    .requestMatchers("/actuator/**").hasAnyRole("UNIVERSITY_ADMIN", "SUPER_ADMIN")
                    .requestMatchers("/images/**").permitAll()
                    .requestMatchers("/api/admin/**").hasAnyRole("UNIVERSITY_ADMIN", "SUPER_ADMIN")
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

            if (exposeDevEndpoints) {
                auth.requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/v2/api-docs").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/configuration/ui").permitAll()
                        .requestMatchers("/configuration/security").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll();
            } else {
                auth.requestMatchers("/v3/api-docs/**").denyAll()
                        .requestMatchers("/v2/api-docs").denyAll()
                        .requestMatchers("/swagger-ui/**").denyAll()
                        .requestMatchers("/swagger-ui.html").denyAll()
                        .requestMatchers("/swagger-resources/**").denyAll()
                        .requestMatchers("/configuration/ui").denyAll()
                        .requestMatchers("/configuration/security").denyAll()
                        .requestMatchers("/webjars/**").denyAll()
                        .requestMatchers("/h2-console/**").denyAll();
            }

            auth.anyRequest().authenticated();
        });

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(authenticationJwtTokenFilter(),
                UsernamePasswordAuthenticationFilter.class);
        http.headers(headers -> headers.frameOptions(frameOptions -> {
            if (exposeDevEndpoints) {
                frameOptions.sameOrigin();
            } else {
                frameOptions.deny();
            }
        }));

        return http.build();
    }
}
