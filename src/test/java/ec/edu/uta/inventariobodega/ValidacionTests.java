package ec.edu.uta.inventariobodega;

import static org.assertj.core.api.Assertions.*;

import ec.edu.uta.inventariobodega.dto.*;
import ec.edu.uta.inventariobodega.service.UsuarioService;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class ValidacionTests {
  @Test
  void formulariosRechazanCantidadYCamposInvalidos() {
    try (var factory = Validation.buildDefaultValidatorFactory()) {
      var validator = factory.getValidator();
      EntradaForm form = new EntradaForm();
      form.setCantidad(-2);
      form.setProductoId(-1L);
      assertThat(validator.validate(form)).hasSize(3);
      ProductoForm producto = new ProductoForm();
      producto.setStockMinimo(-1);
      assertThat(validator.validate(producto)).isNotEmpty();
      ProveedorForm proveedor = new ProveedorForm();
      proveedor.setNombre("Proveedor");
      proveedor.setCorreo("no-es-correo");
      assertThat(validator.validate(proveedor)).isNotEmpty();
    }
  }

  @Test
  void contrasenaSeAlmacenaConBcrypt() {
    var encoder = new BCryptPasswordEncoder();
    String hash = encoder.encode("clave-solo-para-prueba");
    assertThat(hash).startsWith("$2").isNotEqualTo("clave-solo-para-prueba");
    assertThat(encoder.matches("clave-solo-para-prueba", hash)).isTrue();
  }

  @Test
  void contrasenaRechazaLongitudEnBytesYClavesCortas() {
    assertThatThrownBy(() -> UsuarioService.validarPassword("corta"))
        .isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> UsuarioService.validarPassword("á".repeat(40)))
        .isInstanceOf(RuntimeException.class);
  }
}
