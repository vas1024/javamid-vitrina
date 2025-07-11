package javamid.vitrina.app.repositories;

import javamid.vitrina.app.dao.Order;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;


public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {

  public Flux<Order> findAllByUserId(Long userId );

}
