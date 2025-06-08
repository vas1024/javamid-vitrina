package javamid.vitrina.repositories;


import javamid.vitrina.dao.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {


  @Query("SELECT oi.image FROM OrderItem oi WHERE oi.id = :id")
  byte[] findImageById(Long id);

}
