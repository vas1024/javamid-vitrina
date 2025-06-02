package javamid.vitrina.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import javamid.vitrina.model.*;

public interface UserRepository extends JpaRepository<User, Long> {

    @Override
    @NonNull  //@NonNull (из Lombok или Spring) — гарантирует, что метод не вернёт null (вместо этого может вернуть пустой список).
    List<User> findAll();   //Возвращает список всех пользователей (List<User>), а не Iterable<User>, как в стандартном CrudRepository.

    List<User> findAllByName(String name);

  }
