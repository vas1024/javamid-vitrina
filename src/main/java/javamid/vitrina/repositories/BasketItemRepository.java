package javamid.vitrina.repositories;

import javamid.vitrina.model.Basket;
import org.springframework.data.jpa.repository.JpaRepository;

import javamid.vitrina.model.*;

public interface BasketItemRepository extends JpaRepository<Basket,Long> {

}
