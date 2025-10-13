package com.prototipe.model;



import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "DETALLE_DEVOLUCIONES")
public class DetalleDevolucion extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "detalle_devoluciones_seq")
    @SequenceGenerator(name = "detalle_devoluciones_seq", sequenceName = "DETALLE_DEVOLUCIONES_SEQ", allocationSize = 1)
    @Column(name = "ID_DETALLE_DEVOLUCION")
    public Long idDetalleDevolucion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DEVOLUCION", nullable = false)
    public Devolucion devolucion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DETALLE_VENTA", nullable = false)
    public DetalleVenta detalleVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PRODUCTO", nullable = false)
    public Producto producto;

    // ✅ AGREGAR ESTA RELACIÓN QUE FALTABA
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_MOTIVO")
    public MotivoDevolucion motivoDevolucion;

    @Column(name = "CANTIDAD_DEVUELTA", nullable = false)
    public Integer cantidadDevuelta;

    @Column(name = "MOTIVO_ESPECIFICO", nullable = false, length = 200)
    public String motivoEspecifico;

    @Column(name = "TIPO_MOTIVO", length = 50)
    public String tipoMotivo;

    @Column(name = "REINGRESAR_INVENTARIO", length = 1)
    public String reingresarInventario = "S";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PRODUCTO_CAMBIO")
    public Producto productoCambio;

    @Column(name = "CANTIDAD_CAMBIO")
    public Integer cantidadCambio;
}