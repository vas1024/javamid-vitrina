package javamid.vitrina.repositories;

import javamid.vitrina.dao.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {

  public List<Order> findByUserId(Long userId);

}
