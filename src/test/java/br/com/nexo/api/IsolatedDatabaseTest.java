package br.com.nexo.api;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.util.UUID;

/** Destino exclusivo dos testes; não lê credenciais Oracle nem do ambiente. */
@ActiveProfiles("test")
public abstract class IsolatedDatabaseTest {
    @DynamicPropertySource
    static void isolatedDatabase(DynamicPropertyRegistry properties) {
        String url = "jdbc:h2:mem:nexo_test_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        properties.add("spring.datasource.url", () -> url);
        properties.add("spring.datasource.username", () -> "sa");
        properties.add("spring.datasource.password", () -> "");
        properties.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        properties.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");
        properties.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        properties.add("spring.sql.init.mode", () -> "never");
        properties.add("spring.jpa.open-in-view", () -> "false");
        properties.add("spring.h2.console.enabled", () -> "false");
    }
}
