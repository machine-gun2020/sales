package com.prototipe.exceptions;



public class InventarioException extends RuntimeException {
    private final String productoNombre;
    private final Integer cantidadSolicitada;
    private final Integer existenciaActual;

    public InventarioException(String mensaje, String productoNombre,
                               Integer cantidadSolicitada, Integer existenciaActual) {
        super(mensaje);
        this.productoNombre = productoNombre;
        this.cantidadSolicitada = cantidadSolicitada;
        this.existenciaActual = existenciaActual;
    }

    public String getProductoNombre() { return productoNombre; }
    public Integer getCantidadSolicitada() { return cantidadSolicitada; }
    public Integer getExistenciaActual() { return existenciaActual; }
}