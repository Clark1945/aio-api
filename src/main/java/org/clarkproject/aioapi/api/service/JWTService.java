package org.clarkproject.aioapi.api.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

public class JWTService {
    private final SecretKey secretKey;
    private final int validSeconds;
    private final JwtParser jwtParser;
    public JWTService(
            String secretKeyStr,
            int validSeconds) {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyStr.getBytes());
        this.validSeconds = validSeconds;
        this.jwtParser = Jwts.parser().verifyWith(secretKey).build();
    }

    public String createLoginAccessToken(UserDetails user) {
        // 計算過期時間
        long expirationMillis = Instant.now()
                .plusSeconds(validSeconds)
                .getEpochSecond()
                * 1000;

        // 準備 payload 內容
        Claims claims = Jwts.claims()
                .issuedAt(new Date())
                .expiration(new Date(expirationMillis))
                .add("username", user.getUsername())
                .add("authorities", user.getAuthorities())
                .build();

        // 簽名後產生 JWT
        return Jwts.builder()
                .claims(claims)
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String jwt) throws JwtException {
        return jwtParser.parseSignedClaims(jwt).getPayload();
    }
}
