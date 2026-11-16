package ec.edu.uta.inventariobodega.repository;

import ec.edu.uta.inventariobodega.entity.*;
import org.springframework.data.jpa.repository.*;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
  boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
