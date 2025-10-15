package com.prototipe.exceptions;



public class ClienteException extends RuntimeException {
    private final Long clienteId;

    public ClienteException(String mensaje, Long clienteId) {
        super(mensaje);
        this.clienteId = clienteId;
    }

    public Long getClienteId() { return clienteId; }
}