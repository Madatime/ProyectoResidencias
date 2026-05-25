package itch.tsp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import itch.tsp.model.AsesorInterno;
import itch.tsp.model.Residencia;
import itch.tsp.model.Residente;

public interface ResidenciaRepository extends JpaRepository<Residencia, Integer> {

	List<Residencia> findByEstatusOrderByIdDesc(Integer estatus);

	Residencia findByIdAndEstatus(Integer id, Integer estatus);

	List<Residencia> findByPeriodoAndEstatusOrderByIdDesc(String periodo, Integer estatus);

	List<Residencia> findByResidenteAndEstatusOrderByIdDesc(Residente residente, Integer estatus);

	List<Residencia> findByAsesorInternoAndEstatusOrderByIdDesc(AsesorInterno asesorInterno, Integer estatus);

	List<Residencia> findByEstatusAndNombreProyectoContainingIgnoreCaseOrEstatusAndEmpresa_NombreContainingIgnoreCase(
			Integer estatus1, String nombreProyecto,
			Integer estatus2, String nombreEmpresa);

	List<Residencia> findByPeriodoAndEstatusAndNombreProyectoContainingIgnoreCaseOrPeriodoAndEstatusAndEmpresa_NombreContainingIgnoreCase(
			String periodo1, Integer estatus1, String nombreProyecto,
			String periodo2, Integer estatus2, String nombreEmpresa);

	List<Residencia> findByEstatusAndResidente_Estudiante_MatriculaContainingIgnoreCaseOrEstatusAndResidente_Estudiante_NombreContainingIgnoreCaseOrEstatusAndResidente_Estudiante_ApellidosContainingIgnoreCase(
			Integer estatus1, String matricula,
			Integer estatus2, String nombre,
			Integer estatus3, String apellidos);

	long countByPeriodoAndResidente_Estudiante_CarreraAndEstatus(
			String periodo,
			String carrera,
			Integer estatus);

	List<Residencia> findByEstatusAndAsesorInternoIsNotNullOrderByIdDesc(Integer estatus);

	List<Residencia> findByPeriodoAndEstatusAndAsesorInternoIsNotNullOrderByIdDesc(String periodo, Integer estatus);

	List<Residencia> findByEstatusAndAsesorInterno_NoEmpleadoContainingIgnoreCaseOrEstatusAndAsesorInterno_NombreContainingIgnoreCaseOrEstatusAndAsesorInterno_ApellidosContainingIgnoreCaseOrEstatusAndNombreProyectoContainingIgnoreCase(
			Integer estatus1, String noEmpleado,
			Integer estatus2, String nombre,
			Integer estatus3, String apellidos,
			Integer estatus4, String nombreProyecto);

	List<Residencia> findByPeriodoAndEstatusAndAsesorInterno_NoEmpleadoContainingIgnoreCaseOrPeriodoAndEstatusAndAsesorInterno_NombreContainingIgnoreCaseOrPeriodoAndEstatusAndAsesorInterno_ApellidosContainingIgnoreCaseOrPeriodoAndEstatusAndNombreProyectoContainingIgnoreCase(
			String periodo1, Integer estatus1, String noEmpleado,
			String periodo2, Integer estatus2, String nombre,
			String periodo3, Integer estatus3, String apellidos,
			String periodo4, Integer estatus4, String nombreProyecto);

	List<Residencia> findByAsesorInterno_IdAndEstatusOrderByIdDesc(Integer idAsesorInterno, Integer estatus);
}