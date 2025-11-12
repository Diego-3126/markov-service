package com.ova.platform.markov.service;

import com.ova.platform.markov.model.entity.MarkovModel;
import com.ova.platform.markov.model.request.CreateModelRequest;
import com.ova.platform.markov.model.response.ModelResponse;
import com.ova.platform.markov.repository.MarkovModelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ModelService {

    private static final Logger logger = LoggerFactory.getLogger(ModelService.class);

    @Autowired
    private MarkovModelRepository modelRepository;

    @Autowired
    private MarkovNativeService nativeService;

    public List<ModelResponse> getAllModels() {
        List<MarkovModel> models = modelRepository.findAll();
        return models.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public Optional<ModelResponse> getModelById(Long id) {
        return modelRepository.findById(id)
                .map(this::convertToResponse);
    }

    public ModelResponse createModel(CreateModelRequest request) {
        // Verificar si ya existe un modelo con ese nombre
        if (modelRepository.existsByNombre(request.getNombre())) {
            throw new IllegalArgumentException("Ya existe un modelo con el nombre: " + request.getNombre());
        }

        MarkovModel model = new MarkovModel();
        model.setNombre(request.getNombre());
        model.setDescripcion(request.getDescripcion());
        model.setOrden(request.getOrden());
        model.setTextoEntrenamiento(request.getTextoEntrenamiento());
        model.setEstado("ACTIVO");

        // Si hay texto de entrenamiento, entrenar el modelo nativo
        if (request.getTextoEntrenamiento() != null && !request.getTextoEntrenamiento().trim().isEmpty()) {
            try {
                nativeService.initializeModel(request.getOrden());
                nativeService.trainModel(request.getTextoEntrenamiento());

                // Simular estadísticas (en una implementación real, obtendrías esto de la librería nativa)
                model.setVocabularioSize(calculateVocabularySize(request.getTextoEntrenamiento()));
                model.setEstadosCount(calculateStatesCount(request.getTextoEntrenamiento(), request.getOrden()));

            } catch (Exception e) {
                logger.error("Error entrenando modelo durante creación", e);
                model.setEstado("ERROR");
            }
        }

        MarkovModel savedModel = modelRepository.save(model);
        logger.info("Modelo creado exitosamente: {}", savedModel.getNombre());

        return convertToResponse(savedModel);
    }

    public Optional<ModelResponse> updateModel(Long id, CreateModelRequest request) {
        return modelRepository.findById(id).map(existingModel -> {
            existingModel.setNombre(request.getNombre());
            existingModel.setDescripcion(request.getDescripcion());
            existingModel.setOrden(request.getOrden());

            if (request.getTextoEntrenamiento() != null) {
                existingModel.setTextoEntrenamiento(request.getTextoEntrenamiento());


                try {
                    nativeService.initializeModel(request.getOrden());
                    nativeService.trainModel(request.getTextoEntrenamiento());

                    existingModel.setVocabularioSize(calculateVocabularySize(request.getTextoEntrenamiento()));
                    existingModel.setEstadosCount(calculateStatesCount(request.getTextoEntrenamiento(), request.getOrden()));
                    existingModel.setEstado("ACTIVO");

                } catch (Exception e) {
                    logger.error("Error re-entrenando modelo", e);
                    existingModel.setEstado("ERROR");
                }
            }

            MarkovModel updatedModel = modelRepository.save(existingModel);
            logger.info("Modelo actualizado: {}", updatedModel.getNombre());

            return convertToResponse(updatedModel);
        });
    }

    public boolean deleteModel(Long id) {
        Optional<MarkovModel> model = modelRepository.findById(id);
        if (model.isPresent()) {
            modelRepository.deleteById(id);
            logger.info("Modelo eliminado: {}", model.get().getNombre());
            return true;
        }
        return false;
    }

    private ModelResponse convertToResponse(MarkovModel model) {
        ModelResponse response = new ModelResponse();
        response.setId(model.getId());
        response.setNombre(model.getNombre());
        response.setDescripcion(model.getDescripcion());
        response.setOrden(model.getOrden());
        response.setVocabularioSize(model.getVocabularioSize());
        response.setEstadosCount(model.getEstadosCount());
        response.setFechaCreacion(model.getFechaCreacion());
        response.setFechaActualizacion(model.getFechaActualizacion());
        response.setEstado(model.getEstado());
        return response;
    }


    private int calculateVocabularySize(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        return (int) text.toLowerCase().split("\\s+").length;
    }

    private int calculateStatesCount(String text, int order) {
        if (text == null || text.trim().isEmpty()) return 0;
        String[] words = text.toLowerCase().split("\\s+");
        return Math.max(0, words.length - order);
    }
}