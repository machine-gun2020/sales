package com.prototipe.repository;


import com.prototipe.model.Venta;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class VentaRepository implements PanacheRepository<Venta> {

    public Optional<Venta> findByFolio(String folio) {
        return find("folio", folio).firstResultOptional();
    }

    public List<Venta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin) {
        return find("fechaVenta between ?1 and ?2", inicio, fin).list();
    }

    public List<Venta> findByCliente(Long idCliente) {
        return find("cliente.idCliente", idCliente).list();
    }

    public List<Venta> findByEstado(String estado) {
        return find("estado", estado).list();
    }
}