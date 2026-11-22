package ec.edu.uta.inventariobodega.controller;

import ec.edu.uta.inventariobodega.dto.EntradaForm;
import ec.edu.uta.inventariobodega.repository.*;
import ec.edu.uta.inventariobodega.service.*;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/entradas")
public class EntradaController {
  private final EntradaService servicio;
  private final ProductoRepository productos;
  private final ProveedorRepository proveedores;
  private final EntradaInventarioRepository entradas;

  public EntradaController(
      EntradaService s,
      ProductoRepository p,
      ProveedorRepository pr,
      EntradaInventarioRepository e) {
    servicio = s;
    productos = p;
    proveedores = pr;
    entradas = e;
  }

  @GetMapping
  public String historial(@RequestParam(defaultValue = "0") int page, Model m) {
    m.addAttribute(
        "pagina", entradas.findAllByOrderByFechaDescIdDesc(PageRequest.of(Math.max(0, page), 15)));
    return "entradas/historial";
  }

  private String vista(Model m) {
    m.addAttribute("productos", productos.findByActivoTrueOrderByNombreAsc());
    m.addAttribute(
        "proveedores", proveedores.findAll(org.springframework.data.domain.Sort.by("nombre")));
    return "entradas/formulario";
  }

  @GetMapping("/nueva")
  public String nueva(Model m) {
    m.addAttribute("form", new EntradaForm());
    return vista(m);
  }

  @PostMapping
  public String registrar(
      @Valid @ModelAttribute("form") EntradaForm f,
      BindingResult errors,
      Principal usuario,
      Model m,
      RedirectAttributes flash) {
    if (!errors.hasErrors())
      try {
        servicio.registrar(f, usuario.getName());
        flash.addFlashAttribute(
            "exito", "Pedido registrado exitosamente en estado PENDIENTE. Esperando aprobación del administrador.");
        return "redirect:/entradas";
      } catch (ReglaNegocioException ex) {
        errors.reject("negocio", ex.getMessage());
      } catch (org.springframework.dao.DataAccessException ex) {
        errors.reject(
            "persistencia",
            "No se pudo registrar el pedido. No se guardó ningún cambio. Revise los datos e"
                + " intente de nuevo.");
      }
    return vista(m);
  }

  @PostMapping("/{id}/aprobar")
  public String aprobar(@PathVariable Long id, RedirectAttributes flash) {
    try {
      servicio.aprobar(id);
      flash.addFlashAttribute(
          "exito", "Pedido aprobado exitosamente. Se descontaron las unidades correspondientes del inventario.");
    } catch (ReglaNegocioException ex) {
      flash.addFlashAttribute("error", ex.getMessage());
    }
    return "redirect:/entradas";
  }

  @PostMapping("/{id}/rechazar")
  public String rechazar(
      @PathVariable Long id,
      @RequestParam(required = false) String motivo,
      RedirectAttributes flash) {
    try {
      servicio.rechazar(id, motivo);
      flash.addFlashAttribute(
          "exito", "Pedido rechazado. El stock de existencias no fue modificado.");
    } catch (ReglaNegocioException ex) {
      flash.addFlashAttribute("error", ex.getMessage());
    }
    return "redirect:/entradas";
  }
}
