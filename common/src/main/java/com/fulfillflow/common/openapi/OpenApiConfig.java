package com.fulfillflow.common.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shared OpenAPI configuration. Each service provides its own title and
 * description via the {@code openapi.info.title} and
 * {@code openapi.info.description} properties. The OAuth2 password flow points
 * at the FulfillFlow Keycloak realm so Swagger UI can mint tokens for manual
 * testing.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fulfillflowOpenApi(
            @Value("${openapi.info.title:FulfillFlow API}") String title,
            @Value("${openapi.info.description:API}") String description,
            @Value("${KEYCLOAK_URL:http://localhost:8080}") String keycloakUrl,
            @Value("${KEYCLOAK_REALM:fulfillflow}") String realm) {
        String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        String authUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/auth";

        SecurityScheme oauth = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows().password(new OAuthFlow()
                        .tokenUrl(tokenUrl)
                        .authorizationUrl(authUrl)
                        .scopes(new Scopes())));

        return new OpenAPI()
                .info(new Info().title(title).description(description).version("v1"))
                .components(new Components().addSecuritySchemes("oauth2", oauth))
                .addSecurityItem(new SecurityRequirement().addList("oauth2"));
    }
}
