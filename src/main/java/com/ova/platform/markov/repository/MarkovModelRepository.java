package com.ova.platform.markov.repository;

import com.ova.platform.markov.model.entity.MarkovModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarkovModelRepository extends JpaRepository<MarkovModel, Long> {

    Optional<MarkovModel> findByNombre(String nombre);

    List<MarkovModel> findByEstado(String estado);

    @Query("SELECT m FROM MarkovModel m WHERE m.estado = 'ACTIVO'")
    List<MarkovModel> findActiveModels();

    boolean existsByNombre(String nombre);
}