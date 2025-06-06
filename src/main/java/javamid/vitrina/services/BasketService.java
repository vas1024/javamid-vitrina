package javamid.vitrina.services;

import javamid.vitrina.dao.Basket;
import javamid.vitrina.dao.BasketItem;
import javamid.vitrina.repositories.BasketRepository;
import javamid.vitrina.repositories.OrderRepository;
import javamid.vitrina.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BasketService {
  private final ProductRepository productRepository;
  private final BasketRepository basketRepository;
  private final OrderRepository orderRepository;
  BasketService( ProductRepository productRepository,
                  BasketRepository basketRepository,
                  OrderRepository orderRepository) {
    this.productRepository = productRepository;
    this.basketRepository = basketRepository;
    this.orderRepository = orderRepository;
  }

  public Basket getById(Long id ){ return basketRepository.getById( id );}

}
