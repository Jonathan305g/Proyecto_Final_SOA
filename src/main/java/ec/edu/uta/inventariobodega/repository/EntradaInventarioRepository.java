package ec.edu.uta.inventariobodega.repository;

import ec.edu.uta.inventariobodega.entity.*;
import org.springframework.data.jpa.repository.*;

public interface EntradaInventarioRepository extends JpaRepository<EntradaInventario, Long> {
  boolean existsByProductoId(Long id);

  boolean existsByProveedorId(Long id);

  @EntityGraph(attributePaths = {"producto", "proveedor", "usuarioResponsable"})
  org.springframework.data.domain.Page<EntradaInventario> findAllByOrderByFechaDescIdDesc(
      org.springframework.data.domain.Pageable pageable);
}
