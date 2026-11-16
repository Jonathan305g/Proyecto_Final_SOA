package ec.edu.uta.inventariobodega.controller;

import ec.edu.uta.inventariobodega.dto.ProductoForm;
import ec.edu.uta.inventariobodega.repository.*;
import ec.edu.uta.inventariobodega.service.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProductoController {
  private final ProductoRepository productos;
  private final CategoriaRepository categorias;
  private final MarcaRepository marcas;
  private final ProductoService servicio;

  public ProductoController(
      ProductoRepository p, CategoriaRepository c, MarcaRepository m, ProductoService s) {
    productos = p;
    categorias = c;
    marcas = m;
    servicio = s;
  }

  @GetMapping({"/productos", "/inventario"})
  public String listar(
      @RequestParam(defaultValue = "") String q,
      @RequestParam(required = false) Long categoria,
      @RequestParam(defaultValue = "0") int page,
      Model model,
      jakarta.servlet.http.HttpServletRequest req) {
    model.addAttribute(
        "pagina", productos.buscar(q.trim(), categoria, PageRequest.of(Math.max(0, page), 15)));
    model.addAttribute("categorias", categorias.findAll());
    model.addAttribute("q", q);
    model.addAttribute("categoria", categoria);
    model.addAttribute("ruta", req.getServletPath());
    model.addAttribute("esInventario", req.getServletPath().equals("/inventario"));
    return "productos/lista";
  }

  private String vista(Model model, Long id) {
    model.addAttribute("id", id);
    model.addAttribute("categorias", categorias.findAll());
    model.addAttribute("marcas", marcas.findAll());
    return "productos/formulario";
  }

  @GetMapping("/admin/productos/nuevo")
  public String nuevo(Model m) {
    ProductoForm f = new ProductoForm();
    f.setActivo(true);
    f.setStockMinimo(0);
    m.addAttribute("form", f);
    return vista(m, null);
  }

  @GetMapping("/admin/productos/{id}/editar")
  public String editar(@PathVariable Long id, Model m) {
    m.addAttribute("form", servicio.formulario(id));
    return vista(m, id);
  }

  @PostMapping({"/admin/productos", "/admin/productos/{id}"})
  public String guardar(
      @PathVariable(required = false) Long id,
      @Valid @ModelAttribute("form") ProductoForm f,
      BindingResult errors,
      Model m,
      RedirectAttributes flash) {
    if (!errors.hasErrors())
      try {
        servicio.guardar(id, f);
        flash.addFlashAttribute("exito", "Producto guardado correctamente.");
        return "redirect:/productos";
      } catch (ReglaNegocioException ex) {
        errors.reject("negocio", ex.getMessage());
      } catch (org.springframework.dao.DataIntegrityViolationException ex) {
        errors.reject("integridad", "El código ya existe o cambió un catálogo. Revise los datos.");
      }
    return vista(m, id);
  }

  @PostMapping("/admin/productos/{id}/eliminar")
  public String eliminar(@PathVariable Long id, RedirectAttributes f) {
    f.addFlashAttribute("exito", servicio.eliminar(id));
    return "redirect:/productos";
  }
}
