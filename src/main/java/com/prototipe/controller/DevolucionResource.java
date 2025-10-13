package com.prototipe.controller;


import com.prototipe.model.Devolucion;
import com.prototipe.service.DevolucionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/devoluciones")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DevolucionResource {

    @Inject
    DevolucionService devolucionService;

    @POST
    public Response crearDevolucion(Devolucion devolucion) {
        try {
            Devolucion nuevaDevolucion = devolucionService.crearDevolucion(devolucion);
            return Response.status(Response.Status.CREATED).entity(nuevaDevolucion).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error al crear devolución: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/venta/{idVenta}")
    public List<Devolucion> obtenerDevolucionesPorVenta(@PathParam("idVenta") Long idVenta) {
        return devolucionService.obtenerDevolucionesPorVenta(idVenta);
    }
}
