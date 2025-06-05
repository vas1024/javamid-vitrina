package javamid.vitrina;

import javamid.vitrina.dao.Product;
import org.springframework.data.domain.Page;

public class testUtils {

  public static void printPage(Page<Product> page) {
    System.out.println("\n=== Page Content ===");
    System.out.println("Total elements: " + page.getTotalElements());
    System.out.println("Total pages: " + page.getTotalPages());
    System.out.println("Current page: " + (page.getNumber() + 1) + " of " + page.getTotalPages());
    System.out.println("Page size: " + page.getSize());
    System.out.println("Has next: " + page.hasNext());
    System.out.println("Has previous: " + page.hasPrevious());
    System.out.println("\nItems:");

    page.getContent().forEach( product -> {
      System.out.println( product.getId() );
      System.out.println( product.getName() );
      System.out.println( product.getDescription() );
    });


    for( Product product : page.getContent()){
      System.out.println("id " + product.getId() );
      System.out.println("name " + product.getName() );
      System.out.println("description " + product.getDescription() );
    }
  }
}


