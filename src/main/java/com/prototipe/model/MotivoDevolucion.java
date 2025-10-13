package com.prototipe.model;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "MOTIVOS_DEVOLUCION")
public class MotivoDevolucion extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "motivos_devolucion_seq")
    @SequenceGenerator(name = "motivos_devolucion_seq", sequenceName = "MOTIVOS_DEVOLUCION_SEQ", allocationSize = 1)
    @Column(name = "ID_MOTIVO")
    public Long idMotivo;

    @Column(name = "CODIGO", nullable = false, unique = true, length = 10)
    public String codigo;

    @Column(name = "DESCRIPCION", nullable = false, length = 100)
    public String descripcion;

    @Column(name = "TIPO", length = 20)
    public String tipo;

    @Column(name = "REQUIERE_AUTORIZACION", length = 1)
    public String requiereAutorizacion = "N";

    @Column(name = "ACTIVO", length = 1)
    public String activo = "S";

    // ✅ CORREGIDO: mappedBy apunta a la propiedad 'motivoDevolucion' en DetalleDevolucion
    @OneToMany(mappedBy = "motivoDevolucion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<DetalleDevolucion> detallesDevolucion;
}