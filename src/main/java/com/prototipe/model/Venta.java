package com.prototipe.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "VENTAS")
public class Venta extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ventas_seq")
    @SequenceGenerator(name = "ventas_seq", sequenceName = "VENTAS_SEQ", allocationSize = 1)
    @Column(name = "ID_VENTA")
    public Long idVenta;

    @Column(name = "FOLIO", nullable = false, unique = true, length = 20)
    public String folio;

    @Column(name = "FECHA_VENTA", nullable = false)
    public LocalDateTime fechaVenta;

    // ✅ Asegurar que la relación con Cliente esté correcta
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CLIENTE", nullable = false)
    public Cliente cliente;

    @Column(name = "SUBTOTAL", precision = 10, scale = 2)
    public BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "IVA", precision = 10, scale = 2)
    public BigDecimal iva = BigDecimal.ZERO;

    @Column(name = "TOTAL", precision = 10, scale = 2)
    public BigDecimal total = BigDecimal.ZERO;

    @Column(name = "ESTADO", length = 20)
    public String estado = "ACTIVA";

    // ✅ Inicializar las listas
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    public List<DetalleVenta> detalles = new ArrayList<>();

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    public List<PagoVenta> pagos;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    public List<Devolucion> devoluciones;
}
