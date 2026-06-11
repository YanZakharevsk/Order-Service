package com.innowise.order_service.security;

import com.innowise.order_service.exception.InvalidJwtTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    @Value("${security.jwt.token.secret-key:secret-key}")
    private String secretKey;

    private SecretKey signingKey;

    @PostConstruct
    protected void init(){
        try{
            byte[] keyBytes = MessageDigest.getInstance("SHA-256").digest(secretKey.getBytes(StandardCharsets.UTF_8));
            signingKey = Keys.hmacShaKeyFor(keyBytes);
        }catch (NoSuchAlgorithmException ex){
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    public boolean validateToken(String token){
        try{
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        }catch (JwtException | IllegalArgumentException ex){
            throw new InvalidJwtTokenException("Expired or invalid JWT token");
        }
    }

    public Long getUserIdFromToken(String token){
        Claims claims = getClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    public String getRoleFromToken(String token){
        Claims claims = getClaims(token);
        return claims.get("role", String.class);
    }

    private Claims getClaims(String token){
        try{
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }catch (JwtException | IllegalArgumentException ex){
            throw new InvalidJwtTokenException("Expired or invalid JWT token");
        }
    }

    public UsernamePasswordAuthenticationToken getAuthentication(String token){

        Long userId = getUserIdFromToken(token);
        String userRole = getRoleFromToken(token);

        String formattedRole = userRole.startsWith("ROLE_") ? userRole : "ROLE_" + userRole;

        return new UsernamePasswordAuthenticationToken(
                userId,
                "",
                List.of(new SimpleGrantedAuthority(formattedRole)));
    }
}
