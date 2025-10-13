package com.prototipe.model;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "EMPLEADOS")
public class Empleado extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "empleados_seq")
    @SequenceGenerator(name = "empleados_seq", sequenceName = "EMPLEADOS_SEQ", allocationSize = 1)
    @Column(name = "ID_EMPLEADO")
    public Long idEmpleado;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    public String nombre;

    @Column(name = "USUARIO", unique = true, length = 50)
    public String usuario;

    @Column(name = "PUESTO", length = 50)
    public String puesto;

    @Column(name = "ACTIVO", length = 1)
    public String activo = "S";
}
