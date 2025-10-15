package com.prototipe.exceptions;


public class VentaException extends RuntimeException {
    private final String codigoError;

    public VentaException(String mensaje) {
        super(mensaje);
        this.codigoError = "VENTA_001";
    }

    public VentaException(String mensaje, String codigoError) {
        super(mensaje);
        this.codigoError = codigoError;
    }

    public String getCodigoError() { return codigoError; }
}
