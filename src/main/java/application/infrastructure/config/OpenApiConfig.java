package application.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI alchemyOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Operation Alchemy API")
                .version("1.0.0")
                .description("Locates Alchemy's lab by trilateration and rebuilds intercepted messages "
                        + "(Hexagonal Architecture + DDD, in-memory storage)."));
    }
}
