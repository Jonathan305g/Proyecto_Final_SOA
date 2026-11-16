package ec.edu.uta.inventariobodega.controller;

import ec.edu.uta.inventariobodega.dto.*;
import ec.edu.uta.inventariobodega.service.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class CatalogoController {
  private final CatalogoService servicio;

  public CatalogoController(CatalogoService s) {
    servicio = s;
  }

  private String titulo(String tipo) {
    return switch (tipo) {
      case "categorias" -> "Categorías";
      case "marcas" -> "Marcas";
      default -> "Proveedores";
    };
  }

  @GetMapping("/{tipo:categorias|marcas|proveedores}")
  public String lista(@PathVariable String tipo, Model m) {
    m.addAttribute("filas", servicio.listar(tipo));
    m.addAttribute("tipo", tipo);
    m.addAttribute("titulo", titulo(tipo));
    return "catalogos/lista";
  }

  private String vista(String tipo, Long id, Model m) {
    m.addAttribute("tipo", tipo);
    m.addAttribute("id", id);
    m.addAttribute("titulo", titulo(tipo));
    return "catalogos/formulario";
  }

  @GetMapping("/{tipo:categorias|marcas}/nuevo")
  public String nuevo(@PathVariable String tipo, Model m) {
    m.addAttribute("form", new CatalogoForm());
    return vista(tipo, null, m);
  }

  @GetMapping("/{tipo:categorias|marcas}/{id}/editar")
  public String editar(@PathVariable String tipo, @PathVariable Long id, Model m) {
    m.addAttribute("form", servicio.formulario(tipo, id));
    return vista(tipo, id, m);
  }

  @PostMapping({"/{tipo:categorias|marcas}", "/{tipo:categorias|marcas}/{id}"})
  public String guardar(
      @PathVariable String tipo,
      @PathVariable(required = false) Long id,
      @Valid @ModelAttribute("form") CatalogoForm f,
      BindingResult e,
      Model m,
      RedirectAttributes flash) {
    if (!e.hasErrors())
      try {
        servicio.guardar(tipo, id, f);
        flash.addFlashAttribute("exito", "Registro guardado correctamente.");
        return "redirect:/admin/" + tipo;
      } catch (ReglaNegocioException ex) {
        e.reject("negocio", ex.getMessage());
      } catch (org.springframework.dao.DataIntegrityViolationException ex) {
        e.reject("duplicado", "Ya existe un registro con ese nombre.");
      }
    return vista(tipo, id, m);
  }

  @GetMapping("/proveedores/nuevo")
  public String nuevoProveedor(Model m) {
    m.addAttribute("form", new ProveedorForm());
    return vista("proveedores", null, m);
  }

  @GetMapping("/proveedores/{id}/editar")
  public String editarProveedor(@PathVariable Long id, Model m) {
    m.addAttribute("form", servicio.proveedorForm(id));
    return vista("proveedores", id, m);
  }

  @PostMapping({"/proveedores", "/proveedores/{id}"})
  public String guardarProveedor(
      @PathVariable(required = false) Long id,
      @Valid @ModelAttribute("form") ProveedorForm f,
      BindingResult e,
      Model m,
      RedirectAttributes flash) {
    if (!e.hasErrors())
      try {
        servicio.guardarProveedor(id, f);
        flash.addFlashAttribute("exito", "Proveedor guardado correctamente.");
        return "redirect:/admin/proveedores";
      } catch (ReglaNegocioException ex) {
        e.reject("negocio", ex.getMessage());
      }
    return vista("proveedores", id, m);
  }

  @PostMapping("/{tipo:categorias|marcas|proveedores}/{id}/eliminar")
  public String eliminar(@PathVariable String tipo, @PathVariable Long id, RedirectAttributes f) {
    try {
      servicio.eliminar(tipo, id);
      f.addFlashAttribute("exito", "Registro eliminado.");
    } catch (ReglaNegocioException ex) {
      f.addFlashAttribute("error", ex.getMessage());
    } catch (org.springframework.dao.DataIntegrityViolationException ex) {
      f.addFlashAttribute("error", "El registro está en uso y no se puede eliminar.");
    }
    return "redirect:/admin/" + tipo;
  }
}
