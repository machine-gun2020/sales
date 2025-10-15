package com.prototipe.utils;



import com.prototipe.exceptions.ValidationException;
import java.math.BigDecimal;

public class ValidationUtils {

    public static void validarPositivo(Number valor, String nombreCampo) {
        if (valor == null) {
            throw new ValidationException(
                    "El campo no puede ser nulo",
                    nombreCampo,
                    "null"
            );
        }

        if (valor instanceof Integer && (Integer) valor <= 0) {
            throw new ValidationException(
                    "El campo debe ser mayor a cero",
                    nombreCampo,
                    valor.toString()
            );
        }

        if (valor instanceof BigDecimal && ((BigDecimal) valor).compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(
                    "El campo debe ser mayor a cero",
                    nombreCampo,
                    valor.toString()
            );
        }
    }

    public static void validarNoNulo(Object valor, String nombreCampo) {
        if (valor == null) {
            throw new ValidationException(
                    "El campo no puede ser nulo",
                    nombreCampo,
                    "null"
            );
        }
    }

    public static void validarTextoNoVacio(String texto, String nombreCampo) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new ValidationException(
                    "El campo no puede estar vacío",
                    nombreCampo,
                    texto
            );
        }
    }
}
