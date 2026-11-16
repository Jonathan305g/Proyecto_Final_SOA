package ec.edu.uta.inventariobodega.dto;

import jakarta.validation.constraints.*;

public class UsuarioForm {
  @NotBlank
  @Pattern(
      regexp = "[a-zA-Z0-9._-]{3,60}",
      message = "Use entre 3 y 60 letras, números, puntos, guiones o guiones bajos")
  private String username;

  @Size(max = 72)
  private String password;

  @NotNull private ec.edu.uta.inventariobodega.entity.Rol rol;

  private boolean estado;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public ec.edu.uta.inventariobodega.entity.Rol getRol() {
    return rol;
  }

  public void setRol(ec.edu.uta.inventariobodega.entity.Rol rol) {
    this.rol = rol;
  }

  public boolean getEstado() {
    return estado;
  }

  public void setEstado(boolean estado) {
    this.estado = estado;
  }
}
