package com.ova.platform.markov.controller;

import com.ova.platform.markov.model.dto.ApiResponse;
import com.ova.platform.markov.model.request.CreateModelRequest;
import com.ova.platform.markov.model.request.MarkovGenerateRequest;
import com.ova.platform.markov.model.response.MarkovGenerateResponse;
import com.ova.platform.markov.model.response.ModelResponse;
import com.ova.platform.markov.service.MarkovService;
import com.ova.platform.markov.service.ModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/markov")
@Tag(name = "Generador Markov", description = "Endpoints para gestión y generación de texto con Cadenas de Markov")
public class MarkovController {

    private static final Logger logger = LoggerFactory.getLogger(MarkovController.class);

    @Autowired
    private MarkovService markovService;

    @Autowired
    private ModelService modelService;

    // ✅ ENDPOINT EXISTENTE - HU-302
    @PostMapping("/generate")
    @Operation(summary = "Generar texto automático",
            description = "Genera texto usando el modelo de Cadenas de Markov.")
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

    // ✅ NUEVOS ENDPOINTS CRUD

    @GetMapping("/models")
    @Operation(summary = "Obtener todos los modelos",
            description = "Retorna la lista de todos los modelos Markov guardados")
    public ResponseEntity<ApiResponse<List<ModelResponse>>> getAllModels() {
        List<ModelResponse> models = modelService.getAllModels();
        ApiResponse<List<ModelResponse>> response = ApiResponse.success(
                models,
                models.isEmpty() ? "No hay modelos guardados" : "Modelos obtenidos exitosamente"
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/models/{id}")
    @Operation(summary = "Obtener modelo por ID",
            description = "Retorna un modelo Markov específico por su ID")
    public ResponseEntity<ApiResponse<ModelResponse>> getModelById(@PathVariable Long id) {
        return modelService.getModelById(id)
                .map(model -> {
                    ApiResponse<ModelResponse> response = ApiResponse.success(
                            model, "Modelo obtenido exitosamente"
                    );
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/models")
    @Operation(summary = "Crear nuevo modelo",
            description = "Crea un nuevo modelo Markov con los parámetros especificados")
    public ResponseEntity<ApiResponse<ModelResponse>> createModel(
            @Valid @RequestBody CreateModelRequest request) {
        try {
            ModelResponse newModel = modelService.createModel(request);
            ApiResponse<ModelResponse> response = ApiResponse.success(
                    newModel, "Modelo creado exitosamente"
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ApiResponse<ModelResponse> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/models/{id}")
    @Operation(summary = "Actualizar modelo existente",
            description = "Actualiza un modelo Markov existente")
    public ResponseEntity<ApiResponse<ModelResponse>> updateModel(
            @PathVariable Long id,
            @Valid @RequestBody CreateModelRequest request) {
        return modelService.updateModel(id, request)
                .map(updatedModel -> {
                    ApiResponse<ModelResponse> response = ApiResponse.success(
                            updatedModel, "Modelo actualizado exitosamente"
                    );
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/models/{id}")
    @Operation(summary = "Eliminar modelo",
            description = "Elimina un modelo Markov por su ID")
    public ResponseEntity<ApiResponse<Object>> deleteModel(@PathVariable Long id) {
        boolean deleted = modelService.deleteModel(id);
        if (deleted) {
            ApiResponse<Object> response = ApiResponse.success(
                    null, "Modelo eliminado exitosamente"
            );
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Object> response = ApiResponse.error("Modelo no encontrado");
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Health check del servicio Markov")
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