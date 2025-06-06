package javamid.vitrina.controllers;

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
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ProductController {

  private final ProductService productService;
  public ProductController(ProductService productService) {
    this.productService = productService;
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

    //public Page<Product> getProducts(String keyword, int page, int size) {

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
      itemList.add( new Item(product));
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
    model.addAttribute("item", item);
    System.out.println("hello from item!");
    System.out.println("chosen product: " + product.getName());
    System.out.println("converted to item: " + item.getId());
    System.out.println("product.getImage is: " + product.getImage());
    System.out.println("item.getImage is:" +item.getImgPath());
    return "item.html";
  }

}
