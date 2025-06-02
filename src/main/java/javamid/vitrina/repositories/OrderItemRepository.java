package javamid.vitrina.repositories;


import javamid.vitrina.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {

}
