package com.ova.platform.markov.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;

public class MarkovGenerateRequest {

    @Size(max = 500, message = "El texto de inicio no puede exceder 500 caracteres")
    private String textoInicio;

    @Min(value = 10, message = "La longitud debe ser al menos 10 palabras")
    @Max(value = 500, message = "La longitud no puede exceder 500 palabras")
    private int longitud = 50;

    @Min(value = 1, message = "El orden debe ser al menos 1")
    @Max(value = 5, message = "El orden no puede exceder 5")
    private int orden = 2;


    public MarkovGenerateRequest() {
    }

    public MarkovGenerateRequest(String textoInicio, int longitud, int orden) {
        this.textoInicio = textoInicio;
        this.longitud = longitud;
        this.orden = orden;
    }


    public String getTextoInicio() {
        return textoInicio;
    }

    public void setTextoInicio(String textoInicio) {
        this.textoInicio = textoInicio;
    }

    public int getLongitud() {
        return longitud;
    }

    public void setLongitud(int longitud) {
        this.longitud = longitud;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }
}