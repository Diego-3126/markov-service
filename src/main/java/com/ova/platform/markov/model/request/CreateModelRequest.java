package com.ova.platform.markov.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateModelRequest {

    @NotBlank(message = "El nombre del modelo es requerido")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @NotNull(message = "El orden del modelo es requerido")
    private Integer orden;

    private String textoEntrenamiento;


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
}