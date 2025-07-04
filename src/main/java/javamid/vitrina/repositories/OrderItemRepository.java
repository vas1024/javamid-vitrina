package javamid.vitrina.repositories;


import javamid.vitrina.dao.OrderItem;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderItemRepository extends ReactiveCrudRepository<OrderItem, Long> {

  // Стандартные методы уже включены:
  // save(), findById(), findAll(), deleteById() и т.д.

  // Кастомные запросы
  @Query("SELECT image FROM order_items WHERE id = :id")
  Mono<byte[]> findImageById(Long id);

  // Дополнительные полезные методы
  Flux<OrderItem> findByOrderId(Long orderId);


}
