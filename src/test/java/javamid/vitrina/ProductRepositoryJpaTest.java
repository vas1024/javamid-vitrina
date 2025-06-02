package javamid.vitrina;

import jakarta.transaction.Transactional;
import javamid.vitrina.model.Product;
import javamid.vitrina.repositories.BasketRepository;
import javamid.vitrina.repositories.ProductRepository;
import static javamid.vitrina.testUtils.printPage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.Arrays;


import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)  // Не заменять DataSource на embedded БД
public class ProductRepositoryJpaTest {
  @Autowired
  private ProductRepository productRepository;
  @Autowired
  private BasketRepository basketRepository;

  String productName1 = "Кепка для теста";
  String productName2 = "другая кепка для теста";


  @Test
  @Transactional
  public void expectSavedProductKeywordFound() {
    Product product1 = new Product();
    String key = "летняя";
    product1.setName("кепка" + key);
    product1.setDescription("это очень хорошая кепка");
    product1.setPrice(BigDecimal.valueOf(200));
    productRepository.save(product1);
    Product product2 = new Product();
    product2.setName("кепка");
    product2.setDescription("эта кепка еще лучше" + key);
    product2.setPrice(BigDecimal.valueOf(200));
    productRepository.save(product2);
    Product product3 = new Product();
    product3.setName("кепка" + key);
    product3.setDescription("эта кепка лучше всех" + key);
    product3.setPrice(BigDecimal.valueOf(200));
    productRepository.save(product3);

    Pageable pageable = PageRequest.of(0, 2); // Первая страница, 2 элемента
    Page<Product> foundProductPage = productRepository.findByKeyword(key, pageable);

    System.out.println("found product page: ");
    System.out.println(foundProductPage);
    printPage(foundProductPage);

    assertThat(foundProductPage.getContent()).hasSize(2);
    assertThat(foundProductPage.getTotalElements()).isEqualTo(3);

    foundProductPage.getContent().forEach(product -> {
      assertThat(Arrays.asList(product.getName(), product.getDescription()))
              .anyMatch(field -> field.contains(key));
    });


  }

}
