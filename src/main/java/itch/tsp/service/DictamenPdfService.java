package itch.tsp.service;

import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import itch.tsp.model.Directivo;
import itch.tsp.model.Residencia;
import itch.tsp.model.TipoDirectivo;

@Service
public class DictamenPdfService {

	@Autowired
	private IDirectivoService serviceDirectivo;

	private static final String RUTA_IMG = "static/img/";
	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	public void generarDictamen(OutputStream outputStream, List<Residencia> residencias) {
		try {
			Document document = new Document(PageSize.LETTER.rotate(), 36, 36, 28, 36);
			PdfWriter writer = PdfWriter.getInstance(document, outputStream);

			document.open();

			Font fontNormal = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
			Font fontNegrita = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
			Font fontTabla = new Font(Font.FontFamily.HELVETICA, 6, Font.NORMAL);
			Font fontTablaNegrita = new Font(Font.FontFamily.HELVETICA, 6, Font.BOLD);

			agregarEncabezado(document);
			agregarDatosOficio(document, fontNormal, fontNegrita);
			agregarTextoIntroductorio(document, residencias, fontNormal, fontNegrita);
			agregarTabla(document, residencias, fontTabla, fontTablaNegrita);

			Directivo firmante = serviceDirectivo.buscarPorTipoActivo(TipoDirectivo.JEFE_DIVISION);
			agregarFirma(document, fontNormal, fontNegrita, firmante);
			agregarPiePaginaFinal(writer, document);

			document.close();
		} catch (Exception e) {
			throw new RuntimeException("Error al generar el dictamen PDF: " + e.getMessage(), e);
		}
	}

	private void agregarEncabezado(Document document) throws Exception {
		PdfPTable encabezado = new PdfPTable(2);
		encabezado.setWidthPercentage(100);
		encabezado.setWidths(new float[] { 70, 30 });

		PdfPCell izquierda = new PdfPCell();
		izquierda.setBorder(Rectangle.NO_BORDER);

		PdfPTable logosIzquierda = new PdfPTable(3);
		logosIzquierda.setWidthPercentage(100);
		logosIzquierda.setWidths(new float[] { 58, 5, 37 });

			Image logoEducacion = cargarImagenClasspath("logo-educacion_publica.png");
			logoEducacion.scaleToFit(230, 70);

		PdfPCell celdaEducacion = new PdfPCell(logoEducacion, false);
		celdaEducacion.setBorder(Rectangle.NO_BORDER);
		celdaEducacion.setHorizontalAlignment(Element.ALIGN_LEFT);
		celdaEducacion.setVerticalAlignment(Element.ALIGN_MIDDLE);

			Image logoBarra = cargarImagenClasspath("barra_vertical.png");
			logoBarra.scaleToFit(12, 65);

		PdfPCell celdaBarra = new PdfPCell(logoBarra, false);
		celdaBarra.setBorder(Rectangle.NO_BORDER);
		celdaBarra.setHorizontalAlignment(Element.ALIGN_CENTER);
		celdaBarra.setVerticalAlignment(Element.ALIGN_MIDDLE);

			Image logoJaguar = cargarImagenClasspath("logo-jaguar-tecnm.png");
			logoJaguar.scaleToFit(115, 65);

		PdfPCell celdaJaguar = new PdfPCell(logoJaguar, false);
		celdaJaguar.setBorder(Rectangle.NO_BORDER);
		celdaJaguar.setHorizontalAlignment(Element.ALIGN_LEFT);
		celdaJaguar.setVerticalAlignment(Element.ALIGN_MIDDLE);

		logosIzquierda.addCell(celdaEducacion);
		logosIzquierda.addCell(celdaBarra);
		logosIzquierda.addCell(celdaJaguar);
		izquierda.addElement(logosIzquierda);

		PdfPCell derecha = new PdfPCell();
		derecha.setBorder(Rectangle.NO_BORDER);
		derecha.setHorizontalAlignment(Element.ALIGN_RIGHT);

			Image logoMujer = cargarImagenClasspath("logo-mujer.png");
			logoMujer.scaleToFit(95, 95);
		logoMujer.setAlignment(Element.ALIGN_RIGHT);
		derecha.addElement(logoMujer);

		Paragraph textoDerecha = new Paragraph();
		textoDerecha.setAlignment(Element.ALIGN_CENTER);
		textoDerecha.setLeading(12);
		textoDerecha.add(new Chunk("Instituto Tecnologico de Chilpancingo\n",
				new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD)));
		textoDerecha.add(new Chunk("Division de Estudios Profesionales",
				new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL)));
		derecha.addElement(textoDerecha);

		encabezado.addCell(izquierda);
		encabezado.addCell(derecha);

		document.add(encabezado);
		document.add(new Paragraph(" "));
	}

	private void agregarDatosOficio(Document document, Font fontNormal, Font fontNegrita) throws Exception {
		String fecha = LocalDate.now().format(FORMATO_FECHA);

		Paragraph datos = new Paragraph();
		datos.setAlignment(Element.ALIGN_RIGHT);
		datos.setFont(fontNormal);
		datos.setLeading(13);
		datos.add("Chilpancingo de los Bravo, Gro., a " + fecha + "\n");
		datos.add("Oficio No. 12DIT0002E/DEP/063/2026");
		document.add(datos);

		document.add(new Paragraph(" "));

		Paragraph destinatario = new Paragraph();
		destinatario.setFont(fontNegrita);
		destinatario.setLeading(13);
		destinatario.add("MARIA ESTHER DURAN FIGUEROA\n");
		destinatario.add("JEFA DEPTO. DE GESTION TECNOLOGICA Y VINCULACION\n");
		destinatario.add("P R E S E N T E.");
		document.add(destinatario);

		document.add(new Paragraph(" "));
	}

	private void agregarTextoIntroductorio(Document document, List<Residencia> residencias, Font fontNormal,
			Font fontNegrita) throws Exception {
		String periodo = obtenerPeriodoPrincipal(residencias);
		String fecha = LocalDate.now().format(FORMATO_FECHA);
		int total = residencias != null ? residencias.size() : 0;

		Paragraph texto = new Paragraph();
		texto.setAlignment(Element.ALIGN_JUSTIFIED);
		texto.setFont(fontNormal);
		texto.setLeading(13);
		texto.add("En atencion al Oficio No. 12DIT0002E/DEP/063/2026, turnado a su area con fecha ");
		texto.add(new Chunk(fecha, fontNegrita));
		texto.add(", anexo a Usted nuevamente ");
		texto.add(new Chunk(String.valueOf(total), fontNegrita));
		texto.add(" dictamenes de reporte preliminar de residencias profesionales, correspondiente al semestre ");
		texto.add(new Chunk(periodo, fontNegrita));
		texto.add(". A continuacion se presentan los datos del estudiante, proyecto, empresa, asesores y estatus de autorizacion.");

		document.add(texto);
		document.add(new Paragraph(" "));
	}

	private void agregarTabla(Document document, List<Residencia> residencias, Font fontTabla, Font fontTablaNegrita)
			throws Exception {
		PdfPTable tabla = new PdfPTable(11);
		tabla.setWidthPercentage(100);
		tabla.setWidths(new float[] { 8, 23, 7, 11, 25, 17, 25, 18, 18, 14, 14 });

		agregarCeldaEncabezado(tabla, "NO.", fontTablaNegrita);
		agregarCeldaEncabezado(tabla, "NOMBRE DEL ESTUDIANTE", fontTablaNegrita);
		agregarCeldaEncabezado(tabla, "SEXO", fontTablaNegrita);
		agregarCeldaEncabezado(tabla, "NO. CONTROL", fontTablaNegrita);
		agregarCeldaEncabezado(tabla, "PROYECTO", fontTablaNegrita);
		agregarCeldaEncabezado(tabla, "EMPRESA", fontTablaNegrita);
		agregarCeldaEncabezado(tabla, "REP. EMPRESA", fontTablaNegrita);
		agregarCeldaEncabezado(tabla, "ASESOR INTERNO", fontTablaNegrita);
		agregarCeldaEncabezado(tabla, "ASESOR EXTERNO", fontTablaNegrita);
		agregarCeldaEncabezado(tabla, "AUTORIZACION", fontTablaNegrita);
		agregarCeldaEncabezado(tabla, "F. AUTORIZACION", fontTablaNegrita);

		if (residencias != null) {
			int consecutivo = 1;
			for (Residencia residencia : residencias) {
				agregarCelda(tabla, String.valueOf(consecutivo), fontTabla, Element.ALIGN_CENTER);
				agregarCelda(tabla, obtenerNombreEstudiante(residencia), fontTabla, Element.ALIGN_LEFT);
				agregarCelda(tabla, obtenerSexo(residencia), fontTabla, Element.ALIGN_CENTER);
				agregarCelda(tabla, obtenerControl(residencia), fontTabla, Element.ALIGN_CENTER);
				agregarCelda(tabla, obtenerProyecto(residencia), fontTabla, Element.ALIGN_LEFT);
				agregarCelda(tabla, obtenerEmpresa(residencia), fontTabla, Element.ALIGN_LEFT);
				agregarCelda(tabla, obtenerRepresentanteEmpresa(residencia), fontTabla, Element.ALIGN_LEFT);
				agregarCelda(tabla, obtenerAsesorInterno(residencia), fontTabla, Element.ALIGN_LEFT);
				agregarCelda(tabla, obtenerAsesorExterno(residencia), fontTabla, Element.ALIGN_LEFT);
				agregarCelda(tabla, obtenerAutorizacion(residencia), fontTabla, Element.ALIGN_CENTER);
				agregarCelda(tabla, obtenerFechaAutorizacion(residencia), fontTabla, Element.ALIGN_CENTER);
				consecutivo++;
			}
		}

		document.add(tabla);
		document.add(new Paragraph(" "));
	}

	private void agregarPiePaginaFinal(PdfWriter writer, Document document) throws Exception {
		float posicionActual = writer.getVerticalPosition(false);
		if (posicionActual < 170) {
			document.newPage();
		}

		PdfContentByte canvas = writer.getDirectContent();
		Image pie = cargarImagenClasspath("logo-margaritamaza.png");

		float pageWidth = document.getPageSize().getWidth();
		float anchoPie = pageWidth - 70;
		float altoPie = 135;

		pie.scaleToFit(anchoPie, altoPie);
		pie.setAbsolutePosition(35, 18);
		canvas.addImage(pie);
	}

	private void agregarCeldaEncabezado(PdfPTable tabla, String texto, Font font) {
		PdfPCell celda = new PdfPCell(new Phrase(texto, font));
		celda.setHorizontalAlignment(Element.ALIGN_CENTER);
		celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
		celda.setBackgroundColor(new BaseColor(235, 239, 245));
		celda.setPadding(6);
		celda.setMinimumHeight(30);
		tabla.addCell(celda);
	}

	private void agregarCelda(PdfPTable tabla, String texto, Font font, int alineacion) {
		PdfPCell celda = new PdfPCell(new Phrase(texto != null ? texto : "", font));
		celda.setHorizontalAlignment(alineacion);
		celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
		celda.setPadding(6);
		celda.setMinimumHeight(42);
		tabla.addCell(celda);
	}

	private String obtenerControl(Residencia residencia) {
		if (residencia != null && residencia.getResidente() != null && residencia.getResidente().getMatricula() != null) {
			return residencia.getResidente().getMatricula();
		}
		return "";
	}

	private String obtenerNombreEstudiante(Residencia residencia) {
		if (residencia != null && residencia.getResidente() != null
				&& residencia.getResidente().getNombreCompleto() != null) {
			return residencia.getResidente().getNombreCompleto().trim().toUpperCase();
		}
		return "";
	}

	private String obtenerSexo(Residencia residencia) {
		if (residencia != null && residencia.getResidente() != null && residencia.getResidente().getSexo() != null) {
			return residencia.getResidente().getSexo().trim().toUpperCase();
		}
		return "";
	}

	private String obtenerProyecto(Residencia residencia) {
		if (residencia != null && residencia.getNombreProyecto() != null) {
			return residencia.getNombreProyecto().trim().toUpperCase();
		}
		return "";
	}

	private String obtenerEmpresa(Residencia residencia) {
		if (residencia != null && residencia.getEmpresa() != null && residencia.getEmpresa().getNombre() != null) {
			return residencia.getEmpresa().getNombre().trim().toUpperCase();
		}
		return "";
	}

	private String obtenerRepresentanteEmpresa(Residencia residencia) {
		if (residencia == null || residencia.getEmpresa() == null) {
			return "";
		}

		StringBuilder datos = new StringBuilder();
		agregarLinea(datos, valorMayusculas(residencia.getEmpresa().getRepresentante()));
		agregarLinea(datos, valorMayusculas(residencia.getEmpresa().getPuestoRepresentante()));

		String telefono = residencia.getEmpresa().getTelefono() != null ? residencia.getEmpresa().getTelefono().trim() : "";
		if (!telefono.isEmpty()) {
			agregarLinea(datos, "TEL. " + telefono);
		}

		String correo = residencia.getEmpresa().getCorreo() != null ? residencia.getEmpresa().getCorreo().trim() : "";
		if (!correo.isEmpty()) {
			agregarLinea(datos, correo);
		}

		return datos.toString();
	}

	private String obtenerAsesorInterno(Residencia residencia) {
		if (residencia != null && residencia.getAsesorInterno() != null
				&& residencia.getAsesorInterno().getNombreCompleto() != null) {
			return residencia.getAsesorInterno().getNombreCompleto().trim().toUpperCase();
		}
		return "";
	}

	private String obtenerAsesorExterno(Residencia residencia) {
		if (residencia != null && residencia.getAsesorExterno() != null
				&& residencia.getAsesorExterno().getNombreCompleto() != null) {
			return residencia.getAsesorExterno().getNombreCompleto().trim().toUpperCase();
		}
		return "";
	}

	private String obtenerAutorizacion(Residencia residencia) {
		if (residencia == null || residencia.getEstadoAutorizacion() == null) {
			return "";
		}
		return residencia.getEstadoAutorizacion().trim().toUpperCase().replace('_', ' ');
	}

	private String obtenerFechaAutorizacion(Residencia residencia) {
		if (residencia != null && residencia.getFechaAutorizacion() != null) {
			return residencia.getFechaAutorizacion().format(FORMATO_FECHA);
		}
		return "";
	}

	private String obtenerPeriodoPrincipal(List<Residencia> residencias) {
		if (residencias != null) {
			for (Residencia residencia : residencias) {
				if (residencia.getPeriodo() != null && !residencia.getPeriodo().trim().isEmpty()) {
					return residencia.getPeriodo();
				}
			}
		}
		return "";
	}

	private void agregarFirma(Document document, Font fontNormal, Font fontNegrita, Directivo firmante) throws Exception {
		Paragraph cierre = new Paragraph();
		cierre.setFont(fontNormal);
		cierre.setLeading(13);
		cierre.add("Sin otro particular por el momento, envio un cordial saludo.\n\n");
		document.add(cierre);

		Paragraph atentamente = new Paragraph();
		atentamente.setAlignment(Element.ALIGN_LEFT);
		atentamente.setFont(fontNegrita);
		atentamente.setLeading(13);
		atentamente.add("A T E N T A M E N T E\n");
		atentamente.add("\"Excelencia en Educacion Tecnologica\"\n\n\n\n");

		if (firmante != null) {
			atentamente.add(firmante.getNombreCompleto().toUpperCase() + "\n");
			if (firmante.getPuesto() != null && !firmante.getPuesto().trim().isEmpty()) {
				atentamente.add(firmante.getPuesto().toUpperCase());
			} else {
				atentamente.add("JEFE DE DIVISION DE ESTUDIOS PROFESIONALES");
			}
		} else {
			atentamente.add("M.C. RICARDO OLIVAR HERRERA\n");
			atentamente.add("JEFE DE DIVISION DE ESTUDIOS PROFESIONALES");
		}

		document.add(atentamente);
		document.add(new Paragraph(" "));

		Paragraph copia = new Paragraph();
		copia.setFont(new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL));
		copia.setLeading(9);
		copia.add("C.c.p. Dictamenes de estudiantes.\n");
		copia.add("C.c.p. Servicios Escolares del I.T.Ch. Para su conocimiento. Presente.\n");
		copia.add("C.c.p. Coordinacion de Carrera. Igual fin.\n");
		copia.add("C.c.p. Archivo.");
		document.add(copia);
	}

	private void agregarLinea(StringBuilder builder, String valor) {
		if (valor == null || valor.isBlank()) {
			return;
		}
		if (builder.length() > 0) {
			builder.append("\n");
		}
		builder.append(valor);
	}

	private String valorMayusculas(String valor) {
		if (valor == null) {
			return "";
		}
		return valor.trim().toUpperCase();
	}

	private Image cargarImagenClasspath(String nombreArchivo) throws Exception {
		ClassPathResource resource = new ClassPathResource(RUTA_IMG + nombreArchivo);
		return Image.getInstance(resource.getURL());
	}
}
