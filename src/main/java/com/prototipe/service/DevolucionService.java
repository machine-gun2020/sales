package com.prototipe.service;



import com.prototipe.model.*;
import com.prototipe.repository.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class DevolucionService {

    @Inject
    DevolucionRepository devolucionRepository;

    @Inject
    VentaRepository ventaRepository;

    @Inject
    ProductoRepository productoRepository;

    @Transactional
    public Devolucion crearDevolucion(Devolucion devolucion) {
        // Validar venta
        Venta venta = ventaRepository.findById(devolucion.venta.idVenta);
        if (venta == null) {
            throw new RuntimeException("Venta no encontrada");
        }

        devolucion.venta = venta;
        devolucion.cliente = venta.cliente;
        devolucion.fechaDevolucion = LocalDateTime.now();
        devolucion.folioDevolucion = generarFolioDevolucion();

        // Procesar detalles de devolución
        BigDecimal totalDevolucion = BigDecimal.ZERO;

        for (DetalleDevolucion detalle : devolucion.detalles) {
            DetalleVenta detalleVenta = DetalleVenta.findById(detalle.detalleVenta.idDetalle);
            if (detalleVenta == null) {
                throw new RuntimeException("Detalle de venta no encontrado");
            }

            // Validar cantidad a devolver
            if (detalle.cantidadDevuelta > (detalleVenta.cantidad - detalleVenta.cantidadDevuelta)) {
                throw new RuntimeException("Cantidad a devolver excede lo disponible");
            }

            detalle.detalleVenta = detalleVenta;
            detalle.producto = detalleVenta.producto;

            BigDecimal importeDevolucion = detalleVenta.precioUnitario
                    .multiply(BigDecimal.valueOf(detalle.cantidadDevuelta));
            totalDevolucion = totalDevolucion.add(importeDevolucion);

            // Actualizar detalle de venta original
            detalleVenta.cantidadDevuelta += detalle.cantidadDevuelta;
            detalleVenta.estado = detalleVenta.cantidadDevuelta.equals(detalleVenta.cantidad) ?
                    "DEVUELTO_TOTAL" : "DEVUELTO_PARCIAL";

            // Reingresar a inventario si aplica
            if ("S".equals(detalle.reingresarInventario)) {
                Producto producto = detalleVenta.producto;
                producto.existencia += detalle.cantidadDevuelta;
            }
        }

        devolucion.totalDevolucion = totalDevolucion;
        devolucion.estado = "PROCESADA";

        devolucionRepository.persist(devolucion);
        return devolucion;
    }

    private String generarFolioDevolucion() {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = String.format("D%02d%02d%04d",
                now.getDayOfMonth(), now.getMonthValue(), now.getYear());
        Long count = devolucionRepository.count("fechaDevolucion >= ?1",
                LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0));
        return String.format("%s%04d", timestamp, count + 1);
    }

    public List<Devolucion> obtenerDevolucionesPorVenta(Long idVenta) {
        return devolucionRepository.findByVenta(idVenta);
    }
}
