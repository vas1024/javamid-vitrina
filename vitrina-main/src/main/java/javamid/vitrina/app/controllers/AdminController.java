package javamid.vitrina.app.controllers;

import com.opencsv.CSVReader;
import javamid.vitrina.app.dao.Product;
import javamid.vitrina.app.services.ProductService;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Controller
public class AdminController {

  private final ProductService productService;
  private final List<Product> products = new ArrayList<>();

  public AdminController(ProductService productService) {
    this.productService = productService;
  }

  @GetMapping("/admin/upload")
  public Mono<String> showForm(Model model) {
    model.addAttribute("message", "Выберите файл и нажмите 'Распарсить'");
    model.addAttribute("enableUpload", false);
    return Mono.just("admin_upload.html");
  }

  @PostMapping(value = "/admin/upload/parse", consumes = "multipart/form-data")
  public Mono<String> parseFile(@RequestPart("file") FilePart zipFile, Model model) {
    Path uploadDir = Paths.get("uploads");

    return Mono.fromCallable(() -> {
              // Очистка и создание директории
              if (Files.exists(uploadDir)) {
                FileSystemUtils.deleteRecursively(uploadDir);
              }
              Files.createDirectory(uploadDir);
              return uploadDir;
            })
            .flatMap(dir -> DataBufferUtils.join(zipFile.content())
                    .flatMap(dataBuffer -> {
                      try {
                        // Распаковка ZIP
                        ZipInputStream zis = new ZipInputStream(dataBuffer.asInputStream());
                        ZipEntry entry;
                        while ((entry = zis.getNextEntry()) != null) {
                          Path filePath = dir.resolve(entry.getName());
                          Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
                        }
                        return Mono.just(dir);
                      } catch (Exception e) {
                        return Mono.error(e);
                      } finally {
                        DataBufferUtils.release(dataBuffer);
                      }
                    })
            )
            .flatMap(dir -> {
              try {
                // Поиск CSV файла
                return Files.walk(dir)
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().toLowerCase().endsWith(".csv"))
                        .findFirst()
                        .map(csvPath -> processCsvFile(csvPath, dir, zipFile, model))
                        .orElseGet(() -> {
                          model.addAttribute("message", ".csv файл не найден в архиве");
                          model.addAttribute("enableUpload", false);
                          return Mono.just("admin_upload.html");
                        });
              } catch (IOException e) {
                return Mono.error(e);
              }
            });
  }

  @PostMapping("/admin/upload/upload")
  public Mono<String> uploadToDatabase( Model model) {
    return productService.saveProducts(products)
            .collectList()  // Преобразуем Flux<Product> в Mono<List<Product>>
            .doOnNext(savedProducts -> {
              // Добавляем атрибуты в модель после успешного сохранения
              model.addAttribute("message", "Успешно сохранено " + savedProducts.size() + " товаров");
              model.addAttribute("enableUpload", false);
            })
            .thenReturn("admin_upload");  // Возвращаем имя шаблона
  }


  private Mono<String> processCsvFile(Path csvPath, Path baseDir, FilePart zipFile, Model model) {
    return Mono.fromCallable(() -> {
      products.clear();
      try (CSVReader reader = new CSVReader(new InputStreamReader(Files.newInputStream(csvPath)))) {
        String[] nextLine;
        reader.readNext(); // Пропускаем заголовок
        while ((nextLine = reader.readNext()) != null) {
          Product product = new Product();
          product.setName(nextLine[0]);
          product.setDescription(nextLine[2]);
          product.setPrice(BigDecimal.valueOf(Double.parseDouble(nextLine[3])));

          String filePath = nextLine[1].trim();
          if (!filePath.isEmpty()) {
            product.setImage(Files.readAllBytes(baseDir.resolve(filePath)));
          }

          System.out.println("Product: id: " + product.getId() + "  name: " + product.getName() + "  price: " + product.getPrice() );

          products.add(product);
        }
      }
      model.addAttribute("message", "Файл '" + csvPath.getFileName() + "' успешно распарсен");
      model.addAttribute("enableUpload", true);
      model.addAttribute("fileName", zipFile.filename());
      System.out.println("file name after parsing " + zipFile.filename() );
      return "admin_upload.html";
    });
  }


}