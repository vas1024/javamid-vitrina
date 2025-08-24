package javamid.vitrina.app;


import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import javamid.vitrina.app.repositories.ProductRepository;
import javamid.vitrina.app.repositories.BasketRepository;
import javamid.vitrina.app.dao.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@WithMockUser(username = "user1", roles = {"USER"})
@AutoConfigureWebTestClient
public class ReactTest {

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private BasketRepository basketRepository;

  private Long testUserId = 1L;
  private Long testProductId;
  private BigDecimal testProductPrice = new BigDecimal("100");
  private String testProductName = "testProductForSuccessBuying";

  private Long testProductNegativeId;
  private BigDecimal testProductNegativePrice = new BigDecimal("100000000");
  private String testProductNegativeName = "testProductVeryExpensiveForNegativeTest";


  @BeforeEach
  void setup() {
    // Создаем тестовый товар
    Product testProduct = new Product();
    testProduct.setName( testProductName );
    testProduct.setDescription("for test");
    testProduct.setPrice( testProductPrice );
    productRepository.save(testProduct).block();
    testProductId = testProduct.getId();

    Product testProductNegative = new Product();
    testProductNegative.setName( testProductNegativeName );
    testProductNegative.setDescription("for test");
    testProductNegative.setPrice( testProductNegativePrice );
    productRepository.save(testProductNegative).block();
    testProductNegativeId = testProductNegative.getId();

  }

  @AfterEach
  void cleanup() {
    productRepository.deleteById(testProductId).block();
    productRepository.deleteById(testProductNegativeId).block();
  }


  @Test
  void whenAddItemToBasketAndPurchase_thenSuccess() {
    // 1. Добавляем товар в корзину
    webTestClient.post()
            .uri("/main/items/" + testProductId )
//            .header("X-User-Id", testUserId)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED )
            .bodyValue("action=plus")
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueEquals("Location", "/main/items");

    // 2. Получаем текущую корзину (проверяем добавление)
    webTestClient.get()
            .uri("/cart/items")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(result -> {
              String html = result.getResponseBody();
              assertNotNull(html);
              assertTrue(html.contains( testProductName ) );
            });


    // 3. Совершаем покупку
    String redirectUrl = webTestClient.post()
            .uri("/buy")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED )
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader()
            .value("Location", location -> {
              // Проверяем что Location начинается с /orders/
              assertTrue(location.startsWith("/orders/"));
            })
            .returnResult(Void.class)
            .getResponseHeaders()
            .getLocation()
            .toString();
    // 3b. Делаем запрос по полученному URL редиректа
    webTestClient.get()
            .uri(redirectUrl)
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(response -> {
              String html = response.getResponseBody();
              assertTrue(html.contains(testProductName));
              assertTrue(html.contains(testProductPrice.toString()));
            });



    // 4. Проверяем, что корзина очистилась
    webTestClient.get()
            .uri("/cart/items")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(result -> {
              String html = result.getResponseBody();
              assertNotNull(html);
              assertTrue( ! html.contains( testProductName ) );
            });

  }

  @Test
  void whenAddTooExpensiveItemToBasketAndPurchase_thenFailure() {
    // 1. Добавляем товар в корзину
    webTestClient.post()
            .uri("/main/items/" + testProductNegativeId )
            .contentType(MediaType.APPLICATION_FORM_URLENCODED )
            .bodyValue("action=plus")
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueEquals("Location", "/main/items");
    //  Совершаем покупку
    webTestClient.post()
            .uri("/buy")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED )
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueEquals("Location", "/cart/items?error=insufficient_funds");
    //  Удаляем очень дорогой товар из корзины
    webTestClient.post()
            .uri("/cart/items/" + testProductNegativeId )
            .contentType(MediaType.APPLICATION_FORM_URLENCODED )
            .bodyValue("action=delete")
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueEquals("Location", "/cart/items");


  }

}
