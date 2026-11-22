package ec.edu.uta.inventariobodega;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import ec.edu.uta.inventariobodega.dto.EntradaForm;
import ec.edu.uta.inventariobodega.entity.*;
import ec.edu.uta.inventariobodega.repository.*;
import ec.edu.uta.inventariobodega.service.*;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EntradaServiceTests {
  @Mock ProductoRepository productos;
  @Mock ProveedorRepository proveedores;
  @Mock UsuarioRepository usuarios;
  @Mock EntradaInventarioRepository entradas;
  @InjectMocks EntradaService servicio;
  EntradaForm form;

  @BeforeEach
  void preparar() {
    form = new EntradaForm();
    form.setProductoId(1L);
    form.setProveedorId(2L);
    form.setCantidad(5);
  }

  private Producto producto() {
    Producto p = new Producto();
    p.setId(1L);
    p.setActivo(true);
    p.setStockActual(10);
    when(productos.bloquearPorId(1L)).thenReturn(Optional.of(p));
    return p;
  }

  @Test
  void registroCreaPedidoEnEstadoPendienteSinDescontarStock() {
    Producto p = producto();
    Proveedor proveedor = new Proveedor();
    Usuario u = new Usuario();
    u.setEstado(true);
    when(proveedores.findById(2L)).thenReturn(Optional.of(proveedor));
    when(usuarios.findByUsername("operador")).thenReturn(Optional.of(u));
    servicio.registrar(form, "operador");
    assertThat(p.getStockActual()).isEqualTo(10);
    ArgumentCaptor<EntradaInventario> captor = ArgumentCaptor.forClass(EntradaInventario.class);
    verify(entradas).saveAndFlush(captor.capture());
    assertThat(captor.getValue().getUsuarioResponsable()).isSameAs(u);
    assertThat(captor.getValue().getFecha()).isNotNull();
    assertThat(captor.getValue().getProveedor()).isSameAs(proveedor);
    assertThat(captor.getValue().getEstado()).isEqualTo(EstadoEntrada.PENDIENTE);
  }

  @Test
  void aprobarDescuentaStockYCambiaEstado() {
    Producto p = producto();
    EntradaInventario e = new EntradaInventario();
    e.setId(10L);
    e.setCantidad(4);
    e.setProducto(p);
    e.setEstado(EstadoEntrada.PENDIENTE);
    when(entradas.findById(10L)).thenReturn(Optional.of(e));
    servicio.aprobar(10L);
    assertThat(p.getStockActual()).isEqualTo(6);
    assertThat(e.getEstado()).isEqualTo(EstadoEntrada.APROBADO);
    verify(entradas).saveAndFlush(e);
  }

  @Test
  void rechazarCambiaEstadoARechazadoSinModificarStock() {
    Producto p = new Producto();
    p.setStockActual(10);
    EntradaInventario e = new EntradaInventario();
    e.setId(10L);
    e.setCantidad(4);
    e.setProducto(p);
    e.setEstado(EstadoEntrada.PENDIENTE);
    when(entradas.findById(10L)).thenReturn(Optional.of(e));
    servicio.rechazar(10L, "Motivo de prueba");
    assertThat(p.getStockActual()).isEqualTo(10);
    assertThat(e.getEstado()).isEqualTo(EstadoEntrada.RECHAZADO);
    assertThat(e.getObservacion()).contains("Rechazado: Motivo de prueba");
    verify(entradas).saveAndFlush(e);
  }

  @Test
  void cantidadCeroSeRechaza() {
    form.setCantidad(0);
    assertThatThrownBy(() -> servicio.registrar(form, "u"))
        .isInstanceOf(ReglaNegocioException.class);
    verifyNoInteractions(productos, entradas);
  }

  @Test
  void cantidadNegativaSeRechaza() {
    form.setCantidad(-1);
    assertThatThrownBy(() -> servicio.registrar(form, "u"))
        .isInstanceOf(ReglaNegocioException.class);
  }

  @Test
  void cantidadNulaSeRechaza() {
    form.setCantidad(null);
    assertThatThrownBy(() -> servicio.registrar(form, "u"))
        .isInstanceOf(ReglaNegocioException.class);
  }

  @Test
  void productoInexistenteSeRechaza() {
    when(productos.bloquearPorId(1L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> servicio.registrar(form, "u")).hasMessage("El producto no existe.");
    verifyNoInteractions(entradas);
  }

  @Test
  void proveedorInexistenteSeRechaza() {
    producto();
    when(proveedores.findById(2L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> servicio.registrar(form, "u")).hasMessage("El proveedor no existe.");
    verifyNoInteractions(entradas);
  }

  @Test
  void productoInactivoSeRechaza() {
    producto().setActivo(false);
    assertThatThrownBy(() -> servicio.registrar(form, "u"))
        .hasMessage("El producto está inactivo.");
    verifyNoInteractions(entradas);
  }

  @Test
  void cantidadMayorAStockSeRechazaSinCambiarStock() {
    Producto p = producto();
    p.setStockActual(4);
    form.setCantidad(5);
    when(proveedores.findById(2L)).thenReturn(Optional.of(new Proveedor()));
    Usuario u = new Usuario();
    u.setEstado(true);
    when(usuarios.findByUsername("u")).thenReturn(Optional.of(u));
    assertThatThrownBy(() -> servicio.registrar(form, "u"))
        .isInstanceOf(ReglaNegocioException.class)
        .hasMessageContaining("Stock insuficiente");
    assertThat(p.getStockActual()).isEqualTo(4);
    verifyNoInteractions(entradas);
  }
}
