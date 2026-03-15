package com.securevault.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();

        if(path.equals("/admin/login") || path.equals("/") || path.equals("/ping")) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = req.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {

            res.setStatus(401);
            return;
        }

        String token = authHeader.substring(7);

        if(!JwtUtil.validateToken(token)) {

            res.setStatus(401);
            return;
        }

        chain.doFilter(request, response);
    }
}
