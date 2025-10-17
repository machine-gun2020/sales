package com.prototipe.controller;

import com.prototipe.service.ReporteService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.logging.Logger;

@Path("/reportes")
@Produces(MediaType.APPLICATION_JSON)
public class ReporteResource {

    private static final Logger LOG = Logger.getLogger(ReporteResource.class.getName());

    @Inject
    ReporteService reporteService;

    @GET
    @Path("/completo")
    public Response generarReporteCompleto() {
        try {
            LOG.info("📊 Generando reporte completo");
            var reporte = reporteService.generarReporteCompleto();

            LOG.info("✅ Reporte generado exitosamente");
            return Response.ok(reporte).build();

        } catch (Exception e) {
            LOG.severe("💥 Error al generar reporte: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of(
                            "error", "Error interno al generar reporte",
                            "mensaje", e.getMessage()
                    ))
                    .build();
        }
    }

    @GET
    @Path("/ventas")
    public Response reporteVentas() {
        try {
            var reporte = reporteService.generarReporteCompleto();
            return Response.ok(reporte.get("ventas")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/productos")
    public Response reporteProductos() {
        try {
            var reporte = reporteService.generarReporteCompleto();
            return Response.ok(Map.of(
                    "masVendidos", reporte.get("productosMasVendidos"),
                    "masDevueltos", reporte.get("productosMasDevueltos")
            )).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
