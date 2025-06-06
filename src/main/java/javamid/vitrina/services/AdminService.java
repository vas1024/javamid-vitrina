package javamid.vitrina.services;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import javamid.vitrina.dao.Product;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
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
