package ec.edu.uta.inventariobodega.service;

import ec.edu.uta.inventariobodega.dto.UsuarioForm;
import ec.edu.uta.inventariobodega.entity.*;
import ec.edu.uta.inventariobodega.repository.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {
  private final UsuarioRepository usuarios;
  private final PasswordEncoder encoder;

  public UsuarioService(UsuarioRepository u, PasswordEncoder e) {
    usuarios = u;
    encoder = e;
  }

  public static void validarPassword(String password) {
    if (password == null
        || password.length() < 10
        || password.getBytes(StandardCharsets.UTF_8).length > 72)
      throw new ReglaNegocioException(
          "La contraseña debe tener al menos 10 caracteres y hasta 72 bytes UTF-8.");
  }

  @Transactional
  public void guardar(Long id, UsuarioForm f, String actual) {
    // Serializa cambios de roles para conservar al menos un administrador activo.
    usuarios.bloquearUsuarios();
    if (usuarios.existsByUsernameAndIdNot(f.getUsername(), id == null ? 0L : id))
      throw new ReglaNegocioException("El nombre de usuario ya está registrado.");
    Usuario u =
        id == null
            ? new Usuario()
            : usuarios
                .findById(id)
                .orElseThrow(() -> new ReglaNegocioException("El usuario no existe."));
    if (id != null && u.getUsername().equals(actual) && (!f.getEstado() || f.getRol() != Rol.ADMIN))
      throw new ReglaNegocioException(
          "No puede desactivar su propia cuenta ni quitarse el rol de administrador.");
    if (id != null
        && u.getRol() == Rol.ADMIN
        && u.getEstado()
        && (!f.getEstado() || f.getRol() != Rol.ADMIN)
        && usuarios.countByRolAndEstadoTrue(Rol.ADMIN) <= 1)
      throw new ReglaNegocioException("Debe conservar un administrador activo.");
    if (id == null || (f.getPassword() != null && !f.getPassword().isEmpty())) {
      validarPassword(f.getPassword());
      u.setPassword(encoder.encode(f.getPassword()));
    }
    u.setUsername(f.getUsername());
    u.setRol(f.getRol());
    u.setEstado(f.getEstado());
    usuarios.saveAndFlush(u);
  }

  @Transactional(readOnly = true)
  public UsuarioForm formulario(Long id) {
    Usuario u =
        usuarios.findById(id).orElseThrow(() -> new ReglaNegocioException("El usuario no existe."));
    UsuarioForm f = new UsuarioForm();
    f.setUsername(u.getUsername());
    f.setRol(u.getRol());
    f.setEstado(u.getEstado());
    return f;
  }
}
