package com.prototipe.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "PRODUCTOS")
public class Producto extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "productos_seq")
    @SequenceGenerator(name = "productos_seq", sequenceName = "PRODUCTOS_SEQ", allocationSize = 1)
    @Column(name = "ID_PRODUCTO")
    public Long idProducto;

    @Column(name = "CODIGO", nullable = false, unique = true, length = 20)
    public String codigo;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    public String nombre;

    @Column(name = "DESCRIPCION", length = 500)
    public String descripcion;

    @Column(name = "PRECIO_VENTA", nullable = false, precision = 10, scale = 2)
    public BigDecimal precioVenta;

    @Column(name = "COSTO", precision = 10, scale = 2)
    public BigDecimal costo;

    @Column(name = "EXISTENCIA")
    public Integer existencia = 0;

    @Column(name = "CATEGORIA", length = 50)
    public String categoria;

    @Column(name = "ACTIVO", length = 1)
    public String activo = "S";
}
