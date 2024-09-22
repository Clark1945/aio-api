package org.clarkproject.aioapi.api.configure;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.clarkproject.aioapi.api.repository.MemberRepository;
import org.clarkproject.aioapi.api.repository.WalletRepository;
import org.clarkproject.aioapi.api.repository.WalletTransactionRepository;
import org.clarkproject.aioapi.api.service.JWTService;
import org.clarkproject.aioapi.api.service.MemberService;
import org.clarkproject.aioapi.api.service.WalletService;
import org.clarkproject.aioapi.api.tool.UserIdIdentity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BeanConfig {

    private final MemberRepository memberRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserIdIdentity userIdentity;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public BeanConfig(MemberRepository memberRepository,
                      WalletRepository walletRepository,
                      WalletTransactionRepository walletTransactionRepository,
                      PasswordEncoder passwordEncoder,
                      UserIdIdentity userIdentity,
                      AuthenticationManager authenticationManager,
                      JWTService jwtService) {
        this.memberRepository = memberRepository;
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.userIdentity = userIdentity;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
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
        return new MemberService(memberRepository,passwordEncoder,userIdentity,authenticationManager,jwtService);
    }

    @Bean
    public WalletService getWalletService(MemberService memberService) {
        return new WalletService(memberService,walletRepository,walletTransactionRepository);
    }

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("127.0.0.1");
        config.setPort("6379"); // Redis的預設埠號
        config.setPassword(""); // 放Redis的密碼，這裡暫時沒有設
        config.setDatabase("0");

        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxWaitMillis("3000"); // 當連線取完時，欲取得連線的最大的等待時間
        poolConfig.setMaxIdle("8"); // 最大空閒連線數
        poolConfig.setMinIdle("4"); // 最小空閒連線數
        poolConfig.setMaxTotal("3000"); // 最大連線數

        LettucePoolingClientConfiguration poolingClientConfig =
                LettucePoolingClientConfiguration.builder()
                        .commandTimeout(Duration.ofMillis("3000"))
                        .poolConfig(poolConfig)
                        .build();

        return new LettuceConnectionFactory(config, poolingClientConfig);
    }
}
