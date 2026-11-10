package vn.onlinelearn.config;

import java.util.List;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import vn.onlinelearn.security.JwtFilter;

@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }
    @Bean SecurityFilterChain security(HttpSecurity http, JwtFilter jwt, @Value("${app.frontend-url}") String origin) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable).cors(c -> c.configurationSource(r -> {
            var cors = new CorsConfiguration();
            var origins = new java.util.ArrayList<String>(); origins.add(origin);
            var uri = java.net.URI.create(origin);
            if ("localhost".equals(uri.getHost())) origins.add(origin.replace("localhost", "127.0.0.1"));
            else if ("127.0.0.1".equals(uri.getHost())) origins.add(origin.replace("127.0.0.1", "localhost"));
            cors.setAllowedOrigins(origins);
            cors.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
            cors.setAllowedHeaders(List.of("Authorization","Content-Type","X-Registration-Token")); return cors;
        })).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(a -> a
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers("/api/payments/sepay/ipn", "/api/payments/sepay/success", "/api/payments/sepay/error", "/api/payments/sepay/cancel").permitAll()
            .requestMatchers("/actuator/health", "/api/public/**", "/api/auth/register", "/api/auth/login", "/api/auth/verify", "/api/auth/forgot", "/api/auth/reset", "/api/auth/resend", "/api/payments/vnpay/ipn").permitAll()
            .requestMatchers("/api/registrations", "/api/registrations/*", "/api/registrations/*/payments", "/api/registrations/*/cancel").permitAll()
            .requestMatchers("/api/admin/**").hasRole("Admin")
            .requestMatchers("/api/manage/**").hasAnyRole("Admin","Author")
            .requestMatchers("/api/sales/**").hasAnyRole("Admin","Sale")
            .requestMatchers("/api/marketing/**").hasAnyRole("Admin","Marketing")
            .requestMatchers("/api/dashboard/**").hasAnyRole("Admin","Sale","Marketing")
            .requestMatchers("/api/learning/**", "/api/my/**", "/api/attempts/**").hasRole("Customer")
            .anyRequest().authenticated())
        .exceptionHandling(e -> e.authenticationEntryPoint((r,s,x) -> { s.setStatus(401); s.setContentType("application/json;charset=UTF-8"); s.getWriter().write("{\"message\":\"Vui lòng đăng nhập.\"}"); })
            .accessDeniedHandler((r,s,x) -> { s.setStatus(403); s.setContentType("application/json;charset=UTF-8"); s.getWriter().write("{\"message\":\"Bạn không có quyền truy cập.\"}"); }))
        .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class).build();
    }
}
