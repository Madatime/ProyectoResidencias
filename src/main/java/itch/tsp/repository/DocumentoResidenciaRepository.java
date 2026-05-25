package itch.tsp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import itch.tsp.model.DocumentoResidencia;
import itch.tsp.model.TipoDocumentoResidencia;

public interface DocumentoResidenciaRepository extends JpaRepository<DocumentoResidencia, Integer> {

	List<DocumentoResidencia> findByResidencia_IdAndEstatusRegistroOrderByIdDesc(Integer idResidencia, Integer estatusRegistro);

	DocumentoResidencia findByResidencia_IdAndTipoDocumentoAndEstatusRegistro(Integer idResidencia,
			TipoDocumentoResidencia tipoDocumento, Integer estatusRegistro);
}