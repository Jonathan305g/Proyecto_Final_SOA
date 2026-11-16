package ec.edu.uta.inventariobodega.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioController {
  @GetMapping("/login")
  public String login() {
    return "login";
  }

  @GetMapping("/")
  public String inicio() {
    return "inicio";
  }

  @GetMapping("/acceso-denegado")
  public String denegado(HttpServletResponse response) {
    response.setStatus(403);
    return "error/403";
  }
}
