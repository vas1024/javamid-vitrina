package javamid.vitrina.repositories;

import javamid.vitrina.dao.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order,Long> {

}
