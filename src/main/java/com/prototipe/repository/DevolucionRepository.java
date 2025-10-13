package com.prototipe.repository;


import com.prototipe.model.Devolucion;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class DevolucionRepository implements PanacheRepository<Devolucion> {

    public Optional<Devolucion> findByFolioDevolucion(String folioDevolucion) {
        return find("folioDevolucion", folioDevolucion).firstResultOptional();
    }

    public List<Devolucion> findByVenta(Long idVenta) {
        return find("venta.idVenta", idVenta).list();
    }

    public List<Devolucion> findByEstado(String estado) {
        return find("estado", estado).list();
    }
}
