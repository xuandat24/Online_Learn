package vn.onlinelearn.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.onlinelearn.repository.UserRepository;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UserRepository users;
    public JwtFilter(JwtService jwt, UserRepository users) { this.jwt=jwt; this.users=users; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                var token = jwt.decode(header.substring(7));
                var user = users.findById(Long.parseLong(token.getSubject())).orElseThrow();
                Number version = token.getClaim("version");
                if (!"Active".equals(user.status) || user.emailVerifiedAt == null || version == null || version.intValue() != user.tokenVersion) throw new IllegalArgumentException();
                var auth = new UsernamePasswordAuthenticationToken(new Actor(user.userId, user.email, user.roleCode), null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.roleCode)));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (org.springframework.security.oauth2.jwt.JwtException | IllegalArgumentException | java.util.NoSuchElementException e) {
                response.setStatus(401); response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"message\":\"Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.\"}"); return;
            }
        }
        chain.doFilter(request, response);
    }
}
