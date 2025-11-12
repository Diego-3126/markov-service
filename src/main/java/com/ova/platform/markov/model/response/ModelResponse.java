package com.ova.platform.markov.model.response;

import java.time.LocalDateTime;

public class ModelResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private Integer orden;
    private Integer vocabularioSize;
    private Integer estadosCount;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String estado;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }

    public Integer getVocabularioSize() { return vocabularioSize; }
    public void setVocabularioSize(Integer vocabularioSize) { this.vocabularioSize = vocabularioSize; }

    public Integer getEstadosCount() { return estadosCount; }
    public void setEstadosCount(Integer estadosCount) { this.estadosCount = estadosCount; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}