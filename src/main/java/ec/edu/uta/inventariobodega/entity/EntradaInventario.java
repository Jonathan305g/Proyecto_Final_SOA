package ec.edu.uta.inventariobodega.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "entradas_inventario")
public class EntradaInventario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, updatable = false)
  private java.time.LocalDateTime fecha;

  @Column(nullable = false, updatable = false)
  private int cantidad;

  @Column(length = 500, updatable = false)
  private String observacion;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "producto_id", nullable = false)
  private Producto producto;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "proveedor_id", nullable = false)
  private Proveedor proveedor;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "usuario_responsable_id", nullable = false)
  private Usuario usuarioResponsable;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public java.time.LocalDateTime getFecha() {
    return fecha;
  }

  public void setFecha(java.time.LocalDateTime fecha) {
    this.fecha = fecha;
  }

  public int getCantidad() {
    return cantidad;
  }

  public void setCantidad(int cantidad) {
    this.cantidad = cantidad;
  }

  public String getObservacion() {
    return observacion;
  }

  public void setObservacion(String observacion) {
    this.observacion = observacion;
  }

  public Producto getProducto() {
    return producto;
  }

  public void setProducto(Producto producto) {
    this.producto = producto;
  }

  public Proveedor getProveedor() {
    return proveedor;
  }

  public void setProveedor(Proveedor proveedor) {
    this.proveedor = proveedor;
  }

  public Usuario getUsuarioResponsable() {
    return usuarioResponsable;
  }

  public void setUsuarioResponsable(Usuario usuarioResponsable) {
    this.usuarioResponsable = usuarioResponsable;
  }
}
