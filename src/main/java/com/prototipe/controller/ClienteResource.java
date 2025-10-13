package com.prototipe.controller;


import com.prototipe.model.Cliente;
import com.prototipe.repository.ClienteRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;

@Path("/clientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClienteResource {

    @Inject
    ClienteRepository clienteRepository;

    @GET
    public List<Cliente> obtenerTodosClientes() {
        return clienteRepository.findActivos();
    }

    @GET
    @Path("/{id}")
    public Response obtenerCliente(@PathParam("id") Long id) {
        Cliente cliente = clienteRepository.findById(id);
        if (cliente == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(cliente).build();
    }

    @POST
    @Transactional
    public Response crearCliente(Cliente cliente) {
        try {
            // Validar que no exista RFC duplicado si se proporciona
            if (cliente.rfc != null && !cliente.rfc.trim().isEmpty()) {
                if (clienteRepository.findByRfc(cliente.rfc).isPresent()) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("El RFC ya existe").build();
                }
            }
            cliente.fechaRegistro = LocalDateTime.now();
            clienteRepository.persist(cliente);
            return Response.status(Response.Status.CREATED).entity(cliente).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error al crear cliente: " + e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response actualizarCliente(@PathParam("id") Long id, Cliente clienteActualizado) {
        try {
            Cliente cliente = clienteRepository.findById(id);
            if (cliente == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            cliente.nombre = clienteActualizado.nombre;
            cliente.rfc = clienteActualizado.rfc;
            cliente.direccion = clienteActualizado.direccion;
            cliente.telefono = clienteActualizado.telefono;
            cliente.email = clienteActualizado.email;

            return Response.ok(cliente).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error al actualizar cliente: " + e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response desactivarCliente(@PathParam("id") Long id) {
        try {
            Cliente cliente = clienteRepository.findById(id);
            if (cliente != null) {
                cliente.activo = "N";
            }
            return Response.ok().entity("Cliente desactivado exitosamente").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error al desactivar cliente: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/buscar/{nombre}")
    public List<Cliente> buscarPorNombre(@PathParam("nombre") String nombre) {
        return clienteRepository.findByNombre(nombre);
    }
}