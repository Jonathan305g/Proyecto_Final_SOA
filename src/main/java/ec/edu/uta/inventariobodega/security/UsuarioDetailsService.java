package ec.edu.uta.inventariobodega.security;

import ec.edu.uta.inventariobodega.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioDetailsService implements UserDetailsService {
  private final UsuarioRepository usuarios;

  public UsuarioDetailsService(UsuarioRepository usuarios) {
    this.usuarios = usuarios;
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) {
    var u =
        usuarios
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
    return new UsuarioAutenticado(u);
  }
}
