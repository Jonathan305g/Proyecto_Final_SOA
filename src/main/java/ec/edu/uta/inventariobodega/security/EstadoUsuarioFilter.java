package ec.edu.uta.inventariobodega.security;

import ec.edu.uta.inventariobodega.repository.UsuarioRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// Revoca sesiones si un administrador desactiva la cuenta, cambia el rol o la contraseña.
@Component
@Order(0)
public class EstadoUsuarioFilter extends OncePerRequestFilter {
  private final UsuarioRepository usuarios;

  public EstadoUsuarioFilter(UsuarioRepository usuarios) {
    this.usuarios = usuarios;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
      var u = usuarios.findByUsername(auth.getName());
      boolean valido =
          u.isPresent()
              && u.get().getEstado()
              && auth.getAuthorities().stream()
                  .anyMatch(a -> a.getAuthority().equals("ROLE_" + u.get().getRol().name()));
      if (valido && auth.getPrincipal() instanceof UsuarioAutenticado details) {
        valido =
            UsuarioAutenticado.huella(u.get().getPassword())
                .equals(details.getHuellaCredenciales());
      }
      if (!valido) {
        new SecurityContextLogoutHandler().logout(req, res, auth);
        res.sendRedirect(req.getContextPath() + "/login?inactiva");
        return;
      }
    }
    chain.doFilter(req, res);
  }
}
