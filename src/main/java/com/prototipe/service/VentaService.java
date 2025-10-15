package com.prototipe.service;

import com.prototipe.exceptions.*;
import com.prototipe.utils.*;
import com.prototipe.model.*;
import com.prototipe.repository.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class VentaService {

    private static final Logger LOG = Logger.getLogger(VentaService.class.getName());

    @Inject
    VentaRepository ventaRepository;

    @Inject
    ClienteRepository clienteRepository;

    @Inject
    ProductoRepository productoRepository;

    @Transactional
    public Venta crearVenta(Venta venta) {
        try {
            LOG.info("🛒 Iniciando creación de venta - Cliente ID: " +
                    (venta.cliente != null ? venta.cliente.idCliente : "null"));

            // Validaciones básicas usando nuestras nuevas utilidades
            ValidationUtils.validarNoNulo(venta, "venta");
            ValidationUtils.validarNoNulo(venta.cliente, "cliente");
            ValidationUtils.validarNoNulo(venta.detalles, "detalles");

            if (venta.detalles.isEmpty()) {
                throw new VentaException("La venta debe tener al menos un producto", "VENTA_003");
            }

            // Validar cliente
            Cliente cliente = clienteRepository.findById(venta.cliente.idCliente);
            if (cliente == null) {
                throw new ClienteException("Cliente no encontrado", venta.cliente.idCliente);
            }

            // Validar que el cliente esté activo
            if (!"S".equals(cliente.activo)) {
                throw new ClienteException("Cliente inactivo", cliente.idCliente);
            }

            venta.cliente = cliente;

            // Usar fecha actual si no viene
            if (venta.fechaVenta == null) {
                venta.fechaVenta = LocalDateTime.now();
            }

            LOG.info("📋 Procesando " + venta.detalles.size() + " detalles de venta");

            // ✅ PRIMERO: Validar todo el inventario ANTES de hacer cambios
            for (DetalleVenta detalle : venta.detalles) {
                ValidationUtils.validarNoNulo(detalle.producto, "producto en detalle");
                ValidationUtils.validarPositivo(detalle.cantidad, "cantidad");

                Producto producto = productoRepository.findById(detalle.producto.idProducto);
                if (producto == null) {
                    throw new VentaException(
                            "Producto no encontrado: " + detalle.producto.idProducto,
                            "VENTA_004"
                    );
                }

                // Usar nuestra nueva utilidad de validación de stock
                BusinessRules.validarStockSuficiente(producto, detalle.cantidad);

                // Validar precio
                if (detalle.precioUnitario != null) {
                    BusinessRules.validarPrecioPositivo(detalle.precioUnitario);
                }
            }

            // ✅ SEGUNDO: Si toda la validación pasa, procesar la venta
            for (DetalleVenta detalle : venta.detalles) {
                Producto producto = productoRepository.findById(detalle.producto.idProducto);
                detalle.producto = producto;
                detalle.venta = venta; // ✅ Establecer relación bidireccional

                // Usar precio del producto si no se especificó
                if (detalle.precioUnitario == null) {
                    detalle.precioUnitario = producto.precioVenta;
                }

                // Calcular importe
                detalle.importe = detalle.precioUnitario.multiply(BigDecimal.valueOf(detalle.cantidad));

                // ✅ REDUCIR INVENTARIO (ya validado que hay stock suficiente)
                producto.existencia -= detalle.cantidad;
                LOG.info("📦 Inventario actualizado - " + producto.nombre +
                        ": " + (producto.existencia + detalle.cantidad) + " → " + producto.existencia);
            }

            // Calcular totales
            calcularTotales(venta);

            // Generar folio automático si no viene
            if (venta.folio == null || venta.folio.trim().isEmpty()) {
                venta.folio = generarFolioVenta();
            }

            // Validar folio único
            if (ventaRepository.findByFolio(venta.folio).isPresent()) {
                throw new VentaException("El folio ya existe: " + venta.folio, "VENTA_005");
            }

            LOG.info("💾 Persistiendo venta - Folio: " + venta.folio + ", Total: " + venta.total);
            ventaRepository.persist(venta);

            LOG.info("✅ Venta creada exitosamente - ID: " + venta.idVenta + ", Folio: " + venta.folio);
            return venta;

        } catch (VentaException | InventarioException | ClienteException | ValidationException e) {
            // Relanzar excepciones de negocio específicas
            LOG.severe("❌ Error de negocio en venta: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            // Capturar cualquier otra excepción y convertirla a VentaException
            LOG.severe("💥 Error inesperado al crear venta: " + e.getMessage());
            throw new VentaException("Error interno al procesar la venta: " + e.getMessage(), "VENTA_999");
        }
    }

    private void calcularTotales(Venta venta) {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (DetalleVenta detalle : venta.detalles) {
            // Asegurar que el importe esté calculado
            if (detalle.importe == null) {
                detalle.importe = detalle.precioUnitario.multiply(BigDecimal.valueOf(detalle.cantidad));
            }
            subtotal = subtotal.add(detalle.importe);
        }

        venta.subtotal = subtotal;
        venta.iva = subtotal.multiply(BigDecimal.valueOf(0.16)); // 16% IVA
        venta.total = subtotal.add(venta.iva);

        LOG.fine("💰 Totales calculados - Subtotal: " + subtotal + ", Total: " + venta.total);
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
        ValidationUtils.validarNoNulo(id, "idVenta");
        Venta venta = ventaRepository.findById(id);
        if (venta == null) {
            throw new VentaException("Venta no encontrada: " + id, "VENTA_002");
        }
        return venta;
    }

    @Transactional
    public void cancelarVenta(Long idVenta) {
        try {
            ValidationUtils.validarNoNulo(idVenta, "idVenta");
            Venta venta = ventaRepository.findById(idVenta);

            if (venta == null) {
                throw new VentaException("Venta no encontrada: " + idVenta, "VENTA_002");
            }

            if ("CANCELADA".equals(venta.estado)) {
                throw new VentaException("La venta ya está cancelada", "VENTA_006");
            }

            LOG.info("🔄 Cancelando venta - ID: " + idVenta + ", Folio: " + venta.folio);

            // Reintegrar inventario
            for (DetalleVenta detalle : venta.detalles) {
                if (detalle.producto != null) {
                    Producto producto = productoRepository.findById(detalle.producto.idProducto);
                    if (producto != null) {
                        int existenciaAnterior = producto.existencia;
                        producto.existencia += detalle.cantidad;
                        LOG.info("📦 Inventario reintegrado - " + producto.nombre +
                                ": " + existenciaAnterior + " → " + producto.existencia);
                    }
                }
            }

            venta.estado = "CANCELADA";
            LOG.info("✅ Venta cancelada - ID: " + idVenta);

        } catch (VentaException e) {
            LOG.severe("❌ Error al cancelar venta: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.severe("💥 Error inesperado al cancelar venta: " + e.getMessage());
            throw new VentaException("Error interno al cancelar venta", "VENTA_007");
        }
    }
}