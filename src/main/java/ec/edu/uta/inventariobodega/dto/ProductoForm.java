package ec.edu.uta.inventariobodega.dto;

import jakarta.validation.constraints.*;

public class ProductoForm {
  @NotBlank
  @Size(max = 50)
  private String codigo;

  @NotBlank
  @Size(max = 120)
  private String nombre;

  @Size(max = 500)
  private String descripcion;

  @NotNull
  @Min(0)
  private Integer stockMinimo;

  @NotNull @Positive private Long categoriaId;
  @NotNull @Positive private Long marcaId;

  private boolean activo;

  public String getCodigo() {
    return codigo;
  }

  public void setCodigo(String codigo) {
    this.codigo = codigo;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public Integer getStockMinimo() {
    return stockMinimo;
  }

  public void setStockMinimo(Integer stockMinimo) {
    this.stockMinimo = stockMinimo;
  }

  public Long getCategoriaId() {
    return categoriaId;
  }

  public void setCategoriaId(Long categoriaId) {
    this.categoriaId = categoriaId;
  }

  public Long getMarcaId() {
    return marcaId;
  }

  public void setMarcaId(Long marcaId) {
    this.marcaId = marcaId;
  }

  public boolean getActivo() {
    return activo;
  }

  public void setActivo(boolean activo) {
    this.activo = activo;
  }
}
