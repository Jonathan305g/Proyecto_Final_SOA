package ec.edu.uta.inventariobodega.service;

import ec.edu.uta.inventariobodega.dto.ProductoForm;
import ec.edu.uta.inventariobodega.entity.*;
import ec.edu.uta.inventariobodega.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {
  private final ProductoRepository productos;
  private final CategoriaRepository categorias;
  private final MarcaRepository marcas;
  private final EntradaInventarioRepository entradas;

  public ProductoService(
      ProductoRepository p,
      CategoriaRepository c,
      MarcaRepository m,
      EntradaInventarioRepository e) {
    productos = p;
    categorias = c;
    marcas = m;
    entradas = e;
  }

  @Transactional
  public void guardar(Long id, ProductoForm f) {
    if (productos.existsByCodigoIgnoreCaseAndIdNot(f.getCodigo().trim(), id == null ? 0L : id))
      throw new ReglaNegocioException("Ya existe un producto con ese código.");
    Producto p =
        id == null
            ? new Producto()
            : productos
                .bloquearPorId(id)
                .orElseThrow(() -> new ReglaNegocioException("El producto no existe."));
    p.setCodigo(f.getCodigo().trim());
    p.setNombre(f.getNombre().trim());
    p.setDescripcion(f.getDescripcion());
    p.setStockMinimo(f.getStockMinimo());
    p.setActivo(f.getActivo());
    p.setCategoria(
        categorias
            .findById(f.getCategoriaId())
            .orElseThrow(() -> new ReglaNegocioException("La categoría no existe.")));
    p.setMarca(
        marcas
            .findById(f.getMarcaId())
            .orElseThrow(() -> new ReglaNegocioException("La marca no existe.")));
    // No se acepta stock desde formularios: solamente lo modifica EntradaService.
    productos.saveAndFlush(p);
  }

  @Transactional(readOnly = true)
  public ProductoForm formulario(Long id) {
    Producto p =
        productos
            .findById(id)
            .orElseThrow(() -> new ReglaNegocioException("El producto no existe."));
    ProductoForm f = new ProductoForm();
    f.setCodigo(p.getCodigo());
    f.setNombre(p.getNombre());
    f.setDescripcion(p.getDescripcion());
    f.setStockMinimo(p.getStockMinimo());
    f.setCategoriaId(p.getCategoria().getId());
    f.setMarcaId(p.getMarca().getId());
    f.setActivo(p.getActivo());
    return f;
  }

  @Transactional
  public String eliminar(Long id) {
    Producto p =
        productos
            .bloquearPorId(id)
            .orElseThrow(() -> new ReglaNegocioException("El producto no existe."));
    if (entradas.existsByProductoId(id)) {
      p.setActivo(false);
      return "Producto desactivado. Se conservó su historial.";
    }
    productos.delete(p);
    productos.flush();
    return "Producto eliminado.";
  }
}
