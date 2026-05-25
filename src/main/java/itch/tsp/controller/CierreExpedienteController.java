package itch.tsp.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import itch.tsp.model.DocumentoResidencia;
import itch.tsp.model.Residencia;
import itch.tsp.model.TipoDocumentoResidencia;
import itch.tsp.security.SeguridadResidenciaService;
import itch.tsp.service.IDocumentoResidenciaService;
import itch.tsp.service.IEvaluacionResidenciaService;
import itch.tsp.service.IResidenciaService;

@Controller
public class CierreExpedienteController {

	@Autowired
	private IResidenciaService serviceResidencia;

	@Autowired
	private IDocumentoResidenciaService serviceDocumento;

	@Autowired
	private IEvaluacionResidenciaService serviceEvaluacion;

	@Autowired
	private SeguridadResidenciaService seguridadResidenciaService;

	private static final TipoDocumentoResidencia[] DOCUMENTOS_OBLIGATORIOS = {
			TipoDocumentoResidencia.REPORTE_PRELIMINAR,
			TipoDocumentoResidencia.PROYECTO_RESIDENCIA,
			TipoDocumentoResidencia.PRIMER_INFORME,
			TipoDocumentoResidencia.SEGUNDO_INFORME,
			TipoDocumentoResidencia.EVALUACION_FINAL,
			TipoDocumentoResidencia.LIBERACION_ASESOR_INTERNO,
			TipoDocumentoResidencia.ENTREGA_REPORTE_EMPRESA,
			TipoDocumentoResidencia.OFICIO_ENTREGA_DIVISION
	};

	@GetMapping("/cierre-expediente/index")
	public String mostrarIndex(Model model, Authentication authentication) {

		List<Residencia> residencias = serviceResidencia.buscarTodasActivas();

		residencias = seguridadResidenciaService
				.filtrarResidenciasPermitidas(residencias, authentication);

		model.addAttribute("residencias", residencias);

		return "residencias/cierreExpediente";
	}

	@GetMapping("/cierre-expediente/{idResidencia}")
	public String verCierre(
			@PathVariable("idResidencia") Integer idResidencia,
			Model model,
			Authentication authentication) {

		Residencia residencia = serviceResidencia.buscarPorIdResidencia(idResidencia);

		if (residencia == null) {
			return "redirect:/cierre-expediente/index";
		}

		seguridadResidenciaService.validarAccesoResidencia(residencia, authentication);

		Map<TipoDocumentoResidencia, DocumentoResidencia> mapaDocumentos = new LinkedHashMap<>();

		int faltantes = 0;
		int noAprobados = 0;

		for (TipoDocumentoResidencia tipo : DOCUMENTOS_OBLIGATORIOS) {

			DocumentoResidencia documento =
					resolverDocumentoObligatorio(idResidencia, residencia, tipo);

			documento = normalizarDocumentoParaCierre(residencia, documento, tipo);

			mapaDocumentos.put(tipo, documento);

			if (documento == null) {
				faltantes++;
			} else if (documento.getEstatus() == null
					|| !"APROBADO".equals(documento.getEstatus().name())) {
				noAprobados++;
			}
		}

		Double promedioFinal = serviceEvaluacion.calcularPromedioFinal(idResidencia);

		boolean promedioAprobado =
				promedioFinal != null && promedioFinal >= 70;

		boolean puedeCerrar =
				faltantes == 0 && noAprobados == 0 && promedioAprobado;

		model.addAttribute("residencia", residencia);
		model.addAttribute("mapaDocumentos", mapaDocumentos);
		model.addAttribute("promedioFinal", promedioFinal);
		model.addAttribute("promedioAprobado", promedioAprobado);
		model.addAttribute("faltantes", faltantes);
		model.addAttribute("noAprobados", noAprobados);
		model.addAttribute("puedeCerrar", puedeCerrar);

		return "residencias/cierreExpediente";
	}

	@GetMapping("/cierre-expediente/cerrar/{idResidencia}")
	public String cerrar(
			@PathVariable("idResidencia") Integer idResidencia,
			Authentication authentication) {

		Residencia residencia = serviceResidencia.buscarPorIdResidencia(idResidencia);

		if (residencia == null) {
			return "redirect:/cierre-expediente/index";
		}

		seguridadResidenciaService.validarAccesoResidencia(residencia, authentication);

		serviceResidencia.cerrarExpediente(idResidencia);

		return "redirect:/cierre-expediente/" + idResidencia;
	}

	@GetMapping("/cierre-expediente/reabrir/{idResidencia}")
	public String reabrir(
			@PathVariable("idResidencia") Integer idResidencia,
			Authentication authentication) {

		Residencia residencia = serviceResidencia.buscarPorIdResidencia(idResidencia);

		if (residencia == null) {
			return "redirect:/cierre-expediente/index";
		}

		seguridadResidenciaService.validarAccesoResidencia(residencia, authentication);

		serviceResidencia.reabrirExpediente(idResidencia);

		return "redirect:/cierre-expediente/" + idResidencia;
	}

	private DocumentoResidencia resolverDocumentoObligatorio(
			Integer idResidencia,
			Residencia residencia,
			TipoDocumentoResidencia tipo) {

		DocumentoResidencia documento =
				serviceDocumento.buscarPorResidenciaYTipo(idResidencia, tipo);

		if (documento != null) {
			return documento;
		}

		if (!documentoGenerableDisponible(idResidencia, residencia, tipo)) {
			return null;
		}

		serviceDocumento.registrarDocumentoGenerado(
				idResidencia,
				tipo,
				nombreArchivoGenerado(idResidencia, tipo));

		return serviceDocumento.buscarPorResidenciaYTipo(idResidencia, tipo);
	}

	private boolean documentoGenerableDisponible(
			Integer idResidencia,
			Residencia residencia,
			TipoDocumentoResidencia tipo) {

		boolean reportePreliminarCargado = documentoFisicoCargado(
				serviceDocumento.buscarPorResidenciaYTipo(idResidencia, TipoDocumentoResidencia.REPORTE_PRELIMINAR));

		boolean proyectoAutorizado = residencia.getEstadoAutorizacion() != null
				&& "AUTORIZADO".equalsIgnoreCase(residencia.getEstadoAutorizacion());

		boolean primerInformeCompleto = evaluacionCompleta(idResidencia, itch.tsp.model.TipoEvaluacionResidencia.SEGUIMIENTO_1_EXTERNO)
				&& evaluacionCompleta(idResidencia, itch.tsp.model.TipoEvaluacionResidencia.SEGUIMIENTO_1_INTERNO);

		boolean segundoInformeCompleto = evaluacionCompleta(idResidencia, itch.tsp.model.TipoEvaluacionResidencia.SEGUIMIENTO_2_EXTERNO)
				&& evaluacionCompleta(idResidencia, itch.tsp.model.TipoEvaluacionResidencia.SEGUIMIENTO_2_INTERNO);

		boolean evaluacionFinalCompleta = evaluacionCompleta(idResidencia, itch.tsp.model.TipoEvaluacionResidencia.REPORTE_FINAL_EXTERNO)
				&& evaluacionCompleta(idResidencia, itch.tsp.model.TipoEvaluacionResidencia.REPORTE_FINAL_INTERNO);

		switch (tipo) {
		case PROYECTO_RESIDENCIA:
			return reportePreliminarCargado && proyectoAutorizado;
		case PRIMER_INFORME:
			return primerInformeCompleto;
		case SEGUNDO_INFORME:
			return segundoInformeCompleto;
		case EVALUACION_FINAL:
			return evaluacionFinalCompleta;
		case LIBERACION_ASESOR_INTERNO:
			return evaluacionFinalCompleta;
		case ENTREGA_REPORTE_EMPRESA:
			return evaluacionFinalCompleta;
		case OFICIO_ENTREGA_DIVISION:
			return evaluacionFinalCompleta;
		default:
			return false;
		}
	}

	private boolean documentoFisicoCargado(DocumentoResidencia documento) {
		return documento != null
				&& documento.getRutaArchivo() != null
				&& !documento.getRutaArchivo().trim().isEmpty();
	}

	private boolean evaluacionCompleta(Integer idResidencia, itch.tsp.model.TipoEvaluacionResidencia tipoEvaluacion) {
		itch.tsp.model.EvaluacionResidencia evaluacion =
				serviceEvaluacion.buscarPorResidenciaYTipo(idResidencia, tipoEvaluacion);

		return evaluacion != null && evaluacion.getCalificacion() != null;
	}

	private String nombreArchivoGenerado(Integer idResidencia, TipoDocumentoResidencia tipo) {
		switch (tipo) {
		case PROYECTO_RESIDENCIA:
			return "PROYECTO_RESIDENCIA_" + idResidencia + ".pdf";
		case PRIMER_INFORME:
			return "ANEXO_XXIX_" + idResidencia + "_1.pdf";
		case SEGUNDO_INFORME:
			return "ANEXO_XXIX_" + idResidencia + "_2.pdf";
		case EVALUACION_FINAL:
			return "ANEXO_XXX_" + idResidencia + ".pdf";
		case LIBERACION_ASESOR_INTERNO:
			return "LIBERACION_ASESOR_INTERNO_" + idResidencia + ".pdf";
		case ENTREGA_REPORTE_EMPRESA:
			return "5_Entrega_de_Reporte_a_Empresa_" + idResidencia + ".pdf";
		case OFICIO_ENTREGA_DIVISION:
			return "9_Oficio_de_entrega_de_Reporte_" + idResidencia + ".pdf";
		default:
			return tipo.name() + "_" + idResidencia + ".pdf";
		}
	}

	private DocumentoResidencia normalizarDocumentoParaCierre(
			Residencia residencia,
			DocumentoResidencia documento,
			TipoDocumentoResidencia tipo) {

		if (documento == null) {
			return documento;
		}

		boolean residenciaAutorizada = residencia.getEstadoAutorizacion() != null
				&& "AUTORIZADO".equalsIgnoreCase(residencia.getEstadoAutorizacion());

		boolean documentoConArchivo = documento.getRutaArchivo() != null
				&& !documento.getRutaArchivo().trim().isEmpty();

		boolean documentoPendienteRevision = documentoConArchivo
				&& (documento.getEstatus() == itch.tsp.model.EstatusDocumento.CARGADO
					|| documento.getEstatus() == itch.tsp.model.EstatusDocumento.EN_REVISION);

		boolean documentoGeneradoPendienteRevision = "GENERADO_EN_LINEA".equalsIgnoreCase(documento.getRutaArchivo())
				&& (documento.getEstatus() == null
					|| documento.getEstatus() == itch.tsp.model.EstatusDocumento.PENDIENTE
					|| documento.getEstatus() == itch.tsp.model.EstatusDocumento.CARGADO
					|| documento.getEstatus() == itch.tsp.model.EstatusDocumento.EN_REVISION);

		boolean debeAutoAprobar = false;

		if (tipo == TipoDocumentoResidencia.REPORTE_PRELIMINAR) {
			debeAutoAprobar = residenciaAutorizada && documentoPendienteRevision;
		} else if (tipo == TipoDocumentoResidencia.PROYECTO_RESIDENCIA
				|| tipo == TipoDocumentoResidencia.PRIMER_INFORME
				|| tipo == TipoDocumentoResidencia.SEGUNDO_INFORME
				|| tipo == TipoDocumentoResidencia.EVALUACION_FINAL
				|| tipo == TipoDocumentoResidencia.LIBERACION_ASESOR_INTERNO
				|| tipo == TipoDocumentoResidencia.ENTREGA_REPORTE_EMPRESA
				|| tipo == TipoDocumentoResidencia.OFICIO_ENTREGA_DIVISION) {
			debeAutoAprobar = documentoPendienteRevision || documentoGeneradoPendienteRevision;
		}

		if (debeAutoAprobar) {
			serviceDocumento.actualizarEstatus(
					documento.getId(),
					itch.tsp.model.EstatusDocumento.APROBADO,
					"Documento aprobado automáticamente por política de validación del sistema.");
			return serviceDocumento.buscarPorId(documento.getId());
		}

		return documento;
	}
}
