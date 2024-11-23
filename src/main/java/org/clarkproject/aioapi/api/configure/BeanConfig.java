package org.clarkproject.aioapi.api.configure;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
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
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;

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
                                .name("My Github Page")
                                .url("https://clark1945.github.io/"))
                        .description("This is my demo API side project for future interview.")
                        .contact(new Contact()
                                .url("https://github.com/Clark1945")
                                .name("Clark Liu")
                                .email(email))
                        .summary("This is a summary")
                );
    }


    public LettuceConnectionFactory lettuceConnectionFactory(String redisHost,int redisPort) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort); // Redis的預設埠號
        config.setPassword(""); // 放Redis的密碼，這裡暫時沒有設
        config.setDatabase(0);

        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxWaitMillis(3000);
        // 當連線取完時，欲取得連線的最大的等待時間，超出就是連線逾時
        // Pool若是滿的狀況就會一直堵塞值到超出等待時間 JedisConnectionException
        poolConfig.setMaxIdle(8); // 最大空閒連線數
        poolConfig.setMinIdle(4); // 最小空閒連線數
        poolConfig.setMaxTotal(3000); // 最大連線數

        LettucePoolingClientConfiguration poolingClientConfig =
                LettucePoolingClientConfiguration.builder()
                        .commandTimeout(Duration.ofMillis(3000))
                        .poolConfig(poolConfig)
                        .build();
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config, poolingClientConfig);
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(
            @Value("${redis.hostname}") String redisHost,
            @Value("${redis.port}") int redisPort) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory(redisHost,redisPort)); //建立與Redis的連接
        redisTemplate.setDefaultSerializer(
                new Jackson2JsonRedisSerializer<>(Object.class)); // 轉換Java物件格式與Redis儲存格式
        redisTemplate.setEnableTransactionSupport(true); // 加入交易
        redisTemplate.afterPropertiesSet(); // Bean設定完成後才初始化
        return redisTemplate;
    }
}
