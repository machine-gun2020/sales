package com.prototipe.controller;


import com.prototipe.model.DetalleVenta;
import com.prototipe.model.Devolucion;
import com.prototipe.model.Producto;
import com.prototipe.model.Venta;
import com.prototipe.repository.ProductoRepository;
import com.prototipe.service.VentaService;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;

@Path("/ventas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VentaResource {

    @Inject
    VentaService ventaService;

    @Inject
    ProductoRepository productoRepository;

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
    @Transactional
    public Response cancelarVenta(@PathParam("id") Long id) {
        try {
            Venta venta = ventaService.obtenerVentaPorId(id);
            if (venta == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Venta no encontrada").build();
            }

            // ✅ Validar que la venta no esté ya cancelada
            if ("CANCELADA".equals(venta.estado)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("La venta ya está cancelada").build();
            }

            System.out.println("=== CANCELANDO VENTA ID: " + id + " ===");

            // ✅ 1. REINTEGRAR INVENTARIO
            for (DetalleVenta detalle : venta.detalles) {
                if (detalle.producto != null) {
                    Producto producto = productoRepository.findById(detalle.producto.idProducto);
                    if (producto != null) {
                        int cantidadAnterior = producto.existencia;
                        producto.existencia += detalle.cantidad;

                        System.out.println("Inventario reintegrado - Producto: " + producto.nombre +
                                ", Anterior: " + cantidadAnterior +
                                ", Nuevo: " + producto.existencia);
                    }
                }
            }

            // ✅ 2. ACTUALIZAR ESTADO DE LA VENTA
            venta.estado = "CANCELADA";

            // ✅ 3. CREAR DEVOLUCIÓN (opcional)
            crearDevolucionPorCancelacion(venta);

            System.out.println("=== VENTA CANCELADA EXITOSAMENTE ===");
            return Response.ok().entity("Cancelación exitosa - Inventario reintegrado").build();

        } catch (Exception e) {
            System.err.println("=== ERROR AL CANCELAR VENTA ===");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al cancelar venta: " + e.getMessage()).build();
        }
    }

    private void crearDevolucionPorCancelacion(Venta venta) {
        try {
            Devolucion devolucion = new Devolucion();

            // ✅ FOLIO MÁS CORTO que cumpla con 20 caracteres máximo
            String timestamp = String.valueOf(System.currentTimeMillis());
            String folioCorto = "DC-" + venta.folio + "-" + timestamp.substring(timestamp.length() - 4);

            // ✅ Asegurar que no exceda 20 caracteres
            if (folioCorto.length() > 20) {
                folioCorto = folioCorto.substring(0, 20);
            }

            devolucion.folioDevolucion = folioCorto;
            devolucion.fechaDevolucion = LocalDateTime.now();
            devolucion.venta = venta;
            devolucion.cliente = venta.cliente;
            devolucion.totalDevolucion = venta.total;
            devolucion.estado = "PROCESADA";
            devolucion.motivoGeneral = "Cancelación de venta";

            devolucion.persist();
            System.out.println("Devolución creada: " + devolucion.folioDevolucion);

        } catch (Exception e) {
            System.err.println("Error al crear devolución por cancelación: " + e.getMessage());
        }
    }
}
