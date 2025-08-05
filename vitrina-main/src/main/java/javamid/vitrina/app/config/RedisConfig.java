package javamid.vitrina.app.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javamid.vitrina.app.dao.Product;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.*;

import java.util.List;

@Configuration
@EnableRedisRepositories(basePackages = "javamid.vitrina.app")

public class RedisConfig {



  @Bean
  public ReactiveRedisTemplate<String, List<Product>> ProductListRedisTemplate(
          ReactiveRedisConnectionFactory factory
          ) {

    // 1. Настраиваем ObjectMapper
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // 2. Создаём сериализатор для List<Product>
    Jackson2JsonRedisSerializer<List<Product>> serializer =
            new Jackson2JsonRedisSerializer<>(mapper, constructListProductType(mapper));

    // 3. Настраиваем контекст
    RedisSerializationContext<String, List<Product>> context =
            RedisSerializationContext.<String, List<Product>>newSerializationContext(new StringRedisSerializer())
                    .value(serializer)
                    .build();

    return new ReactiveRedisTemplate<>(factory, context);
  }


  // этот метод нужен потому что ide idea не понимает List.class, она хочет List<Product>.class
  private JavaType constructListProductType(ObjectMapper mapper) {
    return mapper.getTypeFactory()
            .constructParametricType(List.class, Product.class);
  }




/*
  @Bean
  public ReactiveRedisTemplate<String, Object> redisTemplate(
          ReactiveRedisConnectionFactory factory
          ) {
    ObjectMapper redisObjectMapper = new ObjectMapper();
    redisObjectMapper.registerModule(new JavaTimeModule());
    redisObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    GenericJackson2JsonRedisSerializer serializer =
            new GenericJackson2JsonRedisSerializer(redisObjectMapper);
    RedisSerializationContext<String, Object> context =
            RedisSerializationContext.<String, Object>newSerializationContext(new StringRedisSerializer())
                    .value(serializer)
                    .build();
    return new ReactiveRedisTemplate<>(factory, context);
  }
 */


  @Bean
  public ReactiveRedisTemplate<String, byte[]> imageRedisTemplate(
          ReactiveRedisConnectionFactory factory) {

    RedisSerializationContext<String, byte[]> context =
            RedisSerializationContext.<String, byte[]>newSerializationContext(new StringRedisSerializer())
                    .key(new StringRedisSerializer())
                    .value(RedisSerializer.byteArray())
                    .hashKey(new StringRedisSerializer())  // Добавляем сериализатор для hashKey
                    .hashValue(RedisSerializer.byteArray()) // Добавляем сериализатор для hashValue
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


