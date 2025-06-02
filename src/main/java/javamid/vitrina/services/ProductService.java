package javamid.vitrina.services;

import javamid.vitrina.repositories.BasketItemRepository;
import javamid.vitrina.repositories.BasketRepository;
import javamid.vitrina.repositories.ProductRepository;
import javamid.vitrina.model.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@Service
public class ProductService {
  private final ProductRepository productRepository;
  private final BasketRepository basketRepository;
  private final BasketItemRepository basketItemRepository;

  ProductService( ProductRepository productRepository,
                  BasketRepository basketRepository,
                  BasketItemRepository basketItemRepository) {
    this.productRepository = productRepository;
    this.basketRepository = basketRepository;
    this.basketItemRepository = basketItemRepository;
  }


  public Page<Product> getProducts(String keyword, int page, int size) {
    PageRequest pageable = PageRequest.of(page, size, Sort.by("name"));  // Страница 0-based
    return productRepository.findByKeyword(keyword, pageable);
  }

  public void addProductToBasket(Product product, Basket basket) {
    BasketItem basketItem = new BasketItem();
    basketItem.setBasket( basket );
    basketItem.setProduct( product );
    List<BasketItem> basketItemList = basket.getBasketItems();
    basketItemList.add( basketItem );
    basketRepository.save(basket);
  }
}
