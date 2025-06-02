package javamid.vitrina.repositories;

import javamid.vitrina.model.Basket;
import org.springframework.data.jpa.repository.JpaRepository;
import javamid.vitrina.model.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;


public interface ProductRepository extends JpaRepository<Product,Long> {



  @Query("""
    SELECT p FROM Product p 
    WHERE 
      LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR 
      LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
  public  Page<Product> findByKeyword(String keyword, Pageable pageable);

}
