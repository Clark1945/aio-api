package org.clarkproject.aioapi.api.configure;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.clarkproject.aioapi.api.repository.MemberRepository;
import org.clarkproject.aioapi.api.repository.WalletRepository;
import org.clarkproject.aioapi.api.repository.WalletTransactionRepository;
import org.clarkproject.aioapi.api.service.MemberService;
import org.clarkproject.aioapi.api.service.WalletService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    private final MemberRepository memberRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    public BeanConfig(MemberRepository memberRepository,
                      WalletRepository walletRepository,
                      WalletTransactionRepository walletTransactionRepository) {
        this.memberRepository = memberRepository;
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
    }
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

    @Bean
    public MemberService getMemberService() {
        return new MemberService(memberRepository);
    }
    @Bean
    public WalletService getWalletService(MemberService memberService) {
        return new WalletService(memberService,walletRepository,walletTransactionRepository);
    }
}
