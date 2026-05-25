package itch.tsp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import itch.tsp.model.Residente;

public interface ResidenteRepository extends JpaRepository<Residente, Integer> {

	List<Residente> findByEstatusOrderByIdDesc(Integer estatus);

	Residente findByIdAndEstatus(Integer id, Integer estatus);

	List<Residente> findByEstudiante_MatriculaAndEstatus(String matricula, Integer estatus);

	List<Residente> findByEstudiante_MatriculaAndEstatusAndIdNot(String matricula, Integer estatus, Integer id);

	List<Residente> findByEstatusAndEstudiante_MatriculaContainingIgnoreCaseOrEstatusAndEstudiante_NombreContainingIgnoreCaseOrEstatusAndEstudiante_ApellidosContainingIgnoreCase(
			Integer estatus1, String matricula,
			Integer estatus2, String nombre,
			Integer estatus3, String apellidos);

	Residente findByEstudiante_IdAndEstatus(Integer idEstudiante, Integer estatus);
}