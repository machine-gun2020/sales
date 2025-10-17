package com.prototipe.service;

import com.prototipe.dto.VentaCreateDTO;
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
    public Venta crearVenta(VentaCreateDTO ventaDTO) {
        try {
            LOG.info("🛒 Iniciando creación de venta desde DTO");

            // Validaciones básicas
            ValidationUtils.validarNoNulo(ventaDTO, "ventaDTO");
            ValidationUtils.validarNoNulo(ventaDTO.clienteId, "clienteId");
            ValidationUtils.validarNoNulo(ventaDTO.detalles, "detalles");

            if (ventaDTO.detalles.isEmpty()) {
                throw new VentaException("La venta debe tener al menos un producto", "VENTA_003");
            }

            LOG.info("📋 Procesando " + ventaDTO.detalles.size() + " detalles desde DTO");

            // Validar cliente
            Cliente cliente = clienteRepository.findById(ventaDTO.clienteId);
            if (cliente == null) {
                throw new ClienteException("Cliente no encontrado", ventaDTO.clienteId);
            }

            // Crear nueva venta
            Venta venta = new Venta();
            venta.cliente = cliente;
            venta.fechaVenta = java.time.LocalDateTime.now();

            // Generar folio si no viene
            if (ventaDTO.folio == null || ventaDTO.folio.trim().isEmpty()) {
                venta.folio = generarFolioVenta();
            } else {
                venta.folio = ventaDTO.folio;
            }

            // ✅ PRIMERO: Validar todo el inventario
            for (VentaCreateDTO.DetalleVentaCreateDTO detalleDTO : ventaDTO.detalles) {
                ValidationUtils.validarNoNulo(detalleDTO.productoId, "productoId");
                ValidationUtils.validarPositivo(detalleDTO.cantidad, "cantidad");

                Producto producto = productoRepository.findById(detalleDTO.productoId);
                if (producto == null) {
                    throw new VentaException("Producto no encontrado: " + detalleDTO.productoId, "VENTA_004");
                }
                BusinessRules.validarStockSuficiente(producto, detalleDTO.cantidad);
            }

            // ✅ SEGUNDO: Crear detalles
            for (VentaCreateDTO.DetalleVentaCreateDTO detalleDTO : ventaDTO.detalles) {
                Producto producto = productoRepository.findById(detalleDTO.productoId);

                DetalleVenta detalle = new DetalleVenta();
                detalle.venta = venta;
                detalle.producto = producto;
                detalle.cantidad = detalleDTO.cantidad;

                // Usar precio del producto si no se especifica
                if (detalleDTO.precioUnitario != null) {
                    detalle.precioUnitario = detalleDTO.precioUnitario;
                } else {
                    detalle.precioUnitario = producto.precioVenta;
                }

                detalle.importe = detalle.precioUnitario.multiply(java.math.BigDecimal.valueOf(detalle.cantidad));

                // Reducir inventario
                producto.existencia -= detalle.cantidad;

                venta.detalles.add(detalle);
            }

            // Calcular totales
            calcularTotales(venta);

            // Validar folio único
            if (ventaRepository.findByFolio(venta.folio).isPresent()) {
                throw new VentaException("El folio ya existe: " + venta.folio, "VENTA_005");
            }

            LOG.info("💾 Persistiendo venta - Folio: " + venta.folio);
            ventaRepository.persist(venta);

            LOG.info("✅ Venta creada exitosamente - ID: " + venta.idVenta + ", Folio: " + venta.folio);
            return venta;

        } catch (VentaException | InventarioException | ClienteException | ValidationException e) {
            LOG.severe("❌ Error de negocio en venta: " + e.getMessage());
            throw e;
        } catch (Exception e) {
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