package javamid.vitrina.app;

import javamid.vitrina.app.repositories.ProductImageRepository;
import javamid.vitrina.app.services.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
@WithMockUser(username = "user1", roles = {"USER"})
public class RedisCacheTest {
  @Autowired
  private ProductService productService;
  @Autowired
  private ReactiveRedisTemplate<String, byte[]> redisTemplate;

  @MockBean  // Заменяем реальный репозиторий моком
  private ProductImageRepository productImageRepository;


  @Test
  void whenImageRequestedTwice_thenSecondTimeFromCache() {
    Long productId = 1L;
    String cacheKey = "product:image:" + productId;
    byte[] dbImage = "real_image".getBytes();
    byte[] cachedImage = "real_image".getBytes();

    redisTemplate.delete(cacheKey).block();

    when(productImageRepository.findImageById(productId))
            .thenReturn(Mono.just(dbImage));

    // Act & Assert (первый запрос - должен идти в БД)
    StepVerifier.create(productService.getImageByProductId(productId))
            .assertNext(image -> assertArrayEquals(dbImage, image, "Первый запрос должен вернуть изображение из БД"))
            .verifyComplete();

    // Act & Assert (второй запрос - должен брать из кеша)
    StepVerifier.create(productService.getImageByProductId(productId))
            .assertNext(image -> assertArrayEquals(cachedImage, image, "Второй запрос должен вернуть изображение из кеша"))
            .verifyComplete();

    // Проверяем, что был только один вызов в БД
    verify(productImageRepository, times(1)).findImageById(productId);

    // Дополнительно проверяем наличие в Redis
    byte[] cached = redisTemplate.opsForValue()
            .get("product:image:" + productId)
            .block();

    assertArrayEquals(dbImage, cached, "Изображение в кеше должно соответствовать изображению из БД");
  }

}
