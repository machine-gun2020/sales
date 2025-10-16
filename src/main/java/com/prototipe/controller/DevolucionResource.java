package com.prototipe.controller;

import com.prototipe.dto.DevolucionCommand;
import com.prototipe.dto.DevolucionResponse;
import com.prototipe.exceptions.*;
import com.prototipe.service.DevolucionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Logger;

@Path("/devoluciones")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DevolucionResource {

    private static final Logger LOG = Logger.getLogger(DevolucionResource.class.getName());

    @Inject
    DevolucionService devolucionService;

    @POST
    @Path("/parcial")
    public Response procesarDevolucionParcial(DevolucionCommand command) {
        try {
            LOG.info("🔄 Solicitando devolución parcial - Venta ID: " + command.ventaId);
            var devolucion = devolucionService.procesarDevolucionParcial(command);
            var response = DevolucionResponse.fromDevolucion(devolucion);

            LOG.info("✅ Devolución parcial procesada - Folio: " + devolucion.folioDevolucion);
            return Response.status(Response.Status.CREATED)
                    .entity(crearSuccessResponse("Devolución procesada exitosamente", response))
                    .build();

        } catch (VentaException e) {
            LOG.warning("💰 Error de venta en devolución: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(crearErrorResponse(e.getMessage(), e.getCodigoError()))
                    .build();
        } catch (InventarioException e) {
            LOG.warning("📦 Error de inventario: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(crearErrorResponse(
                            String.format("Stock insuficiente: %s (Solicitado: %d, Existencia: %d)",
                                    e.getProductoNombre(), e.getCantidadSolicitada(), e.getExistenciaActual()),
                            "INVENTARIO_002"
                    ))
                    .build();
        } catch (ValidationException e) {
            LOG.warning("⚡ Error de validación: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(crearErrorResponse(
                            String.format("Campo inválido: %s (Valor: %s)", e.getCampo(), e.getValor()),
                            "VALIDACION_004"
                    ))
                    .build();
        } catch (Exception e) {
            LOG.severe("💥 Error inesperado en devolución: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(crearErrorResponse("Error interno al procesar devolución", "DEVOL_001"))
                    .build();
        }
    }

    @GET
    @Path("/venta/{ventaId}")
    public Response obtenerDevolucionesPorVenta(@PathParam("ventaId") Long ventaId) {
        try {
            LOG.info("🔍 Buscando devoluciones de venta ID: " + ventaId);
            var devoluciones = devolucionService.obtenerDevolucionesPorVenta(ventaId);
            var responses = devoluciones.stream()
                    .map(DevolucionResponse::fromDevolucion)
                    .toList();

            return Response.ok(responses).build();

        } catch (Exception e) {
            LOG.severe("💥 Error al obtener devoluciones: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(crearErrorResponse("Error interno al obtener devoluciones", "DEVOL_002"))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response obtenerDevolucion(@PathParam("id") Long id) {
        try {
            LOG.info("🔍 Buscando devolución ID: " + id);
            var devolucion = devolucionService.obtenerDevolucionPorId(id);
            var response = DevolucionResponse.fromDevolucion(devolucion);

            return Response.ok(response).build();

        } catch (VentaException e) {
            LOG.warning("⚠️ Devolución no encontrada - ID: " + id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(crearErrorResponse(e.getMessage(), e.getCodigoError()))
                    .build();
        } catch (Exception e) {
            LOG.severe("💥 Error al obtener devolución: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(crearErrorResponse("Error interno al obtener devolución", "DEVOL_003"))
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