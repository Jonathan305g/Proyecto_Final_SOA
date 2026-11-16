package ec.edu.uta.inventariobodega.dto;

import jakarta.validation.constraints.*;

public class CatalogoForm {
  @NotBlank
  @Size(max = 100)
  private String nombre;

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }
}
