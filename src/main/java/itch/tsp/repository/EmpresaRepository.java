package itch.tsp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import itch.tsp.model.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {

	List<Empresa> findByEstatusOrderByIdDesc(Integer estatus);

	Empresa findByIdAndEstatus(Integer id, Integer estatus);

	List<Empresa> findByNombreAndEstatus(String nombre, Integer estatus);

	List<Empresa> findByNombreAndEstatusAndIdNot(String nombre, Integer estatus, Integer id);

	List<Empresa> findByEstatusAndNombreContainingIgnoreCaseOrEstatusAndGiroContainingIgnoreCaseOrEstatusAndRepresentanteContainingIgnoreCaseOrEstatusAndDuenoContainingIgnoreCase(
			Integer estatus1, String nombre,
			Integer estatus2, String giro,
			Integer estatus3, String representante,
			Integer estatus4, String dueno);
}