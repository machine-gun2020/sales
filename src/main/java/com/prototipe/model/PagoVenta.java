package com.prototipe.model;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PAGOS_VENTAS")
public class PagoVenta extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pagos_ventas_seq")
    @SequenceGenerator(name = "pagos_ventas_seq", sequenceName = "PAGOS_VENTAS_SEQ", allocationSize = 1)
    @Column(name = "ID_PAGO")
    public Long idPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_VENTA", nullable = false)
    public Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FORMA_PAGO", nullable = false)
    public FormaPago formaPago;

    @Column(name = "MONTO", nullable = false, precision = 10, scale = 2)
    public BigDecimal monto;

    @Column(name = "FECHA_PAGO")
    public LocalDateTime fechaPago;

    @Column(name = "REFERENCIA", length = 50)
    public String referencia;
}
