package javamid.vitrina.controllers;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import javamid.vitrina.dao.Product;
import javamid.vitrina.repositories.ProductRepository;
import javamid.vitrina.services.ProductService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Controller
public class AdminController{

  private final ProductService productService;
  public AdminController(ProductService productService) {
    this.productService = productService;
  }


  List<Product> products = new ArrayList<>();

  @GetMapping("/admin/upload")
  public String showForm(Model model) {
    model.addAttribute("message", "Выберите файл и нажмите 'Распарсить'");
    model.addAttribute("enableUpload", false); // Кнопка загрузки изначально неактивна
    return "admin_upload.html";
  }

  @PostMapping("/admin/upload/parse")
  public String parseFile(@RequestParam("file") MultipartFile zipFile, Model model) throws IOException, CsvValidationException {

    // 1. Создаём папку для распаковки (если её нет)
    Path uploadDir = Paths.get("uploads");
    if( Files.exists(uploadDir)) FileSystemUtils.deleteRecursively(uploadDir);
    Files.createDirectory(uploadDir);

    // 2. Читаем ZIP и извлекаем файлы
    try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        // Просто записываем файлы в папку uploads
        Path filePath = uploadDir.resolve(entry.getName());
        Files.copy(zis, filePath);
      }
    }

    // 3. находим среди файлов .csv
    Stream<Path> paths = Files.walk(uploadDir) ;
    Optional<Path> csvFile = paths
              .filter(Files::isRegularFile)
              .filter(path -> path.toString().toLowerCase().endsWith(".csv"))
              .findFirst();
    if( csvFile.isEmpty() ){
      model.addAttribute( "message", ".csv файл не найден в архиве " + zipFile.getOriginalFilename());
      model.addAttribute("enableUpload", false);
      return "admin_upload.html";
    }
    Path csvPath = csvFile.get();

    // 4. создаем список объектов из .csv
    Path baseDir = csvPath.getParent();
    Reader fileReader = Files.newBufferedReader(csvPath);
    CSVReader reader = new CSVReader(fileReader);
    String[] nextLine;
    reader.readNext(); // Пропускаем заголовок
    while ((nextLine = reader.readNext()) != null) {
      Product product = new Product();
      product.setName(nextLine[0]);
      product.setDescription(nextLine[2]);
      product.setPrice(BigDecimal.valueOf(Double.parseDouble(nextLine[3])));

      String filePath = nextLine[1].trim();
      System.out.println( "filePath="+filePath);
      if( !filePath.isEmpty() ) product.setImage(Files.readAllBytes(baseDir.resolve(filePath)) );

      products.add(product);
    }

    model.addAttribute("message", "Файл '" + csvPath.getFileName() + "' успешно распарсен");
    model.addAttribute("enableUpload", true); // Активируем кнопку загрузки
    model.addAttribute("fileName", zipFile.getOriginalFilename()); // Передаём имя файла
    return "admin_upload.html";
  }

  @PostMapping("/admin/upload/upload")
  public String uploadToDatabase(@RequestParam("fileName") String fileName, Model model) {

    productService.saveAll(products);

    // Здесь ваша логика загрузки в БД
    model.addAttribute("message", "Данные из файла '" + fileName + "' загружены в БД");
    model.addAttribute("enableUpload", false); // Деактивируем кнопку после загрузки
    return "admin_upload.html";
  }
}
