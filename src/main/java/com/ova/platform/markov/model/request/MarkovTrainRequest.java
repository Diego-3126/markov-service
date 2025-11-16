package com.ova.platform.markov.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MarkovTrainRequest {

    @NotBlank(message = "El texto de entrenamiento es requerido")
    @Size(min = 50, message = "El texto de entrenamiento debe tener al menos 50 caracteres")
    private String textoEntrenamiento;

    @Min(value = 1, message = "El orden debe ser al menos 1")
    @Max(value = 5, message = "El orden no puede exceder 5")
    private int orden = 2;

    private String nombreModelo;

    // Getters and Setters
    public String getTextoEntrenamiento() { return textoEntrenamiento; }
    public void setTextoEntrenamiento(String textoEntrenamiento) {
        this.textoEntrenamiento = textoEntrenamiento;
    }

    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }

    public String getNombreModelo() { return nombreModelo; }
    public void setNombreModelo(String nombreModelo) { this.nombreModelo = nombreModelo; }
}