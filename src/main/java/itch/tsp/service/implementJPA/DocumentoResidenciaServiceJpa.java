package itch.tsp.service.implementJPA;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import itch.tsp.model.DocumentoResidencia;
import itch.tsp.model.EstatusDocumento;
import itch.tsp.model.Residencia;
import itch.tsp.model.TipoDocumentoResidencia;
import itch.tsp.repository.DocumentoResidenciaRepository;
import itch.tsp.repository.ResidenciaRepository;
import itch.tsp.service.IDocumentoResidenciaService;

@Primary
@Service
public class DocumentoResidenciaServiceJpa implements IDocumentoResidenciaService {

	@Autowired
	private DocumentoResidenciaRepository repoDocumento;

	@Autowired
	private ResidenciaRepository repoResidencia;

	@Value("${app.ruta.base}")
	private String rutaBase;

	@Value("${app.carpeta.proyectos}")
	private String carpetaProyectos;

	@Override
	public List<DocumentoResidencia> buscarPorResidencia(Integer idResidencia) {
		return repoDocumento.findByResidencia_IdAndEstatusRegistroOrderByIdDesc(idResidencia, 1);
	}

	@Override
	public DocumentoResidencia buscarPorResidenciaYTipo(Integer idResidencia, TipoDocumentoResidencia tipoDocumento) {
		return repoDocumento.findByResidencia_IdAndTipoDocumentoAndEstatusRegistro(idResidencia, tipoDocumento, 1);
	}

	@Override
	public DocumentoResidencia buscarPorId(Integer idDocumento) {
		return repoDocumento.findById(idDocumento).orElse(null);
	}

	@Override
	public void guardarDocumento(Integer idResidencia, TipoDocumentoResidencia tipoDocumento, MultipartFile archivoPdf) {

		if (archivoPdf == null || archivoPdf.isEmpty()) {
			throw new RuntimeException("Debes seleccionar un archivo PDF.");
		}

		String nombreOriginal = archivoPdf.getOriginalFilename();

		if (nombreOriginal == null || !nombreOriginal.toLowerCase().endsWith(".pdf")) {
			throw new RuntimeException("Solo se permiten archivos PDF.");
		}

		Residencia residencia = repoResidencia.findById(idResidencia).orElse(null);

		if (residencia == null) {
			throw new RuntimeException("La residencia no existe.");
		}

		DocumentoResidencia documento = repoDocumento
				.findByResidencia_IdAndTipoDocumentoAndEstatusRegistro(idResidencia, tipoDocumento, 1);

		if (documento == null) {
			documento = new DocumentoResidencia();
			documento.setResidencia(residencia);
			documento.setTipoDocumento(tipoDocumento);
			documento.setEstatus(EstatusDocumento.APROBADO);
			documento.setEstatusRegistro(1);
		}

		File directorio = resolverDirectorioProyectos();

		try {
			Files.createDirectories(directorio.toPath());

			String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));

			String nombreNuevo = UUID.randomUUID().toString() + extension;

			File destino = new File(directorio, nombreNuevo);

			archivoPdf.transferTo(destino);

			documento.setNombreArchivo(nombreOriginal);
			documento.setRutaArchivo(nombreNuevo);
			documento.setFechaCarga(LocalDateTime.now());
			documento.setEstatus(EstatusDocumento.APROBADO);
			documento.setFechaRevision(LocalDateTime.now());
			documento.setObservaciones("Documento validado automáticamente al cargarse.");

			repoDocumento.save(documento);

		} catch (IOException e) {
			throw new RuntimeException("Error al guardar el PDF: " + e.getMessage());
		}
	}

	@Override
	public void registrarDocumentoGenerado(Integer idResidencia, TipoDocumentoResidencia tipoDocumento, String nombreArchivo) {
		Residencia residencia = repoResidencia.findById(idResidencia).orElse(null);

		if (residencia == null) {
			throw new RuntimeException("La residencia no existe.");
		}

		DocumentoResidencia documento = repoDocumento
				.findByResidencia_IdAndTipoDocumentoAndEstatusRegistro(idResidencia, tipoDocumento, 1);

		if (documento == null) {
			documento = new DocumentoResidencia();
			documento.setResidencia(residencia);
			documento.setTipoDocumento(tipoDocumento);
			documento.setEstatusRegistro(1);
		}

		documento.setNombreArchivo(nombreArchivo);
		documento.setRutaArchivo("GENERADO_EN_LINEA");
		documento.setFechaCarga(LocalDateTime.now());
		documento.setFechaRevision(LocalDateTime.now());
		documento.setEstatus(EstatusDocumento.APROBADO);
		documento.setObservaciones("Documento generado automáticamente por el sistema.");

		repoDocumento.save(documento);
	}

	@Override
	public void actualizarEstatus(Integer idDocumento, EstatusDocumento estatus, String observaciones) {

		DocumentoResidencia documento = repoDocumento.findById(idDocumento).orElse(null);

		if (documento == null) {
			throw new RuntimeException("El documento no existe.");
		}

		documento.setEstatus(estatus);
		documento.setObservaciones(observaciones != null ? observaciones.trim() : null);
		documento.setFechaRevision(LocalDateTime.now());

		repoDocumento.save(documento);
	}

	@Override
	public void eliminar(Integer idDocumento) {

		DocumentoResidencia documento = repoDocumento.findById(idDocumento).orElse(null);

		if (documento != null) {
			documento.setEstatusRegistro(0);
			repoDocumento.save(documento);
		}
	}


	public byte[] generarCartaPresentacion(Residencia residencia) {

		try {
			ByteArrayOutputStream salida = new ByteArrayOutputStream();

			Document document = new Document(com.itextpdf.text.PageSize.LETTER, 45, 45, 35, 35);
			PdfWriter.getInstance(document, salida);
			document.open();

			Font normal = FontFactory.getFont(FontFactory.HELVETICA, 9.5f);
			Font negrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f);
			Font negritaGrande = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.5f);
			Font pie = FontFactory.getFont(FontFactory.HELVETICA, 7.5f);

			PdfPTable encabezado = new PdfPTable(2);
			encabezado.setWidthPercentage(100);
			encabezado.setWidths(new float[] { 45, 55 });

			PdfPCell celdaLogo = new PdfPCell();
			celdaLogo.setBorder(PdfPCell.NO_BORDER);

			try {
				com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance("src/main/resources/static/img/logo-tecnm.png");
				logo.scaleToFit(190, 55);
				celdaLogo.addElement(logo);
			} catch (Exception e) {
				celdaLogo.addElement(new Paragraph("EDUCACIÓN | TecNM", negritaGrande));
			}

			PdfPCell celdaInstituto = new PdfPCell();
			celdaInstituto.setBorder(PdfPCell.NO_BORDER);
			Paragraph instituto = new Paragraph("Instituto Tecnológico de Chilpancingo\nDirección", normal);
			instituto.setAlignment(Element.ALIGN_RIGHT);
			celdaInstituto.addElement(instituto);

			encabezado.addCell(celdaLogo);
			encabezado.addCell(celdaInstituto);
			document.add(encabezado);

			document.add(new Paragraph("\n\n"));

			Paragraph datos = new Paragraph();
			datos.setAlignment(Element.ALIGN_RIGHT);
			datos.setLeading(12f);
			datos.add(new Phrase("Chilpancingo, Gro., a 23 de Agosto de 2024.\n", negrita));
			datos.add(new Phrase("Departamento: GESTIÓN TEC. Y VINC.\n", negrita));
			datos.add(new Phrase("No. de Oficio: 12DTC002E/DGTYV\n", negrita));
			datos.add(new Phrase("ASUNTO: PRESENTACIÓN DEL ESTUDIANTE Y AGRADECIMIENTO", negrita));
			document.add(datos);

			document.add(new Paragraph("\n\n\n"));

			String empresaNombre = residencia.getEmpresa() != null ? residencia.getEmpresa().getNombre() : "";
			String representante = residencia.getEmpresa() != null && residencia.getEmpresa().getRepresentante() != null
					? residencia.getEmpresa().getRepresentante()
					: "A QUIEN CORRESPONDA";

			String puesto = residencia.getEmpresa() != null && residencia.getEmpresa().getPuestoRepresentante() != null
					? residencia.getEmpresa().getPuestoRepresentante()
					: "RESPONSABLE";

			String residente = residencia.getResidente() != null
					? residencia.getResidente().getNombreCompleto()
					: "";

			String matricula = residencia.getResidente() != null
					? residencia.getResidente().getMatricula()
					: "";

			String carrera = obtenerCarrera(residencia);

			String proyecto = residencia.getNombreProyecto() != null
					? residencia.getNombreProyecto()
					: "";

			String fechaInicio = residencia.getFechaInicio() != null
					? residencia.getFechaInicio().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy"))
					: "fecha de inicio";

			String fechaFin = residencia.getFechaFin() != null
					? residencia.getFechaFin().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy"))
					: "fecha de término";

			Paragraph destinatario = new Paragraph();
			destinatario.setLeading(12f);
			destinatario.add(new Phrase(representante.toUpperCase() + "\n", negrita));
			destinatario.add(new Phrase(puesto.toUpperCase() + "\n", negrita));
			destinatario.add(new Phrase(empresaNombre.toUpperCase() + "\n", negrita));
			destinatario.add(new Phrase("PRESENTE:\n", negrita));
			document.add(destinatario);

			document.add(new Paragraph("\n"));

			Paragraph p1 = new Paragraph();
			p1.setAlignment(Element.ALIGN_JUSTIFIED);
			p1.setLeading(13.5f);
			p1.add(new Phrase("El Instituto Tecnológico de Chilpancingo, tiene a bien presentar a sus finas atenciones al (la) C. ", normal));
			p1.add(new Phrase(residente.toUpperCase(), negrita));
			p1.add(new Phrase(", con número de control ", normal));
			p1.add(new Phrase(matricula, negrita));
			p1.add(new Phrase(" de la carrera de ", normal));
			p1.add(new Phrase(carrera.toUpperCase(), negrita));
			p1.add(new Phrase(", quien desea desarrollar en ese organismo el proyecto de Residencias Profesionales, denominado ", normal));
			p1.add(new Phrase(proyecto.toUpperCase(), negrita));
			p1.add(new Phrase(", cubriendo un total de 500 horas, en un período proyectado del ", normal));
			p1.add(new Phrase(fechaInicio + " al " + fechaFin + ".", normal));
			document.add(p1);

			document.add(new Paragraph("\n"));

			Paragraph p2 = new Paragraph(
					"Es importante hacer de su conocimiento que todos los alumnos que se encuentran inscritos en esta institución cuentan con un seguro de ACCIDENTES PERSONALES ESCOLARES, así como afiliación al IMSS.",
					normal);
			p2.setAlignment(Element.ALIGN_JUSTIFIED);
			p2.setLeading(13.5f);
			document.add(p2);

			document.add(new Paragraph("\n"));

			Paragraph p3 = new Paragraph(
					"Asimismo, hacemos patente nuestro sincero agradecimiento por su buena disposición y colaboración para que nuestros alumnos, aun estando en proceso de formación, desarrollen un proyecto de trabajo profesional, donde puedan aplicar el conocimiento y el trabajo en el campo de acción en el que se desenvolverán como futuros profesionistas.",
					normal);
			p3.setAlignment(Element.ALIGN_JUSTIFIED);
			p3.setLeading(13.5f);
			document.add(p3);

			document.add(new Paragraph("\n"));

			Paragraph p4 = new Paragraph(
					"Al vernos favorecidos con su participación en nuestro objetivo, sólo nos resta manifestarle la seguridad de nuestra más atenta y distinguida consideración.",
					normal);
			p4.setAlignment(Element.ALIGN_JUSTIFIED);
			p4.setLeading(13.5f);
			document.add(p4);

			document.add(new Paragraph("\n\n"));

			PdfPTable firmaTabla = new PdfPTable(2);
			firmaTabla.setWidthPercentage(100);
			firmaTabla.setWidths(new float[] { 45, 55 });

			PdfPCell celdaFirma = new PdfPCell();
			celdaFirma.setBorder(PdfPCell.NO_BORDER);
			celdaFirma.addElement(new Paragraph("A T E N T A M E N T E", negrita));
			celdaFirma.addElement(new Paragraph("Excelencia en Educación Tecnológica.\nCrear Tecnología es Forjar Libertad\n\n\n", negrita));
			celdaFirma.addElement(new Paragraph("LORENA GARCÍA RODRÍGUEZ", negrita));
			celdaFirma.addElement(new Paragraph("JEFA DEL DEPARTAMENTO DE\nGESTIÓN TECNOLÓGICA Y VINCULACIÓN", negrita));

			PdfPCell celdaSello = new PdfPCell();
			celdaSello.setBorder(PdfPCell.NO_BORDER);
			celdaSello.setHorizontalAlignment(Element.ALIGN_CENTER);
			celdaSello.addElement(new Paragraph("\n\n"));
			celdaSello.addElement(new Paragraph("__________________________________", normal));
			celdaSello.addElement(new Paragraph("DEPARTAMENTO DE GESTIÓN\nTECNOLÓGICA Y VINCULACIÓN", negrita));

			firmaTabla.addCell(celdaFirma);
			firmaTabla.addCell(celdaSello);
			document.add(firmaTabla);

			document.add(new Paragraph("\n\n"));

			Paragraph piePagina = new Paragraph(
					"Av. José Francisco Ruiz Massieu No. 5, Colonia Villa Moderna, Chilpancingo de los Bravo, Guerrero, México.\n"
							+ "Tel. 747 480 1022, 472 1014, Ext. 125, email: dgtv@chilpancingo.tecnm.mx\n"
							+ "http://chilpancingo.tecnm.mx/",
					pie);
			piePagina.setAlignment(Element.ALIGN_LEFT);
			document.add(piePagina);

			document.close();

			return salida.toByteArray();

		} catch (Exception e) {
			throw new RuntimeException("Error al generar carta de presentación: " + e.getMessage());
		}
	}
	
	private String obtenerCarrera(Residencia residencia) {

		try {

			if (residencia == null || residencia.getResidente() == null) {
				return "";
			}

			if (residencia.getResidente().getEstudiante() != null
					&& residencia.getResidente().getEstudiante().getCarrera() != null) {

				Object carreraObj = residencia.getResidente().getEstudiante().getCarrera();

				if (carreraObj instanceof String) {
					return (String) carreraObj;
				}

				try {
					return (String) carreraObj.getClass()
							.getMethod("getNombre")
							.invoke(carreraObj);

				} catch (Exception e) {
					return carreraObj.toString();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
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
	
	
}
