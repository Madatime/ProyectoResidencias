package itch.tsp.service;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import itch.tsp.model.DocumentoResidencia;
import itch.tsp.model.EstatusDocumento;
import itch.tsp.model.TipoDocumentoResidencia;

@Service
public class DocumentoResidenciaServiceImp implements IDocumentoResidenciaService {

	private List<DocumentoResidencia> listaDocumentos;

	public DocumentoResidenciaServiceImp() {
		listaDocumentos = new LinkedList<>();
	}

	@Override
	public List<DocumentoResidencia> buscarPorResidencia(Integer idResidencia) {
		List<DocumentoResidencia> resultado = new LinkedList<>();

		for (DocumentoResidencia documento : listaDocumentos) {
			if (documento.getEstatusRegistro() != null
					&& documento.getEstatusRegistro() == 1
					&& documento.getResidencia() != null
					&& documento.getResidencia().getId() != null
					&& documento.getResidencia().getId().equals(idResidencia)) {
				resultado.add(documento);
			}
		}

		return resultado;
	}

	@Override
	public DocumentoResidencia buscarPorResidenciaYTipo(Integer idResidencia, TipoDocumentoResidencia tipoDocumento) {
		for (DocumentoResidencia documento : listaDocumentos) {
			if (documento.getEstatusRegistro() != null
					&& documento.getEstatusRegistro() == 1
					&& documento.getResidencia() != null
					&& documento.getResidencia().getId() != null
					&& documento.getResidencia().getId().equals(idResidencia)
					&& documento.getTipoDocumento() == tipoDocumento) {
				return documento;
			}
		}
		return null;
	}

	@Override
	public DocumentoResidencia buscarPorId(Integer idDocumento) {
		for (DocumentoResidencia documento : listaDocumentos) {
			if (documento.getId().equals(idDocumento)) {
				return documento;
			}
		}
		return null;
	}

	@Override
	public void guardarDocumento(Integer idResidencia, TipoDocumentoResidencia tipoDocumento, MultipartFile archivoPdf) {
		DocumentoResidencia documento = buscarPorResidenciaYTipo(idResidencia, tipoDocumento);

		if (documento == null) {
			documento = new DocumentoResidencia();
			documento.setId(listaDocumentos.size() + 1);
			documento.setTipoDocumento(tipoDocumento);
			documento.setEstatus(EstatusDocumento.APROBADO);
			documento.setFechaCarga(LocalDateTime.now());
			documento.setFechaRevision(LocalDateTime.now());
			documento.setObservaciones("Documento validado automáticamente al cargarse.");
			documento.setEstatusRegistro(1);
			listaDocumentos.add(documento);
		}
	}

	@Override
	public void registrarDocumentoGenerado(Integer idResidencia, TipoDocumentoResidencia tipoDocumento, String nombreArchivo) {
		DocumentoResidencia documento = buscarPorResidenciaYTipo(idResidencia, tipoDocumento);

		if (documento == null) {
			documento = new DocumentoResidencia();
			documento.setId(listaDocumentos.size() + 1);
			documento.setTipoDocumento(tipoDocumento);
			documento.setEstatusRegistro(1);
			listaDocumentos.add(documento);
		}

		documento.setNombreArchivo(nombreArchivo);
		documento.setRutaArchivo("GENERADO_EN_LINEA");
		documento.setEstatus(EstatusDocumento.APROBADO);
		documento.setObservaciones("Documento generado automáticamente por el sistema.");
		documento.setFechaCarga(LocalDateTime.now());
		documento.setFechaRevision(LocalDateTime.now());
	}

	@Override
	public void actualizarEstatus(Integer idDocumento, EstatusDocumento estatus, String observaciones) {
		DocumentoResidencia documento = buscarPorId(idDocumento);

		if (documento != null) {
			documento.setEstatus(estatus);
			documento.setObservaciones(observaciones);
			documento.setFechaRevision(LocalDateTime.now());
		}
	}

	@Override
	public void eliminar(Integer idDocumento) {
		DocumentoResidencia documento = buscarPorId(idDocumento);

		if (documento != null) {
			documento.setEstatusRegistro(0);
		}
	}
}
