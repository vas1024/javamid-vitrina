package javamid.vitrina.app;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@AutoConfigureWebTestClient
public class AuthTest {

  @Autowired
  private WebTestClient webTestClient;


  @Test
  @WithMockUser(username = "user1", roles = {"USER"})
  void testRootEndpoint() {
    webTestClient
            .get()
            .uri("/main/items")
            .exchange()
            .expectStatus().isOk() // Проверяем успешный статус
            .expectBody()
            .consumeWith(response -> {
              String responseBody = new String(response.getResponseBody());

              // Проверяем, что страница содержит ссылку "КОРЗИНА"
              assertTrue(responseBody.contains("КОРЗИНА") ||
                              responseBody.contains("Корзина") ||
                              responseBody.contains("корзина"),
                      "Страница должна содержать ссылку на корзину");

              // Дополнительные проверки на наличие HTML-ссылки
              assertTrue(responseBody.contains("href") &&
                              (responseBody.contains("/basket") ||
                                      responseBody.contains("/cart") ),
                      "Страница должна содержать ссылку на корзину");
            });
  }


  @Test
  void rootEndpoint_AnonymousUser_ShouldNotContainBasketLink() {
    webTestClient.get()
            .uri("/main/items")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .value(body -> {
              // Удаляем HTML комментарии из тела ответа
              String bodyWithoutComments = body.replaceAll("(?s)<!--.*?-->", "");

              // Теперь проверяем только активный (незакомментированный) HTML
              assertFalse(bodyWithoutComments.contains("КОРЗИНА"),
                      "Не должно быть активной ссылки КОРЗИНА");

              // Проверяем конкретно ссылки
              assertFalse(bodyWithoutComments.contains("href=\"/cart/items\""),
                      "Не должно быть активной ссылки на /cart/items");

              assertFalse(bodyWithoutComments.contains("href=\"/basket\""),
                      "Не должно быть активной ссылки на /basket");
            });
  }



  @Test
  @WithMockUser(username = "testuser", roles = {"USER"})
  void authenticatedUser_AccessCart_ShouldSeeCartPage() {
    webTestClient
            .get()
            .uri("/cart/items")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .value(body -> {
              // Проверяем, что страница корзины содержит ожидаемые элементы
              assertTrue(body.contains("Итого") ||
                              body.contains("ИТОГО") ||
                              body.contains("итого"),
                      "Страница корзины должна содержать 'Итого'");

            });
  }


  @Test
  void anonymousUser_AccessCart_ShouldRedirectToLogin() {
    webTestClient.get()
            .uri("/cart/items")
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().value("Location", location -> {
              // Проверяем, что редирект ведет на страницу логина
              assertTrue(location.contains("login") ||
                              location.contains("auth") ||
                              location.contains("signin"),
                      "Редирект должен вести на страницу входа: " + location);
            });
  }


}
