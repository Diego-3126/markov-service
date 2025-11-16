package com.ova.platform.markov.model.response;

public class MarkovTrainResponse {
    private boolean exito;
    private String mensaje;
    private int orden;
    private int vocabularioSize;
    private int estadosGenerados;
    private int longitudTexto;
    private long tiempoProcesamientoMs;
    private String nombreModelo;

    public boolean isExito() { return exito; }
    public void setExito(boolean exito) { this.exito = exito; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }

    public int getVocabularioSize() { return vocabularioSize; }
    public void setVocabularioSize(int vocabularioSize) { this.vocabularioSize = vocabularioSize; }

    public int getEstadosGenerados() { return estadosGenerados; }
    public void setEstadosGenerados(int estadosGenerados) { this.estadosGenerados = estadosGenerados; }

    public int getLongitudTexto() { return longitudTexto; }
    public void setLongitudTexto(int longitudTexto) { this.longitudTexto = longitudTexto; }

    public long getTiempoProcesamientoMs() { return tiempoProcesamientoMs; }
    public void setTiempoProcesamientoMs(long tiempoProcesamientoMs) {
        this.tiempoProcesamientoMs = tiempoProcesamientoMs;
    }

    public String getNombreModelo() { return nombreModelo; }
    public void setNombreModelo(String nombreModelo) { this.nombreModelo = nombreModelo; }
}