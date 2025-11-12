package com.ova.platform.markov.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "markov_models")
public class MarkovModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(name = "orden_modelo", nullable = false)
    private Integer orden;

    @Column(name = "texto_entrenamiento", columnDefinition = "TEXT")
    private String textoEntrenamiento;

    @Column(name = "vocabulario_size")
    private Integer vocabularioSize;

    @Column(name = "estados_count")
    private Integer estadosCount;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "estado")
    private String estado = "ACTIVO"; // ACTIVO, INACTIVO, ENTRENANDO


    public MarkovModel() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    public MarkovModel(String nombre, String descripcion, Integer orden) {
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.orden = orden;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }

    public String getTextoEntrenamiento() { return textoEntrenamiento; }
    public void setTextoEntrenamiento(String textoEntrenamiento) {
        this.textoEntrenamiento = textoEntrenamiento;
    }

    public Integer getVocabularioSize() { return vocabularioSize; }
    public void setVocabularioSize(Integer vocabularioSize) { this.vocabularioSize = vocabularioSize; }

    public Integer getEstadosCount() { return estadosCount; }
    public void setEstadosCount(Integer estadosCount) { this.estadosCount = estadosCount; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }


    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}