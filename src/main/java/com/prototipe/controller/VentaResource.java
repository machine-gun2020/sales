package com.prototipe.controller;


import com.prototipe.model.Venta;
import com.prototipe.service.VentaService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/ventas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VentaResource {

    @Inject
    VentaService ventaService;

    @GET
    public List<Venta> obtenerTodasVentas() {
        return ventaService.obtenerTodasVentas();
    }

    @GET
    @Path("/{id}")
    public Response obtenerVenta(@PathParam("id") Long id) {
        Venta venta = ventaService.obtenerVentaPorId(id);
        if (venta == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(venta).build();
    }

    @POST
    public Response crearVenta(Venta venta) {
        try {
            Venta nuevaVenta = ventaService.crearVenta(venta);
            return Response.status(Response.Status.CREATED).entity(nuevaVenta).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error al crear venta: " + e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}/cancelar")
    public Response cancelarVenta(@PathParam("id") Long id) {
        try {
            ventaService.cancelarVenta(id);
            return Response.ok().entity("Venta cancelada exitosamente").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error al cancelar venta: " + e.getMessage()).build();
        }
    }
}
