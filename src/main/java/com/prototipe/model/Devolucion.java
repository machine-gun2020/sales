package com.prototipe.model;



import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "DEVOLUCIONES")
public class Devolucion extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "devoluciones_seq")
    @SequenceGenerator(name = "devoluciones_seq", sequenceName = "DEVOLUCIONES_SEQ", allocationSize = 1)
    @Column(name = "ID_DEVOLUCION")
    public Long idDevolucion;

    @Column(name = "FOLIO_DEVOLUCION", nullable = false, unique = true, length = 20)
    public String folioDevolucion;

    @Column(name = "FECHA_DEVOLUCION")
    public LocalDateTime fechaDevolucion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_VENTA", nullable = false)
    public Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CLIENTE", nullable = false)
    public Cliente cliente;

    @Column(name = "TOTAL_DEVOLUCION", precision = 10, scale = 2)
    public BigDecimal totalDevolucion = BigDecimal.ZERO;

    @Column(name = "ESTADO", length = 20)
    public String estado = "PENDIENTE";

    @Column(name = "MOTIVO_GENERAL", length = 500)
    public String motivoGeneral;

    @OneToMany(mappedBy = "devolucion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<DetalleDevolucion> detalles;
}
