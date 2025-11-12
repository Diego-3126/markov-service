package com.ova.platform.markov.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MarkovGenerateResponse {
    private String textoGenerado;
    private String textoInicio;
    private int longitudSolicitada;
    private int longitudGenerada;
    private long tiempoProcesamientoMs;
    private String modeloUtilizado;
    private boolean exito;
    private String mensaje;


    public MarkovGenerateResponse() {
    }

    public MarkovGenerateResponse(boolean exito, String mensaje) {
        this.exito = exito;
        this.mensaje = mensaje;
    }


    public String getTextoGenerado() {
        return textoGenerado;
    }

    public void setTextoGenerado(String textoGenerado) {
        this.textoGenerado = textoGenerado;
    }

    public String getTextoInicio() {
        return textoInicio;
    }

    public void setTextoInicio(String textoInicio) {
        this.textoInicio = textoInicio;
    }

    public int getLongitudSolicitada() {
        return longitudSolicitada;
    }

    public void setLongitudSolicitada(int longitudSolicitada) {
        this.longitudSolicitada = longitudSolicitada;
    }

    public int getLongitudGenerada() {
        return longitudGenerada;
    }

    public void setLongitudGenerada(int longitudGenerada) {
        this.longitudGenerada = longitudGenerada;
    }

    public long getTiempoProcesamientoMs() {
        return tiempoProcesamientoMs;
    }

    public void setTiempoProcesamientoMs(long tiempoProcesamientoMs) {
        this.tiempoProcesamientoMs = tiempoProcesamientoMs;
    }

    public String getModeloUtilizado() {
        return modeloUtilizado;
    }

    public void setModeloUtilizado(String modeloUtilizado) {
        this.modeloUtilizado = modeloUtilizado;
    }

    public boolean isExito() {
        return exito;
    }

    public void setExito(boolean exito) {
        this.exito = exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}