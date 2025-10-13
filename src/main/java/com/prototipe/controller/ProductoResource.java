package com.prototipe.controller;



import com.prototipe.model.Producto;
import com.prototipe.service.ProductoService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/productos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductoResource {

    @Inject
    ProductoService productoService;

    @GET
    public List<Producto> obtenerTodosProductos() {
        return productoService.obtenerTodosProductos();
    }

    @GET
    @Path("/{id}")
    public Response obtenerProducto(@PathParam("id") Long id) {
        Producto producto = productoService.obtenerProductoPorId(id);
        if (producto == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(producto).build();
    }

    @POST
    public Response crearProducto(Producto producto) {
        try {
            Producto nuevoProducto = productoService.crearProducto(producto);
            return Response.status(Response.Status.CREATED).entity(nuevoProducto).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error al crear producto: " + e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response actualizarProducto(@PathParam("id") Long id, Producto producto) {
        try {
            Producto productoActualizado = productoService.actualizarProducto(id, producto);
            return Response.ok(productoActualizado).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error al actualizar producto: " + e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response desactivarProducto(@PathParam("id") Long id) {
        try {
            productoService.desactivarProducto(id);
            return Response.ok().entity("Producto desactivado exitosamente").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error al desactivar producto: " + e.getMessage()).build();
        }
    }
}
