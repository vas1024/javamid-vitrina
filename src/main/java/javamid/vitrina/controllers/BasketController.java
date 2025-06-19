package javamid.vitrina.controllers;

/*
import javamid.vitrina.dao.Basket;
import javamid.vitrina.dao.BasketItem;
import javamid.vitrina.model.Item;
import javamid.vitrina.services.BasketService;
import javamid.vitrina.services.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class BasketController {
  private final BasketService basketService;
  public BasketController(BasketService basketService) {
    this.basketService = basketService;
  }

  /*
  @GetMapping("/cart/items")
  public String getBasket( Model model ){
    Basket basket = basketService.getById(1L);

    List<Item> itemList = new ArrayList<>();
    for( BasketItem basketItem : basket.getBasketItems() ){
      Item item = new Item( basketItem.getProduct() );
      itemList.add( item );
    }
    model.addAttribute("items", itemList );
    model.addAttribute("empty", false );
    return "cart.html";
  }





}



 */
