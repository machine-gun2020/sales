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
public class VentaService {

    @Inject
    VentaRepository ventaRepository;

    @Inject
    ClienteRepository clienteRepository;

    @Inject
    ProductoRepository productoRepository;

    @Transactional
    public Venta crearVenta(Venta venta) {
        try {
            System.out.println("=== INICIANDO CREACIÓN DE VENTA ===");

            // Validar cliente
            Cliente cliente = clienteRepository.findById(venta.cliente.idCliente);
            if (cliente == null) {
                throw new RuntimeException("Cliente no encontrado");
            }

            venta.cliente = cliente;

            // Usar fecha actual si no viene
            if (venta.fechaVenta == null) {
                venta.fechaVenta = LocalDateTime.now();
            }

            // ✅ VALIDAR INVENTARIO ANTES de procesar la venta
            validarInventario(venta.detalles);

            // ✅ PRIMERO: Establecer relaciones en detalles ANTES de persistir
            for (DetalleVenta detalle : venta.detalles) {
                Producto producto = productoRepository.findById(detalle.producto.idProducto);
                if (producto == null) {
                    throw new RuntimeException("Producto no encontrado: " + detalle.producto.idProducto);
                }
                detalle.producto = producto;
                detalle.venta = venta; // ✅ CRÍTICO: Establecer relación bidireccional

                // ✅ ACTUALIZAR INVENTARIO - Reducir existencia
                producto.existencia -= detalle.cantidad;
                System.out.println("Inventario actualizado - Producto: " + producto.nombre +
                        ", Nueva existencia: " + producto.existencia);
            }

            // Calcular totales
            calcularTotales(venta);

            // Generar folio automático si no viene
            if (venta.folio == null || venta.folio.trim().isEmpty()) {
                venta.folio = generarFolioVenta();
            }

            System.out.println("Persistiendo venta con " + venta.detalles.size() + " detalles...");
            ventaRepository.persist(venta);

            System.out.println("=== VENTA CREADA EXITOSAMENTE ===");
            return venta;

        } catch (Exception e) {
            System.err.println("=== ERROR EN CREAR VENTA ===");
            e.printStackTrace();
            throw new RuntimeException("Error al crear venta: " + e.getMessage(), e);
        }
    }

    /**
     * ✅ VALIDA que haya suficiente inventario para todos los productos
     */
    private void validarInventario(List<DetalleVenta> detalles) {
        for (DetalleVenta detalle : detalles) {
            Producto producto = productoRepository.findById(detalle.producto.idProducto);
            if (producto == null) {
                throw new RuntimeException("Producto no encontrado: " + detalle.producto.idProducto);
            }

            if (producto.existencia < detalle.cantidad) {
                throw new RuntimeException(
                        "Inventario insuficiente para: " + producto.nombre +
                                ". Existencia: " + producto.existencia +
                                ", Solicitado: " + detalle.cantidad
                );
            }

            if (producto.existencia <= 0) {
                throw new RuntimeException(
                        "Producto agotado: " + producto.nombre
                );
            }

            System.out.println("Inventario validado - Producto: " + producto.nombre +
                    ", Existencia: " + producto.existencia +
                    ", Solicitado: " + detalle.cantidad);
        }
    }

    private void calcularTotales(Venta venta) {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (DetalleVenta detalle : venta.detalles) {
            // ✅ Ya tenemos el producto asignado, solo calcular importe
            detalle.importe = detalle.precioUnitario.multiply(BigDecimal.valueOf(detalle.cantidad));
            subtotal = subtotal.add(detalle.importe);
        }

        venta.subtotal = subtotal;
        venta.iva = subtotal.multiply(BigDecimal.valueOf(0.16)); // 16% IVA
        venta.total = subtotal.add(venta.iva);

        System.out.println("Totales calculados - Subtotal: " + venta.subtotal +
                ", IVA: " + venta.iva + ", Total: " + venta.total);
    }

    private String generarFolioVenta() {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = String.format("V%02d%02d%04d",
                now.getDayOfMonth(), now.getMonthValue(), now.getYear());
        Long count = ventaRepository.count("fechaVenta >= ?1",
                LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0));
        return String.format("%s%04d", timestamp, count + 1);
    }

    public List<Venta> obtenerTodasVentas() {
        return ventaRepository.listAll();
    }

    public Venta obtenerVentaPorId(Long id) {
        return ventaRepository.findById(id);
    }

    @Transactional
    public void cancelarVenta(Long idVenta) {
        Venta venta = ventaRepository.findById(idVenta);
        if (venta != null) {
            venta.estado = "CANCELADA";

            // ✅ REINTEGRAR INVENTARIO al cancelar venta
            for (DetalleVenta detalle : venta.detalles) {
                if (detalle.producto != null) {
                    detalle.producto.existencia += detalle.cantidad;
                    System.out.println("Inventario reintegrado - Producto: " + detalle.producto.nombre +
                            ", Nueva existencia: " + detalle.producto.existencia);
                }
            }
        }
    }

    /**
     * ✅ MÉTODO ADICIONAL: Validar inventario sin realizar venta
     */
    public boolean validarDisponibilidad(List<DetalleVenta> detalles) {
        try {
            validarInventario(detalles);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}