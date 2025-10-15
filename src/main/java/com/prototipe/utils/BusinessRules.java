package com.prototipe.utils;



import com.prototipe.exceptions.InventarioException;
import com.prototipe.exceptions.ValidationException;
import com.prototipe.model.Producto;
import java.math.BigDecimal;

public class BusinessRules {

    public static void validarStockSuficiente(Producto producto, Integer cantidadRequerida) {
        if (producto == null) {
            throw new IllegalArgumentException("Producto no puede ser nulo");
        }

        if (cantidadRequerida == null || cantidadRequerida <= 0) {
            throw new IllegalArgumentException("Cantidad debe ser mayor a cero");
        }

        if (producto.existencia < cantidadRequerida) {
            throw new InventarioException(
                    "Stock insuficiente para el producto",
                    producto.nombre,
                    cantidadRequerida,
                    producto.existencia
            );
        }

        if (producto.existencia <= 0) {
            throw new InventarioException(
                    "Producto agotado",
                    producto.nombre,
                    cantidadRequerida,
                    producto.existencia
            );
        }
    }

    public static void validarPrecioPositivo(BigDecimal precio) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(
                    "El precio debe ser mayor a cero",
                    "precio",
                    precio != null ? precio.toString() : "null"
            );
        }
    }
}
