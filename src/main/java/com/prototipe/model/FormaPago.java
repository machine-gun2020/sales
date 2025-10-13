package com.prototipe.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "FORMAS_PAGO")
public class FormaPago extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "formas_pago_seq")
    @SequenceGenerator(name = "formas_pago_seq", sequenceName = "FORMAS_PAGO_SEQ", allocationSize = 1)
    @Column(name = "ID_FORMA_PAGO")
    public Long idFormaPago;

    @Column(name = "DESCRIPCION", nullable = false, length = 50)
    public String descripcion;

    @Column(name = "ACTIVO", length = 1)
    public String activo = "S";

    @OneToMany(mappedBy = "formaPago", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<PagoVenta> pagosVenta;
}
