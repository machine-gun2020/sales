package com.prototipe.controller;



import com.prototipe.exceptions.*;
import com.prototipe.model.Cliente;
import com.prototipe.service.ClienteService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Logger;

@Path("/clientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClienteResource {

    private static final Logger LOG = Logger.getLogger(ClienteResource.class.getName());

    @Inject
    ClienteService clienteService;

    @GET
    public Response obtenerTodosClientes() {
        try {
            LOG.fine("👥 Solicitando listado de clientes activos");
            List<Cliente> clientes = clienteService.obtenerTodosClientes();
            return Response.ok(clientes).build();
        } catch (Exception e) {
            LOG.severe("❌ Error al obtener clientes: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(crearErrorResponse("Error interno al obtener clientes", "CLIENTES_001"))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response obtenerCliente(@PathParam("id") Long id) {
        try {
            LOG.info("🔍 Buscando cliente con ID: " + id);
            Cliente cliente = clienteService.obtenerClientePorId(id);
            return Response.ok(cliente).build();
        } catch (ClienteException e) {
            LOG.warning("⚠️ Cliente no encontrado - ID: " + id + " - " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(crearErrorResponse(e.getMessage(), "CLIENTES_002"))
                    .build();
        } catch (Exception e) {
            LOG.severe("💥 Error inesperado al obtener cliente: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(crearErrorResponse("Error interno al obtener cliente", "CLIENTES_003"))
                    .build();
        }
    }

    @POST
    public Response crearCliente(Cliente cliente) {
        try {
            LOG.info("🆕 Creando nuevo cliente");
            Cliente nuevoCliente = clienteService.crearCliente(cliente);

            LOG.info("✅ Cliente creado exitosamente - ID: " + nuevoCliente.idCliente);
            return Response.status(Response.Status.CREATED)
                    .entity(crearSuccessResponse("Cliente creado exitosamente", nuevoCliente))
                    .build();

        } catch (ValidationException e) {
            LOG.warning("⚡ Error de validación: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(crearErrorResponse(
                            String.format("Campo inválido: %s (Valor: %s)", e.getCampo(), e.getValor()),
                            "VALIDACION_001"
                    ))
                    .build();
        } catch (ClienteException e) {
            LOG.warning("👥 Error de cliente: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(crearErrorResponse(e.getMessage(), "CLIENTES_004"))
                    .build();
        } catch (Exception e) {
            LOG.severe("💥 Error inesperado al crear cliente: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(crearErrorResponse("Error interno al crear cliente", "CLIENTES_005"))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response actualizarCliente(@PathParam("id") Long id, Cliente cliente) {
        try {
            LOG.info("✏️ Actualizando cliente - ID: " + id);
            Cliente clienteActualizado = clienteService.actualizarCliente(id, cliente);

            LOG.info("✅ Cliente actualizado exitosamente - ID: " + id);
            return Response.ok()
                    .entity(crearSuccessResponse("Cliente actualizado exitosamente", clienteActualizado))
                    .build();

        } catch (ClienteException e) {
            LOG.warning("⚠️ Error al actualizar cliente: " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(crearErrorResponse(e.getMessage(), "CLIENTES_006"))
                    .build();
        } catch (ValidationException e) {
            LOG.warning("⚡ Error de validación: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(crearErrorResponse(
                            String.format("Campo inválido: %s (Valor: %s)", e.getCampo(), e.getValor()),
                            "VALIDACION_002"
                    ))
                    .build();
        } catch (Exception e) {
            LOG.severe("💥 Error inesperado al actualizar cliente: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(crearErrorResponse("Error interno al actualizar cliente", "CLIENTES_007"))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response desactivarCliente(@PathParam("id") Long id) {
        try {
            LOG.info("🚫 Desactivando cliente - ID: " + id);
            clienteService.desactivarCliente(id);

            LOG.info("✅ Cliente desactivado exitosamente - ID: " + id);
            return Response.ok()
                    .entity(crearSuccessResponse("Cliente desactivado exitosamente", null))
                    .build();

        } catch (ClienteException e) {
            LOG.warning("⚠️ Error al desactivar cliente: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(crearErrorResponse(e.getMessage(), "CLIENTES_008"))
                    .build();
        } catch (Exception e) {
            LOG.severe("💥 Error inesperado al desactivar cliente: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(crearErrorResponse("Error interno al desactivar cliente", "CLIENTES_009"))
                    .build();
        }
    }

    @GET
    @Path("/buscar/{nombre}")
    public Response buscarPorNombre(@PathParam("nombre") String nombre) {
        try {
            LOG.info("🔍 Buscando clientes por nombre: " + nombre);
            List<Cliente> clientes = clienteService.buscarPorNombre(nombre);
            return Response.ok(clientes).build();
        } catch (ValidationException e) {
            LOG.warning("⚡ Error de validación: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(crearErrorResponse(e.getMessage(), "VALIDACION_003"))
                    .build();
        } catch (Exception e) {
            LOG.severe("💥 Error inesperado al buscar clientes: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(crearErrorResponse("Error interno al buscar clientes", "CLIENTES_010"))
                    .build();
        }
    }

    // Métodos auxiliares para respuestas consistentes
    private Object crearErrorResponse(String mensaje, String codigoError) {
        return new ErrorResponse(mensaje, codigoError);
    }

    private Object crearSuccessResponse(String mensaje, Object data) {
        return new SuccessResponse(mensaje, data);
    }

    // Clases internas para respuestas estructuradas
    public static class ErrorResponse {
        public final String mensaje;
        public final String codigoError;
        public final String tipo = "error";
        public final long timestamp;

        public ErrorResponse(String mensaje, String codigoError) {
            this.mensaje = mensaje;
            this.codigoError = codigoError;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public static class SuccessResponse {
        public final String mensaje;
        public final Object data;
        public final String tipo = "success";
        public final long timestamp;

        public SuccessResponse(String mensaje, Object data) {
            this.mensaje = mensaje;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
    }
}