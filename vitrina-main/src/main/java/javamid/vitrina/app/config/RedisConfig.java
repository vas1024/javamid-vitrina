package javamid.vitrina.app.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories(basePackages = "javamid.vitrina.app")

public class RedisConfig {


  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper()
            .registerModule(new JavaTimeModule()) 
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }


  @Bean
  public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
          ReactiveRedisConnectionFactory factory,
          ObjectMapper objectMapper) {


    GenericJackson2JsonRedisSerializer serializer =
            new GenericJackson2JsonRedisSerializer(objectMapper);

    RedisSerializationContext.RedisSerializationContextBuilder<String, Object> builder =
            RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

    RedisSerializationContext<String, Object> context = builder
            .value(serializer)
            .hashValue(serializer)
            .build();

    return new ReactiveRedisTemplate<>(factory, context);
  }




  @Bean
  public ApplicationRunner reactiveRedisHealthCheck(ReactiveRedisConnectionFactory factory) {
    return args -> {
      factory.getReactiveConnection()
              .ping()
              .doOnSuccess(v -> System.out.println("✅ Redis is available (Reactive)"))
              .doOnError(e -> {
                System.err.println("❌ Redis connection failed: " + e.getMessage());
                throw new IllegalStateException("Redis is not available");
              })
              .subscribe();
    };
  }

}


