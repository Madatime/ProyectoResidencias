package itch.tsp.service.implementJPA;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import itch.tsp.model.EvaluacionResidencia;
import itch.tsp.model.Residencia;
import itch.tsp.model.TipoEvaluacionResidencia;
import itch.tsp.repository.EvaluacionResidenciaRepository;
import itch.tsp.repository.ResidenciaRepository;
import itch.tsp.service.IEvaluacionResidenciaService;

@Primary
@Service
public class EvaluacionResidenciaServiceJpa implements IEvaluacionResidenciaService {

	@Autowired
	private EvaluacionResidenciaRepository repoEvaluacion;

	@Autowired
	private ResidenciaRepository repoResidencia;

	@Override
	public List<EvaluacionResidencia> buscarPorResidencia(Integer idResidencia) {
		return repoEvaluacion.findByResidencia_IdAndEstatusOrderByIdDesc(idResidencia, 1);
	}

	@Override
	public EvaluacionResidencia buscarPorResidenciaYTipo(Integer idResidencia, TipoEvaluacionResidencia tipoEvaluacion) {
		return repoEvaluacion.findByResidencia_IdAndTipoEvaluacionAndEstatus(idResidencia, tipoEvaluacion, 1);
	}

	@Override
	public EvaluacionResidencia buscarPorId(Integer idEvaluacion) {
		return repoEvaluacion.findById(idEvaluacion).orElse(null);
	}

	@Override
	public void guardarEvaluacion(EvaluacionResidencia evaluacion, Integer idResidencia) {
		Residencia residencia = repoResidencia.findById(idResidencia).orElse(null);

		if (residencia == null) {
			throw new RuntimeException("La residencia no existe.");
		}

		if (evaluacion.getCalificacion() == null) {
			throw new RuntimeException("Debes capturar la calificación.");
		}

		if (evaluacion.getCalificacion() < 0 || evaluacion.getCalificacion() > 100) {
			throw new RuntimeException("La calificación debe estar entre 0 y 100.");
		}

		EvaluacionResidencia existente = repoEvaluacion.findByResidencia_IdAndTipoEvaluacionAndEstatus(
				idResidencia, evaluacion.getTipoEvaluacion(), 1);

		if (existente != null && evaluacion.getId() == null) {
			evaluacion.setId(existente.getId());
		}

		evaluacion.setResidencia(residencia);

		if (evaluacion.getFechaEvaluacion() == null) {
			evaluacion.setFechaEvaluacion(LocalDate.now());
		}

		if (evaluacion.getEstatus() == null) {
			evaluacion.setEstatus(1);
		}

		repoEvaluacion.save(evaluacion);
	}

	@Override
	public Double calcularPromedioFinal(Integer idResidencia) {
		EvaluacionResidencia externo = repoEvaluacion.findByResidencia_IdAndTipoEvaluacionAndEstatus(
				idResidencia, TipoEvaluacionResidencia.REPORTE_FINAL_EXTERNO, 1);

		EvaluacionResidencia interno = repoEvaluacion.findByResidencia_IdAndTipoEvaluacionAndEstatus(
				idResidencia, TipoEvaluacionResidencia.REPORTE_FINAL_INTERNO, 1);

		if (externo == null || interno == null || externo.getCalificacion() == null || interno.getCalificacion() == null) {
			return null;
		}

		return (externo.getCalificacion() + interno.getCalificacion()) / 2.0;
	}
	
	public Double calcularPromedioSeguimiento1(Integer idResidencia) {

		EvaluacionResidencia externo = repoEvaluacion
				.findByResidencia_IdAndTipoEvaluacionAndEstatus(
						idResidencia,
						TipoEvaluacionResidencia.SEGUIMIENTO_1_EXTERNO,
						1);

		EvaluacionResidencia interno = repoEvaluacion
				.findByResidencia_IdAndTipoEvaluacionAndEstatus(
						idResidencia,
						TipoEvaluacionResidencia.SEGUIMIENTO_1_INTERNO,
						1);

		if (externo == null || interno == null ||
			externo.getCalificacion() == null ||
			interno.getCalificacion() == null) {
			return null;
		}

		return (externo.getCalificacion() + interno.getCalificacion()) / 2.0;
	}

	@Override
	public void eliminar(Integer idEvaluacion) {
		EvaluacionResidencia evaluacion = repoEvaluacion.findById(idEvaluacion).orElse(null);

		if (evaluacion != null) {
			evaluacion.setEstatus(0);
			repoEvaluacion.save(evaluacion);
		}
	}
	
	@Override
	public Double calcularPromedioSeguimiento2(Integer idResidencia) {

		EvaluacionResidencia externo = repoEvaluacion.findByResidencia_IdAndTipoEvaluacionAndEstatus(
				idResidencia, TipoEvaluacionResidencia.SEGUIMIENTO_2_EXTERNO, 1);

		EvaluacionResidencia interno = repoEvaluacion.findByResidencia_IdAndTipoEvaluacionAndEstatus(
				idResidencia, TipoEvaluacionResidencia.SEGUIMIENTO_2_INTERNO, 1);

		if (externo == null || interno == null ||
				externo.getCalificacion() == null ||
				interno.getCalificacion() == null) {
			return null;
		}

		return (externo.getCalificacion() + interno.getCalificacion()) / 2.0;
	}

	@Override
	public Double calcularPromedioReporteFinal(Integer idResidencia) {

		EvaluacionResidencia externo = repoEvaluacion.findByResidencia_IdAndTipoEvaluacionAndEstatus(
				idResidencia, TipoEvaluacionResidencia.REPORTE_FINAL_EXTERNO, 1);

		EvaluacionResidencia interno = repoEvaluacion.findByResidencia_IdAndTipoEvaluacionAndEstatus(
				idResidencia, TipoEvaluacionResidencia.REPORTE_FINAL_INTERNO, 1);

		if (externo == null || interno == null ||
				externo.getCalificacion() == null ||
				interno.getCalificacion() == null) {
			return null;
		}

		return (externo.getCalificacion() + interno.getCalificacion()) / 2.0;
	}

	@Override
	public Double calcularCalificacionFinalResidencia(Integer idResidencia) {

		Double seguimiento1 = calcularPromedioSeguimiento1(idResidencia);
		Double seguimiento2 = calcularPromedioSeguimiento2(idResidencia);
		Double reporteFinal = calcularPromedioReporteFinal(idResidencia);

		if (seguimiento1 == null || seguimiento2 == null || reporteFinal == null) {
			return null;
		}

		return (seguimiento1 * 0.10) + (seguimiento2 * 0.10) + (reporteFinal * 0.80);
	}
	
}