package ec.edu.uta.inventariobodega.dto;

import jakarta.validation.constraints.*;

public class EntradaForm {
  @NotNull @Positive private Long productoId;
  @NotNull @Positive private Long proveedorId;
  @NotNull @Positive private Integer cantidad;

  @Size(max = 500)
  private String observacion;

  public Long getProductoId() {
    return productoId;
  }

  public void setProductoId(Long productoId) {
    this.productoId = productoId;
  }

  public Long getProveedorId() {
    return proveedorId;
  }

  public void setProveedorId(Long proveedorId) {
    this.proveedorId = proveedorId;
  }

  public Integer getCantidad() {
    return cantidad;
  }

  public void setCantidad(Integer cantidad) {
    this.cantidad = cantidad;
  }

  public String getObservacion() {
    return observacion;
  }

  public void setObservacion(String observacion) {
    this.observacion = observacion;
  }
}
