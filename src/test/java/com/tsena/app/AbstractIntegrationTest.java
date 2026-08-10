package com.tsena.app;

import com.tsena.app.entity.Utilisateur;
import com.tsena.app.security.UtilisateurPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

@Testcontainers
public abstract class AbstractIntegrationTest {

    protected static final String JWT_SECRET_TEST =
            "test-secret-key-for-integration-tests-only-do-not-use-in-prod-32bytes-min";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void proprietesDynamiques(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("jwt.secret", () -> JWT_SECRET_TEST);
    }

    protected static RequestPostProcessor authentifieComme(Utilisateur utilisateur) {
        UtilisateurPrincipal principal = new UtilisateurPrincipal(utilisateur);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name())));
        return SecurityMockMvcRequestPostProcessors.authentication(authentication);
    }
}
