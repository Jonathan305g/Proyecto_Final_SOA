package ec.edu.uta.inventariobodega.repository;

import ec.edu.uta.inventariobodega.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
  boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

  boolean existsByCategoriaId(Long id);

  boolean existsByMarcaId(Long id);

  // SQL compatible con MySQL y el servidor incluido en XAMPP. El bloqueo dura la transacción.
  @Query(value = "SELECT * FROM productos WHERE id = :id FOR UPDATE", nativeQuery = true)
  java.util.Optional<Producto> bloquearPorId(@Param("id") Long id);

  @EntityGraph(attributePaths = {"categoria", "marca"})
  @Query(
      "select p from Producto p where (:q = '' or lower(p.codigo) like lower(concat('%', :q, '%'))"
          + " or lower(p.nombre) like lower(concat('%', :q, '%'))) and (:categoria is null or"
          + " p.categoria.id = :categoria) order by p.nombre")
  org.springframework.data.domain.Page<Producto> buscar(
      @Param("q") String q,
      @Param("categoria") Long categoria,
      org.springframework.data.domain.Pageable pageable);

  @EntityGraph(attributePaths = {"categoria", "marca"})
  java.util.List<Producto> findByActivoTrueOrderByNombreAsc();
}
