package com.ova.platform.markov.service;

import com.ova.platform.markov.model.request.MarkovGenerateRequest;
import com.ova.platform.markov.model.request.MarkovTrainRequest;
import com.ova.platform.markov.model.response.MarkovGenerateResponse;
import com.ova.platform.markov.model.response.MarkovTrainResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class MarkovService {

    private static final Logger logger = LoggerFactory.getLogger(MarkovService.class);

    @Autowired
    private MarkovNativeService nativeService;

    public MarkovGenerateResponse generarTexto(MarkovGenerateRequest request) {
        long startTime = System.currentTimeMillis();
        MarkovGenerateResponse response = new MarkovGenerateResponse();

        try {
            logger.info("Iniciando generación de texto - Inicio: '{}', Longitud: {}, Orden: {}",
                    request.getTextoInicio(), request.getLongitud(), request.getOrden());

            nativeService.initializeModel(request.getOrden());

            String textoGenerado = nativeService.generateText(
                    request.getLongitud(),
                    request.getTextoInicio()
            );

            long endTime = System.currentTimeMillis();

            response.setExito(true);
            response.setTextoGenerado(textoGenerado);
            response.setTextoInicio(request.getTextoInicio());
            response.setLongitudSolicitada(request.getLongitud());
            response.setLongitudGenerada(textoGenerado.split("\\s+").length);
            response.setTiempoProcesamientoMs(endTime - startTime);
            response.setModeloUtilizado("markov-order-" + request.getOrden());
            response.setMensaje("Texto generado exitosamente");

            logger.info("Generación completada - Tiempo: {}ms, Longitud generada: {}",
                    response.getTiempoProcesamientoMs(), response.getLongitudGenerada());

        } catch (Exception e) {
            logger.error("Error en generación de texto Markov", e);
            response.setExito(false);
            response.setMensaje("Error generando texto: " + e.getMessage());
            response.setTiempoProcesamientoMs(System.currentTimeMillis() - startTime);
        }

        return response;
    }

    public MarkovTrainResponse entrenarModelo(MarkovTrainRequest request) {
        long startTime = System.currentTimeMillis();
        MarkovTrainResponse response = new MarkovTrainResponse();

        try {
            logger.info("Iniciando entrenamiento de modelo - Orden: {}, Texto longitud: {}",
                    request.getOrden(), request.getTextoEntrenamiento().length());

            nativeService.initializeModel(request.getOrden());
            nativeService.trainModel(request.getTextoEntrenamiento());

            long endTime = System.currentTimeMillis();

            int vocabularioSize = calcularTamanoVocabulario(request.getTextoEntrenamiento());
            int estadosGenerados = calcularEstadosGenerados(request.getTextoEntrenamiento(), request.getOrden());

            response.setExito(true);
            response.setOrden(request.getOrden());
            response.setVocabularioSize(vocabularioSize);
            response.setEstadosGenerados(estadosGenerados);
            response.setLongitudTexto(request.getTextoEntrenamiento().length());
            response.setTiempoProcesamientoMs(endTime - startTime);
            response.setNombreModelo(request.getNombreModelo());
            response.setMensaje("Modelo entrenado exitosamente con " + vocabularioSize + " palabras de vocabulario");

            logger.info("Entrenamiento completado - Vocabulario: {}, Estados: {}, Tiempo: {}ms",
                    vocabularioSize, estadosGenerados, response.getTiempoProcesamientoMs());

        } catch (Exception e) {
            logger.error("Error en entrenamiento de modelo Markov", e);
            response.setExito(false);
            response.setMensaje("Error entrenando modelo: " + e.getMessage());
            response.setTiempoProcesamientoMs(System.currentTimeMillis() - startTime);
        }

        return response;
    }

    private int calcularTamanoVocabulario(String texto) {
        if (texto == null || texto.trim().isEmpty()) return 0;
        return (int) Arrays.stream(texto.toLowerCase().split("\\s+"))
                .distinct()
                .count();
    }

    private int calcularEstadosGenerados(String texto, int orden) {
        if (texto == null || texto.trim().isEmpty()) return 0;
        String[] palabras = texto.toLowerCase().split("\\s+");
        return Math.max(0, palabras.length - orden);
    }

    public boolean isNativeIntegrationActive() {
        return nativeService.isNativeLibraryLoaded();
    }
}