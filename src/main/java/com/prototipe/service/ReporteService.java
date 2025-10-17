package com.prototipe.service;

import com.prototipe.model.*;
import com.prototipe.repository.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ReporteService {

    @Inject
    VentaRepository ventaRepository;

    @Inject
    DevolucionRepository devolucionRepository;

    @Inject
    DetalleVentaRepository detalleVentaRepository;

    @Inject
    ProductoRepository productoRepository;

    public Map<String, Object> generarReporteCompleto() {
        LocalDateTime ahora = LocalDateTime.now();
        YearMonth mesActual = YearMonth.from(ahora);
        YearMonth mesPasado = mesActual.minusMonths(1);

        return Map.of(
                "ventas", generarReporteVentas(mesPasado, mesActual),
                "cancelaciones", generarReporteCancelaciones(mesPasado, mesActual),
                "ganancias", generarReporteGanancias(mesPasado, mesActual),
                "productosMasVendidos", obtenerProductosMasVendidos(),
                "productosMasDevueltos", obtenerProductosMasDevueltos(),
                "fechaGeneracion", ahora,
                "periodo", Map.of(
                        "mesActual", mesActual.toString(),
                        "mesPasado", mesPasado.toString()
                )
        );
    }

    private Map<String, Object> generarReporteVentas(YearMonth mesPasado, YearMonth mesActual) {
        List<Venta> todasVentas = ventaRepository.listAll();

        BigDecimal ventasMesActual = todasVentas.stream()
                .filter(venta -> estaEnMes(venta.fechaVenta, mesActual))
                .filter(venta -> !"CANCELADA".equals(venta.estado))
                .map(venta -> venta.total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ventasMesPasado = todasVentas.stream()
                .filter(venta -> estaEnMes(venta.fechaVenta, mesPasado))
                .filter(venta -> !"CANCELADA".equals(venta.estado))
                .map(venta -> venta.total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
                "mesActual", ventasMesActual,
                "mesPasado", ventasMesPasado,
                "tendencia", calcularTendencia(ventasMesPasado, ventasMesActual)
        );
    }

    private Map<String, Object> generarReporteCancelaciones(YearMonth mesPasado, YearMonth mesActual) {
        List<Venta> todasVentas = ventaRepository.listAll();

        long cancelacionesMesActual = todasVentas.stream()
                .filter(venta -> estaEnMes(venta.fechaVenta, mesActual))
                .filter(venta -> "CANCELADA".equals(venta.estado))
                .count();

        long cancelacionesMesPasado = todasVentas.stream()
                .filter(venta -> estaEnMes(venta.fechaVenta, mesPasado))
                .filter(venta -> "CANCELADA".equals(venta.estado))
                .count();

        return Map.of(
                "mesActual", cancelacionesMesActual,
                "mesPasado", cancelacionesMesPasado,
                "tendencia", calcularTendencia(
                        BigDecimal.valueOf(cancelacionesMesPasado),
                        BigDecimal.valueOf(cancelacionesMesActual)
                )
        );
    }

    private Map<String, Object> generarReporteGanancias(YearMonth mesPasado, YearMonth mesActual) {
        List<Venta> todasVentas = ventaRepository.listAll();

        // Ganancias = (Ventas - Cancelaciones)
        BigDecimal gananciasMesActual = todasVentas.stream()
                .filter(venta -> estaEnMes(venta.fechaVenta, mesActual))
                .filter(venta -> !"CANCELADA".equals(venta.estado))
                .map(this::calcularGananciaVenta)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal gananciasMesPasado = todasVentas.stream()
                .filter(venta -> estaEnMes(venta.fechaVenta, mesPasado))
                .filter(venta -> !"CANCELADA".equals(venta.estado))
                .map(this::calcularGananciaVenta)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
                "mesActual", gananciasMesActual,
                "mesPasado", gananciasMesPasado,
                "tendencia", calcularTendencia(gananciasMesPasado, gananciasMesActual)
        );
    }

    private List<Map<String, Object>> obtenerProductosMasVendidos() {
        return detalleVentaRepository.streamAll()
                .filter(detalle -> !"CANCELADA".equals(detalle.venta.estado))
                .collect(Collectors.groupingBy(
                        detalle -> detalle.producto,
                        Collectors.summingInt(detalle -> detalle.cantidad - detalle.cantidadDevuelta)
                ))
                .entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .map(entry -> Map.of(
                        "producto", Map.of(
                                "id", entry.getKey().idProducto,
                                "nombre", entry.getKey().nombre,
                                "codigo", entry.getKey().codigo
                        ),
                        "cantidadVendida", entry.getValue(),
                        "totalVendido", calcularTotalVendidoProducto(entry.getKey())
                ))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> obtenerProductosMasDevueltos() {
        return detalleVentaRepository.streamAll()
                .filter(detalle -> detalle.cantidadDevuelta > 0)
                .collect(Collectors.groupingBy(
                        detalle -> detalle.producto,
                        Collectors.summingInt(DetalleVenta::getCantidadDevuelta)
                ))
                .entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .map(entry -> Map.of(
                        "producto", Map.of(
                                "id", entry.getKey().idProducto,
                                "nombre", entry.getKey().nombre,
                                "codigo", entry.getKey().codigo
                        ),
                        "cantidadDevuelta", entry.getValue(),
                        "tasaDevolucion", calcularTasaDevolucion(entry.getKey())
                ))
                .collect(Collectors.toList());
    }

    // MÉTODOS AUXILIARES CON STREAM API
    private boolean estaEnMes(LocalDateTime fecha, YearMonth mes) {
        return fecha != null && YearMonth.from(fecha).equals(mes);
    }

    private BigDecimal calcularGananciaVenta(Venta venta) {
        return venta.detalles.stream()
                .map(detalle -> {
                    BigDecimal costo = detalle.producto.costo != null ? detalle.producto.costo : BigDecimal.ZERO;
                    BigDecimal cantidadNeta = BigDecimal.valueOf(detalle.cantidad - detalle.cantidadDevuelta);
                    return (detalle.precioUnitario.subtract(costo)).multiply(cantidadNeta);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularTotalVendidoProducto(Producto producto) {
        return detalleVentaRepository.streamAll()
                .filter(detalle -> detalle.producto.equals(producto))
                .filter(detalle -> !"CANCELADA".equals(detalle.venta.estado))
                .map(detalle -> {
                    int cantidadNeta = detalle.cantidad - detalle.cantidadDevuelta;
                    return detalle.precioUnitario.multiply(BigDecimal.valueOf(cantidadNeta));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularTasaDevolucion(Producto producto) {
        long totalVendido = detalleVentaRepository.streamAll()
                .filter(detalle -> detalle.producto.equals(producto))
                .filter(detalle -> !"CANCELADA".equals(detalle.venta.estado))
                .mapToInt(detalle -> detalle.cantidad)
                .sum();

        long totalDevuelto = detalleVentaRepository.streamAll()
                .filter(detalle -> detalle.producto.equals(producto))
                .mapToInt(DetalleVenta::getCantidadDevuelta)
                .sum();

        if (totalVendido == 0) return BigDecimal.ZERO;

        return BigDecimal.valueOf(totalDevuelto)
                .divide(BigDecimal.valueOf(totalVendido), 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private String calcularTendencia(BigDecimal valorAnterior, BigDecimal valorActual) {
        if (valorAnterior.compareTo(BigDecimal.ZERO) == 0) {
            return valorActual.compareTo(BigDecimal.ZERO) > 0 ? "📈" : "➡️";
        }

        BigDecimal diferencia = valorActual.subtract(valorAnterior);
        BigDecimal porcentaje = diferencia.divide(valorAnterior, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        if (porcentaje.compareTo(BigDecimal.valueOf(10)) > 0) return "📈↑↑";
        if (porcentaje.compareTo(BigDecimal.valueOf(5)) > 0) return "📈↑";
        if (porcentaje.compareTo(BigDecimal.valueOf(-5)) < 0) return "📉↓";
        if (porcentaje.compareTo(BigDecimal.valueOf(-10)) < 0) return "📉↓↓";
        return "➡️";
    }
}
