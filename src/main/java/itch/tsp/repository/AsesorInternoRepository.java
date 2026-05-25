package itch.tsp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import itch.tsp.model.AsesorInterno;
import itch.tsp.model.Docente;

public interface AsesorInternoRepository extends JpaRepository<AsesorInterno, Integer> {

	List<AsesorInterno> findByEstatusOrderByIdDesc(Integer estatus);

	List<AsesorInterno> findByEstatusOrderByIdAsc(Integer estatus);

	AsesorInterno findByIdAndEstatus(Integer id, Integer estatus);

	AsesorInterno findByDocente_IdAndEstatus(Integer idDocente, Integer estatus);

	boolean existsByClaveAsesor(String claveAsesor);

	boolean existsByDocenteAndEstatus(Docente docente, Integer estatus);

	List<AsesorInterno> findByDocente_NoEmpleadoAndEstatus(String noEmpleado, Integer estatus);

	List<AsesorInterno> findByDocente_NoEmpleadoAndEstatusAndIdNot(String noEmpleado, Integer estatus, Integer id);

	List<AsesorInterno> findByEstatusAndDocente_NoEmpleadoContainingIgnoreCaseOrEstatusAndDocente_NombreContainingIgnoreCaseOrEstatusAndDocente_ApellidosContainingIgnoreCase(
			Integer estatus1, String noEmpleado,
			Integer estatus2, String nombre,
			Integer estatus3, String apellidos);
}
