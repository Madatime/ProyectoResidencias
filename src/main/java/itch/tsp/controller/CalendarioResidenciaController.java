package itch.tsp.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import itch.tsp.model.Residencia;
import itch.tsp.service.CalendarioResidenciaPdfService;

@Controller
public class CalendarioResidenciaController {

	@Autowired
	private CalendarioResidenciaPdfService calendarioResidenciaPdfService;

	@GetMapping("/calendario-actividades/descargar")
	public ResponseEntity<byte[]> descargarCalendarioActividades() {

		Residencia residencia = new Residencia();

		residencia.setPeriodo(obtenerPeriodoActual());

		byte[] pdf = calendarioResidenciaPdfService.generarCalendario(residencia);

		HttpHeaders headers = new HttpHeaders();

		headers.setContentType(MediaType.APPLICATION_PDF);

		headers.setContentDisposition(
				ContentDisposition.attachment()
						.filename("calendario-residencias.pdf")
						.build());

		return ResponseEntity.ok()
				.headers(headers)
				.body(pdf);
	}

	private String obtenerPeriodoActual() {

		LocalDate hoy = LocalDate.now();

		int mes = hoy.getMonthValue();

		int anio = hoy.getYear();

		if (mes >= 1 && mes <= 6) {

			return "ENE-JUN " + anio;

		} else {

			return "AGO-DIC " + anio;
		}
	}
}