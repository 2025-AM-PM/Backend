package AM.PM.Homepage.common.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * spring.data.redis.* 프로퍼티를 그대로 사용.
     * (호스트/포트/패스워드/타임아웃 등은 application-*.properties에 설정)
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory(RedisProperties props) {
        RedisStandaloneConfiguration server = new RedisStandaloneConfiguration();
        server.setHostName(props.getHost());
        server.setPort(props.getPort());
        if (props.getPassword() != null && !props.getPassword().isBlank()) {
            server.setPassword(RedisPassword.of(props.getPassword()));
        }
        // Lettuce 클라이언트 옵션(명령 타임아웃 등)
        LettuceClientConfiguration client = LettuceClientConfiguration.builder()
                .commandTimeout(props.getTimeout() == null ? Duration.ofSeconds(2) : props.getTimeout())
                .shutdownTimeout(Duration.ZERO)
                .build();

        return new LettuceConnectionFactory(server, client);
    }

    /**
     * 키=String, 값=JSON 통일 직렬화 템플릿.
     * 서비스 전반에서 동일 직렬화를 쓰면 디버깅/이행이 편함.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory cf) {
        StringRedisSerializer keySer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valSer = new GenericJackson2JsonRedisSerializer();

        RedisTemplate<String, Object> t = new RedisTemplate<>();
        t.setConnectionFactory(cf);
        t.setKeySerializer(keySer);
        t.setHashKeySerializer(keySer);
        t.setValueSerializer(valSer);
        t.setHashValueSerializer(valSer);
        t.afterPropertiesSet();
        return t;
    }

    /**
     * 필요 시 바로 쓰기 좋은 String 전용 템플릿.
     * (Spring Boot가 기본 StringRedisTemplate을 제공하지만 직렬화 확인용으로 명시)
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }

    /**
     * Spring Cache 추상화용 Redis CacheManager
     * - 기본 TTL 10분
     * - 캐시별 TTL 예시 포함(도메인에 맞게 수정)
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory cf) {
        StringRedisSerializer keySer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valSer = new GenericJackson2JsonRedisSerializer();

        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valSer))
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(10));

        // 캐시 이름별 TTL 커스터마이즈 (예시)
        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
        configMap.put("notice:list",   base.entryTtl(Duration.ofMinutes(5)));
        configMap.put("notice:detail", base.entryTtl(Duration.ofMinutes(30)));
        configMap.put("study:list",    base.entryTtl(Duration.ofMinutes(3)));
        configMap.put("project:list",  base.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(cf)
                .cacheDefaults(base)
                .withInitialCacheConfigurations(configMap)
                .transactionAware() // 트랜잭션 내 캐시 연동 시 유용
                .build();
    }

    /*
    // (선택) 캐시 에러가 나도 비즈니스 로직은 계속 가게 하려면 사용
    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        return new SimpleCacheErrorHandler(); // 기본 구현(필요시 커스텀)
    }
    */
}
