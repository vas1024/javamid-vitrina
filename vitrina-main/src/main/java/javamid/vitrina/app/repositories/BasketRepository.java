package javamid.vitrina.app.repositories;

import javamid.vitrina.app.dao.Basket;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface BasketRepository extends ReactiveCrudRepository<Basket, Long> {

  Mono<Basket> findByUserId(Long userId);
  @Query("SELECT user_id from baskets where id = :basketId")
  Mono<Long> findUserIdByBasketId(Long basketId);

}
