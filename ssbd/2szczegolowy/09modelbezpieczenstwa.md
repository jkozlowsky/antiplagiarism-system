
## 9 Model bezpieczeństwa komponentów aplikacji sieciowej 5 pkt


W celu zabezpieczenia komponentów aplikacji sieciowej zastosowano model kontroli dostępu opartej na rolach – RBAC (Role Based Access Control). Wykorzystano do tego szkielet aplikacyjny _Spring Security_. Publiczne metody komponentów (kontrolerów i serwisów) zostały opatrzone odpowiednimi adnotacjami (np. `@PreAuthorize`), mającymi na celu ochronę przed nieuprawnionym dostępem.

### Deklaratywna kontrola dostępu

Poniższa tabela 2.9.1 przedstawia mapowanie wybranych przypadków użycia na metody klas kontrolerów i serwisów wraz z zastosowanymi adnotacjami bezpieczeństwa. Brak adnotacji `@PreAuthorize` na warstwie kontrolera dla metod publicznych (takich jak logowanie czy rejestracja) przy odpowiedniej konfiguracji `SecurityFilterChain` w Spring Security domyślnie oznacza dostęp publiczny (`permitAll()`) — oznaczone w tabeli jako **default**.

Tabela 2.9.1: Deklaratywna kontrola dostępu w aspekcie metod przypadków użycia

| Przypadek użycia                                  | Metoda                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  | Adnotacja                                                                                                                                                                                                                                                                                                                                           |
|:--------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **MOK.1 (Rejestracja konta użytkownika)**         | `accountController.register(@Validated(OnCreate.class) @RequestBody RegisterAccountDTO dto)`<br>`accountController.activate(@RequestParam String token)`<br>`accountService.registerAccount(Account account)`<br>`accountService.confirmActivation(String token)`<br>`languageDictRepository.findByLanguage(String language)`<br>`accessLevelDictRepository.findByAccessLevelName(String accessLevelName)`<br>`accountRepository.saveAndFlush(Account entity)`<br>`accountRepository.findByActivationToken(String activationToken)`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     | default<br>default<br>default<br>default<br>default<br>default<br>default<br>default                                                                                                                                                                                                                                                                |
| **MOK.2 (Uwierzytelnienie użytkownika)**          | `authController.login(@Valid @RequestBody LoginRequestDTO loginRequest, HttpServletRequest request)`<br>`authController.verify2FACode(@Valid @RequestBody Verify2FARequestDTO requestDTO, HttpServletRequest request, @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "pl") String langHeader)`<br>`authController.resend2FACode(@Valid @RequestBody Resend2FARequestDTO requestDTO)`<br>`authController.loginWithGoogle(@Valid @RequestBody GoogleLoginRequestDTO requestDTO, HttpServletRequest request)`<br>`authService.authenticate(String login, String password, String ipAddress)`<br>`authService.verify2FactorCode(String token, String login, String ipAddress, String langHeader)`<br>`authService.resend2FactorCode(String login)`<br>`authService.authenticateWithGoogle(String idTokenString, String ipAddress)`<br>`accountRepository.findByLogin(String login)`<br>`accountRepository.findByEmail(String email)`<br>`tokenRepository.findByTokenValueAndUsernameAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(String token, String username, OneTimeTokenType tokenType, LocalDateTime now)`<br>`tokenRepository.findByUsernameAndTokenTypeAndIsUsedIsFalse(String username, OneTimeTokenType tokenType)`<br>`tokenRepository.saveAndFlush(OneTimeTokenEnt entity)` | default<br>default<br>default<br>default<br>default<br>default<br>default<br>default<br>default<br>default<br>default<br>default<br>default                                                                                                                                                                                                         |
| **MOK.3 (Reset hasła)**                           | `passwordResetController.requestReset(@RequestBody @Validated(OnCreate.class) ResetPasswordDTO request)`<br>`passwordResetController.confirmReset(@RequestBody @Validated(OnCreate.class) ConfirmResetDto request)`<br>`passwordResetService.initiatePasswordReset(String email)`<br>`passwordResetService.resolvePasswordReset(String token, String passwordHash)`<br>`accountRepository.findByEmail(String email)`<br>`tokenRepository.findByUsernameAndTokenTypeAndIsUsedIsFalse(String username, OneTimeTokenType tokenType)`<br>`tokenRepository.saveAllAndFlush(Iterable<OneTimeTokenEnt> entities)`<br>`tokenRepository.saveAndFlush(OneTimeTokenEnt entity)`<br>`tokenRepository.findByTokenValueAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(String token, OneTimeTokenType tokenType, LocalDateTime now)`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    | default<br>default<br>default<br>default<br>default<br>default<br>default<br>default<br>default                                                                                                                                                                                                                                                     |
| **MOK.4 (Zablokuj konto użytkownika)**            | `accountController.blockUserAccount(@PathVariable UUID id, @RequestHeader("If-Match") String ifMatch)`<br>`accountService.blockUserAccount(UUID id, String versionHash)`<br>`accountRepository.findById(UUID id)`<br>`mokRefreshRepository.refresh(AbstractEntity entity)`<br>`accountRepository.saveAndFlush(Account entity)`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          | `@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")`<br>`@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")`<br>default<br>default<br>default                                                                                                 |
| **MOK.5 (Odblokuj konto użytkownika)**            | `accountController.unblockUserAccount(@PathVariable UUID id, @RequestHeader("If-Match") String ifMatch)`<br>`accountService.unblockUserAccount(UUID id, String versionHash)`<br>`accountRepository.findById(UUID id)`<br>`mokRefreshRepository.refresh(AbstractEntity entity)`<br>`accountRepository.saveAndFlush(Account entity)`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      | `@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")`<br>`@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")`<br>default<br>default<br>default                                                                                                 |
| **MOK.6 (Dołącz poziom dostępu do konta)**        | `accountController.grantAccessLevel(@PathVariable UUID id, @PathVariable String accessLevelName, @RequestHeader("If-Match") String ifMatch)`<br>`accountService.grantAccessLevel(UUID uuid, String accessLevelName, String versionHash)`<br>`accountRepository.findById(UUID id)`<br>`mokRefreshRepository.refresh(AbstractEntity entity)`<br>`accessLevelDictRepository.findByAccessLevelName(String accessLevelName)`<br>`accountRepository.saveAndFlush(Account entity)`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             | `@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")`<br>`@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")`<br>default<br>default<br>default<br>default                                                                                      |
| **MOK.7 (Odłącz poziom dostępu od konta)**        | `accountController.revokeAccessLevel(@PathVariable UUID id, @PathVariable String accessLevelName, @RequestHeader("If-Match") String ifMatch)`<br>`accountService.revokeAccessLevel(UUID uuid, String accessLevelName, String versionHash)`<br>`accountRepository.findById(UUID id)`<br>`mokRefreshRepository.refresh(AbstractEntity entity)`<br>`accessLevelDictRepository.findByAccessLevelName(String accessLevelName)`<br>`accountRepository.saveAndFlush(Account entity)`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           | `@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")`<br>`@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")`<br>default<br>default<br>default<br>default                                                                                      |
| **MOK.8 (Zmień własne hasło)**                    | `accountController.changeOwnPassword(@Valid @RequestBody ChangeOwnPasswordDto dto, @RequestHeader("If-Match") String ifMatch, Principal principal)`<br>`accountService.changeOwnPassword(String login, String oldPassword, String newPassword, String ifMatch)`<br>`accountRepository.findByLogin(String login)`<br>`mokRefreshRepository.refresh(AbstractEntity entity)`<br>`accountRepository.saveAndFlush(Account entity)`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           | `@PreAuthorize("isAuthenticated()")`<br>`@PreAuthorize("isAuthenticated()")`<br>default<br>default<br>default                                                                                                                                                                                                                                       |
| **MOK.9 (Zmień hasło innego użytkownika)**        | `accountController.changeOtherPassword(@PathVariable String login, @Valid @RequestBody ChangeOtherPasswordDto dto, @RequestHeader("If-Match") String ifMatch)`<br>`accountService.changeOtherPassword(String login, String newPassword, String ifMatch)`<br>`accountRepository.findByLogin(String login)`<br>`mokRefreshRepository.refresh(AbstractEntity entity)`<br>`accountRepository.saveAndFlush(Account entity)`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  | `@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN) and #login != authentication.name")`<br>`@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")`<br>default<br>default<br>default                                                               |
| **MOK.10 (Zmień adres e-mail własnego konta)**    | `accountController.requestEmailChange(Authentication authentication)`<br>`accountController.confirmEmailChange(@Valid @RequestBody OwnEmailChangeDTO request)`<br>`accountController.resendEmailChangeRequest(Authentication authentication)`<br>`accountController.revertEmailChange(@RequestParam String token)`<br>`accountService.createUrlOwnEmailChange(String login)`<br>`accountService.confirmOwnEmailChange(String token, String password, String newEmail)`<br>`accountService.resendUrlOwnEmailChange(String login)`<br>`accountService.revertEmailChange(String token)`<br>`accountRepository.findByLogin(String login)`<br>`tokenRepository.saveAndFlush(OneTimeTokenEnt entity)`<br>`tokenRepository.findByTokenValueAndTokenTypeAndIsUsedIsFalseAndExpiresAtAfter(String token, OneTimeTokenType tokenType, LocalDateTime now)`<br>`revertTokenRepository.saveAndFlush(EmailRevertTokenEnt entity)`<br>`revertTokenRepository.findByTokenValue(String tokenValue)`<br>`accountRepository.saveAndFlush(Account entity)`                                                                                                                                                                                                                                                                  | default<br>default<br>default<br>default<br>default<br>default<br>default<br>default<br>default<br>default<br>default<br>default<br>default<br>default                                                                                                                                                                                              |
| **MOK.11 (Zmień adres e-mail innego konta)**      | `accountController.adminChangeAccountEmail(@PathVariable String login, @Validated(OnCreate.class) @RequestBody ChangeEmailDTO changeEmailDTO, @RequestHeader("If-Match") String ifMatch)`<br>`accountService.changeEmailByAdmin(String login, String newEmail, String frontendVersionHash)`<br>`accountRepository.findByLogin(String login)`<br>`mokRefreshRepository.refresh(AbstractEntity entity)`<br>`accountRepository.saveAndFlush(Account entity)`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               | `@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN) and #login != authentication.name")`<br>`@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")`<br>default<br>default<br>default                                                               |
| **MOK.12 (Edytuj dane własnego konta)**           | `accountController.updateMyAccount(@Validated(OnUpdate.class) @RequestBody AccountDTO accountDTO, @RequestHeader("If-Match") String ifMatch, Principal principal)`<br>`accountService.updateAccountByLogin(String login, String versionHash, Account source)`<br>`accountRepository.findByLogin(String login)`<br>`mokRefreshRepository.refresh(AbstractEntity entity)`<br>`accountRepository.saveAndFlush(Account entity)`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             | `@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN) or #login == authentication.name")`<br>`@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN) or #login == authentication.name")`<br>default<br>default<br>default                               |
| **MOK.13 (Edytuj dane konta innego użytkownika)** | `accountController.updateOtherAccount(@PathVariable UUID id, @RequestBody @Validated(OnUpdate.class) AccountDTO accountDTO, @RequestHeader("If-Match") String ifMatch)`<br>`accountService.updateAccount(UUID id, String versionHash, Account source)`<br>`accountRepository.findById(UUID id)`<br>`mokRefreshRepository.refresh(AbstractEntity entity)`<br>`accountRepository.saveAndFlush(Account entity)`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            | `@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")`<br>`@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")`<br>default<br>default<br>default                                                                                                 |
| **MOK.14 (Wyloguj)**                              | `jwtAuthService.logout(HttpServletRequest httpServletRequest, HttpServletResponse response, Authentication authentication)`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             | default                                                                                                                                                                                                                                                                                                                                             |
| **MOK.15 (Wyświetl listę użytkowników)**          | `accountController.getAllAccounts(@RequestParam(required = false) String phrase, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size)`<br>`accountService.getAllAccounts(int page, int size)`<br>`accountService.searchAccounts(String phrase, int page, int size)`<br>`accountRepository.findAll(Pageable pageable)`<br>`accountRepository.findByLoginContainingIgnoreCaseOrEmailContainingIgnoreCaseOrNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(String login, String email, String name, String surname, Pageable pageable)`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          | `@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")`<br>`@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")`<br>`@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")`<br>default<br>default |

### Mapowanie grup i tożsamości użytkowników na role aplikacji

Tabela 2.9.2: Mapowanie tożsamości użytkowników i grup na role aplikacji w Spring Security.

| Grupa             | Rola w aplikacji |
|:------------------|:-----------------|
| **Administrator** | `ADMIN`          |
| **Prowadzący**    | `TEACHER`        |
| **Student**       | `STUDENT`        |

Sprawdzanie ról na warstwie obsługi żądań odbywa się deklaratywnie przy użyciu adnotacji bezpieczeństwa bazujących na uprawnieniach, np.: `@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")`. Zmienne przechowujące stałe nazwy ról zdefiniowane są w klasie `AccountConsts`.

### Mechanizm uwierzytelniania użytkowników — konfiguracja

```java
package pl.lodz.p.it.ssbd2026.ssbd01.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthService jwtAuthService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(httpSecuritySessionManagementConfigurer ->
                        httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Brak lub niepoprawny token autoryzacyjny.\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"Brak uprawnień do tego zasobu.\"}");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/account/register", "/account/activate").permitAll()
                        .requestMatchers("/auth/login", "/auth/refresh", "/auth/check2FA", "/auth/resend2FA", "/auth/google").permitAll()
                        .requestMatchers("/auth/reset/request", "/auth/reset/confirm").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info", "/actuator/loggers", "/actuator/loggers/**").permitAll()
                        .requestMatchers("/account/email/confirm").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthFilter(jwtAuthService), UsernamePasswordAuthenticationFilter.class)

                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .addLogoutHandler(jwtAuthService)
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)));
        return http.build();
    }

    @Bean
    public PasswordEncoder encoder(){
        return new BCryptPasswordEncoder();
    }

    @Value("${app.frontend.url:https://localhost}")
    private String frontendUrl;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendUrl));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Correlation-Id", "If-Match", "Accept-Language"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

Listing 2.9.3. Konfiguracja łańcucha filtrów bezpieczeństwa (`SecurityFilterChain`).

Na podstawie listingu 2.9.3 możemy zobaczyć, że w projekcie punkty wejścia dla błędów uwierzytelniania (`AuthenticationEntryPoint`) i braku dostępu (`AccessDeniedHandler`) zostały zdefiniowane bezpośrednio w konfiguracji łańcucha filtrów bezpieczeństwa (`SecurityFilterChain`) przy użyciu wyrażeń lambda, zwracając komunikaty w formacie JSON oraz odpowiednie kody HTTP (401 Unauthorized i 403 Forbidden).

### Mechanizm uwierzytelniania użytkowników — implementacja

```java
package pl.lodz.p.it.ssbd2026.ssbd01.config.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtAuthService jwtAuthService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        try {
            String username = jwtAuthService.extractUsername(token);

            if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }
            if (!jwtAuthService.isTokenValid(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            List<String> roles = jwtAuthService.extractRoles(token);

            var authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.warn("JWT token has expired: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Token expired\"}");
            return;
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid token format\"}");
            return;
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/auth/refresh");
    }
}
```

Listing 2.9.4. Implementacja głównego filtra uwierzytelniającego do aplikacji. 

Na podstawie listingu 2.9.4 możemy zobaczyć główny filtr uwierzytelniający do aplikacji, parsujący nagłówek `Authorization`, weryfikujący token i odtwarzający kontekst użytkownika w `SecurityContextHolder`.

### Mechanizm żetonów JWT

```java
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

    public String generateToken(String username, List<String> roles) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", roles);
        return createToken(extraClaims, username, expirationTime);
    }

    public String generateRefreshToken(String username, List<String> roles) {
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

    }
}
```

Listing 2.9.5. Komponent `JwtAuthService` odpowiedzialny za generowanie, odczytywanie i sprawdzanie poprawności żetonów JWT.

#### Tworzenie i podpisywanie żetonu JWT

Żetony typu `accessToken` oraz `refreshToken` są tworzone przez wspólną metodę (`createToken(Map<String, Object> extraClaims, String subject, long ttlInSeconds)`), a ich budowa przedstawia się w sposób następujący:

```java
Jwts.builder()
    .claims(extraClaims)
    .subject(subject)
    .issuedAt(new Date(System.currentTimeMillis()))
    .expiration(new Date(System.currentTimeMillis() + (ttlInSeconds * 1000)))
    .signWith(this.secretKey)
    .compact();
```

Listing 2.9.6. Konstruowanie i podpis żetonu — dotyczy tokena dostępu (`accessToken`) i odświeżania (`refreshToken`).

Listing 2.9.6 przedstawia konstruowanie i podpis żetonów, gdzie:
* `claims` – dodatkowe informacje zaszyte w tokenie, w tym lista ról autoryzacyjnych (roles), przypisana do konta w momencie uwierzytelniania.
* `subject` – login konta użytkownika.
* `issuedAt` – moment wytworzenia żetonu.
* `expiration` – moment wygaśnięcia żetonu, wczytywany dynamicznie z pliku konfiguracyjnego application.properties (domyślnie jest to 30 minut dla tokena dostępu i 24 godziny dla tokena odświeżania).
* `signWith` – podpis żetonu przy użyciu klucza symetrycznego (wygenerowanego na podstawie współdzielonego sekretu zapisanego w konfiguracji aplikacji)..

Typ `accessToken` jest przekazywany w nagłówku typu `Bearer` i służy do przyznawania dostępu do zasobów użytkownikowi, który jest do nich autoryzowany. Z kolei typ `refreshToken` służy do odświeżania i uzyskania nowego żetonu dostępu, bez potrzeby podawania hasła przez użytkownika po stronie logowania. Żetony przekazywane są klientowi w formie struktury JSON (z wykorzystaniem `AuthResponseDTO`).

#### Odkodowanie żetonów JWT i weryfikacja podpisu

Za pomocą narzędzi ze strony https://www.jwt.io/ po rozkodowaniu token dostępu (`accessToken`) wygląda w następujący sposób:

```json
{
  "alg": "HS512"
}
```

Listing 2.9.7. Nagłówek żetonu dostępu.

```json
{
  "roles": [
    "STUDENT"
  ],
  "sub": "rbar",
  "iat": 1778688748,
  "exp": 1778690548
}
```

Listing 2.9.8. Ciało żetonu dostępu.

```json
{
  "alg": "HS512"
}
```

Listing 2.9.9. Nagłówek żetonu odświeżania.

```json
{
  "roles": [
    "STUDENT"
  ],
  "sub": "rbar",
  "iat": 1778688748,
  "exp": 1778775148
}
```

Listing 2.9.10. Ciało żetonu odświeżania.

#### Czas trwania sesji uwierzytelnionego użytkownika

Ponieważ aplikacja implementuje autentykację bezstanową opartą o standard HTTP i mechanizm JWT (ustawiono `SessionCreationPolicy.STATELESS`), klasyczne sesje serwerowe (`HttpSession`) w ogóle nie są tworzone. Czas trwania sesji uwierzytelnionego użytkownika uwarunkowany jest czasem życia tokena dostępu (`accessToken`), który jest konfigurowalny we właściwościach konfiguracyjnych i domyślnie wynosi 30 minut (od momentu zalogowania). Po upływie tego czasu klient automatycznie utraci dostęp, chyba że wcześniej posłuży się tokenem odświeżającym (`refreshToken`) do pozyskania nowego tokena dostępu (`accessToken`), co przedłuża jego stan uwierzytelnienia. Utrata żetonu (jego wygaśnięcie lub skasowanie z pamięci lokalnej przeglądarki poprzez wylogowanie) jest równoznaczna z automatycznym przerwaniem dostępu i zakończeniem sesji użytkownika.

### Mechanizm składowania haseł w postaci skrótu

W systemie zaimplementowano mechanizm bezpiecznego składowania haseł z wykorzystaniem funkcji skrótu `BCrypt`. Rozwiązanie to zapewnia, że hasła użytkowników nigdy nie są przechowywane w bazie danych w postaci jawnej.

#### Konfiguracja mechanizmu

```java
@Bean
public PasswordEncoder encoder(){
    return new BCryptPasswordEncoder();
}
```

Listing 2.9.11. Definicja komponentu szyfrującego hasła.

Na listingu 2.9.11 widać komponent `PasswordEncoder`, zdefiniowany w klasie konfiguracji bezpieczeństwa `SecurityConfig` (listing 2.9.3). Wykorzystuje on implementację `BCryptPasswordEncoder` dostarczaną przez Spring Security.

#### Wykorzystanie podczas rejestracji/zmiany hasła (Składowanie)

```java
@MethodCallLogged
@PreAuthorize("hasAuthority(T(pl.lodz.p.it.ssbd2026.ssbd01.config.consts.AccountConsts).ROLE_ADMIN)")
@Retryable(retryFor = ConcurrentUpdateException.class)
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
public void changeOtherPassword(String login, String newPassword, String ifMatch) {
    Account account = accountRepository.findByLogin(login).orElseThrow(AccountNotFoundException::new);

    mokRefreshRepository.refresh(account);
    if (!ifMatch.equals(versionHashService.hashObjectVersion(account.getId(), account.getVersion()))) {
        throw new ConcurrentUpdateException();
    }

    if (passwordEncoder.matches(newPassword, account.getPasswordHash())) {
        throw new PasswordSameAsOldException();
    }

    account.setPasswordHash(passwordEncoder.encode(newPassword));
    accountRepository.saveAndFlush(account);

}
```

Listing 2.9.12. Proces weryfikacji skrótu oraz hashowania hasła przed zapisem do bazy danych przy zmianie hasła innego użytkownika jako administrator (metoda `changeOtherPassword` z klasy `AccountService`).

Powyższy listing 2.9.12 przedstawia metodę `changeOtherPassword` z klasy `AccountService`, w której jest weryfikowane najpierw, czy nowe hasło nie jest takie samo jak poprzednie (za pomocą metody `matches()`), a następnie nowe hasło jest zapisywane w postaci niejawnej (za pomocą `encode()`).

#### Wykorzystanie podczas logowania (Weryfikacja)

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager", noRollbackFor = InvalidCredentialsProvidedException.class)
@Retryable(retryFor = ConcurrentUpdateException.class, backoff = @Backoff(delayExpression = "${app.retry.backoff}"))
public void authenticate(String login, String password, String ipAddress) {
    Account account = accountRepository.findByLogin(login).orElseThrow(InvalidCredentialsProvidedException::new);

    if (account.getIsBlocked()) {
        throw new AccountIsBlockedException();
    }

    if (!account.getIsVerified()) {
        throw new AccountIsNotVerifiedException();
    }

    if (!passwordEncoder.matches(password, account.getPasswordHash())) {
        handleFailedLogin(account, ipAddress);
        throw new InvalidCredentialsProvidedException();
    }
    generate2FactorCode(account);
}
```

Listing 2.9.13. Weryfikacja hasła algorytmem `BCrypt` podczas procesu uwierzytelniania w klasie `AuthService` (metoda `authenticate`).

Na listingu 2.9.13 widać, że podczas uwierzytelniania system pobiera zaszyfrowane hasło z bazy danych i porównuje je z hasłem jawnym, wprowadzonym przez użytkownika w żądaniu logowania. Służy do tego metoda `matches()`, która automatycznie odczytuje sól (salt) z zapisanego skrótu i wylicza hash z podanego hasła, sprawdzając ich zgodność.

```
Należy też zaprezentować rozwiązanie zapewniające zgłaszanie do dziennika zdarzeń, w którym dla każdego wywołania metody biznesowej każdego z komponentów aplikacji sieciowej odnotowane będą co najmniej:
* tożsamość użytkownika, na rzecz którego wykonywany jest przypadek użycia,
* nazwa wykonywanej metody i klasy, której jest ona członkiem,
* wartości (reprezentacja tekstowa) parametrów i wartości zwracanej, o ile takowe istnieją,
* w przypadku zakończenia metody przez zgłoszenie wyjątku - nazwa i przyczyna (cause) wyjątku.
```



```
W ramach prezentacji rozwiązania należy zacytować kod klas i metod realizujących funkcjonalność dziennika zdarzeń oraz fragment kodu wybranego (przykładowego) komponentu obrazujący zastosowanie rozwiązania.

_Realizacja: Wszystkie metody biznesowe wszystkich komponentów (we wszystkich modułach funkcjonalnych aplikacji) muszą być opatrzone stosownymi adnotacjami konfigurującymi deklaratywny model bezpieczeństwa._

_Przedstawione rozwiązanie tworzenia dziennika zdarzeń musi być zaimplementowane i zastosowane do wszystkich klas / metod komponentów_
```