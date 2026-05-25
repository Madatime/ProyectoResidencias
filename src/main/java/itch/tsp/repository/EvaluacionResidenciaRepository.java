package itch.tsp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import itch.tsp.model.EvaluacionResidencia;
import itch.tsp.model.TipoEvaluacionResidencia;

public interface EvaluacionResidenciaRepository extends JpaRepository<EvaluacionResidencia, Integer> {

	List<EvaluacionResidencia> findByResidencia_IdAndEstatusOrderByIdDesc(Integer idResidencia, Integer estatus);

	EvaluacionResidencia findByResidencia_IdAndTipoEvaluacionAndEstatus(Integer idResidencia,
			TipoEvaluacionResidencia tipoEvaluacion, Integer estatus);
}