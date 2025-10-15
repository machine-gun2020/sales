package com.prototipe.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "DETALLE_VENTAS")
public class DetalleVenta extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "detalle_ventas_seq")
    @SequenceGenerator(name = "detalle_ventas_seq", sequenceName = "DETALLE_VENTAS_SEQ", allocationSize = 1)
    @Column(name = "ID_DETALLE")
    public Long idDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_VENTA", nullable = false)
    @JsonIgnore
    public Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PRODUCTO", nullable = false)
    @JsonIgnore
    public Producto producto;

    @Column(name = "CANTIDAD", nullable = false)
    public Integer cantidad;

    @Column(name = "PRECIO_UNITARIO", nullable = false, precision = 10, scale = 2)
    public BigDecimal precioUnitario;

    @Column(name = "IMPORTE", nullable = false, precision = 10, scale = 2)
    public BigDecimal importe;

    @Column(name = "CANTIDAD_DEVUELTA")
    public Integer cantidadDevuelta = 0;

    @Column(name = "MOTIVO_DEVOLUCION", length = 200)
    public String motivoDevolucion;

    @Column(name = "FECHA_DEVOLUCION")
    public LocalDateTime fechaDevolucion;

    @Column(name = "ESTADO", length = 20)
    public String estado = "VENDIDO";
}
