package org.clarkproject.aioapi.api.configure;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    /**
     * Swagger文件Header
     * @param appVersion
     * @param email
     * @return
     */
    @Bean
    public OpenAPI OpenAPIHeader(@Value("${document.title}") String documentTitle,
                                 @Value("${springdoc.version}") String appVersion,
                                 @Value("${contact.email}") String email) {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title(documentTitle)
                        .version(appVersion)
                        .license(new License()
                                .name("My Github")
                                .url("https://clark1945.github.io/"))
                        .description("This is my demo API side project for future interview.")
                        .contact(new Contact()
                                .url("https://github.com/Clark1945")
                                .name("Clark Liu")
                                .email(email))
                        .summary("This is a summary")
                );
    }
}
