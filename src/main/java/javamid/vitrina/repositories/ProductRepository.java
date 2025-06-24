package javamid.vitrina.repositories;


import javamid.vitrina.dao.Product;

import javamid.vitrina.model.Item;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

@Repository
public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {


  @Query("SELECT image FROM products WHERE id = :id")
  Mono<ByteBuffer> findImageById(Long id);


  @Query("""
        SELECT COUNT(*) FROM products
        WHERE
            LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%'))
        """)
  Mono<Long> countProducts(String keyword);

  @Query("""
    SELECT * FROM products
    WHERE
        (:keyword IS NULL OR
        LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%')))
    ORDER BY
        CASE WHEN :sort = 'ALPHA' THEN name END ASC,
        CASE WHEN :sort = 'NAME_DESC' THEN name END DESC,
        CASE WHEN :sort = 'PRICE' THEN price END ASC,
        CASE WHEN :sort = 'PRICE_DESC' THEN price END DESC,
        id ASC
    LIMIT :limit OFFSET :offset
    """)
  Flux<Product> getProducts( String keyword, String sort, int limit, long offset  );


  /*

  @Query("""
        SELECT p.* FROM products p 
        WHERE 
          LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR 
          LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY ${#sort}
        LIMIT :pageSize OFFSET :offset
        """)
  Flux<Product> findByKeyword(String keyword, int pageSize, long offset, String sort);

  @Query("SELECT COUNT(*) FROM products WHERE " +
          "LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
          "LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  Mono<Long> countByKeyword(String keyword);




  */
}