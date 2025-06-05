package javamid.vitrina.repositories;


import javamid.vitrina.dao.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {

}
