package itch.tsp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import itch.tsp.model.Estudiante;

public interface EstudianteRepository extends JpaRepository<Estudiante, Integer> {

	List<Estudiante> findByEstatusOrderByIdDesc(Integer estatus);

	Estudiante findByIdAndEstatus(Integer id, Integer estatus);

	Estudiante findByMatriculaAndEstatus(String matricula, Integer estatus);

	List<Estudiante> findByMatriculaAndEstatusAndIdNot(String matricula, Integer estatus, Integer id);

	boolean existsByNombreIgnoreCaseAndApellidosIgnoreCaseAndEstatus(
			String nombre,
			String apellidos,
			Integer estatus
	);

	boolean existsByNombreIgnoreCaseAndApellidosIgnoreCaseAndEstatusAndIdNot(
			String nombre,
			String apellidos,
			Integer estatus,
			Integer id
	);

	@Query("""
		SELECT e FROM Estudiante e
		WHERE e.estatus = 1
		AND (
			LOWER(e.matricula) LIKE LOWER(CONCAT('%', :texto, '%'))
			OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
			OR LOWER(e.apellidos) LIKE LOWER(CONCAT('%', :texto, '%'))
			OR LOWER(CONCAT(e.nombre, ' ', e.apellidos)) LIKE LOWER(CONCAT('%', :texto, '%'))
		)
		ORDER BY e.id DESC
	""")
	List<Estudiante> buscarActivosPorTexto(@Param("texto") String texto);

	List<Estudiante> findByEstatusAndMatriculaContainingIgnoreCaseOrEstatusAndNombreContainingIgnoreCaseOrEstatusAndApellidosContainingIgnoreCase(
			Integer estatus1,
			String matricula,
			Integer estatus2,
			String nombre,
			Integer estatus3,
			String apellidos
	);
}