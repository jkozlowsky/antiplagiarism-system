package pl.lodz.p.it.ssbd2026.ssbd01.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.repositories.AccountRepository;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtAuthService implements LogoutHandler {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expTime}")
    private long expirationTime;

    @Value("${security.jwt.refreshExpTime}")
    private long refreshExpirationTime;

    private SecretKey secretKey;

    private final AccountRepository accountRepository;
    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, List<String> roles, Boolean forcePasswordChange) {
        if (Boolean.TRUE.equals(forcePasswordChange)) {
            roles = List.of(AccountConsts.ROLE_FORCE_PASSWORD_CHANGE);
        }
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", roles);
//        extraClaims.put("token_version", tokenVersion);
        return createToken(extraClaims, username, expirationTime);
    }

    public String generateRefreshToken(String username, List<String> roles, Boolean forcePasswordChange) {
        if (Boolean.TRUE.equals(forcePasswordChange)) {
            roles = List.of(AccountConsts.ROLE_FORCE_PASSWORD_CHANGE);
        }
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", roles);
        return createToken(extraClaims, username, refreshExpirationTime);
    }

    private String createToken(Map<String, Object> extraClaims, String subject, long ttlInSeconds) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + (ttlInSeconds * 1000)))
                .signWith(this.secretKey)
                .compact();
    }

    public boolean isTokenValid(@NotNull @NotBlank @NotEmpty String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> claims.get("roles", List.class));
    }

    public Integer extractTokenVersion(String token) {
        return extractClaim(token, claims -> claims.get("token_version", Integer.class));
    }

//    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, transactionManager = "mokTransactionManager")
//    public boolean checkTokenVersion(String token){
//        try {
//            Integer tokenVersion = extractTokenVersion(token);
//            String username = extractUsername(token);
//
//            if (tokenVersion == null || username == null) {
//                return false;
//            }
//
//            Account account = accountRepository.findByLogin(username)
//                    .orElseThrow(() -> new AccountNotFoundException("Account not found"));
//
//            return account.getTokenVersion().equals(tokenVersion);
//
//        } catch (AccountNotFoundException | JwtException | IllegalArgumentException e) {
//            return false;
//        }
//    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(this.secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public void logout(HttpServletRequest httpServletRequest, @NonNull HttpServletResponse response, Authentication authentication) {
        //Architektura bezstanowa więc metoda nic nie robi.
    }

    public String generateShortLivedToken(String username, List<String> roles) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", roles);
        return createToken(extraClaims, username, expirationTime / 2);
    }

}
