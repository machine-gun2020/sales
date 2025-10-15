package com.prototipe.exceptions;



public class ValidationException extends RuntimeException {
    private final String campo;
    private final String valor;

    public ValidationException(String mensaje, String campo, String valor) {
        super(mensaje);
        this.campo = campo;
        this.valor = valor;
    }

    public String getCampo() { return campo; }
    public String getValor() { return valor; }
}
