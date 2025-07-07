package javamid.vitrina;

import javamid.vitrina.dao.User;
import javamid.vitrina.services.ProductService;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class UserServiceTest {

  @Autowired
  private ProductService productService;

  @Test
  public void getUserOne() {
    var user = productService.getUserById( 1L ) ;
    assertThat(user)
            .withFailMessage("Пользователь не должен быть null")
            .isNotNull();
  }


  @Test
  public void testUserOneMono() {
    Mono<User> userMono = productService.getUserById(1L);

    StepVerifier.create(userMono)
            .assertNext(user -> {
              // Проверка всех полей пользователя
              assertThat(user)
                      .withFailMessage("User should not be null")
                      .isNotNull()
                      .extracting(User::getId,
                              User::getName,
                              User::getBasketId )
                      .containsExactly(1L, "Иван Иванович", 1L);

              // Или более подробная проверка каждого поля
              assertThat(user.getId()).isEqualTo(1L);
              assertThat(user.getName()).isEqualTo("Иван Иванович");
            })
            .verifyComplete();
  }

  @Test
  public void testGetListOfProducts(){
    productService.getProducts("", "", 0,10)
            .subscribe(
                    product -> System.out.println( product.getDescription() )
                    );
  }

  @Test
  public void testGetListOfProducts2() {
    productService.getProducts("", "", 0, 10)
            .doOnNext(product -> System.out.println("Product: " + product.getId() +
                    "   " + product.getDescription() + "  image: " + product.getImage() ))
            .subscribe(); // важно вызвать subscribe()
  }


}