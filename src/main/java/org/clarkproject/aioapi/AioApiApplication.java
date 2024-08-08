package org.clarkproject.aioapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class AioApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AioApiApplication.class, args);
    }

    /**
     * 文件Header
     * @param appVersion
     * @param email
     * @return
     */
    @Bean
    public OpenAPI OpenAPIHeader(@Value("${springdoc.version}") String appVersion,
                                 @Value("${contact.email}") String email) {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("Wallet API")
                        .version(appVersion)
                        .license(new License()
                                .name("Github")
                                .url("https://github.com/Clark1945/Clark1945.github.io"))
                        .description("This is my demo API side project for future interview.")
                        .contact(new Contact()
                                .url("https://www.linkedin.com/in/clark-liu-b48740253/")
                                .name("Clark Liu")
                                .email(email))
                        .summary("This is a summary")
                );
    }
}
