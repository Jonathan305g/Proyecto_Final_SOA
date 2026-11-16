package ec.edu.uta.inventariobodega.dto;

import jakarta.validation.constraints.*;

public class ProveedorForm {
  @NotBlank
  @Size(max = 120)
  private String nombre;

  @Size(max = 30)
  @Pattern(regexp = "[0-9+() .-]*", message = "El teléfono contiene caracteres no válidos")
  private String telefono;

  @Email
  @Size(max = 160)
  private String correo;

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getTelefono() {
    return telefono;
  }

  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }

  public String getCorreo() {
    return correo;
  }

  public void setCorreo(String correo) {
    this.correo = correo;
  }
}
