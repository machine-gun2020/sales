package com.prototipe.service;

import com.prototipe.exceptions.*;
import com.prototipe.utils.*;
import com.prototipe.model.Cliente;
import com.prototipe.repository.ClienteRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class ClienteService {

    private static final Logger LOG = Logger.getLogger(ClienteService.class.getName());

    @Inject
    ClienteRepository clienteRepository;

    public List<Cliente> obtenerTodosClientes() {
        LOG.fine("👥 Obteniendo lista de clientes activos");
        return clienteRepository.findActivos();
    }

    public Cliente obtenerClientePorId(Long id) {
        ValidationUtils.validarNoNulo(id, "idCliente");
        ValidationUtils.validarPositivo(id, "idCliente");

        Cliente cliente = clienteRepository.findById(id);
        if (cliente == null) {
            throw new ClienteException("Cliente no encontrado", id);
        }
        return cliente;
    }

    @Transactional
    public Cliente crearCliente(Cliente cliente) {
        try {
            LOG.info("🆕 Creando nuevo cliente");

            // Validaciones básicas
            ValidationUtils.validarNoNulo(cliente, "cliente");
            ValidationUtils.validarTextoNoVacio(cliente.nombre, "nombre");

            // Validar RFC único si se proporciona
            if (cliente.rfc != null && !cliente.rfc.trim().isEmpty()) {
                if (clienteRepository.findByRfc(cliente.rfc).isPresent()) {
                    throw new ValidationException(
                            "El RFC ya está registrado",
                            "rfc",
                            cliente.rfc
                    );
                }

                // Validar formato básico de RFC
                if (!cliente.rfc.matches("^[A-Z&Ñ]{3,4}[0-9]{6}[A-Z0-9]{3}$")) {
                    LOG.warning("⚠️ RFC con formato potencialmente inválido: " + cliente.rfc);
                }
            }

            // Validar email si se proporciona
            if (cliente.email != null && !cliente.email.trim().isEmpty()) {
                if (!cliente.email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    throw new ValidationException(
                            "Formato de email inválido",
                            "email",
                            cliente.email
                    );
                }
            }

            // Establecer valores por defecto
            if (cliente.activo == null) {
                cliente.activo = "S";
            }

            clienteRepository.persist(cliente);
            LOG.info("✅ Cliente creado - ID: " + cliente.idCliente + ", Nombre: " + cliente.nombre);

            return cliente;

        } catch (ValidationException e) {
            LOG.severe("❌ Error de validación en cliente: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.severe("💥 Error inesperado al crear cliente: " + e.getMessage());
            throw new ClienteException("Error interno al crear cliente", null);
        }
    }

    @Transactional
    public Cliente actualizarCliente(Long id, Cliente clienteActualizado) {
        try {
            ValidationUtils.validarNoNulo(id, "idCliente");
            ValidationUtils.validarPositivo(id, "idCliente");
            ValidationUtils.validarNoNulo(clienteActualizado, "clienteActualizado");

            Cliente cliente = clienteRepository.findById(id);
            if (cliente == null) {
                throw new ClienteException("Cliente no encontrado", id);
            }

            LOG.info("✏️ Actualizando cliente - ID: " + id);

            // Validar y actualizar campos
            if (clienteActualizado.nombre != null) {
                ValidationUtils.validarTextoNoVacio(clienteActualizado.nombre, "nombre");
                cliente.nombre = clienteActualizado.nombre;
            }

            if (clienteActualizado.rfc != null) {
                // Validar RFC único (excluyendo el propio cliente)
                if (!clienteActualizado.rfc.equals(cliente.rfc)) {
                    if (clienteRepository.findByRfc(clienteActualizado.rfc).isPresent()) {
                        throw new ValidationException(
                                "El RFC ya está registrado por otro cliente",
                                "rfc",
                                clienteActualizado.rfc
                        );
                    }
                }
                cliente.rfc = clienteActualizado.rfc;
            }

            if (clienteActualizado.direccion != null) {
                cliente.direccion = clienteActualizado.direccion;
            }

            if (clienteActualizado.telefono != null) {
                cliente.telefono = clienteActualizado.telefono;
            }

            if (clienteActualizado.email != null) {
                if (!clienteActualizado.email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    throw new ValidationException(
                            "Formato de email inválido",
                            "email",
                            clienteActualizado.email
                    );
                }
                cliente.email = clienteActualizado.email;
            }

            LOG.info("✅ Cliente actualizado - ID: " + id);
            return cliente;

        } catch (ValidationException e) {
            LOG.severe("❌ Error de validación al actualizar cliente: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.severe("💥 Error inesperado al actualizar cliente: " + e.getMessage());
            throw new ClienteException("Error interno al actualizar cliente", id);
        }
    }

    @Transactional
    public void desactivarCliente(Long id) {
        try {
            ValidationUtils.validarNoNulo(id, "idCliente");
            ValidationUtils.validarPositivo(id, "idCliente");

            Cliente cliente = clienteRepository.findById(id);
            if (cliente != null) {
                // ✅ CORREGIDO: Eliminada la validación de ventas activas
                // (la implementaremos en una fase posterior)

                cliente.activo = "N";
                LOG.info("🚫 Cliente desactivado - ID: " + id + ", Nombre: " + cliente.nombre);
            } else {
                LOG.warning("⚠️ Intento de desactivar cliente no existente - ID: " + id);
            }

        } catch (Exception e) {
            LOG.severe("💥 Error inesperado al desactivar cliente: " + e.getMessage());
            throw new ClienteException("Error interno al desactivar cliente", id);
        }
    }

    public List<Cliente> buscarPorNombre(String nombre) {
        ValidationUtils.validarTextoNoVacio(nombre, "nombre");
        LOG.fine("🔍 Buscando clientes por nombre: " + nombre);
        return clienteRepository.findByNombre(nombre);
    }
}