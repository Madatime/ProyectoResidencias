package itch.tsp.service;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import itch.tsp.model.EvaluacionResidencia;
import itch.tsp.model.TipoEvaluacionResidencia;

@Service
public class EvaluacionResidenciaServiceImp implements IEvaluacionResidenciaService {

	private List<EvaluacionResidencia> listaEvaluaciones;

	public EvaluacionResidenciaServiceImp() {
		listaEvaluaciones = new LinkedList<>();
	}

	@Override
	public List<EvaluacionResidencia> buscarPorResidencia(Integer idResidencia) {
		return listaEvaluaciones;
	}

	@Override
	public EvaluacionResidencia buscarPorResidenciaYTipo(Integer idResidencia, TipoEvaluacionResidencia tipoEvaluacion) {
		for (EvaluacionResidencia evaluacion : listaEvaluaciones) {
			if (evaluacion.getResidencia() != null
					&& evaluacion.getResidencia().getId() != null
					&& evaluacion.getResidencia().getId().equals(idResidencia)
					&& evaluacion.getTipoEvaluacion() == tipoEvaluacion
					&& evaluacion.getEstatus() != null
					&& evaluacion.getEstatus() == 1) {
				return evaluacion;
			}
		}
		return null;
	}

	@Override
	public EvaluacionResidencia buscarPorId(Integer idEvaluacion) {
		for (EvaluacionResidencia evaluacion : listaEvaluaciones) {
			if (evaluacion.getId().equals(idEvaluacion)) {
				return evaluacion;
			}
		}
		return null;
	}

	@Override
	public void guardarEvaluacion(EvaluacionResidencia evaluacion, Integer idResidencia) {
		if (evaluacion.getId() == null) {
			evaluacion.setId(listaEvaluaciones.size() + 1);
			evaluacion.setEstatus(1);
			listaEvaluaciones.add(evaluacion);
		}
	}

	@Override
	public Double calcularPromedioFinal(Integer idResidencia) {
		return null;
	}
	
	@Override
	public Double calcularPromedioSeguimiento1(Integer idResidencia) {
		EvaluacionResidencia externo = buscarPorResidenciaYTipo(idResidencia, TipoEvaluacionResidencia.SEGUIMIENTO_1_EXTERNO);
		EvaluacionResidencia interno = buscarPorResidenciaYTipo(idResidencia, TipoEvaluacionResidencia.SEGUIMIENTO_1_INTERNO);

		if (externo == null || interno == null ||
				externo.getCalificacion() == null ||
				interno.getCalificacion() == null) {
			return null;
		}

		return (externo.getCalificacion() + interno.getCalificacion()) / 2.0;
	}

	@Override
	public void eliminar(Integer idEvaluacion) {
		EvaluacionResidencia evaluacion = buscarPorId(idEvaluacion);

		if (evaluacion != null) {
			evaluacion.setEstatus(0);
		}
	}
	
	@Override
	public Double calcularPromedioSeguimiento2(Integer idResidencia) {
		return null;
	}

	@Override
	public Double calcularPromedioReporteFinal(Integer idResidencia) {
		return null;
	}

	@Override
	public Double calcularCalificacionFinalResidencia(Integer idResidencia) {
		return null;
	}
	
}