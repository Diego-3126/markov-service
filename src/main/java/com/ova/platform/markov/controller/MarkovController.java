package com.ova.platform.markov.controller;

import com.ova.platform.markov.model.dto.ApiResponse;
import com.ova.platform.markov.model.request.MarkovGenerateRequest;
import com.ova.platform.markov.model.response.MarkovGenerateResponse;
import com.ova.platform.markov.service.MarkovService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/markov")
@Tag(name = "Generador Markov", description = "Endpoints para generación de texto con Cadenas de Markov")
public class MarkovController {

    private static final Logger logger = LoggerFactory.getLogger(MarkovController.class);

    @Autowired
    private MarkovService markovService;

    @PostMapping("/generate")
    @Operation(summary = "Generar texto automático",
            description = "Genera texto usando el modelo de Cadenas de Markov. " +
                    "Puede especificar texto inicial, longitud y orden del modelo.")
    public ResponseEntity<ApiResponse<MarkovGenerateResponse>> generarTexto(
            @Valid @RequestBody MarkovGenerateRequest request) {

        logger.info("Solicitud recibida para generar texto - Inicio: '{}', Longitud: {}",
                request.getTextoInicio(), request.getLongitud());

        MarkovGenerateResponse result = markovService.generarTexto(request);

        ApiResponse<MarkovGenerateResponse> response;
        if (result.isExito()) {
            response = ApiResponse.success(result, result.getMensaje());
            logger.info("Generación exitosa - Texto generado: {} palabras",
                    result.getLongitudGenerada());
        } else {
            response = ApiResponse.error(result.getMensaje());
            logger.warn("Generación fallida - Error: {}", result.getMensaje());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check del servicio Markov",
            description = "Verifica el estado del servicio y la integración con la librería nativa")
    public ResponseEntity<ApiResponse<Object>> healthCheck() {
        boolean nativeActive = markovService.isNativeIntegrationActive();

        var healthInfo = new Object() {
            public final String status = "UP";
            public final String service = "markov-service";
            public final boolean nativeIntegration = nativeActive;
            public final String nativeStatus = nativeActive ? "ACTIVE" : "SIMULATION";
            public final String timestamp = java.time.LocalDateTime.now().toString();
        };

        ApiResponse<Object> response = ApiResponse.success(
                healthInfo,
                nativeActive ? "Servicio Markov operativo con librería nativa" : "Servicio Markov en modo simulación"
        );

        return ResponseEntity.ok(response);
    }
}