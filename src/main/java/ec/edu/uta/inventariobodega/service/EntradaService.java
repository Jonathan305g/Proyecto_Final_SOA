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
    if (form.getCantidad() > producto.getStockActual()) {
      throw new ReglaNegocioException(
          "Stock insuficiente ("
              + producto.getStockActual()
              + "). No se puede solicitar una cantidad mayor al stock actual ("
              + form.getCantidad()
              + ").");
    }
    EntradaInventario entrada = new EntradaInventario();
    entrada.setFecha(LocalDateTime.now(ZoneId.of("America/Guayaquil")));
    entrada.setCantidad(form.getCantidad());
    entrada.setObservacion(form.getObservacion());
    entrada.setProducto(producto);
    entrada.setProveedor(proveedor);
    entrada.setUsuarioResponsable(usuario);
    entrada.setEstado(EstadoEntrada.PENDIENTE);
    // El stock NO se descuenta aquí; permanece intacto hasta la confirmación del administrador.
    entradas.saveAndFlush(entrada);
  }

  @Transactional
  public void aprobar(Long entradaId) {
    EntradaInventario entrada =
        entradas
            .findById(entradaId)
            .orElseThrow(() -> new ReglaNegocioException("El pedido no existe."));
    if (entrada.getEstado() != EstadoEntrada.PENDIENTE) {
      throw new ReglaNegocioException("Solo se pueden confirmar pedidos en estado PENDIENTE.");
    }
    Producto producto =
        productos
            .bloquearPorId(entrada.getProducto().getId())
            .orElseThrow(() -> new ReglaNegocioException("El producto no existe."));
    if (!producto.getActivo()) {
      throw new ReglaNegocioException("El producto está inactivo.");
    }
    if (entrada.getCantidad() > producto.getStockActual()) {
      throw new ReglaNegocioException(
          "Stock insuficiente ("
              + producto.getStockActual()
              + "). No se puede descontar la cantidad registrada ("
              + entrada.getCantidad()
              + ").");
    }
    producto.setStockActual(producto.getStockActual() - entrada.getCantidad());
    entrada.setEstado(EstadoEntrada.APROBADO);
    entradas.saveAndFlush(entrada);
  }

  @Transactional
  public void rechazar(Long entradaId, String motivo) {
    EntradaInventario entrada =
        entradas
            .findById(entradaId)
            .orElseThrow(() -> new ReglaNegocioException("El pedido no existe."));
    if (entrada.getEstado() != EstadoEntrada.PENDIENTE) {
      throw new ReglaNegocioException("Solo se pueden rechazar pedidos en estado PENDIENTE.");
    }
    entrada.setEstado(EstadoEntrada.RECHAZADO);
    if (motivo != null && !motivo.isBlank()) {
      String obs = entrada.getObservacion();
      entrada.setObservacion(
          (obs == null || obs.isBlank() ? "" : obs + " | ") + "Rechazado: " + motivo.trim());
    }
    entradas.saveAndFlush(entrada);
  }
}
