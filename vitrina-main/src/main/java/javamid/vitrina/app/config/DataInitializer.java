package javamid.vitrina.app.config;


import javamid.vitrina.app.dao.Basket;
import javamid.vitrina.app.dao.User;
import javamid.vitrina.app.repositories.BasketRepository;
import javamid.vitrina.app.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class DataInitializer {

  private final UserRepository userRepository;
  private final BasketRepository basketRepository;
  private final PasswordEncoder passwordEncoder;

  public DataInitializer(UserRepository userRepository,
                         BasketRepository basketRepository,
                         PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.basketRepository = basketRepository;
    this.passwordEncoder = passwordEncoder;
  }



  @Bean
  public CommandLineRunner initTestUsers(UserRepository userRepository,
                                         BasketRepository basketRepository,
                                         PasswordEncoder passwordEncoder) {
    return args -> {
      Mono<Void> admin = createUserWithBasket("admin", "admin", "Администратор" );
      Mono<Void> user1 = createUserWithBasket("user1", "aaa", "Первый Пользак" );
      Mono<Void> user2 = createUserWithBasket("user2", "bbb", "Второй Пользак" );
      Mono<Void> user3 = createUserWithBasket("user3", "ccc", "Третий Пользак" );
      // Последовательное создание
      admin.then(user1).then(user2).then(user3)
              .subscribe(
                      null,
                      error -> System.err.println("Ошибка: " + error),
                      () -> System.out.println("✅ Все пользователи созданы")
              );
    };
  }




  private Mono<Void> createUserWithBasket(String login, String password, String name ) {
    return userRepository.findByLogin(login)
            .switchIfEmpty(Mono.defer(() -> {
              System.out.println("➕ Создаю: " + login);

              // 1. Создаем пользователя
              User user = new User();
              user.setName(name);
              user.setLogin(login);
              user.setPassword( passwordEncoder.encode(password));

              // 2. Сохраняем пользователя
              return userRepository.save(user)
                      .flatMap(savedUser -> {
                        System.out.println("✅ Пользователь создан: " + savedUser.getId());

                        // 3. Создаем корзину
                        Basket basket = new Basket();
                        basket.setUserId(savedUser.getId());

                        return basketRepository.save(basket)
                                .doOnNext(savedBasket -> {
                                  System.out.println("✅ Корзина создана: " + savedBasket.getId());

                                  // 4. Связываем пользователя с корзиной
                                  savedUser.setBasketId(savedBasket.getId());
                                })
                                .thenReturn(savedUser);
                      })
                      .flatMap(userWithBasket -> {
                        // 5. Обновляем пользователя с basketId
                        System.out.println("🔄 Связываю пользователя с корзиной");
                        return userRepository.save(userWithBasket);
                      });
            }))
            .then()
            .doOnSuccess(v -> System.out.println("✓ Завершено для: " + login));
  }




  /*
  @Bean
  public CommandLineRunner initTestUsers() {
     return args ->

  {
    String username = "user1";
    String rawPassword = "aaa";
    String name = "первый пользак";

    userRepository.findByLogin(username)
            .switchIfEmpty(Mono.defer(() -> {
              System.out.println("➕ Создаю пользователя и корзину: " + username);

              // 1. Создаем пользователя
              User user = new User();
              user.setName(name);
              user.setLogin(username);
              user.setPassword(passwordEncoder.encode(rawPassword));

              // 2. Сохраняем пользователя
              return userRepository.save(user)
                      .flatMap(savedUser -> {
                        // 3. Создаем корзину
                        Basket basket =  new Basket();
                        basket.setUserId(savedUser.getId());

                        // 4. Сохраняем корзину
                        return basketRepository.save(basket)
                                .doOnNext(savedBasket -> {
                                  // 5. Связываем пользователя с корзиной
                                  savedUser.setBasketId(savedBasket.getId());
                                })
                                .thenReturn(savedUser);
                      })
                      .flatMap(userWithBasket ->
                              // 6. Обновляем пользователя с корзиной
                              userRepository.save(userWithBasket)
                      );
            }))
            .then()
            .subscribe(
                    null,
                    error -> System.err.println("Ошибка: " + error),
                    () -> System.out.println("Инициализация завершена")
            );
  };

}
*/





  /*
  @Bean
  public CommandLineRunner initTestUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    return args -> {
      String username = "user1";
      String rawPassword = "aaa";

      userRepository.findByLogin(username)
              .flatMap(existingUser -> {
                System.out.println("✓ Пользователь " + username + " уже существует");
                return Mono.empty();
              })
              .switchIfEmpty(Mono.defer(() -> {
                System.out.println("➕ Создаю пользователя: " + username);
                User user = new User( "первый пользователь", username,
                        passwordEncoder.encode(rawPassword));
                return userRepository.save(user);
              }))
              .then()
              .subscribe( // Ключевой момент: подписываемся на поток
                      null,
                      error -> System.err.println("Ошибка: " + error),
                      () -> System.out.println("Инициализация завершена")
              );
    };
  }

   */


}