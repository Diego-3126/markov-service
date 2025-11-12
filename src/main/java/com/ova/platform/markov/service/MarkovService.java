package com.ova.platform.markov.service;

import com.ova.platform.markov.model.request.MarkovGenerateRequest;
import com.ova.platform.markov.model.response.MarkovGenerateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


            String trainingText = "Las cadenas de Markov son modelos probabilísticos muy útiles " +
                    "para la generación de texto automático. Estos modelos aprenden " +
                    "patrones del lenguaje natural y pueden crear contenido nuevo " +
                    "basado en probabilidades estadísticas.";
            nativeService.trainModel(trainingText);


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

    public boolean isNativeIntegrationActive() {
        return nativeService.isNativeLibraryLoaded();
    }
}