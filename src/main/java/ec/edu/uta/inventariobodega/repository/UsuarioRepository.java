package ec.edu.uta.inventariobodega.repository;

import ec.edu.uta.inventariobodega.entity.*;
import org.springframework.data.jpa.repository.*;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
  java.util.Optional<Usuario> findByUsername(String username);

  boolean existsByUsernameAndIdNot(String username, Long id);

  long countByRolAndEstadoTrue(Rol rol);

  @Query(value = "SELECT * FROM usuarios ORDER BY id FOR UPDATE", nativeQuery = true)
  java.util.List<Usuario> bloquearUsuarios();
}
