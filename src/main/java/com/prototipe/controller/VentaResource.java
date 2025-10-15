package com.prototipe.controller;

import com.prototipe.dto.VentaResponse;
import com.prototipe.exceptions.*;
import com.prototipe.model.Venta;
import com.prototipe.service.VentaService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("/ventas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VentaResource {

    private static final Logger LOG = Logger.getLogger(VentaResource.class.getName());

    @Inject
    VentaService ventaService;

    @GET
    public Response obtenerTodasVentas() {
        try {
            LOG.fine("📋 Solicitando listado de todas las ventas");
            List<Venta> ventas = ventaService.obtenerTodasVentas();

            // ✅ USAR DTOs para evitar recursión
            List<VentaResponse> ventasResponse = ventas.stream()
                    .map(VentaResponse::fromVenta)
                    .collect(Collectors.toList());

            return Response.ok(ventasResponse).build();
        } catch (Exception e) {
            LOG.severe("❌ Error al obtener ventas: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(crearErrorResponse("Error interno al obtener ventas", "VENTAS_001"))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response obtenerVenta(@PathParam("id") Long id) {
        try {
            LOG.info("🔍 Buscando venta con ID: " + id);
            Venta venta = ventaService.obtenerVentaPorId(id);
            return Response.ok(venta).build();
        } catch (VentaException e) {
            LOG.warning("⚠️ Venta no encontrada - ID: " + id + " - " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(crearErrorResponse(e.getMessage(), e.getCodigoError()))
                    .build();
        } catch (Exception e) {
            LOG.severe("💥 Error inesperado al obtener venta: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(crearErrorResponse("Error interno al obtener venta", "VENTAS_002"))
                    .build();
        }
    }

    @POST
    public Response crearVenta(Venta venta) {
        try {
            LOG.info("🛒 Creando nueva venta");
            Venta nuevaVenta = ventaService.crearVenta(venta);

            LOG.info("✅ Venta creada exitosamente - ID: " + nuevaVenta.idVenta);
            return Response.status(Response.Status.CREATED)
                    .entity(crearSuccessResponse("Venta creada exitosamente", nuevaVenta))
                    .build();

        } catch (InventarioException e) {
            LOG.warning("📦 Error de inventario: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(crearErrorResponse(
                            String.format("Stock insuficiente: %s (Solicitado: %d, Existencia: %d)",
                                    e.getProductoNombre(), e.getCantidadSolicitada(), e.getExistenciaActual()),
                            "INVENTARIO_001"
                    ))
                    .build();
        } catch (ClienteException e) {
            LOG.warning("👥 Error de cliente: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(crearErrorResponse(e.getMessage(), "CLIENTE_001"))
                    .build();
        } catch (ValidationException e) {
            LOG.warning("⚡ Error de validación: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(crearErrorResponse(
                            String.format("Campo inválido: %s (Valor: %s)", e.getCampo(), e.getValor()),
                            "VALIDACION_001"
                    ))
                    .build();
        } catch (VentaException e) {
            LOG.warning("💰 Error de venta: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(crearErrorResponse(e.getMessage(), e.getCodigoError()))
                    .build();
        } catch (Exception e) {
            LOG.severe("💥 Error inesperado al crear venta: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(crearErrorResponse("Error interno al procesar la venta", "VENTAS_003"))
                    .build();
        }
    }

    @PUT
    @Path("/{id}/cancelar")
    public Response cancelarVenta(@PathParam("id") Long id) {
        try {
            LOG.info("🔄 Solicitando cancelación de venta - ID: " + id);
            ventaService.cancelarVenta(id);

            LOG.info("✅ Venta cancelada exitosamente - ID: " + id);
            return Response.ok()
                    .entity(crearSuccessResponse("Venta cancelada exitosamente - Inventario reintegrado", null))
                    .build();

        } catch (VentaException e) {
            LOG.warning("⚠️ Error al cancelar venta: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(crearErrorResponse(e.getMessage(), e.getCodigoError()))
                    .build();
        } catch (Exception e) {
            LOG.severe("💥 Error inesperado al cancelar venta: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(crearErrorResponse("Error interno al cancelar venta", "VENTAS_004"))
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