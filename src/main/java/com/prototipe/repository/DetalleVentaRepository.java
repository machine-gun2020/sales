package com.prototipe.repository;

import com.prototipe.model.DetalleVenta;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class DetalleVentaRepository implements PanacheRepository<DetalleVenta> {

    // Encontrar detalles por venta
    public List<DetalleVenta> findByVenta(Long ventaId) {
        return find("venta.idVenta", ventaId).list();
    }

    // Encontrar detalles con devoluciones pendientes
    public List<DetalleVenta> findConDevolucionesPendientes() {
        return find("estado in ('VENDIDO', 'DEVUELTO_PARCIAL')").list();
    }
}