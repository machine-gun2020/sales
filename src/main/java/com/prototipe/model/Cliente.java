package com.prototipe.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CLIENTES")
public class Cliente extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clientes_seq")
    @SequenceGenerator(name = "clientes_seq", sequenceName = "CLIENTES_SEQ", allocationSize = 1)
    @Column(name = "ID_CLIENTE")
    public Long idCliente;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    public String nombre;

    @Column(name = "RFC", length = 13)
    public String rfc;

    @Column(name = "DIRECCION", length = 200)
    public String direccion;

    @Column(name = "TELEFONO", length = 15)
    public String telefono;

    @Column(name = "EMAIL", length = 100)
    public String email;

    @Column(name = "FECHA_REGISTRO")
    public LocalDateTime fechaRegistro;

    @Column(name = "ACTIVO", length = 1)
    public String activo = "S";
}