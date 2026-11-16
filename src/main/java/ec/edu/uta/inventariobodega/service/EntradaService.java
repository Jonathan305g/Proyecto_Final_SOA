package ec.edu.uta.inventariobodega.service;

import ec.edu.uta.inventariobodega.dto.EntradaForm;
import ec.edu.uta.inventariobodega.entity.*;
import ec.edu.uta.inventariobodega.repository.*;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EntradaService {
  private final ProductoRepository productos;
  private final ProveedorRepository proveedores;
  private final UsuarioRepository usuarios;
  private final EntradaInventarioRepository entradas;

  public EntradaService(
      ProductoRepository p,
      ProveedorRepository pr,
      UsuarioRepository u,
      EntradaInventarioRepository e) {
    productos = p;
    proveedores = pr;
    usuarios = u;
    entradas = e;
  }

  // El bloqueo se mantiene hasta confirmar o revertir toda la transacción.
  @Transactional
  public void registrar(EntradaForm form, String username) {
    if (form.getCantidad() == null || form.getCantidad() <= 0)
      throw new ReglaNegocioException("La cantidad debe ser un entero mayor que cero.");
    if (form.getProductoId() == null || form.getProveedorId() == null)
      throw new ReglaNegocioException("Seleccione producto y proveedor.");
    if (form.getObservacion() != null && form.getObservacion().length() > 500)
      throw new ReglaNegocioException("La observación admite hasta 500 caracteres.");
    Producto producto =
        productos
            .bloquearPorId(form.getProductoId())
            .orElseThrow(() -> new ReglaNegocioException("El producto no existe."));
    if (!producto.getActivo()) throw new ReglaNegocioException("El producto está inactivo.");
    Proveedor proveedor =
        proveedores
            .findById(form.getProveedorId())
            .orElseThrow(() -> new ReglaNegocioException("El proveedor no existe."));
    Usuario usuario =
        usuarios
            .findByUsername(username)
            .filter(Usuario::getEstado)
            .orElseThrow(() -> new ReglaNegocioException("El usuario responsable no está activo."));
    int nuevoStock;
    try {
      nuevoStock = Math.addExact(producto.getStockActual(), form.getCantidad());
    } catch (ArithmeticException ex) {
      throw new ReglaNegocioException("La cantidad supera la capacidad del inventario.");
    }
    EntradaInventario entrada = new EntradaInventario();
    entrada.setFecha(LocalDateTime.now(ZoneId.of("America/Guayaquil")));
    entrada.setCantidad(form.getCantidad());
    entrada.setObservacion(form.getObservacion());
    entrada.setProducto(producto);
    entrada.setProveedor(proveedor);
    entrada.setUsuarioResponsable(usuario);
    producto.setStockActual(nuevoStock);
    entradas.saveAndFlush(entrada);
  }
}
