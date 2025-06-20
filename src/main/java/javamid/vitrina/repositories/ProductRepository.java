package javamid.vitrina.repositories;


import javamid.vitrina.dao.Product;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {


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


  @Query("SELECT image FROM products WHERE id = :id")
  Mono<byte[]> findImageById(Long id);

  */
}