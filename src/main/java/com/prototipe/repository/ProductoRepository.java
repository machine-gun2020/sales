package com.prototipe.repository;


import com.prototipe.model.Producto;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProductoRepository implements PanacheRepository<Producto> {

    public Optional<Producto> findByCodigo(String codigo) {
        return find("codigo", codigo).firstResultOptional();
    }

    public List<Producto> findActivos() {
        return find("activo", "S").list();
    }

    public List<Producto> findByCategoria(String categoria) {
        return find("categoria = ?1 and activo = 'S'", categoria).list();
    }

    public List<Producto> findByNombre(String nombre) {
        return find("UPPER(nombre) LIKE UPPER(?1) and activo = 'S'", "%" + nombre + "%").list();
    }
}
