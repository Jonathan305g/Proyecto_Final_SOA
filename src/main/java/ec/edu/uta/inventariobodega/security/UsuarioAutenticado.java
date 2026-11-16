package ec.edu.uta.inventariobodega.security;

import ec.edu.uta.inventariobodega.entity.Usuario;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

/** Conserva una huella para revocar sesiones sin retener la contraseña de autenticación. */
public final class UsuarioAutenticado extends User {
  private static final long serialVersionUID = 1L;
  private final String huellaCredenciales;

  public UsuarioAutenticado(Usuario usuario) {
    super(
        usuario.getUsername(),
        usuario.getPassword(),
        usuario.getEstado(),
        true,
        true,
        true,
        List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())));
    huellaCredenciales = huella(usuario.getPassword());
  }

  public String getHuellaCredenciales() {
    return huellaCredenciales;
  }

  public static String huella(String hash) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(hash.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 no está disponible en el JDK.", ex);
    }
  }
}
