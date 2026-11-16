package ec.edu.uta.inventariobodega.controller;

import ec.edu.uta.inventariobodega.dto.UsuarioForm;
import ec.edu.uta.inventariobodega.entity.Rol;
import ec.edu.uta.inventariobodega.repository.UsuarioRepository;
import ec.edu.uta.inventariobodega.service.*;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioController {
  private final UsuarioRepository usuarios;
  private final UsuarioService servicio;

  public UsuarioController(UsuarioRepository u, UsuarioService s) {
    usuarios = u;
    servicio = s;
  }

  @GetMapping
  public String lista(Model m) {
    m.addAttribute(
        "usuarios", usuarios.findAll(org.springframework.data.domain.Sort.by("username")));
    return "usuarios/lista";
  }

  private String vista(Model m, Long id) {
    m.addAttribute("roles", Rol.values());
    m.addAttribute("id", id);
    return "usuarios/formulario";
  }

  @GetMapping("/nuevo")
  public String nuevo(Model m) {
    UsuarioForm f = new UsuarioForm();
    f.setEstado(true);
    f.setRol(Rol.BODEGUERO);
    m.addAttribute("form", f);
    return vista(m, null);
  }

  @GetMapping("/{id}/editar")
  public String editar(@PathVariable Long id, Model m) {
    m.addAttribute("form", servicio.formulario(id));
    return vista(m, id);
  }

  @PostMapping({"", "/{id}"})
  public String guardar(
      @PathVariable(required = false) Long id,
      @Valid @ModelAttribute("form") UsuarioForm f,
      BindingResult e,
      Model m,
      Principal actual,
      RedirectAttributes flash) {
    if (!e.hasErrors())
      try {
        servicio.guardar(id, f, actual.getName());
        flash.addFlashAttribute("exito", "Usuario guardado correctamente.");
        return "redirect:/admin/usuarios";
      } catch (ReglaNegocioException ex) {
        e.reject("negocio", ex.getMessage());
      } catch (org.springframework.dao.DataIntegrityViolationException ex) {
        e.reject("duplicado", "El nombre de usuario ya está registrado.");
      }
    f.setPassword(null);
    return vista(m, id);
  }
}
