package javamid.vitrina.repositories;

import javamid.vitrina.dao.Basket;
import javamid.vitrina.dao.Order;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {

  public Flux<Order> findAllByUserId(Long userId );

}
