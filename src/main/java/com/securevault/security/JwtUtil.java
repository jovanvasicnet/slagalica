package com.securevault.security;

import io.jsonwebtoken.*;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "c2xhZ2FsaWNhX3N1cGVyX3NlY3VyZV9qd3Rfc2VjcmV0X2tleV8yMDI2";

    public static String generateToken(String username) {

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    public static boolean validateToken(String token) {

        try {

            Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token);

            return true;

        } catch(Exception e) {
            return false;
        }
    }
}

