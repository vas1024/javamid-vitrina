package javamid.vitrina.repositories;


import javamid.vitrina.dao.BasketItem;
import javamid.vitrina.model.Item;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

public interface BasketItemRepository extends ReactiveCrudRepository<BasketItem, Long> {

  // Стандартные методы уже включены в ReactiveCrudRepository:
  // save(), saveAll(), findById(), findAll(), deleteById(), deleteAll() и т.д.

  // Добавляем кастомные запросы
  Flux<BasketItem> findByBasketId(Long basketId);


  @Query("SELECT quantity FROM basket_item WHERE basket_id = :basketId AND product_id = :productId")
  Mono<Integer> getQuantity( Long basketId, Long productId );



  @Query("""
        SELECT
            p.id as id,
            p.name as title,
            p.price as price,
            p.description as description,
            bi.quantity as count,
            p.id as imgPath
        FROM basket_item bi
        JOIN products p ON bi.product_id = p.id
        WHERE bi.basket_id = :basketId
        """)
  Flux<Item> findBasketItemsAndProducts(Long basketId);


  @Query( "UPDATE basket_item SET quantity = :quantity WHERE basket_id = :basketId AND product_id = :productId" )
  Mono<Void> updateQuantity(Long basketId, Long productId, int quantity);

//  Mono<BasketItem> create(Long basketId, Long productId, int quantity);

  Mono<Void> deleteByBasketId(Long basketId);

  Mono<BasketItem> findByBasketIdAndProductId(Long basketId, Long productId);

  Mono<Void> deleteByBasketIdAndProductId(Long basketId, Long productId);

}