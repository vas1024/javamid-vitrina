package javamid.vitrina.app.config;


import javamid.vitrina.app.dao.User;
import javamid.vitrina.app.repositories.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class DataInitializer {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }


  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    // Сначала проверяем, есть ли уже пользователи
    userRepository.count()
            .flatMap(count -> {
              if (count == 0) {
                // Если таблица пустая, создаем тестовых пользователей

                return createTestUsers();
              }
              return Mono.empty(); // Если пользователи уже есть, ничего не делаем
            })
            .subscribe(
                    null, // onNext не нужен
                    error -> System.err.println("Ошибка инициализации данных: " + error),
                    () -> System.out.println("Инициализация данных завершена, это сообщение из DataInitializer.init()")
            );
  }

  private Mono<Void> createTestUsers() {
    return Flux.just(
                    new User(null, "первый пользователь", "user1", passwordEncoder.encode("aaa")),
                    new User(null, "второй пользователь", "user2", passwordEncoder.encode("bbb"))
            )
            .flatMap(userRepository::save)
            .then();
  }

}