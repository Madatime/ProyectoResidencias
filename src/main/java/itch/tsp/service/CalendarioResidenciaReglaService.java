package itch.tsp.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import itch.tsp.model.Residencia;

@Service
public class CalendarioResidenciaReglaService {

	public void prepararFechasPorDefecto(Residencia residencia) {
		if (residencia == null) {
			return;
		}

		LocalDate fechaInicioOficial = obtenerFechaInicioOficial(residencia.getPeriodo());
		LocalDate fechaFinBase = obtenerFechaFinBase(residencia.getPeriodo());
		LocalDate fechaFinMaxima = obtenerFechaFinMaxima(residencia.getPeriodo());
		boolean prorrogaAutorizada = Boolean.TRUE.equals(residencia.getProrrogaAutorizada());

		residencia.setFechaInicio(fechaInicioOficial);
		residencia.setFechaFin(prorrogaAutorizada ? fechaFinMaxima : fechaFinBase);
	}

	public void validarCalendarioResidencia(Residencia residencia) {
		if (residencia == null) {
			throw new RuntimeException("No se recibió información de la residencia.");
		}

		if (residencia.getPeriodo() == null || residencia.getPeriodo().trim().isEmpty()) {
			throw new RuntimeException("No se pudo determinar el periodo académico de la residencia.");
		}

		if (residencia.getFechaInicio() == null) {
			throw new RuntimeException("Debes indicar la fecha de inicio de la residencia.");
		}

		if (residencia.getFechaFin() == null) {
			throw new RuntimeException("Debes indicar la fecha de fin de la residencia.");
		}

		LocalDate fechaInicioOficial = obtenerFechaInicioOficial(residencia.getPeriodo());
		LocalDate fechaFinBase = obtenerFechaFinBase(residencia.getPeriodo());
		LocalDate fechaFinMaxima = obtenerFechaFinMaxima(residencia.getPeriodo());
		boolean prorrogaAutorizada = Boolean.TRUE.equals(residencia.getProrrogaAutorizada());

		if (!residencia.getFechaInicio().equals(fechaInicioOficial)) {
			throw new RuntimeException(
					"La residencia debe iniciar en la fecha oficial del calendario para el periodo: "
							+ fechaInicioOficial + ".");
		}

		if (!prorrogaAutorizada && !residencia.getFechaFin().equals(fechaFinBase)) {
			throw new RuntimeException(
					"La fecha de fin debe coincidir con la fecha base oficial del calendario: "
							+ fechaFinBase + ".");
		}

		if (prorrogaAutorizada && !residencia.getFechaFin().equals(fechaFinMaxima)) {
			throw new RuntimeException(
					"Con prórroga autorizada, la fecha de fin debe coincidir con la fecha máxima del calendario: "
							+ fechaFinMaxima + ".");
		}

		if (prorrogaAutorizada && LocalDate.now().isBefore(fechaFinBase)) {
			throw new RuntimeException(
					"La prórroga solo puede autorizarse una vez concluido el periodo base de 4 meses: "
							+ fechaFinBase + ".");
		}
	}

	public LocalDate obtenerFechaInicioOficial(String periodo) {
		if (periodo == null) {
			throw new RuntimeException("No se pudo determinar el periodo académico.");
		}

		String periodoNormalizado = periodo.trim().toUpperCase();
		int anio = obtenerAnio(periodoNormalizado);

		if (periodoNormalizado.contains("ENE-JUN")) {
			return LocalDate.of(anio, 1, 26);
		}

		if (periodoNormalizado.contains("AGO-DIC")) {
			return LocalDate.of(anio, 8, 25);
		}

		throw new RuntimeException("No existe una fecha oficial configurada para el periodo " + periodo + ".");
	}

	public LocalDate obtenerFechaFinBase(String periodo) {
		return obtenerFechaInicioOficial(periodo).plusMonths(4);
	}

	public LocalDate obtenerFechaFinMaxima(String periodo) {
		return obtenerFechaInicioOficial(periodo).plusMonths(6);
	}

	private int obtenerAnio(String periodo) {
		String[] partes = periodo.split(" ");

		for (String parte : partes) {
			try {
				return Integer.parseInt(parte.trim());
			} catch (NumberFormatException e) {
			}
		}

		throw new RuntimeException("No se pudo obtener el año del periodo: " + periodo);
	}
}
