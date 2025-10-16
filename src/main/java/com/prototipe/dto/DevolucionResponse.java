package com.prototipe.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class DevolucionResponse {
    public Long idDevolucion;
    public String folioDevolucion;
    public LocalDateTime fechaDevolucion;
    public VentaInfo venta;
    public ClienteInfo cliente;
    public BigDecimal totalDevolucion;
    public String estado;
    public String motivoGeneral;
    public List<DetalleDevolucionInfo> detalles;

    public static DevolucionResponse fromDevolucion(com.prototipe.model.Devolucion devolucion) {
        DevolucionResponse response = new DevolucionResponse();
        response.idDevolucion = devolucion.idDevolucion;
        response.folioDevolucion = devolucion.folioDevolucion;
        response.fechaDevolucion = devolucion.fechaDevolucion;
        response.totalDevolucion = devolucion.totalDevolucion;
        response.estado = devolucion.estado;
        response.motivoGeneral = devolucion.motivoGeneral;

        // Información de venta (sin recursión)
        if (devolucion.venta != null) {
            response.venta = new VentaInfo();
            response.venta.idVenta = devolucion.venta.idVenta;
            response.venta.folio = devolucion.venta.folio;
            response.venta.fechaVenta = devolucion.venta.fechaVenta;
            response.venta.total = devolucion.venta.total;
        }

        // Información de cliente
        if (devolucion.cliente != null) {
            response.cliente = new ClienteInfo();
            response.cliente.idCliente = devolucion.cliente.idCliente;
            response.cliente.nombre = devolucion.cliente.nombre;
        }

        // Detalles de devolución
        if (devolucion.detalles != null) {
            response.detalles = devolucion.detalles.stream()
                    .map(DetalleDevolucionInfo::fromDetalle)
                    .collect(Collectors.toList());
        }

        return response;
    }

    public static class VentaInfo {
        public Long idVenta;
        public String folio;
        public LocalDateTime fechaVenta;
        public BigDecimal total;
    }

    public static class ClienteInfo {
        public Long idCliente;
        public String nombre;
    }

    public static class DetalleDevolucionInfo {
        public Long idDetalleDevolucion;
        public ProductoInfo producto;
        public Integer cantidadDevuelta;
        public String motivoEspecifico;
        public String tipoMotivo;
        public ProductoInfo productoCambio;
        public Integer cantidadCambio;

        public static DetalleDevolucionInfo fromDetalle(com.prototipe.model.DetalleDevolucion detalle) {
            DetalleDevolucionInfo info = new DetalleDevolucionInfo();
            info.idDetalleDevolucion = detalle.idDetalleDevolucion;
            info.cantidadDevuelta = detalle.cantidadDevuelta;
            info.motivoEspecifico = detalle.motivoEspecifico;
            info.tipoMotivo = detalle.tipoMotivo;
            info.cantidadCambio = detalle.cantidadCambio;

            // Producto original
            if (detalle.producto != null) {
                info.producto = new ProductoInfo();
                info.producto.idProducto = detalle.producto.idProducto;
                info.producto.nombre = detalle.producto.nombre;
                info.producto.codigo = detalle.producto.codigo;
            }

            // Producto de cambio (si aplica)
            if (detalle.productoCambio != null) {
                info.productoCambio = new ProductoInfo();
                info.productoCambio.idProducto = detalle.productoCambio.idProducto;
                info.productoCambio.nombre = detalle.productoCambio.nombre;
                info.productoCambio.codigo = detalle.productoCambio.codigo;
            }

            return info;
        }
    }

    public static class ProductoInfo {
        public Long idProducto;
        public String codigo;
        public String nombre;
    }
}