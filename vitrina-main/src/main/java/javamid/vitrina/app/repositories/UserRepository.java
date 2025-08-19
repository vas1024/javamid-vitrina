package javamid.vitrina.app.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import javamid.vitrina.app.dao.User;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {

  Mono<User> findByLogin(String login);


  }
