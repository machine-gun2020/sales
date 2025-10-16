package com.prototipe.service;

import com.prototipe.dto.DevolucionCommand;
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
public class DevolucionService {

    private static final Logger LOG = Logger.getLogger(DevolucionService.class.getName());

    @Inject
    DevolucionRepository devolucionRepository;

    @Inject
    VentaRepository ventaRepository;

    @Inject
    ProductoRepository productoRepository;

    @Inject
    DetalleVentaRepository detalleVentaRepository;

    @Transactional
    public Devolucion procesarDevolucionParcial(DevolucionCommand command) {
        try {
            LOG.info("🔄 Iniciando procesamiento de devolución parcial");

            // Validaciones básicas
            ValidationUtils.validarNoNulo(command, "comandoDevolucion");
            ValidationUtils.validarNoNulo(command.ventaId, "ventaId");
            ValidationUtils.validarTextoNoVacio(command.motivoGeneral, "motivoGeneral");
            ValidationUtils.validarNoNulo(command.items, "itemsDevolucion");

            if (command.items.isEmpty()) {
                throw new ValidationException(
                        "La devolución debe tener al menos un item",
                        "items",
                        "vacío"
                );
            }

            // Obtener y validar venta
            Venta venta = ventaRepository.findById(command.ventaId);
            if (venta == null) {
                throw new VentaException("Venta no encontrada: " + command.ventaId, "DEV_001");
            }

            // Validar que la venta no esté cancelada
            if ("CANCELADA".equals(venta.estado)) {
                throw new VentaException("No se puede procesar devolución en venta cancelada", "DEV_002");
            }

            LOG.info("📋 Procesando devolución para venta: " + venta.folio +
                    " - Items: " + command.items.size());

            // Crear devolución
            Devolucion devolucion = crearDevolucion(venta, command);

            // Procesar cada item de devolución
            BigDecimal totalDevolucion = BigDecimal.ZERO;

            for (DevolucionCommand.ItemDevolucionCommand item : command.items) {
                DetalleDevolucion detalleDev = procesarItemDevolucion(devolucion, item, venta);
                totalDevolucion = totalDevolucion.add(calcularImporteDevolucion(detalleDev));
            }

            // Actualizar total de devolución
            devolucion.totalDevolucion = totalDevolucion;

            // Actualizar estado de la venta original
            actualizarEstadoVenta(venta);

            LOG.info("✅ Devolución procesada exitosamente - Folio: " + devolucion.folioDevolucion +
                    " - Total: " + totalDevolucion);

            return devolucion;

        } catch (VentaException | InventarioException | ValidationException e) {
            LOG.severe("❌ Error de negocio en devolución: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.severe("💥 Error inesperado al procesar devolución: " + e.getMessage());
            throw new VentaException("Error interno al procesar devolución", "DEV_999");
        }
    }

    private Devolucion crearDevolucion(Venta venta, DevolucionCommand command) {
        Devolucion devolucion = new Devolucion();
        devolucion.folioDevolucion = generarFolioDevolucion();
        devolucion.fechaDevolucion = LocalDateTime.now();
        devolucion.venta = venta;
        devolucion.cliente = venta.cliente;
        devolucion.estado = "PROCESADA";
        devolucion.motivoGeneral = command.motivoGeneral;

        devolucionRepository.persist(devolucion);
        LOG.info("📄 Devolución creada - Folio: " + devolucion.folioDevolucion);

        return devolucion;
    }

    private DetalleDevolucion procesarItemDevolucion(Devolucion devolucion,
                                                     DevolucionCommand.ItemDevolucionCommand item,
                                                     Venta venta) {
        try {
            // Validaciones del item
            ValidationUtils.validarNoNulo(item.detalleVentaId, "detalleVentaId");
            ValidationUtils.validarPositivo(item.cantidadDevuelta, "cantidadDevuelta");
            ValidationUtils.validarTextoNoVacio(item.motivoEspecifico, "motivoEspecifico");
            ValidationUtils.validarNoNulo(item.tipo, "tipoDevolucion");

            // Obtener detalle de venta original
            DetalleVenta detalleOriginal = detalleVentaRepository.findById(item.detalleVentaId);
            if (detalleOriginal == null) {
                throw new VentaException("Detalle de venta no encontrado: " + item.detalleVentaId, "DEV_003");
            }

            // Validar que el detalle pertenezca a la venta
            if (!detalleOriginal.venta.idVenta.equals(venta.idVenta)) {
                throw new VentaException("El detalle no pertenece a la venta especificada", "DEV_004");
            }

            // Validar cantidad a devolver
            validarCantidadDevolucion(detalleOriginal, item.cantidadDevuelta);

            LOG.info("📦 Procesando item devolución - Producto: " + detalleOriginal.producto.nombre +
                    ", Cantidad: " + item.cantidadDevuelta + ", Tipo: " + item.tipo);

            // Crear detalle de devolución
            DetalleDevolucion detalleDev = new DetalleDevolucion();
            detalleDev.devolucion = devolucion;
            detalleDev.detalleVenta = detalleOriginal;
            detalleDev.producto = detalleOriginal.producto;
            detalleDev.cantidadDevuelta = item.cantidadDevuelta;
            detalleDev.motivoEspecifico = item.motivoEspecifico;
            detalleDev.tipoMotivo = item.tipo.toString();
            detalleDev.reingresarInventario = true; // Por defecto

            // Procesar según el tipo de devolución
            switch (item.tipo) {
                case DEVOLUCION_TOTAL:
                    procesarDevolucionTotal(detalleOriginal, detalleDev);
                    break;
                case DEVOLUCION_PARCIAL:
                    procesarDevolucionParcial(detalleOriginal, detalleDev);
                    break;
                case CAMBIO_PRODUCTO:
                    procesarCambioProducto(detalleOriginal, detalleDev, item);
                    break;
                case REPOSICION:
                    procesarReposicion(detalleOriginal, detalleDev);
                    break;
            }

            // Persistir detalle de devolución
            detalleDev.persist();

            return detalleDev;

        } catch (Exception e) {
            LOG.severe("❌ Error al procesar item de devolución: " + e.getMessage());
            throw e;
        }
    }

    private void procesarDevolucionTotal(DetalleVenta detalleOriginal, DetalleDevolucion detalleDev) {
        // Validar que se devuelva la cantidad completa
        if (!detalleDev.cantidadDevuelta.equals(detalleOriginal.cantidad)) {
            throw new ValidationException(
                    "La devolución total debe incluir toda la cantidad original",
                    "cantidadDevuelta",
                    detalleDev.cantidadDevuelta.toString()
            );
        }

        // Actualizar estado del detalle original
        detalleOriginal.cantidadDevuelta = detalleDev.cantidadDevuelta;
        detalleOriginal.estado = "DEVUELTO_TOTAL";

        // Reintegrar inventario
        reintegrarInventario(detalleOriginal.producto, detalleDev.cantidadDevuelta);

        LOG.info("🔄 Devolución total procesada - Producto: " + detalleOriginal.producto.nombre);
    }

    private void procesarDevolucionParcial(DetalleVenta detalleOriginal, DetalleDevolucion detalleDev) {
        // Validar cantidad parcial
        if (detalleDev.cantidadDevuelta >= detalleOriginal.cantidad) {
            throw new ValidationException(
                    "La devolución parcial debe ser menor a la cantidad original",
                    "cantidadDevuelta",
                    detalleDev.cantidadDevuelta.toString()
            );
        }

        // Actualizar estado del detalle original
        detalleOriginal.cantidadDevuelta += detalleDev.cantidadDevuelta;
        detalleOriginal.estado = "DEVUELTO_PARCIAL";

        // Reintegrar inventario
        reintegrarInventario(detalleOriginal.producto, detalleDev.cantidadDevuelta);

        LOG.info("🔄 Devolución parcial procesada - Producto: " + detalleOriginal.producto.nombre +
                " (" + detalleDev.cantidadDevuelta + " de " + detalleOriginal.cantidad + ")");
    }

    private void procesarCambioProducto(DetalleVenta detalleOriginal, DetalleDevolucion detalleDev,
                                        DevolucionCommand.ItemDevolucionCommand item) {
        ValidationUtils.validarNoNulo(item.productoReemplazoId, "productoReemplazoId");

        // Obtener producto de reemplazo
        Producto productoReemplazo = productoRepository.findById(item.productoReemplazoId);
        if (productoReemplazo == null) {
            throw new VentaException("Producto de reemplazo no encontrado: " + item.productoReemplazoId, "DEV_005");
        }

        // Validar stock del producto de reemplazo
        BusinessRules.validarStockSuficiente(productoReemplazo, detalleDev.cantidadDevuelta);

        // Configurar cambio
        detalleDev.productoCambio = productoReemplazo;
        detalleDev.cantidadCambio = detalleDev.cantidadDevuelta;
        detalleDev.reingresarInventario = false; // No reingresar porque es cambio

        // Reintegrar producto original y reducir producto de reemplazo
        reintegrarInventario(detalleOriginal.producto, detalleDev.cantidadDevuelta);
        reducirInventario(productoReemplazo, detalleDev.cantidadDevuelta);

        // Actualizar estado del detalle original
        detalleOriginal.cantidadDevuelta += detalleDev.cantidadDevuelta;
        detalleOriginal.estado = "CAMBIO_PRODUCTO";

        LOG.info("🔄 Cambio de producto procesado - De: " + detalleOriginal.producto.nombre +
                " Por: " + productoReemplazo.nombre);
    }

    private void procesarReposicion(DetalleVenta detalleOriginal, DetalleDevolucion detalleDev) {
        // Para reposición, no reingresamos inventario (producto defectuoso)
        detalleDev.reingresarInventario = false;

        // Actualizar estado del detalle original
        detalleOriginal.cantidadDevuelta += detalleDev.cantidadDevuelta;
        detalleOriginal.estado = "REPOSICION";

        LOG.info("🔄 Reposición procesada - Producto: " + detalleOriginal.producto.nombre +
                " (No reingresa inventario)");
    }

    private void validarCantidadDevolucion(DetalleVenta detalleOriginal, Integer cantidadDevuelta) {
        int cantidadDisponible = detalleOriginal.cantidad - detalleOriginal.cantidadDevuelta;

        if (cantidadDevuelta > cantidadDisponible) {
            throw new InventarioException(
                    "Cantidad a devolver excede lo disponible",
                    detalleOriginal.producto.nombre,
                    cantidadDevuelta,
                    cantidadDisponible
            );
        }
    }

    private void reintegrarInventario(Producto producto, Integer cantidad) {
        producto.existencia += cantidad;
        LOG.info("📦 Inventario reintegrado - " + producto.nombre + ": +" + cantidad +
                " unidades → " + producto.existencia + " total");
    }

    private void reducirInventario(Producto producto, Integer cantidad) {
        producto.existencia -= cantidad;
        LOG.info("📦 Inventario reducido - " + producto.nombre + ": -" + cantidad +
                " unidades → " + producto.existencia + " total");
    }

    private BigDecimal calcularImporteDevolucion(DetalleDevolucion detalle) {
        return detalle.detalleVenta.precioUnitario.multiply(BigDecimal.valueOf(detalle.cantidadDevuelta));
    }

    private void actualizarEstadoVenta(Venta venta) {
        // Verificar si todos los detalles están completamente devueltos
        boolean todosDevueltos = venta.detalles.stream()
                .allMatch(detalle -> detalle.cantidadDevuelta.equals(detalle.cantidad));

        // Verificar si algunos detalles están parcialmente devueltos
        boolean algunosDevueltos = venta.detalles.stream()
                .anyMatch(detalle -> detalle.cantidadDevuelta > 0);

        if (todosDevueltos) {
            venta.estado = "COMPLETAMENTE_DEVUELTA";
            LOG.info("🏷️ Venta marcada como COMPLETAMENTE_DEVUELTA");
        } else if (algunosDevueltos) {
            venta.estado = "PARCIALMENTE_DEVUELTA";
            LOG.info("🏷️ Venta marcada como PARCIALMENTE_DEVUELTA");
        }
        // Si no hay devoluciones, mantiene su estado original
    }

    private String generarFolioDevolucion() {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = String.format("D%02d%02d%04d",
                now.getDayOfMonth(), now.getMonthValue(), now.getYear());
        Long count = devolucionRepository.count("fechaDevolucion >= ?1",
                LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0));
        return String.format("%s%04d", timestamp, count + 1);
    }

    public List<Devolucion> obtenerDevolucionesPorVenta(Long ventaId) {
        ValidationUtils.validarNoNulo(ventaId, "ventaId");
        return devolucionRepository.findByVenta(ventaId);
    }

    public Devolucion obtenerDevolucionPorId(Long id) {
        ValidationUtils.validarNoNulo(id, "idDevolucion");
        Devolucion devolucion = devolucionRepository.findById(id);
        if (devolucion == null) {
            throw new VentaException("Devolución no encontrada: " + id, "DEV_006");
        }
        return devolucion;
    }
}