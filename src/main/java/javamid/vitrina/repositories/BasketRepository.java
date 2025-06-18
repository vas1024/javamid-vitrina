package javamid.vitrina.repositories;

import javamid.vitrina.dao.Basket;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface BasketRepository extends ReactiveCrudRepository<Basket, Long> {

  Mono<Basket> findByUserId(Long userId);

}
