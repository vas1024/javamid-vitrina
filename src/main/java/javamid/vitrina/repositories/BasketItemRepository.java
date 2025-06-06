package javamid.vitrina.repositories;

import javamid.vitrina.dao.Basket;
import javamid.vitrina.dao.BasketItem;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BasketItemRepository extends JpaRepository<BasketItem,Long> {

}
