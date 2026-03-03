package com.elioneto.pixapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openFinancePixAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, jwtSecurityScheme())
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("Open Finance Pix API")
                .description("""
                        ## API REST de Iniciação de Pagamentos Pix
                        
                        Simulação do padrão **Open Finance Brasil - Fase 3** para iniciação de pagamentos Pix.
                        
                        ### Fluxo de Autenticação
                        1. Faça `POST /auth/login` com usuário e senha
                        2. Copie o `token` retornado
                        3. Clique em **Authorize 🔒** e cole: `Bearer {token}`
                        4. Agora você pode usar os endpoints protegidos
                        
                        ### Credenciais de Teste
                        - **username:** `tpp-user`
                        - **password:** `senha123`
                        
                        ### Referências
                        - [Open Finance Brasil - Docs Oficiais](https://openfinancebrasil.atlassian.net/)
                        - [Banco Central - SPI](https://www.bcb.gov.br/estabilidadefinanceira/pix)
                        """)
                .version("v1.0.0")
                .contact(new Contact()
                        .name("Elio Neto")
                        .url("https://github.com/ElioNeto")
                        .email("netoo.elio@hotmail.com")
                )
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT")
                );
    }

    private SecurityScheme jwtSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Insira o token JWT obtido em POST /auth/login. Formato: Bearer {token}");
    }
}
