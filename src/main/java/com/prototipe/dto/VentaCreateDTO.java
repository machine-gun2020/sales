package com.prototipe.dto;

import java.util.List;

public class VentaCreateDTO {
    public Long clienteId;
    public String folio;
    public List<DetalleVentaCreateDTO> detalles;

    public static class DetalleVentaCreateDTO {
        public Long productoId;
        public Integer cantidad;
        public java.math.BigDecimal precioUnitario;
    }
}