package javamid.vitrina.app.services;



import javamid.vitrina.app.repositories.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/*
  @Service
  public class UserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsService(UserRepository userRepository) {
      this.userRepository = userRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String login) {
      return userRepository.findByLogin(login)
              .cast(UserDetails.class)
              .switchIfEmpty(Mono.error(
                      new UsernameNotFoundException("User not found: " + login )
              ));
    }

  }
*/



@Service
public class UserDetailsService implements ReactiveUserDetailsService {

  private final UserRepository userRepository;

  // Добавляем PasswordEncoder для проверки паролей
  public UserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Mono<UserDetails> findByUsername(String login) {
    System.out.println("🔍 Поиск пользователя: " + login);

    return userRepository.findByLogin(login)
            .doOnNext(user -> {
              System.out.println("✅ Найден пользователь: " + user.getLogin());
              System.out.println("🔐 Хеш пароля в БД: " + user.getPassword());
//              System.out.println("закодированный пароль: " + encoder.);
            })
            .switchIfEmpty(Mono.defer(() -> {
              System.out.println("❌ Пользователь не найден: " + login);
              return Mono.error(new UsernameNotFoundException("User not found: " + login));
            }))
            .cast(UserDetails.class)
            .doOnError(e -> System.out.println("🔥 Ошибка при поиске пользователя: " + e.getMessage()));
  }
}




