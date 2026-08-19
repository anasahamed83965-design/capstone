package com.college.babysitter.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Creates and validates HS256 JWTs. The token carries the account id and
 * role as claims so controllers can authorize without extra database reads.
 */
@Service
public class JwtService {

    private final String secret;
    private final long expirationMs;

    /**
     * Reads the signing secret and lifetime from configuration. The secret
     * must come from the JWT_SECRET env var in production, never from code.
     *
     * @param secret HMAC signing secret
     * @param expirationMs token lifetime in milliseconds
     */
    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    private Key signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Issues a signed token for an authenticated account.
     *
     * @param userDetails the account (its username becomes the token subject)
     * @param userId account id stored as a claim
     * @param role account role stored as a claim
     * @return compact JWT string
     */
    public String generateToken(UserDetails userDetails, Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        return buildToken(claims, userDetails.getUsername());
    }

    private String buildToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Reads the token subject (the account email).
     *
     * @param token compact JWT string
     * @return the subject email
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Reads the account-id claim.
     *
     * @param token compact JWT string
     * @return the account id
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Reads the role claim.
     *
     * @param token compact JWT string
     * @return the account role name
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Parses and signature-checks a token, then resolves one claim from it.
     *
     * @param token compact JWT string
     * @param resolver picks the wanted claim out of the parsed claims
     * @param <T> claim type
     * @return the resolved claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return resolver.apply(claims);
    }

    /**
     * Checks that a token belongs to the given account and has not expired.
     *
     * @param token compact JWT string
     * @param userDetails the account the token should belong to
     * @return true when the token is usable
     */
    public boolean isValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isExpired(token);
    }

    private boolean isExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
