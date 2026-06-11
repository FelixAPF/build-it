package com.build_it.buildit.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    final String securitySchemeName = "bearerAuth";

    return new OpenAPI()
      .info(new Info().title("BuildIt API").version("1.0").description("API endpoints for the BuildIt application"))
      // This adds the "Authorize" button to the top of the UI
      .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
      // This defines the format of the token (Bearer JWT)
      .components(new Components()
        .addSecuritySchemes(securitySchemeName,
          new SecurityScheme()
            .name(securitySchemeName)
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")));
  }
}
