package javamid.vitrina.repositories;

import javamid.vitrina.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order,Long> {

}
