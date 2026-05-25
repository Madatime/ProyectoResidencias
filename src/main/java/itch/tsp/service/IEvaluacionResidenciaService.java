package itch.tsp.service;

import java.util.List;

import itch.tsp.model.EvaluacionResidencia;
import itch.tsp.model.TipoEvaluacionResidencia;

public interface IEvaluacionResidenciaService {

	List<EvaluacionResidencia> buscarPorResidencia(Integer idResidencia);

	EvaluacionResidencia buscarPorResidenciaYTipo(Integer idResidencia, TipoEvaluacionResidencia tipoEvaluacion);

	EvaluacionResidencia buscarPorId(Integer idEvaluacion);

	void guardarEvaluacion(EvaluacionResidencia evaluacion, Integer idResidencia);

	Double calcularPromedioFinal(Integer idResidencia);

	void eliminar(Integer idEvaluacion);
	
	Double calcularPromedioSeguimiento1(Integer idResidencia);
	
	Double calcularPromedioSeguimiento2(Integer idResidencia);

	Double calcularPromedioReporteFinal(Integer idResidencia);

	Double calcularCalificacionFinalResidencia(Integer idResidencia);
}
