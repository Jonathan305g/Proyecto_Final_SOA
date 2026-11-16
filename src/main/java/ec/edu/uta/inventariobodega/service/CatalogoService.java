package ec.edu.uta.inventariobodega.service;

import ec.edu.uta.inventariobodega.dto.*;
import ec.edu.uta.inventariobodega.entity.*;
import ec.edu.uta.inventariobodega.repository.*;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogoService {
  private final CategoriaRepository categorias;
  private final MarcaRepository marcas;
  private final ProveedorRepository proveedores;
  private final ProductoRepository productos;
  private final EntradaInventarioRepository entradas;

  public CatalogoService(
      CategoriaRepository c,
      MarcaRepository m,
      ProveedorRepository p,
      ProductoRepository pr,
      EntradaInventarioRepository e) {
    categorias = c;
    marcas = m;
    proveedores = p;
    productos = pr;
    entradas = e;
  }

  public void validarTipo(String tipo) {
    if (!List.of("categorias", "marcas", "proveedores").contains(tipo))
      throw new ReglaNegocioException("Catálogo no válido.");
  }

  @Transactional(readOnly = true)
  public List<?> listar(String tipo) {
    validarTipo(tipo);
    return switch (tipo) {
      case "categorias" -> categorias.findAll(org.springframework.data.domain.Sort.by("nombre"));
      case "marcas" -> marcas.findAll(org.springframework.data.domain.Sort.by("nombre"));
      default -> proveedores.findAll(org.springframework.data.domain.Sort.by("nombre"));
    };
  }

  @Transactional(readOnly = true)
  public CatalogoForm formulario(String tipo, Long id) {
    validarTipo(tipo);
    CatalogoForm f = new CatalogoForm();
    f.setNombre(
        switch (tipo) {
          case "categorias" ->
              categorias
                  .findById(id)
                  .orElseThrow(() -> new ReglaNegocioException("La categoría no existe."))
                  .getNombre();
          case "marcas" ->
              marcas
                  .findById(id)
                  .orElseThrow(() -> new ReglaNegocioException("La marca no existe."))
                  .getNombre();
          default -> throw new ReglaNegocioException("Use el formulario de proveedores.");
        });
    return f;
  }

  @Transactional(readOnly = true)
  public ProveedorForm proveedorForm(Long id) {
    Proveedor p =
        proveedores
            .findById(id)
            .orElseThrow(() -> new ReglaNegocioException("El proveedor no existe."));
    ProveedorForm f = new ProveedorForm();
    f.setNombre(p.getNombre());
    f.setTelefono(p.getTelefono());
    f.setCorreo(p.getCorreo());
    return f;
  }

  @Transactional
  public void guardar(String tipo, Long id, CatalogoForm f) {
    validarTipo(tipo);
    String nombre = f.getNombre().trim();
    Long excepto = id == null ? 0L : id;
    if (tipo.equals("categorias")) {
      if (categorias.existsByNombreIgnoreCaseAndIdNot(nombre, excepto))
        throw new ReglaNegocioException("La categoría ya existe.");
      Categoria c =
          id == null
              ? new Categoria()
              : categorias
                  .findById(id)
                  .orElseThrow(() -> new ReglaNegocioException("La categoría no existe."));
      c.setNombre(nombre);
      categorias.saveAndFlush(c);
    } else if (tipo.equals("marcas")) {
      if (marcas.existsByNombreIgnoreCaseAndIdNot(nombre, excepto))
        throw new ReglaNegocioException("La marca ya existe.");
      Marca m =
          id == null
              ? new Marca()
              : marcas
                  .findById(id)
                  .orElseThrow(() -> new ReglaNegocioException("La marca no existe."));
      m.setNombre(nombre);
      marcas.saveAndFlush(m);
    } else throw new ReglaNegocioException("Use el formulario de proveedores.");
  }

  @Transactional
  public void guardarProveedor(Long id, ProveedorForm f) {
    Proveedor p =
        id == null
            ? new Proveedor()
            : proveedores
                .findById(id)
                .orElseThrow(() -> new ReglaNegocioException("El proveedor no existe."));
    p.setNombre(f.getNombre().trim());
    p.setTelefono(f.getTelefono());
    p.setCorreo(f.getCorreo());
    proveedores.saveAndFlush(p);
  }

  @Transactional
  public void eliminar(String tipo, Long id) {
    validarTipo(tipo);
    switch (tipo) {
      case "categorias" -> {
        if (productos.existsByCategoriaId(id))
          throw new ReglaNegocioException(
              "La categoría está asociada a productos y no puede eliminarse.");
        categorias.deleteById(id);
        categorias.flush();
      }
      case "marcas" -> {
        if (productos.existsByMarcaId(id))
          throw new ReglaNegocioException(
              "La marca está asociada a productos y no puede eliminarse.");
        marcas.deleteById(id);
        marcas.flush();
      }
      default -> {
        if (entradas.existsByProveedorId(id))
          throw new ReglaNegocioException(
              "El proveedor tiene entradas registradas y no puede eliminarse.");
        proveedores.deleteById(id);
        proveedores.flush();
      }
    }
  }
}
