package itch.tsp.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import itch.tsp.model.Residencia;

@Service
public class CalendarioResidenciaPdfService {

	private static final String RUTA_IMG = "static/img/";
	private static final BaseColor AZUL_ENCABEZADO = new BaseColor(24, 99, 128);
	private static final BaseColor AZUL_CLARO = new BaseColor(191, 230, 242);
	private static final BaseColor VERDE_ESTUDIANTE = new BaseColor(126, 222, 137);
	private static final BaseColor AZUL_DIVISION = new BaseColor(84, 153, 215);
	private static final BaseColor AMARILLO_JEFE = new BaseColor(255, 192, 0);
	private static final BaseColor MORADO_GESTION = new BaseColor(214, 151, 214);
	private static final BaseColor NARANJA_EMPRESA = new BaseColor(237, 125, 49);
	private static final BaseColor CYAN = new BaseColor(78, 233, 235);
	private static final BaseColor VERDE_FUERTE = new BaseColor(0, 255, 0);
	private static final BaseColor ROSA = new BaseColor(255, 0, 255);
	private static final BaseColor AZUL_FUERTE = new BaseColor(0, 112, 192);

	public byte[] generarCalendario(Residencia residencia) {

		try {
			String periodo = residencia.getPeriodo() != null ? residencia.getPeriodo().toUpperCase().trim() : "";
			int anio = obtenerAnio(periodo);

			List<ActividadCalendario> actividades;

			if (periodo.contains("ENE-JUN")) {
				actividades = calendarioEneJun(anio);
			} else if (periodo.contains("AGO-DIC")) {
				actividades = calendarioAgoDic(anio);
			} else {
				throw new RuntimeException("No existe calendario configurado para el periodo: " + periodo);
			}

			ByteArrayOutputStream salida = new ByteArrayOutputStream();

			Document document = new Document(PageSize.LETTER, 35, 35, 30, 30);
			PdfWriter.getInstance(document, salida);

			document.open();

			agregarPaginaTabla(document, periodo, anio, actividades);
			agregarPaginaCronograma(document, periodo, anio, actividades);

			document.close();

			return salida.toByteArray();

		} catch (Exception e) {
			throw new RuntimeException("Error al generar calendario de residencia: " + e.getMessage());
		}
	}

	private void agregarPaginaTabla(Document document, String periodo, int anio, List<ActividadCalendario> actividades)
			throws Exception {

		Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
		Font encabezado = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.WHITE);
		Font normal = FontFactory.getFont(FontFactory.HELVETICA, 8);
		Font negrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
		Font pie = FontFactory.getFont(FontFactory.HELVETICA, 7);

		agregarLogosOficiales(document);

		Paragraph tituloDoc = new Paragraph("Calendario de Residencia Profesional " + tituloPeriodo(periodo, anio),
				titulo);
		tituloDoc.setAlignment(Element.ALIGN_CENTER);
		tituloDoc.setSpacingBefore(12);
		tituloDoc.setSpacingAfter(10);
		document.add(tituloDoc);

		PdfPTable tabla = crearTablaActividades(encabezado);

		int contador = 0;

		for (ActividadCalendario a : actividades) {
			contador++;

			if (periodo.contains("ENE-JUN") && contador == 13) {
				document.add(tabla);
				agregarPie(document, pie);
				document.newPage();

				agregarLogosOficiales(document);
				tabla = crearTablaActividades(encabezado);
			}

			if (periodo.contains("AGO-DIC") && contador == 13) {
				document.add(tabla);
				agregarPie(document, pie);
				document.newPage();

				agregarLogosOficiales(document);
				tabla = crearTablaActividades(encabezado);
			}

			agregarCelda(tabla, String.valueOf(a.numero), negrita, Element.ALIGN_CENTER, AZUL_CLARO);
			agregarCelda(tabla, a.responsable, negrita, Element.ALIGN_LEFT, colorResponsable(a.responsable));
			agregarCelda(tabla, a.fecha, negrita, Element.ALIGN_CENTER, colorFecha(a.numero));
			agregarCelda(tabla, a.actividad, normal, Element.ALIGN_LEFT, AZUL_CLARO);
		}

		document.add(tabla);

		Paragraph nota = new Paragraph(
				"*Calificación en acta = 10% de Promedio 1 + 10% de Promedio 2 + 80% de Promedio 3\n"
						+ "Promedio 1 = Promedio de las calificaciones asentadas por asesor interno y externo en el primer seguimiento (anexo XXIX)\n"
						+ "Promedio 2 = Promedio de las calificaciones asentadas por asesor interno y externo en el segundo seguimiento (anexo XXIX)\n"
						+ "Promedio 3 = Promedio de las calificaciones asentadas por asesor interno y externo en la evaluación del Reporte de Residencia Profesional (anexo XXX)",
				negrita);
		nota.setSpacingBefore(12);
		nota.setAlignment(Element.ALIGN_LEFT);
		document.add(nota);

		agregarPie(document, pie);
	}

	private PdfPTable crearTablaActividades(Font encabezado) throws Exception {
		PdfPTable tabla = new PdfPTable(4);
		tabla.setWidthPercentage(100);
		tabla.setWidths(new float[] { 8, 28, 22, 42 });
		tabla.setSpacingBefore(8);

		agregarEncabezadoTabla(tabla, "Num.", encabezado);
		agregarEncabezadoTabla(tabla, "RESPONSABLE", encabezado);
		agregarEncabezadoTabla(tabla, "FECHA", encabezado);
		agregarEncabezadoTabla(tabla, "ACTIVIDAD", encabezado);

		return tabla;
	}

	private void agregarPaginaCronograma(Document document, String periodo, int anio, List<ActividadCalendario> actividades)
			throws Exception {

		document.setPageSize(PageSize.LETTER.rotate());
		document.newPage();

		Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
		Font normal = FontFactory.getFont(FontFactory.HELVETICA, 6);
		Font negrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6);
		Font encabezado = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
		Font pie = FontFactory.getFont(FontFactory.HELVETICA, 7);

		agregarLogosOficiales(document);

		Paragraph tituloDoc = new Paragraph("Calendario de Residencia Profesional " + tituloPeriodo(periodo, anio),
				titulo);
		tituloDoc.setAlignment(Element.ALIGN_CENTER);
		tituloDoc.setSpacingBefore(8);
		tituloDoc.setSpacingAfter(6);
		document.add(tituloDoc);

		if (periodo.contains("ENE-JUN")) {
			agregarCronogramaEneJun(document, actividades, encabezado, normal, negrita);
		} else {
			agregarCronogramaAgoDic(document, actividades, encabezado, normal, negrita, anio);
		}

		agregarPie(document, pie);
	}

	private void agregarCronogramaEneJun(Document document, List<ActividadCalendario> actividades, Font encabezado,
			Font normal, Font negrita) throws Exception {

		PdfPTable meses = new PdfPTable(5);
		meses.setWidthPercentage(100);
		meses.setWidths(new float[] { 32, 8, 8, 37, 15 });

		agregarMes(meses, "ENERO", encabezado);
		agregarMes(meses, "MARZO", encabezado);
		agregarMes(meses, "ABRIL", encabezado);
		agregarMes(meses, "MAYO", encabezado);
		agregarMes(meses, "JUNIO", encabezado);

		document.add(meses);

		PdfPTable grid = new PdfPTable(23);
		grid.setWidthPercentage(100);

		for (ActividadCalendario a : actividades) {
			for (int col = 1; col <= 23; col++) {
				PdfPCell celda;

				if (col == a.numero) {
					celda = new PdfPCell(new Phrase(a.numero + "  " + a.responsable.toUpperCase(), negrita));
					celda.setBackgroundColor(colorResponsable(a.responsable));
				} else {
					celda = new PdfPCell(new Phrase(" ", normal));
					celda.setBackgroundColor(BaseColor.WHITE);
				}

				celda.setFixedHeight(16);
				celda.setBorderColor(new BaseColor(220, 220, 220));
				celda.setPadding(2);
				grid.addCell(celda);
			}
		}

		document.add(grid);
	}

	private void agregarCronogramaAgoDic(Document document, List<ActividadCalendario> actividades, Font encabezado,
			Font normal, Font negrita, int anio) throws Exception {

		PdfPTable meses = new PdfPTable(6);
		meses.setWidthPercentage(100);

		agregarMes(meses, "AGOSTO " + anio, encabezado);
		agregarMes(meses, "SEPTIEMBRE " + anio, encabezado);
		agregarMes(meses, "OCTUBRE " + anio, encabezado);
		agregarMes(meses, "NOVIEMBRE " + anio, encabezado);
		agregarMes(meses, "DICIEMBRE " + anio, encabezado);
		agregarMes(meses, "ENERO " + (anio + 1), encabezado);

		document.add(meses);

		PdfPTable grid = new PdfPTable(26);
		grid.setWidthPercentage(100);

		for (ActividadCalendario a : actividades) {
			for (int col = 1; col <= 26; col++) {
				PdfPCell celda;

				if (col == a.numero) {
					celda = new PdfPCell(new Phrase(a.numero + "  " + a.responsable.toUpperCase(), negrita));
					celda.setBackgroundColor(colorResponsable(a.responsable));
				} else {
					celda = new PdfPCell(new Phrase(" ", normal));
					celda.setBackgroundColor(BaseColor.WHITE);
				}

				celda.setFixedHeight(16);
				celda.setBorderColor(new BaseColor(220, 220, 220));
				celda.setPadding(2);
				grid.addCell(celda);
			}
		}

		document.add(grid);
	}

	private void agregarLogosOficiales(Document document) throws Exception {

		PdfPTable tabla = new PdfPTable(3);
		tabla.setWidthPercentage(100);
		tabla.setWidths(new float[] { 40, 25, 35 });

		PdfPCell izquierda = new PdfPCell();
		izquierda.setBorder(PdfPCell.NO_BORDER);

		try {
			Image logoEducacion = cargarImagenClasspath("logo-educacion_publica.png");
			logoEducacion.scaleToFit(170, 55);
			izquierda.addElement(logoEducacion);
		} catch (Exception e) {
			izquierda.addElement(new Phrase(""));
		}

		PdfPCell centro = new PdfPCell();
		centro.setBorder(PdfPCell.NO_BORDER);
		centro.setHorizontalAlignment(Element.ALIGN_CENTER);

		try {
			Image logoTecnm = cargarImagenClasspath("logo-jaguar-tecnm.png");
			logoTecnm.scaleToFit(95, 55);
			logoTecnm.setAlignment(Element.ALIGN_CENTER);
			centro.addElement(logoTecnm);
		} catch (Exception e) {
			centro.addElement(new Phrase(""));
		}

		PdfPCell derecha = new PdfPCell();
		derecha.setBorder(PdfPCell.NO_BORDER);
		derecha.setHorizontalAlignment(Element.ALIGN_RIGHT);

		try {
			Image logoMujer = cargarImagenClasspath("logo-mujer.png");
			logoMujer.scaleToFit(90, 62);
			logoMujer.setAlignment(Element.ALIGN_RIGHT);
			derecha.addElement(logoMujer);
		} catch (Exception e) {
			derecha.addElement(new Phrase(""));
		}

		tabla.addCell(izquierda);
		tabla.addCell(centro);
		tabla.addCell(derecha);

		document.add(tabla);

		Font fuente = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);

		Paragraph institucion = new Paragraph(
				"Instituto Tecnológico de Chilpancingo\nDivisión de Estudios Profesionales",
				fuente);
		institucion.setAlignment(Element.ALIGN_RIGHT);
		institucion.setSpacingAfter(6);
		document.add(institucion);
	}

	private void agregarMes(PdfPTable tabla, String texto, Font fuente) {
		PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
		celda.setHorizontalAlignment(Element.ALIGN_CENTER);
		celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
		celda.setPadding(4);
		celda.setBorderColor(BaseColor.BLACK);
		tabla.addCell(celda);
	}

	private void agregarEncabezadoTabla(PdfPTable tabla, String texto, Font fuente) {
		PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
		celda.setBackgroundColor(AZUL_ENCABEZADO);
		celda.setHorizontalAlignment(Element.ALIGN_CENTER);
		celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
		celda.setPadding(7);
		tabla.addCell(celda);
	}

	private void agregarCelda(PdfPTable tabla, String texto, Font fuente, int alineacion, BaseColor color) {
		PdfPCell celda = new PdfPCell(new Phrase(texto != null ? texto : "", fuente));
		celda.setBackgroundColor(color);
		celda.setHorizontalAlignment(alineacion);
		celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
		celda.setPadding(5);
		celda.setBorderColor(BaseColor.BLACK);
		tabla.addCell(celda);
	}

	private void agregarPie(Document document, Font pie) throws Exception {

		PdfPTable tabla = new PdfPTable(3);
		tabla.setWidthPercentage(100);
		tabla.setWidths(new float[] { 25, 50, 25 });
		tabla.setSpacingBefore(15);

		PdfPCell izquierda = new PdfPCell();
		izquierda.setBorder(PdfPCell.NO_BORDER);

		try {
			Image logoMargarita = cargarImagenClasspath("logo-margaritamaza.png");
			logoMargarita.scaleToFit(90, 55);
			izquierda.addElement(logoMargarita);
		} catch (Exception e) {
			izquierda.addElement(new Phrase(""));
		}

		PdfPCell centro = new PdfPCell();
		centro.setBorder(PdfPCell.NO_BORDER);

		Paragraph p = new Paragraph(
				"Av. José Francisco Ruiz Massieu No. 5, Colonia Villa Moderna, Chilpancingo de los Bravo, Guerrero. México.\n"
						+ "Tel. (747) 45 4 1300, Ext. 1328, email: dep@chilpancingo.tecnm.mx\n"
						+ "http://chilpancingo.tecnm.mx/   https://www.facebook.com/TecNMcampusChilpancingo",
				pie);
		p.setAlignment(Element.ALIGN_CENTER);
		centro.addElement(p);

		PdfPCell derecha = new PdfPCell();
		derecha.setBorder(PdfPCell.NO_BORDER);

		tabla.addCell(izquierda);
		tabla.addCell(centro);
		tabla.addCell(derecha);

		document.add(tabla);
	}

	private Image cargarImagenClasspath(String nombreArchivo) throws Exception {
		ClassPathResource resource = new ClassPathResource(RUTA_IMG + nombreArchivo);
		try (InputStream inputStream = resource.getInputStream()) {
			return Image.getInstance(inputStream.readAllBytes());
		}
	}

	private BaseColor colorResponsable(String responsable) {
		if (responsable == null) {
			return BaseColor.WHITE;
		}

		String r = responsable.toUpperCase();

		if (r.contains("ESTUDIANTE")) {
			return VERDE_ESTUDIANTE;
		}

		if (r.contains("DIVISIÓN")) {
			return AZUL_DIVISION;
		}

		if (r.contains("JEFE") || r.contains("DEPARTAMENTO")) {
			return AMARILLO_JEFE;
		}

		if (r.contains("GESTIÓN")) {
			return MORADO_GESTION;
		}

		if (r.contains("EMPRESA")) {
			return NARANJA_EMPRESA;
		}

		if (r.contains("ASESOR")) {
			return BaseColor.YELLOW;
		}

		return AZUL_CLARO;
	}

	private BaseColor colorFecha(Integer numero) {
		switch (numero) {
		case 1:
			return CYAN;
		case 3:
			return AMARILLO_JEFE;
		case 4:
			return new BaseColor(82, 205, 91);
		case 5:
			return BaseColor.YELLOW;
		case 6:
			return MORADO_GESTION;
		case 7:
			return NARANJA_EMPRESA;
		case 8:
			return new BaseColor(226, 239, 218);
		case 10:
			return new BaseColor(244, 176, 132);
		case 11:
			return ROSA;
		case 12:
			return CYAN;
		case 13:
			return VERDE_FUERTE;
		case 14:
			return CYAN;
		case 15:
		case 16:
		case 17:
		case 18:
		case 19:
		case 20:
		case 21:
		case 22:
		case 23:
		case 24:
		case 25:
			return AZUL_FUERTE;
		case 26:
			return new BaseColor(146, 208, 80);
		default:
			return AZUL_CLARO;
		}
	}

	private int obtenerAnio(String periodo) {
		String[] partes = periodo.split(" ");

		for (String parte : partes) {
			try {
				return Integer.parseInt(parte.trim());
			} catch (Exception e) {
			}
		}

		throw new RuntimeException("No se pudo obtener el año del periodo: " + periodo);
	}

	private String tituloPeriodo(String periodo, int anio) {
		if (periodo.contains("ENE-JUN")) {
			return "Enero – Junio " + anio;
		}

		return "Agosto - Diciembre " + anio;
	}

	private List<ActividadCalendario> calendarioEneJun(int anio) {
		List<ActividadCalendario> lista = new ArrayList<>();

		lista.add(new ActividadCalendario(1, "Estudiante", "07 AL 08 DE ENERO",
				"Entrega Reporte Preliminar a División de Estudios Profesionales"));
		lista.add(new ActividadCalendario(2, "División de Estudios Profesionales", "09 DE ENERO",
				"Entrega Reporte Preliminar al Jefe Académico"));
		lista.add(new ActividadCalendario(3, "Jefe de Departamento Académico y Academias", "12 AL 15 DE ENERO",
				"Dictamen de Reporte Preliminar y asignación de asesor interno"));
		lista.add(new ActividadCalendario(4, "División de Estudios Profesionales", "15 AL 20 DE ENERO",
				"Inscripción a Residencia Profesional"));
		lista.add(new ActividadCalendario(5, "División de Estudios Profesionales", "19 AL 21 DE ENERO",
				"Recibe dictámenes de Reporte Preliminar y envía a Departamento de Gestión Tecnológica y Vinculación"));
		lista.add(new ActividadCalendario(6, "Gestión Tecnológica y Vinculación", "21 AL 23 DE ENERO",
				"Elabora carta de presentación y agradecimiento"));
		lista.add(new ActividadCalendario(7, "Estudiante", "21 AL 23 DE ENERO",
				"Entrega carta de presentación y agradecimiento a la empresa o institución"));
		lista.add(new ActividadCalendario(8, "Empresa o dependencia", "26 AL 30 DE ENERO",
				"Elabora Carta de Aceptación y entrega al Departamento de Gestión Tecnológica y Vinculación a través del Estudiante"));
		lista.add(new ActividadCalendario(9, "Estudiante", "26 DE ENERO",
				"Inicia periodo de Residencia Profesional"));
		lista.add(new ActividadCalendario(10, "Estudiante", "02 AL 06 DE MARZO",
				"Entrega primer formato de evaluación y seguimiento de Residencia Profesional a División de Estudios Profesionales (anexo XXIX con sello del Departamento Académico)"));
		lista.add(new ActividadCalendario(11, "Estudiante", "27 DE ABRIL AL 04 DE MAYO",
				"Entrega segundo formato de evaluación y seguimiento de Residencia Profesional a División de Estudios Profesionales (anexo XXIX con sello del Departamento Académico)"));
		lista.add(new ActividadCalendario(12, "División de Estudios Profesionales", "30 DE ABRIL",
				"Reunión con estudiantes para integración del expediente para conclusión de Residencia Profesional"));
		lista.add(new ActividadCalendario(13, "Estudiante", "04 AL 29 DE MAYO",
				"Elabora reporte de Residencia Profesional"));
		lista.add(new ActividadCalendario(14, "Estudiante", "29 DE MAYO",
				"Termina periodo de Residencia Profesional"));
		lista.add(new ActividadCalendario(15, "Asesor Interno y Externo", "01 AL 05 DE JUNIO",
				"Evalúa Reporte de Residencia Profesional y asienta resultados en anexo XXX"));
		lista.add(new ActividadCalendario(16, "Estudiante", "01 AL 05 DE JUNIO",
				"Entrega el Reporte de Residencia Profesional a la empresa o entidad y le solicita carta de terminación"));
		lista.add(new ActividadCalendario(17, "Estudiante", "01 AL 05 DE JUNIO",
				"Entrega formato de evaluación del Reporte de Residencia a División de Estudios Profesionales (Anexo XXX con sello del Departamento Académico)"));
		lista.add(new ActividadCalendario(18, "Empresa o dependencia", "08 AL 12 DE JUNIO",
				"Elabora carta de terminación de Residencia Profesional y entrega al Departamento de Gestión Tecnológica y Vinculación a través del Estudiante"));
		lista.add(new ActividadCalendario(19, "Asesor interno", "10 AL 12 DE JUNIO",
				"Elabora documento de liberación de Residencia Profesional"));
		lista.add(new ActividadCalendario(20, "Estudiante", "10 AL 12 DE JUNIO",
				"Entrega documentos físicos y digitales a División de Estudios Profesionales para conclusión de la Residencia Profesional"));
		lista.add(new ActividadCalendario(21, "Asesor Interno", "15 AL 17 DE JUNIO",
				"Registra calificación de Residencia Profesional en Mindbox y entrega al Departamento Académico el acta de calificación"));
		lista.add(new ActividadCalendario(22, "Jefe de Departamento Académico", "18 Y 19 DE JUNIO",
				"Envía actas de calificaciones a División de Estudios Profesionales"));
		lista.add(new ActividadCalendario(23, "División de Estudios Profesionales", "19 DE JUNIO",
				"Entrega Actas de calificaciones a Departamento de Servicios Escolares"));

		return lista;
	}

	private List<ActividadCalendario> calendarioAgoDic(int anio) {
		List<ActividadCalendario> lista = new ArrayList<>();

		lista.add(new ActividadCalendario(1, "Estudiante", "04 AL 07 AGO",
				"Entrega de Reporte Preliminar a la División de Estudios Profesionales"));
		lista.add(new ActividadCalendario(2, "División de Estudios Profesionales", "07 AGO",
				"Entrega al Jefe Académico el reporte preliminar del Proyecto de Residencia Profesional"));
		lista.add(new ActividadCalendario(3, "Jefe de Departamento Académico", "08 AGO",
				"Asigna asesor interno y entrega al asesor interno propuesto el reporte preliminar de Residencia Profesional para revisión y validación"));
		lista.add(new ActividadCalendario(4, "Asesor Interno", "11 Y 12 AGO",
				"Revisa y valida el reporte preliminar del proyecto de Residencia Profesional"));
		lista.add(new ActividadCalendario(5, "Jefe de Departamento Académico y Academias", "13 AGO",
				"Dictaminan el Proyecto de Residencia Profesional"));
		lista.add(new ActividadCalendario(6, "División de Estudios Profesionales", "14 Y 15 AGO",
				"Recibe los dictámenes de los proyectos, Reportes Preliminares y solicitudes de Residencias Profesionales corregidas"));
		lista.add(new ActividadCalendario(7, "División de Estudios Profesionales", "18 Y 19 AGO",
				"Inscripción a Residencia Profesional"));
		lista.add(new ActividadCalendario(8, "Gestión Tecnológica y Vinculación", "20 AL 22 AGO",
				"Elabora carta de presentación y agradecimiento de Residencia Profesional del estudiante"));
		lista.add(new ActividadCalendario(9, "Estudiante", "20 AL 22 AGO",
				"Entrega carta de presentación y agradecimiento de Residencia Profesional a la empresa o institución"));
		lista.add(new ActividadCalendario(10, "Empresa o dependencia", "25 AL 29 AGO",
				"Elabora Carta de Aceptación en hoja membretada y entrega al Departamento de Gestión Tecnológica y Vinculación a través del Estudiante"));
		lista.add(new ActividadCalendario(11, "Estudiante", "25 AGO",
				"Inicia Residencia Profesional (fecha de conclusión 12 de diciembre)"));
		lista.add(new ActividadCalendario(12, "Estudiante", "29 SEP AL 03 OCT",
				"Entrega de primer formato de evaluación y seguimiento de Residencia Profesional (anexo XXIX con sello del Departamento Académico)"));
		lista.add(new ActividadCalendario(13, "División de Estudios Profesionales", "14 NOV",
				"Reunión con estudiantes para informar sobre la estructura del reporte de Residencia Profesional y requisitos para conclusión de Residencia Profesional"));
		lista.add(new ActividadCalendario(14, "Estudiante", "17 AL 21 NOV",
				"Entrega de segundo formato de evaluación y seguimiento de Residencia Profesional (anexo XXIX con sello del Departamento Académico)"));
		lista.add(new ActividadCalendario(15, "Estudiante", "15 AL 19 DIC",
				"Realiza reporte de Residencia Profesional"));
		lista.add(new ActividadCalendario(16, "Asesor Interno y Externo", "15 AL 19 DIC",
				"Revisa el reporte de Residencia Profesional y comunica al Estudiante las observaciones"));
		lista.add(new ActividadCalendario(17, "Estudiante", "15 AL 19 DIC",
				"Realiza las correcciones pertinentes hasta que el informe técnico cumpla los requisitos y esté autorizado"));
		lista.add(new ActividadCalendario(18, "Asesor Interno y Externo", "15 AL 19 DIC",
				"Evalúa Reporte de Residencia y asienta resultados en anexo XXX"));
		lista.add(new ActividadCalendario(19, "Asesor interno", "15 AL 19 DIC",
				"Elabora documento de liberación de Residencia Profesional"));
		lista.add(new ActividadCalendario(20, "Estudiante", "15 AL 19 DIC",
				"Entrega el Reporte de Residencia Profesional a la empresa o entidad y le solicita carta de terminación"));
		lista.add(new ActividadCalendario(21, "Gestión Tecnológica y Vinculación", "15 AL 19 DIC",
				"Recibe carta de terminación de Residencia Profesional del Estudiante"));
		lista.add(new ActividadCalendario(22, "División de Estudios Profesionales", "15 AL 19 DIC",
				"Recibe de los estudiantes los registros de evaluación (Anexo XXX con sello del Departamento Académico)"));
		lista.add(new ActividadCalendario(23, "Estudiante", "15 AL 19 DIC Y 07 AL 12 ENE",
				"Entrega documentos en formato físico y digital a División de Estudios Profesionales para el cierre del expediente y conclusión de la Residencia Profesional"));
		lista.add(new ActividadCalendario(24, "Asesor Interno", "15 AL 19 DIC Y 07 AL 13 ENE",
				"Captura calificación de Residencia Profesional en el Sistema Mindbox y entrega al Departamento Académico el acta de calificaciones"));
		lista.add(new ActividadCalendario(25, "Jefe de Departamento Académico y Academias", "16 AL 19 DIC Y 07 AL 13 ENE",
				"Envían actas de calificaciones al departamento de División de Estudios Profesionales"));
		lista.add(new ActividadCalendario(26, "División de Estudios Profesionales", "14 AL 15 ENE",
				"Entrega Actas de calificaciones de los proyectos de Residencia Profesional concluidos al Depto. de Servicios Escolares"));

		return lista;
	}

	private static class ActividadCalendario {
		Integer numero;
		String responsable;
		String fecha;
		String actividad;

		ActividadCalendario(Integer numero, String responsable, String fecha, String actividad) {
			this.numero = numero;
			this.responsable = responsable;
			this.fecha = fecha;
			this.actividad = actividad;
		}
	}
}
