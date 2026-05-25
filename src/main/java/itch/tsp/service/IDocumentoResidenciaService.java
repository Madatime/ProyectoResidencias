package itch.tsp.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import itch.tsp.model.DocumentoResidencia;
import itch.tsp.model.EstatusDocumento;
import itch.tsp.model.TipoDocumentoResidencia;

public interface IDocumentoResidenciaService {

	List<DocumentoResidencia> buscarPorResidencia(Integer idResidencia);

	DocumentoResidencia buscarPorResidenciaYTipo(Integer idResidencia, TipoDocumentoResidencia tipoDocumento);

	DocumentoResidencia buscarPorId(Integer idDocumento);

	void guardarDocumento(Integer idResidencia, TipoDocumentoResidencia tipoDocumento, MultipartFile archivoPdf);

	void registrarDocumentoGenerado(Integer idResidencia, TipoDocumentoResidencia tipoDocumento, String nombreArchivo);

	void actualizarEstatus(Integer idDocumento, EstatusDocumento estatus, String observaciones);

	void eliminar(Integer idDocumento);
}
