package javamid.vitrina.services;
/*
import com.opencsv.CSVReader;
import javamid.vitrina.dao.Product;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStreamReader;
import java.math.BigDecimal;

@Service
public class AdminService {

  public Flux<Product> parseCsvWithOpenCsv(FilePart file) {
    return Mono.fromCallable(() -> {
              // Создаем CSVReader из содержимого файла
              CSVReader reader = new CSVReader(
                      new InputStreamReader(file.content().blockFirst().asInputStream())
              );
              return reader;
            })
            .flatMapMany(reader -> {
              try {
                reader.readNext(); // Пропускаем заголовок
                return Flux.generate(() -> reader, (state, sink) -> {
                  try {
                    String[] nextLine = state.readNext();
                    if (nextLine != null) {
                      sink.next(createProduct(nextLine));
                    } else {
                      state.close();
                      sink.complete();
                    }
                  } catch (Exception e) {
                    sink.error(e);
                  }
                  return state;
                });
              } catch (Exception e) {
                return Flux.error(e);
              }
            });
  }

  private Product createProduct(String[] nextLine) throws Exception {
    Product product = new Product();
    product.setName(nextLine[0]);
    product.setDescription(nextLine[2]);
    product.setPrice(BigDecimal.valueOf(Double.parseDouble(nextLine[3])));

    // Загрузка изображения (блокирующая операция, но в fromCallable)
    product.setImage(loadImageBytes(nextLine[1]));

    System.out.println("Product: id: " + product.getId() + "  name: " + product.getName() + "  price: " + product.getPrice() );

    return product;
  }

  private byte[] loadImageBytes(String path) throws Exception {
    return StreamUtils.copyToByteArray(
            new ClassPathResource(path).getInputStream()
    );
  }
}

/*
public class AdminService {


  public List<Product> parseCsvWithOpenCsv(MultipartFile file) throws IOException, CsvValidationException {
    List<Product> products = new ArrayList<>();
    try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
      String[] nextLine;
      reader.readNext(); // Пропускаем заголовок
      while ((nextLine = reader.readNext()) != null) {
        Product product = new Product();
        product.setName(nextLine[0]);
        product.setDescription(nextLine[2]);
        product.setPrice(BigDecimal.valueOf(Double.parseDouble(nextLine[3])));

        String filePath = nextLine[1];
        ClassPathResource imgFile = new ClassPathResource(filePath);
        product.setImage( StreamUtils.copyToByteArray(imgFile.getInputStream()) );

        products.add(product);
      }
    }
    return products;
  }

}


 */
