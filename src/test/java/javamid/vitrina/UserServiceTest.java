package javamid.vitrina;

import jakarta.transaction.Transactional;
import javamid.vitrina.model.*;
import javamid.vitrina.repositories.BasketRepository;
import javamid.vitrina.repositories.UserRepository;
import javamid.vitrina.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

//@DataJpaTest
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)  // Не заменять DataSource на embedded БД
public class UserServiceTest {
  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private BasketRepository basketRepository;

  @Test
  @Transactional
  public void expectSavedUserIdFound() {

    String userName = "Иван Семенович";
    userService.createUserWithBasket( userName );

    Optional<User> foundUserOptional = userRepository.findById( 1L );
    if (foundUserOptional.isPresent()) {
      User user = foundUserOptional.get();
      System.out.println("Найден пользователь: " + user.getName());
    } else {
      System.out.println("Пользователь с ID=1 не найден");
    }

    User foundUser = userRepository.findAllByName( userName ).getFirst();
    assertThat(foundUser).isNotNull();
    assertThat(foundUser.getName()).isEqualTo(userName);
    System.out.println("getFirst gives us user with id " + foundUser.getId() );

    Basket basket = foundUser.getBasket();
    Optional<Basket> foundBasketOptional = basketRepository.findById( basket.getId() );
    assertTrue(foundBasketOptional.isPresent(), "у пользователя должена быть корзина в таблице baskets");
    Basket foundBasket = foundBasketOptional.get();

    User foundUser2 = foundBasket.getUser();
    System.out.println("foundUser = " + foundUser + "  with id = " + foundUser.getId() );
    System.out.println( "foundUser2 = " + foundUser2 + "  with id = " + foundUser.getId() );

    assertEquals( foundUser.getId(),foundUser2.getId());

  }


}
