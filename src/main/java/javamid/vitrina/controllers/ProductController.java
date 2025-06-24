package javamid.vitrina.controllers;


import javamid.vitrina.dao.Basket;
import javamid.vitrina.dao.BasketItem;
import javamid.vitrina.model.Item;
import javamid.vitrina.model.Paging;
import javamid.vitrina.dao.Product;
import javamid.vitrina.dao.Order;
import javamid.vitrina.dao.OrderItem;
import javamid.vitrina.services.ProductService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.util.Pair;



import javamid.vitrina.services.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

@Controller
public class ProductController {

  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  public Long currentBasket() { return 1L; }

  @GetMapping("/")
  public Mono<String> homePage() {
    return Mono.just("redirect:/main/items");
  }


  /*

  @GetMapping("/main/items")
  public Mono<String> getItems(
          @RequestParam(name = "search", required = false, defaultValue = "") String keyword,
          @RequestParam(name = "sort", defaultValue = "NO") String sort,
          @RequestParam(name = "pageSize", defaultValue = "10") int size,
          @RequestParam(name = "pageNumber", defaultValue = "1") int page,
          Model model) {


    Flux<Product> productFlux = productService.getProducts(keyword, sort, page - 1, size);
    Flux<BasketItem> basketItemFlux = productService.getBasketItems( currentBasket() );


// Сначала собираем корзину в Map для быстрого поиска
    Mono<Map<Long, Integer>> basketItemsMapMono = basketItemFlux
            .collectMap(BasketItem::getProductId, BasketItem::getQuantity);

// Затем обрабатываем продукты с учетом данных из корзины
    Mono<List<Item>> itemsMono = basketItemsMapMono.flatMap(basketItemsMap ->
            productFlux
                    .map(product -> {
                      Item item = new Item(product);
                      // Устанавливаем количество из корзины или 0, если продукта нет в корзине
                      item.setCount(basketItemsMap.getOrDefault(product.getId(), 0));
                      return item;
                    })
                    .collectList()
    );


    // Фиксированные значения пагинации
    int pageNumber = 1;
    int pageSize = 10;

    //  Заполняем модель и возвращаем шаблон
    return itemsMono
            .doOnNext(itemList -> {
              model.addAttribute("items", itemList);
              model.addAttribute("search", keyword);
              model.addAttribute("sort", sort);

              // Фиктивная пагинация (всегда page=1, size=1)
              Paging paging = new Paging();
              paging.setPageNumber(pageNumber);
              paging.setPageSize(pageSize);
              paging.setNext(false); // Нет следующей страницы
              paging.setPrevious(false); // Нет предыдущей страницы

              model.addAttribute("paging", paging);
            })
            .thenReturn("main.html");
  }

  */


  @GetMapping("/main/items")
  public Mono<String> getItems(
          @RequestParam(name = "search", required = false, defaultValue = "") String keyword,
          @RequestParam(name = "sort", defaultValue = "NO") String sort,
          @RequestParam(name = "pageSize", defaultValue = "10") int size,
          @RequestParam(name = "pageNumber", defaultValue = "1") int page,
          Model model) {

    Mono<Long> totalItemsMono = productService.countProducts(keyword, sort, page - 1, size);
    Flux<Product> productFlux = productService.getProducts(keyword, sort, page - 1, size);
    Flux<BasketItem> basketItemFlux = productService.getBasketItems(currentBasket());

    Mono<Map<Long, Integer>> basketItemsMapMono = basketItemFlux
            .collectMap(BasketItem::getProductId, BasketItem::getQuantity);

    Mono<List<Item>> itemsMono = basketItemsMapMono.flatMap(basketItemsMap ->
            productFlux
                    .map(product -> {
                      Item item = new Item(product);
                      item.setCount(basketItemsMap.getOrDefault(product.getId(), 0));
                      return item;
                    })
                    .collectList()
    );

    return Mono.zip(itemsMono, totalItemsMono)
            .doOnNext(tuple -> {
              List<Item> items = tuple.getT1();
              long totalItems = tuple.getT2();

              model.addAttribute("items", items);
              model.addAttribute("search", keyword);
              model.addAttribute("sort", sort);

              Paging paging = new Paging();
              paging.setPageNumber(page);
              paging.setPageSize(size);

              int totalPages = (int) Math.ceil((double) totalItems / size);
              paging.setNext(page < totalPages);
              paging.setPrevious(page > 1);

              model.addAttribute("paging", paging);
              model.addAttribute("totalItems", totalItems);
            })
            .thenReturn("main.html");
  }







  @GetMapping("/images/{id}")
  public Mono<ResponseEntity<byte[]>> getImage(@PathVariable Long id) {
    return productService.getImageByProductId(id)
            .map(imageData -> ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .header(HttpHeaders.CACHE_CONTROL, "no-transform")
                    .body(imageData));
  }


  @GetMapping("/items/{id}")
  public Mono<String> getItem(@PathVariable(name="id") long id,
                        Model model ) {
//    Mono<Item> itemMono = productService.getProductItem(id, currentBasket())
    return productService.getProductItem(id, currentBasket())
            .doOnNext(item -> {
              model.addAttribute("item", item);
//              System.out.println("Item = " + item.getDescription());
            })
            .doOnError(e -> System.err.println("Error: " + e)) // Добавлено
            .thenReturn("item.html")
            .switchIfEmpty(Mono.defer(() -> {
              System.out.println("Item not found for id: " + id); // Добавлено
              return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND));
            }));

  }

  @GetMapping("/cart/items")
  public Mono<String> getBasket( Model model ){
    Flux<Item> basketItems = productService.getItemsFromBasket( currentBasket() ) ;
    return basketItems
            .collectList()  // собираем Flux в Mono<List<Item>>
            .doOnNext(items -> {
              BigDecimal total = items.stream()
                      .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getCount())))
                      .reduce(BigDecimal.ZERO, BigDecimal::add);
              model.addAttribute("items", items);
              model.addAttribute("empty", items.isEmpty());
              model.addAttribute("total", total);
            })
            .thenReturn("cart.html");
  }


// три раза одинаковый код. костыль потому что не получается  @RequestParam(name="action")
  @PostMapping(value = "/items/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE )
  public Mono<String> addRemoveDeleteProductInProductItem( @PathVariable(name="id") long id,
                                                      ServerWebExchange exchange  ) {
    return exchange.getFormData()
            .flatMap(formData -> {
              String action = formData.getFirst("action");
              return productService.plusMinusDelete(id, currentBasket(), action)
                      .thenReturn("redirect:/items/" + id );
            });
  }
  @PostMapping(value = "/cart/items/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE )
  public Mono<String> addRemoveDeleteProductInBasket( @PathVariable(name="id") long id,
                                                    ServerWebExchange exchange  ) {
    return exchange.getFormData()
            .flatMap(formData -> {
              String action = formData.getFirst("action");
              return productService.plusMinusDelete(id, currentBasket(), action)
                      .thenReturn("redirect:/cart/items");
            });
  }
    @PostMapping(value = "/main/items/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE )
  public Mono<String> addRemoveDeleteProductInMain( @PathVariable(name="id") long id,
// does not work !!!                                @RequestParam(name="action") String action
                                                    ServerWebExchange exchange ) {
    return exchange.getFormData()
            .flatMap(formData -> {
              String action = formData.getFirst("action");
              return productService.plusMinusDelete(id, currentBasket(), action)
                      .thenReturn("redirect:/main/items");
            });
  }




}









/*

@Controller
public class ProductController {

  private final ProductService productService;
  private final UserService userService;
  public ProductController(ProductService productService,
                           UserService userService) {
    this.productService = productService;
    this.userService = userService;
  }

  private  Basket basket;
  private  Map<Long,Integer> productsInThisBasket = new HashMap<>();
  private  BigDecimal inTotal;



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






  @GetMapping("/orders")
  public String getOrders( Model model ){
    Long basketId = basket.getId();
    List<Order> orderList = productService.findAllOrders( basketId );

    record OrderModel( Long id, BigDecimal totalSum, List<Item> items ) {}
    List<OrderModel> orderModelList = new ArrayList<>();
    for( Order order : orderList ){
      List<OrderItem> orderItemList = order.getOrderItems();
      System.out.println("!!!!! " + orderItemList );
      BigDecimal inTotal = BigDecimal.valueOf(0);
      List<Item> itemList = new ArrayList<>();
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



  @PostMapping("/buy")
  public String postBuy( RedirectAttributes redirectAttributes ){

    Long id = productService.makeOrder( basket );
    System.out.println("Buy id : " + id);
    redirectAttributes.addAttribute("id", id); // для URL
    redirectAttributes.addFlashAttribute("newOrder", true); // для модели
    return "redirect:/orders/{id}";
  }

}


 */


