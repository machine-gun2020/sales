package com.prototipe.controller;


import com.prototipe.dto.ProductoCreateDTO;
import com.prototipe.exceptions.ValidationException;
import com.prototipe.exceptions.VentaException;
import com.prototipe.model.Producto;
import com.prototipe.service.ProductoService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Logger;

@Path("/productos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductoResource {

    // ✅ AGREGAR EL LOGGER
    private static final Logger LOG = Logger.getLogger(ProductoResource.class.getName());

    @Inject
    ProductoService productoService;

    @GET
    public Response obtenerTodosProductos() {
        try {
            LOG.fine("📦 Solicitando listado de productos activos");
            List<Producto> productos = productoService.obtenerTodosProductos();
            return Response.ok(productos).build();
        } catch (Exception e) {
            LOG.severe("❌ Error al obtener productos: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(crearErrorResponse("Error interno al obtener productos", "PRODUCTOS_001"))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response obtenerProducto(@PathParam("id") Long id) {
        try {
            LOG.info("🔍 Buscando producto con ID: " + id);
            Producto producto = productoService.obtenerProductoPorId(id);
            return Response.ok(producto).build();
        } catch (VentaException e) {
            LOG.warning("⚠️ Producto no encontrado - ID: " + id + " - " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(crearErrorResponse(e.getMessage(), e.getCodigoError()))
                    .build();
        } catch (Exception e) {
            LOG.severe("💥 Error inesperado al obtener producto: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(crearErrorResponse("Error interno al obtener producto", "PRODUCTOS_002"))
                    .build();
        }
    }

    @POST
    public Response crearProducto(ProductoCreateDTO productoDTO) {
        try {
            LOG.info("🆕 Creando nuevo producto desde DTO");
            Producto nuevoProducto = productoService.crearProducto(productoDTO);

            LOG.info("✅ Producto creado exitosamente - ID: " + nuevoProducto.idProducto);
            return Response.status(Response.Status.CREATED)
                    .entity(crearSuccessResponse("Producto creado exitosamente", nuevoProducto))
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
            LOG.warning("💰 Error de producto: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(crearErrorResponse(e.getMessage(), e.getCodigoError()))
                    .build();
        } catch (Exception e) {
            LOG.severe("💥 Error inesperado al crear producto: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(crearErrorResponse("Error interno al crear producto", "PRODUCTOS_003"))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response actualizarProducto(@PathParam("id") Long id, Producto producto) {
        try {
            LOG.info("✏️ Actualizando producto - ID: " + id);
            Producto productoActualizado = productoService.actualizarProducto(id, producto);

            LOG.info("✅ Producto actualizado exitosamente - ID: " + id);
            return Response.ok()
                    .entity(crearSuccessResponse("Producto actualizado exitosamente", productoActualizado))
                    .build();

        } catch (VentaException e) {
            LOG.warning("⚠️ Error al actualizar producto: " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(crearErrorResponse(e.getMessage(), e.getCodigoError()))
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
            LOG.severe("💥 Error inesperado al actualizar producto: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(crearErrorResponse("Error interno al actualizar producto", "PRODUCTOS_004"))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response desactivarProducto(@PathParam("id") Long id) {
        try {
            LOG.info("🚫 Desactivando producto - ID: " + id);
            productoService.desactivarProducto(id);

            LOG.info("✅ Producto desactivado exitosamente - ID: " + id);
            return Response.ok()
                    .entity(crearSuccessResponse("Producto desactivado exitosamente", null))
                    .build();

        } catch (VentaException e) {
            LOG.warning("⚠️ Error al desactivar producto: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(crearErrorResponse(e.getMessage(), e.getCodigoError()))
                    .build();
        } catch (Exception e) {
            LOG.severe("💥 Error inesperado al desactivar producto: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(crearErrorResponse("Error interno al desactivar producto", "PRODUCTOS_005"))
                    .build();
        }
    }

    // ✅ AGREGAR MÉTODOS AUXILIARES PARA RESPUESTAS
    private Object crearErrorResponse(String mensaje, String codigoError) {
        return new ErrorResponse(mensaje, codigoError);
    }

    private Object crearSuccessResponse(String mensaje, Object data) {
        return new SuccessResponse(mensaje, data);
    }

    // ✅ AGREGAR CLASES INTERNAS PARA RESPUESTAS ESTRUCTURADAS
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