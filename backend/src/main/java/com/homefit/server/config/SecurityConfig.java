package com.homefit.server.config;

import com.homefit.server.security.JsonErrorWriter;
import com.homefit.server.security.TokenService;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

/**
 * Ba chuá»—i báº£o máº­t: /api/** (JWT, khÃ´ng phiÃªn), /admin/** (Ä‘Äƒng nháº­p báº±ng form, phiÃªn, CSRF, chá»‰ ADMIN)
 * vÃ  pháº§n cÃ²n láº¡i (tá»« chá»‘i háº¿t).
 */
@Configuration
@EnableConfigurationProperties({JwtProperties.class, AdminProperties.class})
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecretKey jwtSecretKey(JwtProperties props) {
        String secret = props.secret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "homefit.jwt.secret pháº£i cÃ³ Ã­t nháº¥t 32 kÃ½ tá»±. Khai bÃ¡o trong application-local.yml "
                            + "(xem application-example.yml). Náº¿u chuá»—i cÃ³ kÃ½ tá»± # hoáº·c :, Ä‘áº·t trong nhÃ¡y kÃ©p.");
        }
        if (secret.startsWith("CHANGE_ME")) {
            log.warn("homefit.jwt.secret váº«n lÃ  giÃ¡ trá»‹ máº«u. HÃ£y Ä‘á»•i sang chuá»—i ngáº«u nhiÃªn riÃªng.");
        }
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(TokenService.ISSUER));
        return decoder;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("role");
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);

        http
                .securityMatcher("/api/**")
                .cors(cors -> cors.configure(http)).csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o
                        .jwt(j -> j.jwtAuthenticationConverter(converter))
                        .authenticationEntryPoint((request, response, ex) -> {
                            response.setHeader("WWW-Authenticate", "Bearer");
                            JsonErrorWriter.write(response, HttpStatus.UNAUTHORIZED.value(), "UNAUTHORIZED",
                                    "PhiÃªn Ä‘Äƒng nháº­p khÃ´ng há»£p lá»‡ hoáº·c Ä‘Ã£ háº¿t háº¡n.");
                        })
                        .accessDeniedHandler((request, response, ex) ->
                                JsonErrorWriter.write(response, HttpStatus.FORBIDDEN.value(), "FORBIDDEN",
                                        "Báº¡n khÃ´ng cÃ³ quyá»n thá»±c hiá»‡n thao tÃ¡c nÃ y.")));
        return http.build();
    }

    /** Trang quáº£n trá»‹: Ä‘Äƒng nháº­p form + phiÃªn + CSRF (máº·c Ä‘á»‹nh báº­t), chá»‰ vai trÃ² ADMIN. */
    @Bean
    @Order(2)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/", "/admin/**")
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/", "/admin/login").permitAll()
                        .anyRequest().hasRole("ADMIN"))
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .defaultSuccessUrl("/admin", true)
                        .failureUrl("/admin/login?error"))
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout"));
        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/error").permitAll()
                        .anyRequest().denyAll())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}

