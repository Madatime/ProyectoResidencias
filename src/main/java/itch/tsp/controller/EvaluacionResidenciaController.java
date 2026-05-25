package itch.tsp.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import itch.tsp.model.EvaluacionResidencia;
import itch.tsp.model.Residencia;
import itch.tsp.model.TipoEvaluacionResidencia;
import itch.tsp.security.SeguridadResidenciaService;
import itch.tsp.service.IEvaluacionResidenciaService;
import itch.tsp.service.IResidenciaService;

@Controller
public class EvaluacionResidenciaController {

	@Autowired
	private IEvaluacionResidenciaService serviceEvaluacion;

	@Autowired
	private IResidenciaService serviceResidencia;

	@Autowired
	private SeguridadResidenciaService seguridadResidenciaService;

	@GetMapping("/evaluaciones-residencia/externo/{idResidencia}/{tipo}")
	public String evaluarExterno(
			@PathVariable("idResidencia") Integer idResidencia,
			@PathVariable("tipo") TipoEvaluacionResidencia tipo,
			Model model,
			Authentication authentication) {

		Residencia residencia = serviceResidencia.buscarPorIdResidencia(idResidencia);

		if (residencia == null) {
			return "redirect:/residencias/index";
		}

		seguridadResidenciaService.validarEvaluacionExterna(residencia, authentication);

		model.addAttribute("residencia", residencia);
		model.addAttribute("evaluacionExistente", serviceEvaluacion.buscarPorResidenciaYTipo(idResidencia, tipo));
		model.addAttribute("tipoEvaluacion", tipo);
		model.addAttribute("criterios", criteriosExterno());

		return "residencias/evaluacionExterno";
	}

	@GetMapping("/evaluaciones-residencia/interno/{idResidencia}/{tipo}")
	public String evaluarInterno(
			@PathVariable("idResidencia") Integer idResidencia,
			@PathVariable("tipo") TipoEvaluacionResidencia tipo,
			Model model,
			Authentication authentication) {

		Residencia residencia = serviceResidencia.buscarPorIdResidencia(idResidencia);

		if (residencia == null) {
			return "redirect:/residencias/index";
		}

		seguridadResidenciaService.validarEvaluacionInterna(residencia, authentication);

		model.addAttribute("residencia", residencia);
		model.addAttribute("evaluacionExistente", serviceEvaluacion.buscarPorResidenciaYTipo(idResidencia, tipo));
		model.addAttribute("tipoEvaluacion", tipo);
		model.addAttribute("criterios", criteriosInterno());

		return "residencias/evaluacionInterno";
	}

	@PostMapping("/evaluaciones-residencia/guardar-criterios")
	public String guardarEvaluacionCriterios(
			@RequestParam("idResidencia") Integer idResidencia,
			@RequestParam("tipoEvaluacion") TipoEvaluacionResidencia tipoEvaluacion,

			@RequestParam(name = "criterio1", required = false) Double criterio1,
			@RequestParam(name = "criterio2", required = false) Double criterio2,
			@RequestParam(name = "criterio3", required = false) Double criterio3,
			@RequestParam(name = "criterio4", required = false) Double criterio4,
			@RequestParam(name = "criterio5", required = false) Double criterio5,
			@RequestParam(name = "criterio6", required = false) Double criterio6,
			@RequestParam(name = "criterio7", required = false) Double criterio7,
			@RequestParam(name = "criterio8", required = false) Double criterio8,
			@RequestParam(name = "criterio9", required = false) Double criterio9,
			@RequestParam(name = "criterio10", required = false) Double criterio10,
			@RequestParam(name = "criterio11", required = false) Double criterio11,
			@RequestParam(name = "criterio12", required = false) Double criterio12,
			@RequestParam(name = "criterio13", required = false) Double criterio13,

			@RequestParam(name = "observaciones", required = false) String observaciones,
			Authentication authentication) {

		Residencia residencia = serviceResidencia.buscarPorIdResidencia(idResidencia);

		if (residencia == null) {
			return "redirect:/residencias/index";
		}

		if (tipoEvaluacion.name().contains("EXTERNO")) {
			seguridadResidenciaService.validarEvaluacionExterna(residencia, authentication);
		} else {
			seguridadResidenciaService.validarEvaluacionInterna(residencia, authentication);
		}

		EvaluacionResidencia evaluacion =
				serviceEvaluacion.buscarPorResidenciaYTipo(idResidencia, tipoEvaluacion);

		if (evaluacion == null) {
			evaluacion = new EvaluacionResidencia();
		}

		evaluacion.setTipoEvaluacion(tipoEvaluacion);

		evaluacion.setCriterio1(valor(criterio1));
		evaluacion.setCriterio2(valor(criterio2));
		evaluacion.setCriterio3(valor(criterio3));
		evaluacion.setCriterio4(valor(criterio4));
		evaluacion.setCriterio5(valor(criterio5));
		evaluacion.setCriterio6(valor(criterio6));
		evaluacion.setCriterio7(valor(criterio7));
		evaluacion.setCriterio8(valor(criterio8));
		evaluacion.setCriterio9(valor(criterio9));
		evaluacion.setCriterio10(valor(criterio10));
		evaluacion.setCriterio11(valor(criterio11));
		evaluacion.setCriterio12(valor(criterio12));
		evaluacion.setCriterio13(valor(criterio13));

		Double total =
				valor(criterio1)
				+ valor(criterio2)
				+ valor(criterio3)
				+ valor(criterio4)
				+ valor(criterio5)
				+ valor(criterio6)
				+ valor(criterio7)
				+ valor(criterio8)
				+ valor(criterio9)
				+ valor(criterio10)
				+ valor(criterio11)
				+ valor(criterio12)
				+ valor(criterio13);

		evaluacion.setCalificacion(total);
		evaluacion.setObservaciones(observaciones);

		if (tipoEvaluacion.name().contains("EXTERNO")) {
			evaluacion.setEvaluadorRol("ASESOR EXTERNO");
		} else {
			evaluacion.setEvaluadorRol("ASESOR INTERNO");
		}

		serviceEvaluacion.guardarEvaluacion(evaluacion, idResidencia);

		return "redirect:/documentos-residencia/" + idResidencia;
	}

	@GetMapping("/evaluaciones-residencia/final/{rol}/{idResidencia}")
	public String evaluarReporteFinal(
			@PathVariable("rol") String rol,
			@PathVariable("idResidencia") Integer idResidencia,
			Model model,
			Authentication authentication) {

		Residencia residencia = serviceResidencia.buscarPorIdResidencia(idResidencia);

		if (residencia == null) {
			return "redirect:/residencias/index";
		}

		TipoEvaluacionResidencia tipoEvaluacion;

		if (rol.equalsIgnoreCase("externo")) {
			seguridadResidenciaService.validarEvaluacionExterna(residencia, authentication);
			tipoEvaluacion = TipoEvaluacionResidencia.REPORTE_FINAL_EXTERNO;
			model.addAttribute("tituloEvaluacion", "EVALUACIÓN FINAL POR ASESOR EXTERNO");
		} else {
			seguridadResidenciaService.validarEvaluacionInterna(residencia, authentication);
			tipoEvaluacion = TipoEvaluacionResidencia.REPORTE_FINAL_INTERNO;
			model.addAttribute("tituloEvaluacion", "EVALUACIÓN FINAL POR ASESOR INTERNO");
		}

		model.addAttribute("residencia", residencia);
		model.addAttribute("evaluacionExistente", serviceEvaluacion.buscarPorResidenciaYTipo(idResidencia, tipoEvaluacion));
		model.addAttribute("tipoEvaluacion", tipoEvaluacion);
		model.addAttribute("criterios", criteriosReporteFinal());

		return "residencias/evaluacionReporteFinal";
	}

	private Double valor(Double numero) {
		return numero != null ? numero : 0.0;
	}

	private List<Map<String, Object>> criteriosExterno() {
		List<Map<String, Object>> criterios = new ArrayList<>();

		criterios.add(criterio("Asiste puntualmente en el horario establecido", 5));
		criterios.add(criterio("Trabaja en equipo y se comunica de forma efectiva (oral y escrita)", 10));
		criterios.add(criterio("Tiene iniciativa para colaborar", 5));
		criterios.add(criterio("Propone mejoras al proyecto", 10));
		criterios.add(criterio("Cumple con los objetivos correspondientes al proyecto", 15));
		criterios.add(criterio("Es ordenado y cumple satisfactoriamente con las actividades encomendadas en los tiempos establecidos del cronograma", 15));
		criterios.add(criterio("Demuestra liderazgo en su actuar", 10));
		criterios.add(criterio("Demuestra conocimiento en el área de su especialidad", 20));
		criterios.add(criterio("Demuestra un comportamiento ético", 10));

		return criterios;
	}

	private List<Map<String, Object>> criteriosInterno() {
		List<Map<String, Object>> criterios = new ArrayList<>();

		criterios.add(criterio("Asiste puntualmente en el horario establecido", 10));
		criterios.add(criterio("Demuestra conocimiento en el área de su especialidad", 20));
		criterios.add(criterio("Trabaja en equipo y se comunica de forma efectiva (oral y escrita)", 15));
		criterios.add(criterio("Es dedicado y proactivo en las actividades encomendadas", 20));
		criterios.add(criterio("Es ordenado y cumple satisfactoriamente con las actividades encomendadas en los tiempos establecidos en el cronograma", 20));
		criterios.add(criterio("Propone mejoras al proyecto", 15));

		return criterios;
	}

	private List<Map<String, Object>> criteriosReporteFinal() {
		List<Map<String, Object>> criterios = new ArrayList<>();

		criterios.add(criterio("Portada", 2));
		criterios.add(criterio("Agradecimientos", 2));
		criterios.add(criterio("Resumen", 2));
		criterios.add(criterio("Índice", 2));
		criterios.add(criterio("Introducción", 2));
		criterios.add(criterio("Problemas a resolver, priorizándolos", 5));
		criterios.add(criterio("Objetivos", 5));
		criterios.add(criterio("Marco teórico (fundamentos teóricos)", 10));
		criterios.add(criterio("Procedimiento y descripción de las actividades realizadas", 5));
		criterios.add(criterio("Resultados, planos, gráficas, prototipos, manuales, programas, análisis estadísticos, modelos matemáticos, simulaciones, normativas, regulaciones y restricciones, entre otros", 45));
		criterios.add(criterio("Conclusiones, recomendaciones y experiencia profesional adquirida", 15));
		criterios.add(criterio("Competencias desarrolladas y/o aplicadas", 3));
		criterios.add(criterio("Fuentes de información", 2));

		return criterios;
	}

	private Map<String, Object> criterio(String descripcion, Integer valor) {
		Map<String, Object> criterio = new LinkedHashMap<>();
		criterio.put("descripcion", descripcion);
		criterio.put("valor", valor);
		return criterio;
	}
}