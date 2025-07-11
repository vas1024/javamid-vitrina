package javamid.vitrina.app.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import javamid.vitrina.app.dao.User;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {

}
