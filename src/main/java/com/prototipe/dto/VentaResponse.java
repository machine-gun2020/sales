package com.prototipe.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class VentaResponse {
    public Long idVenta;
    public String folio;
    public LocalDateTime fechaVenta;
    public ClienteInfo cliente;
    public BigDecimal subtotal;
    public BigDecimal iva;
    public BigDecimal total;
    public String estado;
    public List<DetalleVentaInfo> detalles;
    public List<DevolucionInfo> devoluciones;

    public static VentaResponse fromVenta(com.prototipe.model.Venta venta) {
        VentaResponse response = new VentaResponse();
        response.idVenta = venta.idVenta;
        response.folio = venta.folio;
        response.fechaVenta = venta.fechaVenta;
        response.subtotal = venta.subtotal;
        response.iva = venta.iva;
        response.total = venta.total;
        response.estado = venta.estado;

        // Cliente
        if (venta.cliente != null) {
            response.cliente = new ClienteInfo();
            response.cliente.idCliente = venta.cliente.idCliente;
            response.cliente.nombre = venta.cliente.nombre;
            response.cliente.rfc = venta.cliente.rfc;
            response.cliente.direccion = venta.cliente.direccion;
            response.cliente.telefono = venta.cliente.telefono;
            response.cliente.email = venta.cliente.email;
            response.cliente.activo = venta.cliente.activo;
        }

        // Detalles (sin recursión)
        if (venta.detalles != null) {
            response.detalles = venta.detalles.stream()
                    .map(DetalleVentaInfo::fromDetalle)
                    .collect(Collectors.toList());
        }

        // Devoluciones (sin recursión)
        if (venta.devoluciones != null) {
            response.devoluciones = venta.devoluciones.stream()
                    .map(DevolucionInfo::fromDevolucion)
                    .collect(Collectors.toList());
        }

        return response;
    }

    public static class ClienteInfo {
        public Long idCliente;
        public String nombre;
        public String rfc;
        public String direccion;
        public String telefono;
        public String email;
        public String activo;
    }

    public static class DetalleVentaInfo {
        public Long idDetalle;
        public ProductoInfo producto;
        public Integer cantidad;
        public BigDecimal precioUnitario;
        public BigDecimal importe;
        public Integer cantidadDevuelta;
        public String estado;

        public static DetalleVentaInfo fromDetalle(com.prototipe.model.DetalleVenta detalle) {
            DetalleVentaInfo info = new DetalleVentaInfo();
            info.idDetalle = detalle.idDetalle;
            info.cantidad = detalle.cantidad;
            info.precioUnitario = detalle.precioUnitario;
            info.importe = detalle.importe;
            info.cantidadDevuelta = detalle.cantidadDevuelta;
            info.estado = detalle.estado;

            if (detalle.producto != null) {
                info.producto = new ProductoInfo();
                info.producto.idProducto = detalle.producto.idProducto;
                info.producto.codigo = detalle.producto.codigo;
                info.producto.nombre = detalle.producto.nombre;
            }

            return info;
        }
    }

    public static class DevolucionInfo {
        public Long idDevolucion;
        public String folioDevolucion;
        public LocalDateTime fechaDevolucion;
        public BigDecimal totalDevolucion;
        public String estado;
        public String motivoGeneral;

        public static DevolucionInfo fromDevolucion(com.prototipe.model.Devolucion devolucion) {
            DevolucionInfo info = new DevolucionInfo();
            info.idDevolucion = devolucion.idDevolucion;
            info.folioDevolucion = devolucion.folioDevolucion;
            info.fechaDevolucion = devolucion.fechaDevolucion;
            info.totalDevolucion = devolucion.totalDevolucion;
            info.estado = devolucion.estado;
            info.motivoGeneral = devolucion.motivoGeneral;
            return info;
        }
    }

    public static class ProductoInfo {
        public Long idProducto;
        public String codigo;
        public String nombre;
    }
}
