package itch.tsp.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import itch.tsp.model.DocumentoResidencia;
import itch.tsp.model.Directivo;
import itch.tsp.model.EstatusDocumento;
import itch.tsp.model.EvaluacionResidencia;
import itch.tsp.model.Residencia;
import itch.tsp.model.TipoDocumentoResidencia;
import itch.tsp.model.TipoDirectivo;
import itch.tsp.model.TipoEvaluacionResidencia;
import itch.tsp.service.CalendarioResidenciaPdfService;
import itch.tsp.service.IDirectivoService;
import itch.tsp.service.IDocumentoResidenciaService;
import itch.tsp.service.IEvaluacionResidenciaService;
import itch.tsp.service.IResidenciaService;
import itch.tsp.service.implementJPA.DocumentoResidenciaServiceJpa;
import org.springframework.security.core.Authentication;
import itch.tsp.security.SeguridadResidenciaService;

@Controller
public class DocumentoResidenciaController {

	@Autowired
	private IDocumentoResidenciaService serviceDocumento;

	@Autowired
	private DocumentoResidenciaServiceJpa cartaPresentacionPdfService;

	@Autowired
	private IResidenciaService serviceResidencia;

	@Autowired
	private IEvaluacionResidenciaService serviceEvaluacion;

	@Autowired
	private CalendarioResidenciaPdfService calendarioResidenciaPdfService;

	@Autowired
	private IDirectivoService serviceDirectivo;

	@Value("${app.ruta.base}")
	private String rutaBase;

	@Value("${app.carpeta.proyectos}")
	private String carpetaProyectos;
	
	@Autowired
	private SeguridadResidenciaService seguridadResidenciaService;

	@GetMapping("/documentos-residencia/{idResidencia:\\d+}")
	public String verDocumentos(
			@PathVariable("idResidencia") Integer idResidencia,
			Model model,
			Authentication authentication) {

		Residencia residencia = serviceResidencia.buscarPorIdResidencia(idResidencia);
		seguridadResidenciaService.validarAccesoResidencia(residencia, authentication);

		if (residencia == null) {
			return "redirect:/residencias/index";
		}

		List<DocumentoResidencia> documentos = serviceDocumento.buscarPorResidencia(idResidencia);

		int total = documentos.size();
		int aprobados = 0;
		int enRevision = 0;
		int rechazados = 0;
		int cargados = 0;
		int pendientes = 0;

		for (DocumentoResidencia documento : documentos) {
			if (documento.getEstatus() == EstatusDocumento.APROBADO) {
				aprobados++;
			} else if (documento.getEstatus() == EstatusDocumento.EN_REVISION) {
				enRevision++;
			} else if (documento.getEstatus() == EstatusDocumento.RECHAZADO) {
				rechazados++;
			} else if (documento.getEstatus() == EstatusDocumento.CARGADO) {
				cargados++;
			} else if (documento.getEstatus() == EstatusDocumento.PENDIENTE) {
				pendientes++;
			}
		}

		DocumentoResidencia reportePreliminar = serviceDocumento.buscarPorResidenciaYTipo(
				idResidencia,
				TipoDocumentoResidencia.REPORTE_PRELIMINAR);
		DocumentoResidencia entregaReporteEmpresa = serviceDocumento.buscarPorResidenciaYTipo(
				idResidencia,
				TipoDocumentoResidencia.ENTREGA_REPORTE_EMPRESA);
		DocumentoResidencia oficioEntregaDivision = serviceDocumento.buscarPorResidenciaYTipo(
				idResidencia,
				TipoDocumentoResidencia.OFICIO_ENTREGA_DIVISION);

		boolean reportePreliminarCargado = reportePreliminar != null
				&& reportePreliminar.getRutaArchivo() != null
				&& !reportePreliminar.getRutaArchivo().trim().isEmpty();

		boolean proyectoAutorizado = residencia.getEstadoAutorizacion() != null
				&& residencia.getEstadoAutorizacion().equalsIgnoreCase("AUTORIZADO");

		boolean documentosInicialesDesbloqueados = reportePreliminarCargado && proyectoAutorizado;

		EvaluacionResidencia primerExterno = serviceEvaluacion.buscarPorResidenciaYTipo(
				idResidencia, TipoEvaluacionResidencia.SEGUIMIENTO_1_EXTERNO);

		EvaluacionResidencia primerInterno = serviceEvaluacion.buscarPorResidenciaYTipo(
				idResidencia, TipoEvaluacionResidencia.SEGUIMIENTO_1_INTERNO);

		EvaluacionResidencia segundoExterno = serviceEvaluacion.buscarPorResidenciaYTipo(
				idResidencia, TipoEvaluacionResidencia.SEGUIMIENTO_2_EXTERNO);

		EvaluacionResidencia segundoInterno = serviceEvaluacion.buscarPorResidenciaYTipo(
				idResidencia, TipoEvaluacionResidencia.SEGUIMIENTO_2_INTERNO);

		EvaluacionResidencia finalExterno = serviceEvaluacion.buscarPorResidenciaYTipo(
				idResidencia, TipoEvaluacionResidencia.REPORTE_FINAL_EXTERNO);

		EvaluacionResidencia finalInterno = serviceEvaluacion.buscarPorResidenciaYTipo(
				idResidencia, TipoEvaluacionResidencia.REPORTE_FINAL_INTERNO);

		Double promedioPrimerInforme = calcularPromedio(primerExterno, primerInterno);
		Double promedioSegundoInforme = calcularPromedio(segundoExterno, segundoInterno);
		Double promedioReporteFinal = calcularPromedio(finalExterno, finalInterno);
		Double calificacionFinalResidencia = serviceEvaluacion.calcularCalificacionFinalResidencia(idResidencia);

		String estadoExpediente;

		if (calificacionFinalResidencia != null) {
			estadoExpediente = "DOCUMENTACION_FINALIZADA";
		} else if (promedioPrimerInforme != null || promedioSegundoInforme != null || promedioReporteFinal != null) {
			estadoExpediente = "EN_SEGUIMIENTO";
		} else {
			estadoExpediente = "SIN_AVANCE";
		}

		model.addAttribute("residencia", residencia);
		model.addAttribute("documentos", documentos);

		model.addAttribute("reportePreliminar", reportePreliminar);
		model.addAttribute("entregaReporteEmpresa", entregaReporteEmpresa);
		model.addAttribute("oficioEntregaDivision", oficioEntregaDivision);
		model.addAttribute("reportePreliminarCargado", reportePreliminarCargado);
		model.addAttribute("proyectoAutorizado", proyectoAutorizado);
		model.addAttribute("documentosInicialesDesbloqueados", documentosInicialesDesbloqueados);

		model.addAttribute("estadoExpediente", estadoExpediente);

		model.addAttribute("totalDocumentos", total);
		model.addAttribute("totalAprobados", aprobados);
		model.addAttribute("totalEnRevision", enRevision);
		model.addAttribute("totalRechazados", rechazados);
		model.addAttribute("totalCargados", cargados);
		model.addAttribute("totalPendientes", pendientes);

		model.addAttribute("primerExterno", primerExterno);
		model.addAttribute("primerInterno", primerInterno);
		model.addAttribute("segundoExterno", segundoExterno);
		model.addAttribute("segundoInterno", segundoInterno);

		model.addAttribute("promedioPrimerInforme", promedioPrimerInforme);
		model.addAttribute("promedioSegundoInforme", promedioSegundoInforme);

		model.addAttribute("primerInformeCompleto", promedioPrimerInforme != null);
		model.addAttribute("segundoInformeCompleto", promedioSegundoInforme != null);

		model.addAttribute("finalExterno", finalExterno);
		model.addAttribute("finalInterno", finalInterno);
		model.addAttribute("promedioReporteFinal", promedioReporteFinal);
		model.addAttribute("reporteFinalCompleto", promedioReporteFinal != null);
		model.addAttribute("calificacionFinalResidencia", calificacionFinalResidencia);
		model.addAttribute("liberacionInternoDisponible", promedioReporteFinal != null);

		return "residencias/documentosResidencia";
	}

	@GetMapping("/documentos-residencia/archivo/{idDocumento:\\d+}")
	public ResponseEntity<byte[]> descargarArchivoDocumento(
			@PathVariable("idDocumento") Integer idDocumento,
			Authentication authentication) {
		return responderArchivoDocumento(idDocumento, authentication, false);
	}

	@GetMapping("/documentos-residencia/archivo/{idDocumento:\\d+}/inline")
	public ResponseEntity<byte[]> verArchivoDocumentoEnLinea(
			@PathVariable("idDocumento") Integer idDocumento,
			Authentication authentication) {
		return responderArchivoDocumento(idDocumento, authentication, true);
	}

	private ResponseEntity<byte[]> responderArchivoDocumento(
			Integer idDocumento,
			Authentication authentication,
			boolean enLinea) {

		try {
			DocumentoResidencia documento = serviceDocumento.buscarPorId(idDocumento);

			if (documento == null || documento.getResidencia() == null) {
				throw new RuntimeException("El documento no existe.");
			}

			seguridadResidenciaService.validarAccesoResidencia(documento.getResidencia(), authentication);

			if (documento.getRutaArchivo() == null
					|| documento.getRutaArchivo().trim().isEmpty()
					|| "GENERADO_EN_LINEA".equalsIgnoreCase(documento.getRutaArchivo())) {
				throw new RuntimeException("El documento seleccionado no tiene un archivo fisico asociado.");
			}

			File archivo = new File(
					resolverDirectorioProyectos(),
					documento.getRutaArchivo().replace("/", File.separator).replace("\\", File.separator));

			if (!archivo.exists() || !archivo.isFile()) {
				throw new RuntimeException("No se encontro el archivo del documento.");
			}

			String nombreDescarga = documento.getNombreArchivo() != null && !documento.getNombreArchivo().trim().isEmpty()
					? documento.getNombreArchivo()
					: archivo.getName();

			String disposition = (enLinea ? "inline" : "attachment") + "; filename=\"" + nombreDescarga + "\"";

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, disposition)
					.contentType(MediaType.APPLICATION_PDF)
					.body(Files.readAllBytes(archivo.toPath()));

		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("No fue posible descargar el documento: " + e.getMessage());
		}
	}

	@GetMapping("/documentos-residencia/{idResidencia:\\d+}/anexo-xxix-pdf/{numero:\\d+}")
	public ResponseEntity<byte[]> generarAnexoXXIX(
			@PathVariable("idResidencia") Integer idResidencia, Authentication authentication,
			@PathVariable("numero") Integer numero) {
		
		
		try {
			
			Residencia residencia = serviceResidencia.buscarPorIdResidencia(idResidencia);
			
			seguridadResidenciaService.validarAccesoResidencia(residencia, authentication);

			ByteArrayOutputStream salida = new ByteArrayOutputStream();

			Document document = new Document(PageSize.LETTER, 40, 40, 35, 40);

			PdfWriter.getInstance(document, salida);

			document.open();

			Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
			Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f);
			Font normal = FontFactory.getFont(FontFactory.HELVETICA, 8.5f);
			Font negrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f);
			Font pie = FontFactory.getFont(FontFactory.HELVETICA, 7.5f);

			agregarEncabezadoInstitucional(document, subtitulo, normal);

			Paragraph tituloDoc = new Paragraph(
					"ANEXO XXIX. FORMATO DE EVALUACIÓN Y SEGUIMIENTO DE RESIDENCIA PROFESIONAL",
					titulo);

			tituloDoc.setAlignment(Element.ALIGN_CENTER);
			tituloDoc.setSpacingBefore(10);
			tituloDoc.setSpacingAfter(18);

			document.add(tituloDoc);

			TipoEvaluacionResidencia tipoExterno = numero == 1
					? TipoEvaluacionResidencia.SEGUIMIENTO_1_EXTERNO
					: TipoEvaluacionResidencia.SEGUIMIENTO_2_EXTERNO;

			TipoEvaluacionResidencia tipoInterno = numero == 1
					? TipoEvaluacionResidencia.SEGUIMIENTO_1_INTERNO
					: TipoEvaluacionResidencia.SEGUIMIENTO_2_INTERNO;

			EvaluacionResidencia externo = serviceEvaluacion.buscarPorResidenciaYTipo(idResidencia, tipoExterno);
			EvaluacionResidencia interno = serviceEvaluacion.buscarPorResidenciaYTipo(idResidencia, tipoInterno);

			PdfPTable datos = new PdfPTable(2);

			datos.setWidthPercentage(100);
			datos.setWidths(new float[] { 30, 70 });

			addDatoOficial(datos, "Nombre del Residente:",
					residencia.getResidente() != null ? residencia.getResidente().getNombreCompleto() : "",
					negrita, normal);

			addDatoOficial(datos, "Número de Control:",
					residencia.getResidente() != null ? residencia.getResidente().getMatricula() : "",
					negrita, normal);

			addDatoOficial(datos, "Nombre del Proyecto:", residencia.getNombreProyecto(), negrita, normal);

			addDatoOficial(datos, "Programa Educativo:", obtenerCarrera(residencia), negrita, normal);

			addDatoOficial(datos, "Periodo de realización de la Residencia Profesional:",
					residencia.getPeriodo(), negrita, normal);

			Double promedio = calcularPromedio(externo, interno);

			addDatoOficial(datos, "Calificación Parcial:",
					promedio != null ? String.format("%.2f", promedio) : "",
					negrita, normal);

			document.add(datos);
			document.add(new Paragraph("\n"));

			Paragraph tituloExterno = new Paragraph("Evaluación por el asesor externo", subtitulo);
			tituloExterno.setSpacingAfter(8);

			document.add(tituloExterno);

			PdfPTable tablaExterna = tablaCriteriosOficial();

			addCriterioOficial(tablaExterna, "Asiste puntualmente en el horario establecido", "5", criterio(externo, 1));
			addCriterioOficial(tablaExterna, "Trabaja en equipo y se comunica de forma efectiva (oral y escrita)", "10", criterio(externo, 2));
			addCriterioOficial(tablaExterna, "Tiene iniciativa para colaborar", "5", criterio(externo, 3));
			addCriterioOficial(tablaExterna, "Propone mejoras al proyecto", "10", criterio(externo, 4));
			addCriterioOficial(tablaExterna, "Cumple con los objetivos correspondientes al proyecto", "15", criterio(externo, 5));
			addCriterioOficial(tablaExterna, "Es ordenado y cumple satisfactoriamente con las actividades encomendadas", "15", criterio(externo, 6));
			addCriterioOficial(tablaExterna, "Demuestra liderazgo en su actuar", "10", criterio(externo, 7));
			addCriterioOficial(tablaExterna, "Demuestra conocimiento en el área de su especialidad", "20", criterio(externo, 8));
			addCriterioOficial(tablaExterna, "Demuestra un comportamiento ético", "10", criterio(externo, 9));
			addCriterioOficial(tablaExterna, "Calificación total", "100",
					externo != null && externo.getCalificacion() != null ? String.format("%.2f", externo.getCalificacion()) : "");

			document.add(tablaExterna);
			document.add(new Paragraph("\n"));

			agregarBloqueFirmas(
					document,
					residencia.getAsesorExterno() != null
							? residencia.getAsesorExterno().getNombre() + " " + residencia.getAsesorExterno().getApellidos()
							: "",
					"Sello de la empresa, organismo o dependencia",
					LocalDate.now().toString(),
					normal,
					negrita);

			document.newPage();

			agregarEncabezadoInstitucional(document, subtitulo, normal);

			Paragraph tituloInterno = new Paragraph("Evaluación por el asesor interno", subtitulo);
			tituloInterno.setSpacingBefore(15);
			tituloInterno.setSpacingAfter(8);

			document.add(tituloInterno);

			PdfPTable tablaInterna = tablaCriteriosOficial();

			addCriterioOficial(tablaInterna, "Asiste puntualmente en el horario establecido", "10", criterio(interno, 1));
			addCriterioOficial(tablaInterna, "Demuestra conocimiento en el área de su especialidad", "20", criterio(interno, 2));
			addCriterioOficial(tablaInterna, "Trabaja en equipo y se comunica de forma efectiva (oral y escrita)", "15", criterio(interno, 3));
			addCriterioOficial(tablaInterna, "Es dedicado y proactivo en las actividades encomendadas", "20", criterio(interno, 4));
			addCriterioOficial(tablaInterna, "Es ordenado y cumple satisfactoriamente con las actividades", "20", criterio(interno, 5));
			addCriterioOficial(tablaInterna, "Propone mejoras al proyecto", "15", criterio(interno, 6));
			addCriterioOficial(tablaInterna, "Calificación total", "100",
					interno != null && interno.getCalificacion() != null ? String.format("%.2f", interno.getCalificacion()) : "");

			document.add(tablaInterna);
			document.add(new Paragraph("\n"));

			agregarBloqueFirmas(
					document,
					residencia.getAsesorInterno() != null
							? residencia.getAsesorInterno().getNombre() + " " + residencia.getAsesorInterno().getApellidos()
							: "",
					"Sello de la Institución",
					LocalDate.now().toString(),
					normal,
					negrita);

			agregarPieInstitucional(document, pie);

			document.close();

			String nombreArchivo = "ANEXO_XXIX_" + idResidencia + "_" + numero + ".pdf";
			TipoDocumentoResidencia tipoDocumento = numero == 1
					? TipoDocumentoResidencia.PRIMER_INFORME
					: TipoDocumentoResidencia.SEGUNDO_INFORME;
			serviceDocumento.registrarDocumentoGenerado(idResidencia, tipoDocumento, nombreArchivo);

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombreArchivo)
					.contentType(MediaType.APPLICATION_PDF)
					.body(salida.toByteArray());

		} catch (Exception e) {
			throw new RuntimeException("Error al generar ANEXO XXIX: " + e.getMessage());
		}
	}

	@GetMapping("/documentos-residencia/{idResidencia:\\d+}/anexo-xxx-pdf")
	public ResponseEntity<byte[]> generarAnexoXXX(@PathVariable("idResidencia") Integer idResidencia, Authentication authentication) {

		try {

			Residencia residencia = serviceResidencia.buscarPorIdResidencia(idResidencia);
			seguridadResidenciaService.validarAccesoResidencia(residencia, authentication);

			EvaluacionResidencia externo = serviceEvaluacion.buscarPorResidenciaYTipo(
					idResidencia,
					TipoEvaluacionResidencia.REPORTE_FINAL_EXTERNO);

			EvaluacionResidencia interno = serviceEvaluacion.buscarPorResidenciaYTipo(
					idResidencia,
					TipoEvaluacionResidencia.REPORTE_FINAL_INTERNO);

			if (externo == null || interno == null
					|| externo.getCalificacion() == null
					|| interno.getCalificacion() == null) {

				throw new RuntimeException("Aún no están completas ambas evaluaciones finales.");
			}

			Double promedioFinal = (externo.getCalificacion() + interno.getCalificacion()) / 2.0;

			ByteArrayOutputStream salida = new ByteArrayOutputStream();

			Document document = new Document(PageSize.LETTER, 40, 40, 35, 40);

			PdfWriter.getInstance(document, salida);

			document.open();

			Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
			Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f);
			Font normal = FontFactory.getFont(FontFactory.HELVETICA, 8.5f);
			Font negrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f);
			Font pie = FontFactory.getFont(FontFactory.HELVETICA, 7.5f);

			agregarEncabezadoInstitucional(document, subtitulo, normal);

			Paragraph tituloDoc = new Paragraph(
					"ANEXO XXX. FORMATO DE EVALUACIÓN DE REPORTE DE RESIDENCIA PROFESIONAL",
					titulo);

			tituloDoc.setAlignment(Element.ALIGN_CENTER);
			tituloDoc.setSpacingBefore(10);
			tituloDoc.setSpacingAfter(18);

			document.add(tituloDoc);

			PdfPTable datos = new PdfPTable(2);

			datos.setWidthPercentage(100);
			datos.setWidths(new float[] { 30, 70 });

			addDatoOficial(datos, "Nombre del Residente:",
					residencia.getResidente() != null ? residencia.getResidente().getNombreCompleto() : "",
					negrita, normal);

			addDatoOficial(datos, "Número de Control:",
					residencia.getResidente() != null ? residencia.getResidente().getMatricula() : "",
					negrita, normal);

			addDatoOficial(datos, "Nombre del Proyecto:", residencia.getNombreProyecto(), negrita, normal);
			addDatoOficial(datos, "Programa Educativo:", obtenerCarrera(residencia), negrita, normal);
			addDatoOficial(datos, "Periodo de realización de la Residencia Profesional:", residencia.getPeriodo(), negrita, normal);
			addDatoOficial(datos, "Calificación Final:", String.format("%.2f", promedioFinal), negrita, normal);

			document.add(datos);
			document.add(new Paragraph("\n"));

			Paragraph leyendaExterna = new Paragraph("En qué medida el residente cumple con lo siguiente", negrita);
			leyendaExterna.setSpacingAfter(8);

			document.add(leyendaExterna);

			Paragraph tituloExterno = new Paragraph("Evaluación por el asesor externo", subtitulo);
			tituloExterno.setSpacingAfter(8);

			document.add(tituloExterno);

			PdfPTable tablaExterna = tablaCriteriosOficial();

			agregarCriteriosReporteFinalOficial(tablaExterna, externo);

			addCriterioOficial(tablaExterna, "Calificación total", "100", String.format("%.2f", externo.getCalificacion()));

			document.add(tablaExterna);
			document.add(new Paragraph("\n"));

			agregarBloqueFirmas(
					document,
					residencia.getAsesorExterno() != null
							? residencia.getAsesorExterno().getNombre() + " " + residencia.getAsesorExterno().getApellidos()
							: "",
					"Sello de la empresa, organismo o dependencia",
					fecha(externo),
					normal,
					negrita);

			document.newPage();

			agregarEncabezadoInstitucional(document, subtitulo, normal);

			Paragraph leyendaInterna = new Paragraph("En qué medida el residente cumple con lo siguiente", negrita);
			leyendaInterna.setSpacingBefore(15);
			leyendaInterna.setSpacingAfter(8);

			document.add(leyendaInterna);

			Paragraph tituloInterno = new Paragraph("Evaluación por el asesor interno", subtitulo);
			tituloInterno.setSpacingAfter(8);

			document.add(tituloInterno);

			PdfPTable tablaInterna = tablaCriteriosOficial();

			agregarCriteriosReporteFinalOficial(tablaInterna, interno);

			addCriterioOficial(tablaInterna, "Calificación total", "100", String.format("%.2f", interno.getCalificacion()));

			document.add(tablaInterna);
			document.add(new Paragraph("\n"));

			agregarBloqueFirmas(
					document,
					residencia.getAsesorInterno() != null
							? residencia.getAsesorInterno().getNombre() + " " + residencia.getAsesorInterno().getApellidos()
							: "",
					"Sello de la Institución",
					fecha(interno),
					normal,
					negrita);

			agregarPieInstitucional(document, pie);

			document.close();

			String nombreArchivo = "ANEXO_XXX_RESIDENCIA_" + idResidencia + ".pdf";
			serviceDocumento.registrarDocumentoGenerado(idResidencia, TipoDocumentoResidencia.EVALUACION_FINAL, nombreArchivo);

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombreArchivo)
					.contentType(MediaType.APPLICATION_PDF)
					.body(salida.toByteArray());

		} catch (Exception e) {
			throw new RuntimeException("Error al generar ANEXO XXX: " + e.getMessage());
		}
	}

	@GetMapping("/documentos-residencia/{idResidencia:\\d+}/proyecto-pdf")
	public ResponseEntity<byte[]> generarProyectoResidenciaPdf(
			@PathVariable("idResidencia") Integer idResidencia, Authentication authentication) {

		try {

			Residencia residencia = serviceResidencia.buscarPorIdResidencia(idResidencia);
			seguridadResidenciaService.validarAccesoResidencia(residencia, authentication);

			if (residencia == null) {
				throw new RuntimeException("La residencia no existe.");
			}

			ByteArrayOutputStream salida = new ByteArrayOutputStream();

			Document document = new Document(PageSize.LETTER, 45, 45, 35, 45);
			PdfWriter.getInstance(document, salida);

			document.open();

			BaseColor azulInstitucional = new BaseColor(0, 46, 109);
			BaseColor azulClaro = new BaseColor(232, 241, 255);
			BaseColor grisTexto = new BaseColor(70, 80, 95);
			BaseColor grisBorde = new BaseColor(210, 220, 235);
			BaseColor blanco = BaseColor.WHITE;

			Font fontTituloPrincipal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, azulInstitucional);
			Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, grisTexto);
			Font fontSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, azulInstitucional);
			Font fontEtiqueta = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, azulInstitucional);
			Font fontValor = FontFactory.getFont(FontFactory.HELVETICA, 9, grisTexto);
			Font fontTexto = FontFactory.getFont(FontFactory.HELVETICA, 10, grisTexto);
			Font fontPie = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, grisTexto);

			agregarEncabezadoProyectoResidencia(document, azulInstitucional, fontSubtitulo);

			Paragraph tituloDoc = new Paragraph("PROYECTO DE RESIDENCIA", fontTituloPrincipal);
			tituloDoc.setAlignment(Element.ALIGN_CENTER);
			tituloDoc.setSpacingBefore(18);
			tituloDoc.setSpacingAfter(4);
			document.add(tituloDoc);

			Paragraph subtituloDoc = new Paragraph(
					"Documento generado automáticamente con la información registrada en el sistema",
					fontSubtitulo);
			subtituloDoc.setAlignment(Element.ALIGN_CENTER);
			subtituloDoc.setSpacingAfter(22);
			document.add(subtituloDoc);

			PdfPTable tarjetaTitulo = new PdfPTable(1);
			tarjetaTitulo.setWidthPercentage(100);

			PdfPCell celdaTarjetaTitulo = new PdfPCell(new Phrase("Información general del proyecto", fontSeccion));
			celdaTarjetaTitulo.setBackgroundColor(azulClaro);
			celdaTarjetaTitulo.setBorderColor(grisBorde);
			celdaTarjetaTitulo.setPaddingTop(10);
			celdaTarjetaTitulo.setPaddingBottom(10);
			celdaTarjetaTitulo.setPaddingLeft(12);

			tarjetaTitulo.addCell(celdaTarjetaTitulo);
			document.add(tarjetaTitulo);

			PdfPTable datos = new PdfPTable(2);
			datos.setWidthPercentage(100);
			datos.setWidths(new float[] { 32, 68 });
			datos.setSpacingAfter(18);

			addDatoProyectoBonito(datos, "Nombre del proyecto", residencia.getNombreProyecto(), fontEtiqueta, fontValor, grisBorde);
			addDatoProyectoBonito(datos, "Periodo", residencia.getPeriodo(), fontEtiqueta, fontValor, grisBorde);

			addDatoProyectoBonito(datos, "Residente",
					residencia.getResidente() != null ? residencia.getResidente().getNombreCompleto() : "",
					fontEtiqueta, fontValor, grisBorde);

			addDatoProyectoBonito(datos, "Número de control",
					residencia.getResidente() != null ? residencia.getResidente().getMatricula() : "",
					fontEtiqueta, fontValor, grisBorde);

			addDatoProyectoBonito(datos, "Programa educativo", obtenerCarrera(residencia), fontEtiqueta, fontValor, grisBorde);

			addDatoProyectoBonito(datos, "Empresa",
					residencia.getEmpresa() != null ? residencia.getEmpresa().getNombre() : "",
					fontEtiqueta, fontValor, grisBorde);

			addDatoProyectoBonito(datos, "Asesor interno",
					residencia.getAsesorInterno() != null
							? residencia.getAsesorInterno().getNombre() + " " + residencia.getAsesorInterno().getApellidos()
							: "",
					fontEtiqueta, fontValor, grisBorde);

			addDatoProyectoBonito(datos, "Asesor externo",
					residencia.getAsesorExterno() != null
							? residencia.getAsesorExterno().getNombre() + " " + residencia.getAsesorExterno().getApellidos()
							: "",
					fontEtiqueta, fontValor, grisBorde);

			document.add(datos);

			agregarSeccionTextoProyecto(
					document,
					"Descripción del proyecto",
					residencia.getDescripcion(),
					fontSeccion,
					fontTexto,
					azulClaro,
					grisBorde);

			agregarSeccionTextoProyecto(
					document,
					"Objetivo del proyecto",
					residencia.getObjetivo(),
					fontSeccion,
					fontTexto,
					azulClaro,
					grisBorde);

			Paragraph pie = new Paragraph(
					"Instituto Tecnológico de Chilpancingo • División de Estudios Profesionales\n"
							+ "Documento generado el " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
					fontPie);
			pie.setAlignment(Element.ALIGN_CENTER);
			pie.setSpacingBefore(28);
			document.add(pie);

			document.close();

			String nombreArchivo = "PROYECTO_RESIDENCIA_" + idResidencia + ".pdf";
			serviceDocumento.registrarDocumentoGenerado(idResidencia, TipoDocumentoResidencia.PROYECTO_RESIDENCIA, nombreArchivo);

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombreArchivo)
					.contentType(MediaType.APPLICATION_PDF)
					.body(salida.toByteArray());

		} catch (Exception e) {
			throw new RuntimeException("Error al generar Proyecto de Residencia: " + e.getMessage());
		}
	}

	private void agregarEncabezadoProyectoResidencia(Document document, BaseColor azulInstitucional, Font fontSubtitulo)
			throws Exception {

		PdfPTable encabezado = new PdfPTable(2);
		encabezado.setWidthPercentage(100);
		encabezado.setWidths(new float[] { 30, 70 });

		PdfPCell celdaLogo = new PdfPCell();
		celdaLogo.setBorder(PdfPCell.NO_BORDER);
		celdaLogo.setPaddingBottom(8);

		try {
			com.itextpdf.text.Image logo = cargarImagenClasspath("static/img/logo-tecnm.png");
			logo.scaleToFit(155, 48);
			celdaLogo.addElement(logo);
		} catch (Exception e) {
			Paragraph textoLogo = new Paragraph("TecNM", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, azulInstitucional));
			celdaLogo.addElement(textoLogo);
		}

		PdfPCell celdaInstitucion = new PdfPCell();
		celdaInstitucion.setBorder(PdfPCell.NO_BORDER);
		celdaInstitucion.setPaddingBottom(8);

		Paragraph institucion = new Paragraph(
				"Instituto Tecnológico de Chilpancingo\nDivisión de Estudios Profesionales",
				fontSubtitulo);
		institucion.setAlignment(Element.ALIGN_RIGHT);
		celdaInstitucion.addElement(institucion);

		encabezado.addCell(celdaLogo);
		encabezado.addCell(celdaInstitucion);

		document.add(encabezado);

		PdfPTable linea = new PdfPTable(1);
		linea.setWidthPercentage(100);

		PdfPCell celdaLinea = new PdfPCell(new Phrase(""));
		celdaLinea.setFixedHeight(3);
		celdaLinea.setBorder(PdfPCell.NO_BORDER);
		celdaLinea.setBackgroundColor(azulInstitucional);

		linea.addCell(celdaLinea);
		document.add(linea);
	}

	private void addDatoProyectoBonito(PdfPTable table, String etiqueta, String valor, Font fontEtiqueta,
			Font fontValor, BaseColor grisBorde) {

		PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta + ":", fontEtiqueta));
		celdaEtiqueta.setPaddingTop(8);
		celdaEtiqueta.setPaddingBottom(8);
		celdaEtiqueta.setPaddingLeft(10);
		celdaEtiqueta.setBorderColor(grisBorde);
		celdaEtiqueta.setVerticalAlignment(Element.ALIGN_MIDDLE);

		PdfPCell celdaValor = new PdfPCell(new Phrase(valor != null ? valor : "", fontValor));
		celdaValor.setPaddingTop(8);
		celdaValor.setPaddingBottom(8);
		celdaValor.setPaddingLeft(10);
		celdaValor.setBorderColor(grisBorde);
		celdaValor.setVerticalAlignment(Element.ALIGN_MIDDLE);

		table.addCell(celdaEtiqueta);
		table.addCell(celdaValor);
	}

	private void agregarSeccionTextoProyecto(Document document, String titulo, String contenido, Font fontTitulo,
			Font fontTexto, BaseColor azulClaro, BaseColor grisBorde) throws Exception {

		PdfPTable seccion = new PdfPTable(1);
		seccion.setWidthPercentage(100);
		seccion.setSpacingBefore(10);
		seccion.setSpacingAfter(8);

		PdfPCell celdaTitulo = new PdfPCell(new Phrase(titulo, fontTitulo));
		celdaTitulo.setBackgroundColor(azulClaro);
		celdaTitulo.setBorderColor(grisBorde);
		celdaTitulo.setPaddingTop(9);
		celdaTitulo.setPaddingBottom(9);
		celdaTitulo.setPaddingLeft(12);

		PdfPCell celdaContenido = new PdfPCell(new Phrase(
				contenido != null && !contenido.trim().isEmpty() ? contenido : "Sin información registrada.",
				fontTexto));
		celdaContenido.setBorderColor(grisBorde);
		celdaContenido.setPaddingTop(12);
		celdaContenido.setPaddingBottom(14);
		celdaContenido.setPaddingLeft(12);
		celdaContenido.setPaddingRight(12);

		seccion.addCell(celdaTitulo);
		seccion.addCell(celdaContenido);

		document.add(seccion);
	}

	@GetMapping("/documentos-residencia/{id:\\d+}/carta-presentacion-pdf")
	public ResponseEntity<byte[]> descargarCartaPresentacion(@PathVariable("id") Integer idResidencia, Authentication authentication) {

		Residencia residencia = serviceResidencia.buscarPorIdResidencia(idResidencia);
		seguridadResidenciaService.validarAccesoResidencia(residencia, authentication);

		if (residencia == null) {
			throw new RuntimeException("La residencia no existe.");
		}

		byte[] pdf = cartaPresentacionPdfService.generarCartaPresentacion(residencia);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=carta_presentacion_residencia.pdf")
				.contentType(MediaType.APPLICATION_PDF)
				.body(pdf);
	}

	@GetMapping("/documentos-residencia/{idResidencia:\\d+}/entrega-reporte-empresa-pdf")
	public ResponseEntity<byte[]> generarEntregaReporteEmpresaPdf(
			@PathVariable("idResidencia") Integer idResidencia,
			Authentication authentication) {

		try {
			Residencia residencia = serviceResidencia.buscarPorIdResidencia(idResidencia);
			seguridadResidenciaService.validarAccesoResidencia(residencia, authentication);

			if (residencia == null) {
				throw new RuntimeException("La residencia no existe.");
			}

			ByteArrayOutputStream salida = new ByteArrayOutputStream();
			Document document = new Document(PageSize.LETTER, 46, 46, 32, 34);
			PdfWriter.getInstance(document, salida);
			document.open();

			Font normal = FontFactory.getFont(FontFactory.HELVETICA, 11f);
			Font negrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11f);
			Font negritaGrande = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f);

			agregarEncabezadoOficioInstitucional(document);

			Paragraph fecha = new Paragraph("Chilpancingo, Gro., " + formatearFechaLarga(fechaActualMexico()), normal);
			fecha.setAlignment(Element.ALIGN_RIGHT);
			document.add(fecha);
			document.add(new Paragraph("\n\n", normal));

			String representante = valorOrDefault(
					residencia.getEmpresa() != null ? residencia.getEmpresa().getRepresentante() : null,
					"REPRESENTANTE DE LA EMPRESA");
			String puestoRepresentante = valorOrDefault(
					residencia.getEmpresa() != null ? residencia.getEmpresa().getPuestoRepresentante() : null,
					"REPRESENTANTE");
			String empresa = valorOrDefault(
					residencia.getEmpresa() != null ? residencia.getEmpresa().getNombre() : null,
					"EMPRESA");

			document.add(new Paragraph("\n", normal));

			Paragraph destinatario = new Paragraph();
			destinatario.setLeading(14f);
			destinatario.add(new Phrase(representante.toUpperCase() + "\n", negritaGrande));
			destinatario.add(new Phrase(puestoRepresentante.toUpperCase() + "\n", normal));
			destinatario.add(new Phrase(empresa.toUpperCase() + "\n", normal));
			destinatario.add(new Phrase("P R E S E N T E", normal));
			document.add(destinatario);
			document.add(new Paragraph("\n\n\n", normal));

			String residente = valorOrDefault(
					residencia.getResidente() != null ? residencia.getResidente().getNombreCompleto() : null,
					"NOMBRE DEL RESIDENTE");
			String matricula = valorOrDefault(
					residencia.getResidente() != null ? residencia.getResidente().getMatricula() : null,
					"NÚMERO DE CONTROL");
			String carrera = valorOrDefault(obtenerCarrera(residencia), "PROGRAMA EDUCATIVO");
			String proyecto = valorOrDefault(residencia.getNombreProyecto(), "NOMBRE DEL PROYECTO");
			String periodo = valorOrDefault(formatearPeriodo(residencia.getPeriodo()), "PERIODO DE RESIDENCIA");
			String asesorExterno = valorOrDefault(
					residencia.getAsesorExterno() != null ? residencia.getAsesorExterno().getNombreCompleto() : null,
					"ASESOR EXTERNO");

			Paragraph cuerpo = new Paragraph(
					"El que suscribe, estudiante del Programa Educativo de " + carrera
							+ " del Instituto Tecnológico de Chilpancingo, por este medio hago entrega del Reporte de Residencia Profesional denominado "
							+ proyecto.toUpperCase()
							+ ", que fue realizado en el periodo "
							+ periodo
							+ ", en esta empresa que dignamente dirige, teniendo como asesor externo a "
							+ asesorExterno.toUpperCase()
							+ ".",
					normal);
			cuerpo.setAlignment(Element.ALIGN_JUSTIFIED);
			cuerpo.setLeading(18f);
			document.add(cuerpo);
			document.add(new Paragraph("\n\n\n", normal));

			Paragraph cierre = new Paragraph(
					"Sin otro particular por el momento, se agradece la atención y apoyo brindados.",
					normal);
			cierre.setAlignment(Element.ALIGN_JUSTIFIED);
			cierre.setLeading(18f);
			document.add(cierre);
			document.add(new Paragraph("\n\n\n", normal));

			Paragraph atentamente = new Paragraph("A t e n t a m e n t e", normal);
			atentamente.setAlignment(Element.ALIGN_CENTER);
			document.add(atentamente);
			document.add(new Paragraph("\n\n\n", normal));

			PdfPTable firmas = new PdfPTable(1);
			firmas.setWidthPercentage(88);

			PdfPCell firma = new PdfPCell();
			firma.setBorder(PdfPCell.NO_BORDER);
			firma.addElement(crearLineaFirma(residente.toUpperCase(), matricula, negrita, normal));
			firmas.addCell(firma);
			document.add(firmas);

			document.add(new Paragraph("\n\n", normal));
			document.add(new Paragraph("C.c.p. Archivo.", normal));

			document.close();

			String nombreArchivo = "5_Entrega_de_Reporte_a_Empresa_" + idResidencia + ".pdf";
			serviceDocumento.registrarDocumentoGenerado(
					idResidencia,
					TipoDocumentoResidencia.ENTREGA_REPORTE_EMPRESA,
					nombreArchivo);

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + nombreArchivo)
					.contentType(MediaType.APPLICATION_PDF)
					.body(salida.toByteArray());

		} catch (Exception e) {
			throw new RuntimeException("Error al generar la entrega de reporte a empresa: " + e.getMessage());
		}
	}

	@GetMapping("/documentos-residencia/{idResidencia:\\d+}/oficio-entrega-division-pdf")
	public ResponseEntity<byte[]> generarOficioEntregaDivisionPdf(
			@PathVariable("idResidencia") Integer idResidencia,
			Authentication authentication) {

		try {
			Residencia residencia = serviceResidencia.buscarPorIdResidencia(idResidencia);
			seguridadResidenciaService.validarAccesoResidencia(residencia, authentication);

			if (residencia == null) {
				throw new RuntimeException("La residencia no existe.");
			}

			ByteArrayOutputStream salida = new ByteArrayOutputStream();
			Document document = new Document(PageSize.LETTER, 55, 55, 42, 50);
			PdfWriter.getInstance(document, salida);
			document.open();

			Font normal = FontFactory.getFont(FontFactory.HELVETICA, 9.6f);
			Font negrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.8f);
			Font negritaGrande = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.6f);

			agregarEncabezadoOficioInstitucional(document);

			Paragraph fecha = new Paragraph("Chilpancingo, Gro., " + formatearFechaLarga(fechaActualMexico()), normal);
			fecha.setAlignment(Element.ALIGN_RIGHT);
			document.add(fecha);
			document.add(new Paragraph("\n", normal));

			Directivo jefeDivision = serviceDirectivo.buscarPorTipoActivo(TipoDirectivo.JEFE_DIVISION);
			String nombreDivision = valorOrDefault(
					jefeDivision != null ? jefeDivision.getNombreCompleto() : null,
					"JEFE DE LA DIVISIÓN DE ESTUDIOS PROFESIONALES");
			String puestoDivision = valorOrDefault(
					jefeDivision != null ? jefeDivision.getPuesto() : null,
					"JEFE DE LA DIVISIÓN DE ESTUDIOS PROFESIONALES");

			Paragraph destinatario = new Paragraph();
			destinatario.setLeading(11.5f);
			destinatario.add(new Phrase(nombreDivision.toUpperCase() + "\n", negritaGrande));
			destinatario.add(new Phrase(puestoDivision.toUpperCase() + "\n", negritaGrande));
			destinatario.add(new Phrase("INSTITUTO TECNOLÓGICO DE CHILPANCINGO\n", negritaGrande));
			destinatario.add(new Phrase("P R E S E N T E", negritaGrande));
			document.add(destinatario);
			document.add(new Paragraph("\n", normal));

			String proyecto = valorOrDefault(residencia.getNombreProyecto(), "NOMBRE DEL PROYECTO");
			String carrera = valorOrDefault(obtenerCarrera(residencia), "PROGRAMA EDUCATIVO");
			String residente = valorOrDefault(
					residencia.getResidente() != null ? residencia.getResidente().getNombreCompleto() : null,
					"NOMBRE DEL RESIDENTE");
			String matricula = valorOrDefault(
					residencia.getResidente() != null ? residencia.getResidente().getMatricula() : null,
					"NÚMERO DE CONTROL");
			String periodo = valorOrDefault(formatearPeriodo(residencia.getPeriodo()), "PERIODO DE RESIDENCIA");

			Paragraph cuerpo = new Paragraph(
					"Por medio del presente hago entrega de la documentación relativa al Proyecto de Residencia Profesional denominado "
							+ proyecto.toUpperCase()
							+ ", desarrollado en el semestre "
							+ periodo
							+ " en el Programa Educativo de "
							+ carrera
							+ ". Se adjuntan los documentos requeridos para la integración del expediente final.",
					normal);
			cuerpo.setAlignment(Element.ALIGN_JUSTIFIED);
			cuerpo.setLeading(12.2f);
			document.add(cuerpo);
			document.add(new Paragraph("\n", normal));

			PdfPTable lista = new PdfPTable(1);
			lista.setWidthPercentage(100);
			lista.getDefaultCell().setPadding(0f);
			lista.addCell(celdaTexto("UNA CARPETA DIGITAL CON LOS SIGUIENTES DOCUMENTOS EN FORMATO PDF:", negrita, Element.ALIGN_LEFT, false));
			lista.addCell(celdaTexto("1. Reporte de Residencia Profesional.", normal, Element.ALIGN_LEFT, false));
			lista.addCell(celdaTexto("2. Oficio de entrega del reporte de Residencia Profesional a la División de Estudios Profesionales.", normal, Element.ALIGN_LEFT, false));
			lista.addCell(celdaTexto("3. Liberación del asesor interno.", normal, Element.ALIGN_LEFT, false));
			lista.addCell(celdaTexto("4. Oficio de entrega del reporte de Residencia Profesional a la empresa o institución.", normal, Element.ALIGN_LEFT, false));
			lista.addCell(celdaTexto("5. Carta de presentación y agradecimiento con sello de recibido por la empresa o institución.", normal, Element.ALIGN_LEFT, false));
			lista.addCell(celdaTexto("6. Seguimiento y evaluaciones del estudiante residente.", normal, Element.ALIGN_LEFT, false));
			lista.addCell(celdaTexto("7. Evidencias complementarias que obren en el expediente.", normal, Element.ALIGN_LEFT, false));
			lista.addCell(celdaTexto("DOCUMENTOS FÍSICOS ORIGINALES:", negrita, Element.ALIGN_LEFT, false));
			lista.addCell(celdaTexto("1. Reporte de Residencia Profesional con firmas y sellos correspondientes.", normal, Element.ALIGN_LEFT, false));
			lista.addCell(celdaTexto("2. Liberación del asesor interno con sello de recibido por el Departamento Académico.", normal, Element.ALIGN_LEFT, false));
			lista.addCell(celdaTexto("3. Oficio de entrega del reporte de Residencia Profesional a la empresa o institución con sello de recibido.", normal, Element.ALIGN_LEFT, false));
			lista.addCell(celdaTexto("4. Carta de presentación y agradecimiento con sellos de recibido.", normal, Element.ALIGN_LEFT, false));
			document.add(lista);
			document.add(new Paragraph("\n", normal));

			Paragraph cierre = new Paragraph(
					"Lo anterior con la finalidad de dar continuidad al trámite de asignación de calificación y conclusión del Proyecto de Residencia Profesional. Sin otro particular, agradezco su atención.",
					normal);
			cierre.setAlignment(Element.ALIGN_JUSTIFIED);
			cierre.setLeading(12.2f);
			document.add(cierre);
			document.add(new Paragraph("\n\n", normal));

			Paragraph atentamente = new Paragraph("A t e n t a m e n t e", normal);
			atentamente.setAlignment(Element.ALIGN_CENTER);
			document.add(atentamente);
			document.add(new Paragraph("\n", normal));

			PdfPTable firmas = new PdfPTable(1);
			firmas.setWidthPercentage(78);

			PdfPCell firma = new PdfPCell();
			firma.setBorder(PdfPCell.NO_BORDER);
			firma.addElement(crearLineaFirma(residente.toUpperCase(), matricula, negrita, normal));
			firmas.addCell(firma);
			document.add(firmas);

			document.close();

			String nombreArchivo = "9_Oficio_de_entrega_de_Reporte_" + idResidencia + ".pdf";
			serviceDocumento.registrarDocumentoGenerado(
					idResidencia,
					TipoDocumentoResidencia.OFICIO_ENTREGA_DIVISION,
					nombreArchivo);

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + nombreArchivo)
					.contentType(MediaType.APPLICATION_PDF)
					.body(salida.toByteArray());

		} catch (Exception e) {
			throw new RuntimeException("Error al generar el oficio de entrega a división: " + e.getMessage());
		}
	}

	@GetMapping("/documentos-residencia/{idResidencia:\\d+}/calendario-pdf")
	public ResponseEntity<byte[]> generarCalendarioResidencia(
			@PathVariable("idResidencia") Integer idResidencia, Authentication authentication) {

		try {

			Residencia residencia = serviceResidencia.buscarPorIdResidencia(idResidencia);
			seguridadResidenciaService.validarAccesoResidencia(residencia, authentication);

			if (residencia == null) {
				throw new RuntimeException("La residencia no existe.");
			}

			byte[] pdf = calendarioResidenciaPdfService.generarCalendario(residencia);

			String nombreArchivo = "CALENDARIO_RESIDENCIA_" + residencia.getPeriodo().replace(" ", "_") + ".pdf";

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + nombreArchivo)
					.contentType(MediaType.APPLICATION_PDF)
					.body(pdf);

		} catch (Exception e) {
			throw new RuntimeException("Error al generar calendario de residencia: " + e.getMessage());
		}
	}

	@GetMapping("/documentos-residencia/{idResidencia:\\d+}/liberacion-asesor-interno-pdf")
	public ResponseEntity<byte[]> generarLiberacionAsesorInterno(
			@PathVariable("idResidencia") Integer idResidencia,
			Authentication authentication) {

		try {
			Residencia residencia = serviceResidencia.buscarPorIdResidencia(idResidencia);
			seguridadResidenciaService.validarAccesoResidencia(residencia, authentication);

			if (residencia == null) {
				throw new RuntimeException("La residencia no existe.");
			}

			EvaluacionResidencia externo = serviceEvaluacion.buscarPorResidenciaYTipo(
					idResidencia,
					TipoEvaluacionResidencia.REPORTE_FINAL_EXTERNO);

			EvaluacionResidencia interno = serviceEvaluacion.buscarPorResidenciaYTipo(
					idResidencia,
					TipoEvaluacionResidencia.REPORTE_FINAL_INTERNO);

			if (externo == null || interno == null
					|| externo.getCalificacion() == null
					|| interno.getCalificacion() == null) {
				throw new RuntimeException("La liberación del asesor interno se habilita cuando ambas evaluaciones finales están completas.");
			}

			ByteArrayOutputStream salida = new ByteArrayOutputStream();
			Document document = new Document(PageSize.LETTER, 60, 60, 45, 45);
			PdfWriter.getInstance(document, salida);
			document.open();

			Font normal = FontFactory.getFont(FontFactory.HELVETICA, 11f);
			Font negrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11f);
			Font negritaGrande = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f);

			agregarEncabezadoLiberacionInterna(document);
			document.add(new Paragraph("\n", normal));

			Directivo jefe = serviceDirectivo.buscarJefeDepartamentoPorCarrera(obtenerCarrera(residencia));
			String nombreJefe = jefe != null && jefe.getNombreCompleto() != null && !jefe.getNombreCompleto().trim().isEmpty()
					? jefe.getNombreCompleto().toUpperCase()
					: "JEFE DEL DEPARTAMENTO";
			String puestoJefe = jefe != null && jefe.getPuesto() != null && !jefe.getPuesto().trim().isEmpty()
					? jefe.getPuesto().toUpperCase()
					: "JEFE DEL DEPARTAMENTO DE " + obtenerCarrera(residencia).toUpperCase();

			String nombreProyecto = residencia.getNombreProyecto() != null ? residencia.getNombreProyecto().toUpperCase() : "";
			String carrera = obtenerCarrera(residencia);
			String residentes = obtenerNombresResidentes(residencia);
			String controles = obtenerMatriculasResidentes(residencia);
			String asesorInterno = residencia.getAsesorInterno() != null ? residencia.getAsesorInterno().getNombreCompleto().toUpperCase() : "";
			String fechaDocumento = LocalDate.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy"));
			String fechaInicio = residencia.getFechaInicio() != null ? residencia.getFechaInicio().format(DateTimeFormatter.ofPattern("dd 'de' MMMM")) : "";
			String fechaFin = residencia.getFechaFin() != null ? residencia.getFechaFin().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")) : "";

			Paragraph fecha = new Paragraph("Chilpancingo, Gro., " + fechaDocumento, normal);
			fecha.setAlignment(Element.ALIGN_RIGHT);
			document.add(fecha);
			document.add(new Paragraph("\n", normal));

			Paragraph destinatario = new Paragraph();
			destinatario.setLeading(14f);
			destinatario.add(new Phrase(nombreJefe + "\n", negritaGrande));
			destinatario.add(new Phrase(puestoJefe + "\n", negritaGrande));
			destinatario.add(new Phrase("INSTITUTO TECNOLÓGICO DE CHILPANCINGO\n", negritaGrande));
			destinatario.add(new Phrase("P R E S E N T E", negritaGrande));
			document.add(destinatario);
			document.add(new Paragraph("\n", normal));

			Paragraph cuerpo1 = new Paragraph(
					"Por este medio comunico a usted, que el Proyecto de Residencia Profesional denominado "
							+ nombreProyecto
							+ ", realizado por " + residentes
							+ " con no. de control " + controles
							+ ", del Programa Educativo de " + carrera
							+ ", en el que fungí como asesor(a) interno(a); fue desarrollado en tiempo y forma de acuerdo con su programa de actividades en el periodo del "
							+ fechaInicio + " al " + fechaFin + ".",
					normal);
			cuerpo1.setAlignment(Element.ALIGN_JUSTIFIED);
			cuerpo1.setLeading(17f);
			document.add(cuerpo1);
			document.add(new Paragraph("\n", normal));

			Paragraph cuerpo2 = new Paragraph(
					"Por lo anterior, una vez que ha sido revisado y avalado el Reporte de Residencia Profesional mencionado, se da por concluido el proyecto, quedando liberadas las y los estudiantes que en él intervinieron.",
					normal);
			cuerpo2.setAlignment(Element.ALIGN_JUSTIFIED);
			cuerpo2.setLeading(17f);
			document.add(cuerpo2);
			document.add(new Paragraph("\n", normal));

			document.add(new Paragraph("Sin otro particular por el momento, reciba un cordial saludo.", normal));
			document.add(new Paragraph("\n", normal));
			document.add(new Paragraph("A t e n t a m e n t e", normal));
			document.add(new Paragraph("\n\n", normal));

			Paragraph firma = new Paragraph("____________________________________\n" + asesorInterno + "\nAsesor(a) Interno(a)", negrita);
			firma.setAlignment(Element.ALIGN_LEFT);
			document.add(firma);
			document.add(new Paragraph("\n", normal));
			document.add(new Paragraph("C.c.p. Archivo.", normal));

			document.close();

			String nombreArchivo = "LIBERACION_ASESOR_INTERNO_" + idResidencia + ".pdf";
			serviceDocumento.registrarDocumentoGenerado(idResidencia, TipoDocumentoResidencia.LIBERACION_ASESOR_INTERNO, nombreArchivo);

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + nombreArchivo)
					.contentType(MediaType.APPLICATION_PDF)
					.body(salida.toByteArray());

		} catch (Exception e) {
			throw new RuntimeException("Error al generar la liberación del asesor interno: " + e.getMessage());
		}
	}

	@PostMapping("/documentos-residencia/reporte-preliminar/upload")
	public String subirReportePreliminar(
			@RequestParam("idResidencia") Integer idResidencia,
			@RequestParam("archivoPdf") MultipartFile archivoPdf,
			RedirectAttributes flash,
			Authentication authentication) {

		try {

			Residencia residencia =
					serviceResidencia.buscarPorIdResidencia(idResidencia);

			if (residencia == null) {
				flash.addFlashAttribute("msgError",
						"La residencia no existe.");

				return "redirect:/residencias/index";
			}

			seguridadResidenciaService
					.validarAccesoResidencia(residencia, authentication);

			serviceDocumento.guardarDocumento(
					idResidencia,
					TipoDocumentoResidencia.REPORTE_PRELIMINAR,
					archivoPdf);

			flash.addFlashAttribute(
					"msgSuccess",
					"Reporte preliminar cargado correctamente.");

		} catch (AccessDeniedException e) {

			flash.addFlashAttribute(
					"msgError",
					"No tienes permiso para cargar el reporte preliminar de esta residencia.");
		} catch (RuntimeException e) {

			flash.addFlashAttribute(
					"msgError",
					e.getMessage());
		} catch (Exception e) {

			flash.addFlashAttribute(
					"msgError",
					"No fue posible cargar el reporte preliminar. Verifica que el archivo sea PDF y no exceda el tamano permitido.");
		}

		return "redirect:/documentos-residencia/" + idResidencia;
	}

	private Double calcularPromedio(EvaluacionResidencia externa, EvaluacionResidencia interna) {

		if (externa == null || interna == null) {
			return null;
		}

		if (externa.getCalificacion() == null || interna.getCalificacion() == null) {
			return null;
		}

		return (externa.getCalificacion() + interna.getCalificacion()) / 2.0;
	}

	private void addDato(PdfPTable table, String etiqueta, String valor, Font negrita, Font normal) {

		PdfPCell celda1 = new PdfPCell(new Phrase(etiqueta, negrita));
		PdfPCell celda2 = new PdfPCell(new Phrase(valor != null ? valor : "", normal));

		celda1.setPadding(5);
		celda2.setPadding(5);

		table.addCell(celda1);
		table.addCell(celda2);
	}

	private String fecha(EvaluacionResidencia evaluacion) {

		if (evaluacion == null || evaluacion.getFechaEvaluacion() == null) {
			return "";
		}

		return evaluacion.getFechaEvaluacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
	}

	private String obtenerCarrera(Residencia residencia) {

		if (residencia == null
				|| residencia.getResidente() == null
				|| residencia.getResidente().getEstudiante() == null
				|| residencia.getResidente().getEstudiante().getCarrera() == null
				|| residencia.getResidente().getEstudiante().getCarrera().getNombre() == null) {
			return "";
		}

		return residencia.getResidente().getEstudiante().getCarrera().getNombre();
	}

	private String obtenerNombresResidentes(Residencia residencia) {
		if (residencia == null || residencia.getResidente() == null) {
			return "";
		}
		return residencia.getResidente().getNombreCompleto();
	}

	private String obtenerMatriculasResidentes(Residencia residencia) {
		if (residencia == null || residencia.getResidente() == null) {
			return "";
		}
		return residencia.getResidente().getMatricula();
	}

	private String valorOrDefault(String valor, String valorDefault) {
		return valor != null && !valor.trim().isEmpty() ? valor.trim() : valorDefault;
	}

	private LocalDate fechaActualMexico() {
		return LocalDate.now(ZoneId.of("America/Mexico_City"));
	}

	private String formatearFechaLarga(LocalDate fecha) {
		return fecha != null
				? fecha.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "MX")))
				: "";
	}

	private String formatearPeriodo(String periodo) {
		if (periodo == null || periodo.trim().isEmpty()) {
			return "";
		}

		String periodoNormalizado = periodo.trim().toUpperCase();

		if (periodoNormalizado.startsWith("ENE-JUN")) {
			return periodoNormalizado.replace("ENE-JUN", "Enero - Junio");
		}

		if (periodoNormalizado.startsWith("AGO-DIC")) {
			return periodoNormalizado.replace("AGO-DIC", "Agosto - Diciembre");
		}

		return periodo;
	}

	private File resolverDirectorioProyectos() {
		String baseNormalizada = rutaBase != null ? rutaBase.trim() : "";
		String carpetaNormalizada = carpetaProyectos != null ? carpetaProyectos.trim() : "";

		File base = new File(baseNormalizada);

		if (!base.isAbsolute()) {
			base = new File(System.getProperty("user.dir"), baseNormalizada);
		}

		return new File(base, carpetaNormalizada);
	}

	private com.itextpdf.text.Image cargarImagenClasspath(String rutaClasspath) throws Exception {
		ClassPathResource resource = new ClassPathResource(rutaClasspath);

		try (InputStream inputStream = resource.getInputStream()) {
			return com.itextpdf.text.Image.getInstance(inputStream.readAllBytes());
		}
	}

	private void agregarEncabezadoInstitucional(Document document, Font negrita, Font normal) throws Exception {

		PdfPTable encabezado = new PdfPTable(2);
		encabezado.setWidthPercentage(100);
		encabezado.setWidths(new float[] { 45, 55 });

		PdfPCell celdaLogo = new PdfPCell();
		celdaLogo.setBorder(PdfPCell.NO_BORDER);

		try {
			com.itextpdf.text.Image logo = cargarImagenClasspath("static/img/logo-tecnm.png");
			logo.scaleToFit(190, 55);
			celdaLogo.addElement(logo);
		} catch (Exception e) {
			celdaLogo.addElement(new Paragraph("EDUCACIÓN | TecNM", negrita));
		}

		PdfPCell celdaTexto = new PdfPCell();
		celdaTexto.setBorder(PdfPCell.NO_BORDER);

		Paragraph instituto = new Paragraph(
				"Instituto Tecnológico de Chilpancingo\nDivisión de Estudios Profesionales",
				negrita);

		instituto.setAlignment(Element.ALIGN_RIGHT);

		celdaTexto.addElement(instituto);

		encabezado.addCell(celdaLogo);
		encabezado.addCell(celdaTexto);

		document.add(encabezado);
		document.add(new Paragraph("\n"));
	}

	private void agregarEncabezadoLiberacionInterna(Document document) throws Exception {

		PdfPTable encabezado = new PdfPTable(3);
		encabezado.setWidthPercentage(100);
		encabezado.setWidths(new float[] { 38, 27, 35 });
		encabezado.setSpacingAfter(10);

		PdfPCell izquierda = new PdfPCell();
		izquierda.setBorder(PdfPCell.NO_BORDER);
		izquierda.setVerticalAlignment(Element.ALIGN_TOP);

		try {
			com.itextpdf.text.Image logoEducacion = cargarImagenClasspath("static/img/logo-educacion_publica.png");
			logoEducacion.scaleToFit(230, 62);
			izquierda.addElement(logoEducacion);
		} catch (Exception e) {
			izquierda.addElement(new Paragraph("EDUCACIÓN", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
		}

		PdfPCell centro = new PdfPCell();
		centro.setBorder(PdfPCell.NO_BORDER);
		centro.setHorizontalAlignment(Element.ALIGN_CENTER);
		centro.setVerticalAlignment(Element.ALIGN_TOP);

		try {
			com.itextpdf.text.Image logoTecnm = cargarImagenClasspath("static/img/logo-tecnm.png");
			logoTecnm.scaleToFit(120, 55);
			logoTecnm.setAlignment(Element.ALIGN_CENTER);
			centro.addElement(logoTecnm);
		} catch (Exception e) {
			Paragraph fallbackTecnm = new Paragraph("TECNOLÓGICO NACIONAL DE MÉXICO",
					FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11));
			fallbackTecnm.setAlignment(Element.ALIGN_CENTER);
			centro.addElement(fallbackTecnm);
		}

		PdfPCell derecha = new PdfPCell();
		derecha.setBorder(PdfPCell.NO_BORDER);
		derecha.setHorizontalAlignment(Element.ALIGN_RIGHT);
		derecha.setVerticalAlignment(Element.ALIGN_TOP);

		try {
			com.itextpdf.text.Image logoMujer = cargarImagenClasspath("static/img/logo-mujer.png");
			logoMujer.scaleToFit(82, 62);
			logoMujer.setAlignment(Element.ALIGN_RIGHT);
			derecha.addElement(logoMujer);
		} catch (Exception e) {
			Paragraph fallbackMujer = new Paragraph("ITCh", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
			fallbackMujer.setAlignment(Element.ALIGN_RIGHT);
			derecha.addElement(fallbackMujer);
		}

		Paragraph instituto = new Paragraph(
				"Instituto Tecnológico de Chilpancingo\nDivisión de Estudios Profesionales",
				FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.5f));
		instituto.setAlignment(Element.ALIGN_RIGHT);
		instituto.setSpacingBefore(6);
		derecha.addElement(instituto);

		encabezado.addCell(izquierda);
		encabezado.addCell(centro);
		encabezado.addCell(derecha);

		document.add(encabezado);
	}

	private void agregarEncabezadoOficioInstitucional(Document document) throws Exception {

		PdfPTable encabezado = new PdfPTable(3);
		encabezado.setWidthPercentage(100);
		encabezado.setWidths(new float[] { 42, 23, 35 });
		encabezado.setSpacingAfter(6);

		PdfPCell izquierda = new PdfPCell();
		izquierda.setBorder(PdfPCell.NO_BORDER);
		izquierda.setVerticalAlignment(Element.ALIGN_TOP);

		try {
			com.itextpdf.text.Image logoEducacion = cargarImagenClasspath("static/img/logo-educacion_publica.png");
			logoEducacion.scaleToFit(240, 60);
			izquierda.addElement(logoEducacion);
		} catch (Exception e) {
			izquierda.addElement(new Paragraph("EDUCACIÓN", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
		}

		PdfPCell centro = new PdfPCell();
		centro.setBorder(PdfPCell.NO_BORDER);
		centro.setHorizontalAlignment(Element.ALIGN_CENTER);
		centro.setVerticalAlignment(Element.ALIGN_TOP);

		try {
			com.itextpdf.text.Image logoTecnm = cargarImagenClasspath("static/img/logo-jaguar-tecnm.png");
			logoTecnm.scaleToFit(78, 58);
			logoTecnm.setAlignment(Element.ALIGN_CENTER);
			centro.addElement(logoTecnm);
		} catch (Exception e) {
			try {
				com.itextpdf.text.Image logoTecnm = cargarImagenClasspath("static/img/logo-tecnm.png");
				logoTecnm.scaleToFit(110, 52);
				logoTecnm.setAlignment(Element.ALIGN_CENTER);
				centro.addElement(logoTecnm);
			} catch (Exception ex) {
				Paragraph fallbackTecnm = new Paragraph("TECNM",
					FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
				fallbackTecnm.setAlignment(Element.ALIGN_CENTER);
				centro.addElement(fallbackTecnm);
			}
		}

		PdfPCell derecha = new PdfPCell();
		derecha.setBorder(PdfPCell.NO_BORDER);
		derecha.setHorizontalAlignment(Element.ALIGN_RIGHT);
		derecha.setVerticalAlignment(Element.ALIGN_TOP);

		Paragraph instituto = new Paragraph(
				"Instituto Tecnológico de Chilpancingo\nDivisión de Estudios Profesionales",
				FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.5f));
		instituto.setAlignment(Element.ALIGN_RIGHT);
		instituto.setSpacingBefore(6);
		derecha.addElement(instituto);

		encabezado.addCell(izquierda);
		encabezado.addCell(centro);
		encabezado.addCell(derecha);

		document.add(encabezado);
		document.add(new Paragraph("\n"));
	}

	private PdfPCell celdaTexto(String texto, Font fuente, int align, boolean borde) {

		PdfPCell celda = new PdfPCell(new Phrase(texto != null ? texto : "", fuente));

		celda.setPadding(5);
		celda.setHorizontalAlignment(align);
		celda.setVerticalAlignment(Element.ALIGN_MIDDLE);

		if (!borde) {
			celda.setBorder(PdfPCell.NO_BORDER);
		}

		return celda;
	}

	private Paragraph crearLineaFirma(String nombre, String matricula, Font negrita, Font normal) {
		Paragraph firma = new Paragraph();
		firma.setAlignment(Element.ALIGN_CENTER);
		firma.add(new Phrase("____________________________________________\n", normal));
		firma.add(new Phrase(nombre + "\n", negrita));
		firma.add(new Phrase("No. de control: " + matricula, normal));
		return firma;
	}

	private void addDatoOficial(PdfPTable tabla, String etiqueta, String valor, Font negrita, Font normal) {

		PdfPCell c1 = celdaTexto(etiqueta, negrita, Element.ALIGN_LEFT, true);
		PdfPCell c2 = celdaTexto(valor, normal, Element.ALIGN_LEFT, true);

		tabla.addCell(c1);
		tabla.addCell(c2);
	}

	private PdfPTable tablaCriteriosOficial() throws Exception {

		PdfPTable tabla = new PdfPTable(3);
		tabla.setWidthPercentage(100);
		tabla.setWidths(new float[] { 72, 12, 16 });

		Font head = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f);

		PdfPCell h1 = celdaTexto("Criterios a evaluar", head, Element.ALIGN_CENTER, true);
		PdfPCell h2 = celdaTexto("Valor", head, Element.ALIGN_CENTER, true);
		PdfPCell h3 = celdaTexto("Evaluación", head, Element.ALIGN_CENTER, true);

		h1.setBackgroundColor(new BaseColor(235, 240, 247));
		h2.setBackgroundColor(new BaseColor(235, 240, 247));
		h3.setBackgroundColor(new BaseColor(235, 240, 247));

		tabla.addCell(h1);
		tabla.addCell(h2);
		tabla.addCell(h3);

		return tabla;
	}

	private void addCriterioOficial(PdfPTable tabla, String criterio, String valor, String evaluacion) {

		Font normal = FontFactory.getFont(FontFactory.HELVETICA, 8f);

		tabla.addCell(celdaTexto(criterio, normal, Element.ALIGN_LEFT, true));
		tabla.addCell(celdaTexto(valor, normal, Element.ALIGN_CENTER, true));
		tabla.addCell(celdaTexto(evaluacion != null ? evaluacion : "", normal, Element.ALIGN_CENTER, true));
	}

	private void agregarBloqueFirmas(Document document, String nombreAsesor, String sello, String fecha, Font normal, Font negrita) throws Exception {

		document.add(new Paragraph("\nObservaciones:", negrita));
		document.add(new Paragraph("\n\n"));

		PdfPTable firmas = new PdfPTable(3);
		firmas.setWidthPercentage(100);
		firmas.setWidths(new float[] { 40, 35, 25 });

		PdfPCell f1 = new PdfPCell();
		f1.setBorder(PdfPCell.NO_BORDER);
		f1.addElement(new Paragraph(nombreAsesor != null ? nombreAsesor.toUpperCase() : "", negrita));
		f1.addElement(new Paragraph("Nombre y firma del asesor", normal));

		PdfPCell f2 = new PdfPCell();
		f2.setBorder(PdfPCell.NO_BORDER);

		Paragraph selloP = new Paragraph(sello, normal);
		selloP.setAlignment(Element.ALIGN_CENTER);

		f2.addElement(selloP);

		PdfPCell f3 = new PdfPCell();
		f3.setBorder(PdfPCell.NO_BORDER);

		Paragraph fechaP = new Paragraph(fecha != null ? fecha : "", negrita);
		fechaP.setAlignment(Element.ALIGN_CENTER);

		f3.addElement(fechaP);

		Paragraph fechaTxt = new Paragraph("Fecha de Evaluación", normal);
		fechaTxt.setAlignment(Element.ALIGN_CENTER);

		f3.addElement(fechaTxt);

		firmas.addCell(f1);
		firmas.addCell(f2);
		firmas.addCell(f3);

		document.add(firmas);
	}

	private void agregarCriteriosReporteFinalOficial(PdfPTable tabla, EvaluacionResidencia evaluacion) {

		addCriterioOficial(tabla, "Portada.", "2", criterio(evaluacion, 1));
		addCriterioOficial(tabla, "Agradecimientos.", "2", criterio(evaluacion, 2));
		addCriterioOficial(tabla, "Resumen.", "2", criterio(evaluacion, 3));
		addCriterioOficial(tabla, "Índice.", "2", criterio(evaluacion, 4));
		addCriterioOficial(tabla, "Introducción.", "2", criterio(evaluacion, 5));
		addCriterioOficial(tabla, "Problemas a resolver, priorizándolos.", "5", criterio(evaluacion, 6));
		addCriterioOficial(tabla, "Objetivos.", "5", criterio(evaluacion, 7));
		addCriterioOficial(tabla, "Marco teórico.", "10", criterio(evaluacion, 8));
		addCriterioOficial(tabla, "Procedimiento y descripción de actividades.", "5", criterio(evaluacion, 9));

		addCriterioOficial(
				tabla,
				"Resultados, planos, gráficas, prototipos, manuales, programas, análisis estadísticos, modelos matemáticos, simulaciones, normativas y restricciones.",
				"45",
				criterio(evaluacion, 10));

		addCriterioOficial(
				tabla,
				"Conclusiones, recomendaciones y experiencia profesional adquirida.",
				"15",
				criterio(evaluacion, 11));

		addCriterioOficial(
				tabla,
				"Competencias desarrolladas y/o aplicadas.",
				"3",
				criterio(evaluacion, 12));

		addCriterioOficial(
				tabla,
				"Fuentes de información.",
				"2",
				criterio(evaluacion, 13));
	}

	private void agregarPieInstitucional(Document document, Font pie) throws Exception {

		Paragraph p = new Paragraph(
				"Av. José Francisco Ruiz Massieu No. 5, Colonia Villa Moderna, Chilpancingo de los Bravo, Guerrero. México.\n"
						+ "Tel. (747) 45 4 1300, Ext. 1328, email: dep@chilpancingo.tecnm.mx\n"
						+ "http://chilpancingo.tecnm.mx/              https://www.facebook.com/TecNMcampusChilpancingo",
				pie);

		p.setAlignment(Element.ALIGN_LEFT);
		p.setSpacingBefore(25);

		document.add(p);
	}

	private String criterio(EvaluacionResidencia evaluacion, int numero) {

		if (evaluacion == null) {
			return "";
		}

		Double valor = null;

		switch (numero) {
		case 1:
			valor = evaluacion.getCriterio1();
			break;
		case 2:
			valor = evaluacion.getCriterio2();
			break;
		case 3:
			valor = evaluacion.getCriterio3();
			break;
		case 4:
			valor = evaluacion.getCriterio4();
			break;
		case 5:
			valor = evaluacion.getCriterio5();
			break;
		case 6:
			valor = evaluacion.getCriterio6();
			break;
		case 7:
			valor = evaluacion.getCriterio7();
			break;
		case 8:
			valor = evaluacion.getCriterio8();
			break;
		case 9:
			valor = evaluacion.getCriterio9();
			break;
		case 10:
			valor = evaluacion.getCriterio10();
			break;
		case 11:
			valor = evaluacion.getCriterio11();
			break;
		case 12:
			valor = evaluacion.getCriterio12();
			break;
		case 13:
			valor = evaluacion.getCriterio13();
			break;
		default:
			valor = null;
		}

		return valor != null ? String.format("%.2f", valor) : "";
	}
}
