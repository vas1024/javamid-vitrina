package javamid.vitrina.repositories;

import javamid.vitrina.dao.Basket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BasketItemRepository extends JpaRepository<Basket,Long> {

}
