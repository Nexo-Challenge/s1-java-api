package br.com.nexo.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração central da documentação OpenAPI 3 / Swagger da API Nexo.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nexoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Nexo - API Educacional de Tecnologia Nuclear")
                        .description("API RESTful desenvolvida no contexto do Challenge FIAP / AMAZUL / Marinha para catalogação, disseminação educacional e demonstração dos impactos socioeconômicos e pacíficos da tecnologia nuclear no Brasil.")
                        .version("v1")
                        .contact(new Contact()
                                .name("Equipe Nexo Challenge")
                                .url("https://github.com/Nexo-Challenge"))
                        .license(new License()
                                .name("Uso Acadêmico - FIAP / AMAZUL / Marinha do Brasil")
                                .url("https://www.fiap.com.br")));
    }
}
