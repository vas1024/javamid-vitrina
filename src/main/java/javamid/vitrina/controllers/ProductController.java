package javamid.vitrina.controllers;

import jakarta.annotation.PostConstruct;
import javamid.vitrina.dao.Basket;
import javamid.vitrina.dao.BasketItem;
import javamid.vitrina.model.Item;
import javamid.vitrina.model.Paging;
import javamid.vitrina.dao.Product;
import javamid.vitrina.dao.Order;
import javamid.vitrina.dao.OrderItem;
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
    Map<Long,Integer> productsInThisBasketRefreshed = new HashMap<>();
    BigDecimal sum = BigDecimal.valueOf(0);
    for ( BasketItem basketItem : basketItems ){
      Product product = basketItem.getProduct();
      int quantity = basketItem.getQuantity();
      productsInThisBasketRefreshed.put(product.getId(),quantity);

      BigDecimal price = product.getPrice();
      sum = sum.add( price.multiply(BigDecimal.valueOf(quantity)) );
    }
    this.productsInThisBasket = productsInThisBasketRefreshed;
    this.inTotal = sum;

    System.out.println("after refresh basket");
    System.out.println("productsInThisBasket: " + productsInThisBasket );
    System.out.println("inTotal: " + inTotal );
  }


  @GetMapping("/")
  public String homePage() { return "redirect:/main/items"; }


  @GetMapping("/main/items")
  public String getItems(
          @RequestParam(name = "search", required = false, defaultValue = "" ) String keyword,
          @RequestParam(name = "sort",  defaultValue = "NO" ) String sort,
          @RequestParam(name = "pageSize", defaultValue = "10") int size,
          @RequestParam(name = "pageNumber", defaultValue = "1") int page,
          Model model
          ) {

    Page<Product> productPage = productService.getProducts( keyword, sort,page - 1, size );

    List<Item> itemList = new ArrayList<>();
    for( Product product : productPage.getContent() ){
      Item item = new Item(product);
      int count = productsInThisBasket.getOrDefault(product.getId(), 0);
      item.setCount(count);
      itemList.add( item );
    }

    model.addAttribute("items", itemList);


    Paging paging = new Paging();
    paging.setPageNumber( productPage.getNumber() + 1 );
    paging.setPageSize( productPage.getSize() );
    paging.setNext( productPage.hasNext() );
    paging.setPrevious( productPage.hasPrevious() );

    model.addAttribute("paging", paging );
    model.addAttribute("search", keyword );
    model.addAttribute("sort",   sort );
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


  @GetMapping("/orderimages/{id}")
  public ResponseEntity<byte[]> getOrderImage(@PathVariable( name = "id" ) Long id) throws IOException {
    byte[]  imageData = productService.getImageByOrderItemId(id); // image хранится как byte[]
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
    return "item.html";
  }



  public void plusMinusDelete( Long id, String action ){
    Long basketId = basket.getId();
    if( action.equals("plus") )   productService.addProductToBasket(id,basketId );
    if( action.equals("minus") )  productService.removeProductFromBasket(id,basketId );
    if( action.equals("delete") ) productService.dropProductFromBasket(id,basketId );
    refreshBasket();
  }

  @PostMapping("/items/{id}")
  public String addRemoveDeleteProductInProductItem( @PathVariable(name="id") long id,
                                @RequestParam String action,
                                Model model ) {
    plusMinusDelete( id, action );
    return "redirect:/items/{id}";
  }

  @PostMapping("/cart/items/{id}")
  public String addRemoveDeleteProductInBasket( @PathVariable(name="id") long id,
                                                @RequestParam String action,
                                                Model model ) {
    plusMinusDelete( id, action );
    return "redirect:/cart/items";
  }

  @PostMapping("/main/items/{id}")
  public String addRemoveDeleteProductInMain( @PathVariable(name="id") long id,
                                              @RequestParam String action,
                                              Model model ) {
    plusMinusDelete( id, action );
    return "redirect:/main/items";
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


  @GetMapping("/orders")
  public String getOrders( Model model ){
    Long basketId = basket.getId();
    List<Order> orderList = productService.findAllOrders( basketId );

    List<Item> itemList = new ArrayList<>();
    record OrderModel( Long id, BigDecimal totalSum, List<Item> items ) {}
    List<OrderModel> orderModelList = new ArrayList<>();
    for( Order order : orderList ){
      List<OrderItem> orderItemList = order.getOrderItems();
      System.out.println("!!!!! " + orderItemList );
      BigDecimal inTotal = BigDecimal.valueOf(0);
      for( OrderItem orderItem : orderItemList ){
        Item item = new Item(orderItem);
        itemList.add( item );
        inTotal = inTotal.add(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
      }
      OrderModel orderModel = new OrderModel( order.getId(), inTotal, itemList );
      orderModelList.add( orderModel );
    }
    model.addAttribute("orders", orderModelList );

    for( OrderModel orderModel : orderModelList ){
      System.out.println( orderModel.id() );
      for( Item item : orderModel.items() ){
        System.out.println( "- " + item.getId() + " " + item.getTitle() );
      }
    }

    return "orders.html";
  }



  @GetMapping("/orders/{id}")
  public String getOrder( @PathVariable(name="id") long orderId,
                          @RequestParam(name = "newOrder", required = false, defaultValue = "false" ) Boolean newOrder,
                          Model model ) {

//    Long basketId = basket.getId();
    Order order = productService.findOrderById( orderId );

    BigDecimal inTotal = BigDecimal.valueOf(0);
    List<Item> itemList = new ArrayList<>();
    for( OrderItem orderItem : order.getOrderItems() ) {
      Item item = new Item(orderItem);
      item.setId(orderItem.getProductId());  // это костыль, чтобы при клике на картинку шел переход по ссылке на товар
      itemList.add( item );
      inTotal = inTotal.add(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
    }

    record OrderModel( Long id, BigDecimal totalSum, List<Item> items ) {}
    OrderModel orderModel = new OrderModel( orderId, inTotal, itemList );
    model.addAttribute("order", orderModel );
    return "order.html";
  }



  @PostMapping
  public String postBuy(){

    return "redirect:/orders/{id}?newOrder=true";
  }

}
