package ec.edu.uta.inventariobodega.controller;

import ec.edu.uta.inventariobodega.service.ReglaNegocioException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class ErroresController {
  @ExceptionHandler(ReglaNegocioException.class)
  public String negocio(ReglaNegocioException ex, Model m, HttpServletResponse r) {
    r.setStatus(400);
    m.addAttribute("mensaje", ex.getMessage());
    return "error/400";
  }

  @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
  public String integridad(Model m, HttpServletResponse r) {
    r.setStatus(409);
    m.addAttribute("mensaje", "El registro está asociado a otros datos. No se guardaron cambios.");
    return "error/400";
  }
}
