package com.prototipe.dto;

import java.util.List;

public class DevolucionCommand {
    public Long ventaId;
    public String motivoGeneral;
    public boolean reintegrarInventario = true;
    public List<ItemDevolucionCommand> items;

    public static class ItemDevolucionCommand {
        public Long detalleVentaId;
        public Integer cantidadDevuelta;
        public String motivoEspecifico;
        public TipoDevolucion tipo;
        public Long productoReemplazoId; // Para cambios

        public enum TipoDevolucion {
            DEVOLUCION_TOTAL,
            DEVOLUCION_PARCIAL,
            CAMBIO_PRODUCTO,
            REPOSICION
        }
    }
}