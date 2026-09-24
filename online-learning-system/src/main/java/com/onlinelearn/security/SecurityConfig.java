// Thư mục: src/main/java/com/onlinelearn/security/SecurityConfig.java
package com.onlinelearn.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Tắt CSRF để đơn giản hóa test API bằng Postman
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            
            // 2. Phân quyền truy cập dựa trên Role và cấu trúc thư mục
            .authorizeHttpRequests(auth -> auth
                // Static assets & public resources
                .requestMatchers("/shared/**", "/static/**", "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
                // Public common pages
                .requestMatchers("/", "/home", "/courses/**", "/common/**", "/login", "/register", "/access-denied", "/error").permitAll()
                // Role-based route access
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**", "/my/**", "/learning/**").hasAnyRole("CUSTOMER", "USER")
                .requestMatchers("/sale/**").hasRole("SALE")
                .requestMatchers("/marketing/**").hasRole("MARKETING")
                .requestMatchers("/content/**").hasAnyRole("EXPERT", "ADMIN")
                .anyRequest().permitAll()
            )
            
            // 3. Form login
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(authenticationSuccessHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            
            // 4. Logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            
            // 5. Exception handling
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
            );

        http.authenticationProvider(authenticationProvider());

        return http.build();
    }
}
