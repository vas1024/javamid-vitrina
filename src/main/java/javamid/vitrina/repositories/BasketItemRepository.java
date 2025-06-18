package javamid.vitrina.repositories;


import javamid.vitrina.dao.BasketItem;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BasketItemRepository extends ReactiveCrudRepository<BasketItem, Long> {

  // Стандартные методы уже включены в ReactiveCrudRepository:
  // save(), saveAll(), findById(), findAll(), deleteById(), deleteAll() и т.д.

  // Добавляем кастомные запросы
  Flux<BasketItem> findByBasketId(Long basketId);

  Mono<Void> deleteByBasketId(Long basketId);

  Mono<BasketItem> findByBasketIdAndProductId(Long basketId, Long productId);

  Mono<Void> deleteByBasketIdAndProductId(Long basketId, Long productId);
}