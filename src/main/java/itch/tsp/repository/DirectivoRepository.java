package itch.tsp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import itch.tsp.model.Directivo;
import itch.tsp.model.Docente;
import itch.tsp.model.TipoDirectivo;

public interface DirectivoRepository extends JpaRepository<Directivo, Integer> {

	List<Directivo> findByEstatusOrderByIdAsc(Integer estatus);

	List<Directivo> findByEstatusOrderByIdDesc(Integer estatus);

	Directivo findByIdAndEstatus(Integer id, Integer estatus);

	Directivo findFirstByTipoDirectivoAndEstatusOrderByIdDesc(TipoDirectivo tipoDirectivo, Integer estatus);

	boolean existsByClaveDirectivo(String claveDirectivo);
	
	List<Directivo> findByDocente_IdAndEstatusOrderByIdDesc(Integer idDocente, Integer estatus);

	boolean existsByDocenteAndEstatus(Docente docente, Integer estatus);

	Directivo findByTipoDirectivoAndDepartamentoAndEstatus(TipoDirectivo tipoDirectivo, String departamento, Integer estatus);
}
