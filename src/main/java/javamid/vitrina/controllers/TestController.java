package javamid.vitrina.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TestController {

  @GetMapping("/form")
  public String showForm() {
    return "test_form.html";
  }

  @PostMapping("/submit")
  public String submitForm(@RequestParam("name") String name,
                           @RequestParam("email") String email,
                           Model model) {
    System.out.println("name = " + name );
    System.out.println("email = " + email );
    return "test_form.html";
  }
}