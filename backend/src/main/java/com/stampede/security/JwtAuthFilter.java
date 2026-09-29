package com.stampede.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Runs once per request. If a valid "Authorization: Bearer &lt;token&gt;" header is
 * present, it puts the authenticated person into the SecurityContext so downstream
 * handlers know who is calling. Invalid/missing tokens simply leave the request
 * unauthenticated — SecurityConfig then decides whether that endpoint is allowed.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            String personId = jwtService.extractPersonId(token);

            if (personId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                String role = jwtService.extractRole(token);
                List<SimpleGrantedAuthority> authorities =
                        role != null ? List.of(new SimpleGrantedAuthority("ROLE_" + role)) : List.of();

                var authentication = new UsernamePasswordAuthenticationToken(personId, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
