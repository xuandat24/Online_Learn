// Thư mục: src/main/java/com/onlinelearn/security/CustomAuthenticationSuccessHandler.java
package com.onlinelearn.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String targetUrl = "/";
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            if ("ROLE_ADMIN".equals(role)) {
                targetUrl = "/admin/dashboard";
                break;
            } else if ("ROLE_SALE".equals(role)) {
                targetUrl = "/sale";
                break;
            } else if ("ROLE_MARKETING".equals(role)) {
                targetUrl = "/marketing";
                break;
            } else if ("ROLE_EXPERT".equals(role)) {
                targetUrl = "/content";
                break;
            } else if ("ROLE_CUSTOMER".equals(role) || "ROLE_USER".equals(role)) {
                targetUrl = "/user/dashboard";
                break;
            }
        }

        if (response.isCommitted()) {
            return;
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
