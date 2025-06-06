package javamid.vitrina.controllers;

import jakarta.annotation.PostConstruct;
import javamid.vitrina.dao.Basket;
import javamid.vitrina.dao.BasketItem;
import javamid.vitrina.model.Item;
import javamid.vitrina.model.Paging;
import javamid.vitrina.dao.Product;
import javamid.vitrina.services.ProductService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ProductController {

  private final ProductService productService;
  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  private  Basket basket;
  private  Map<Long,Integer> productsInThisBasket = new HashMap<>();
  private  BigDecimal inTotal;

  @PostConstruct
  public void initBasket(){
    refreshBasket();;
  }

  public void refreshBasket(){
    this.basket = productService.getBasketById(1L);
    List<BasketItem> basketItems = basket.getBasketItems();
    BigDecimal sum = BigDecimal.valueOf(0);
    for ( BasketItem basketItem : basketItems ){
      Product product = basketItem.getProduct();
      int quantity = basketItem.getQuantity();
      productsInThisBasket.put(product.getId(),quantity);

      Product productFromDb = productService.getProductById(product.getId());
//      System.out.println("!!!ProductFromDb: " + productFromDb );
      System.out.println("!!!ProductFromDb id: " + productFromDb.getId() );
      System.out.println("!!!ProductfromDb name: " + productFromDb.getName() );

      //String description = productFromDb.getDescription();
      /*
      BigDecimal price = productFromDb.getPrice();
      sum = sum.add( price.multiply(BigDecimal.valueOf(quantity)) );

       */
    }
    this.inTotal = sum;
  }


  @GetMapping("/")
  public String homePage() { return "redirect:/main/items"; }


  @GetMapping("/main/items")
  public String getItems(
          @RequestParam(name = "search", required = false, defaultValue = "" ) String search,
          @RequestParam(name = "pageSize", defaultValue = "10") int size,
          @RequestParam(name = "pageNumber", defaultValue = "1") int page,
          Model model
          ) {

    Page<Product> productPage = productService.getProducts("",0,10 );

    System.out.println("keyword is: " + search );
    System.out.println("found this products:");
    for( Product product : productPage.getContent()){
      System.out.println("id " + product.getId() );
      System.out.println("name " + product.getName() );
      System.out.println("description " + product.getDescription() );
    }

    List<Item> itemList = new ArrayList<>();
    for( Product product : productPage.getContent() ){
      Item item = new Item(product);
      int count = productsInThisBasket.getOrDefault(product.getId(), 0);
      item.setCount(count);
      itemList.add( item );
    }

    model.addAttribute("items", itemList);

    Paging paging = new Paging();
    paging.setPageNumber(1);
    paging.setPageSize(10);
    paging.setNext(false);
    paging.setPrevious(false);

    model.addAttribute("paging", paging );

    return "main.html";
  }

  @GetMapping("/images/{id}")
  public ResponseEntity<byte[]> getImage(@PathVariable( name = "id" ) Long id) throws IOException {
    byte[]  imageData = productService.getImageByProductId(id); // image хранится как byte[]
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")  // Жёстко задаём тип
            .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(imageData.length))
            .header(HttpHeaders.CACHE_CONTROL, "no-transform") // Запрещаем преобразования
            .body(imageData);
  }


  @GetMapping("/items/{id}")
  public String getItem(@PathVariable(name="id") long id,
                        Model model ){
    Product product = productService.getProductById(id);
    Item item = new Item(product);
    int count = productsInThisBasket.getOrDefault(id,0);
    item.setCount(count);
    model.addAttribute("item", item);
    System.out.println("hello from item!");
    System.out.println("chosen product: " + product.getName());
    System.out.println("converted to item: " + item.getId());
    System.out.println("product.getImage is: " + product.getImage());
    System.out.println("item.getImage is:" +item.getImgPath());
    return "item.html";
  }

  @PostMapping("/items/{id}")
  public String addRemoveDeleteProductInBasket( @PathVariable(name="id") long id,
                                @RequestParam String action,
                                Model model ) {
    Long basketId = basket.getId();
    if( action.equals("plus") )   productService.addProductToBasket(id,basketId );
    if( action.equals("minus") )  productService.removeProductFromBasket(id,basketId );
    if( action.equals("delete") ) productService.dropProductFromBasket(id,basketId );
    refreshBasket();

    return "redirect:/items/{id}";
  }

  @GetMapping("/cart/items")
  public String getBasket( Model model ){
    List<Item> itemList = new ArrayList<>();
    for( BasketItem basketItem : basket.getBasketItems() ){
      Item item = new Item( basketItem.getProduct() );
      int count = basketItem.getQuantity();
      item.setCount(count);
      itemList.add( item );
    }
    model.addAttribute("items", itemList );
    boolean empty = false;
    if( itemList.isEmpty()) empty = true;
    model.addAttribute("empty", empty );
    model.addAttribute("total", inTotal );
    return "cart.html";
  }

}
