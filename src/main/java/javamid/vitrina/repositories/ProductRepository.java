package javamid.vitrina.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import javamid.vitrina.dao.Product;

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


  @Query("SELECT p.image FROM Product p WHERE p.id = :id")
  byte[] findImageById(Long id);
}
