package ec.edu.uta.inventariobodega.security;

import ec.edu.uta.inventariobodega.entity.*;
import ec.edu.uta.inventariobodega.repository.UsuarioRepository;
import ec.edu.uta.inventariobodega.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.bootstrap.enabled", havingValue = "true", matchIfMissing = true)
public class PrimerAdministrador implements CommandLineRunner {
  private final UsuarioRepository usuarios;
  private final PasswordEncoder encoder;
  private final String username, password;

  public PrimerAdministrador(
      UsuarioRepository u,
      PasswordEncoder e,
      @Value("${app.bootstrap.username:}") String username,
      @Value("${app.bootstrap.password:}") String password) {
    usuarios = u;
    encoder = e;
    this.username = username;
    this.password = password;
  }

  @Override
  @Transactional
  public void run(String... args) {
    if (usuarios.count() != 0) return;
    if (!username.matches("[a-zA-Z0-9._-]{3,60}"))
      throw new IllegalStateException(
          "Base vacía: configure APP_ADMIN_USERNAME y APP_ADMIN_PASSWORD, o"
              + " .local/application.properties.");
    UsuarioService.validarPassword(password);
    Usuario u = new Usuario();
    u.setUsername(username);
    u.setPassword(encoder.encode(password));
    u.setRol(Rol.ADMIN);
    u.setEstado(true);
    usuarios.save(u);
  }
}
