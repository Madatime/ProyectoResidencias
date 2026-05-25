package itch.tsp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import itch.tsp.model.AsesorExterno;

public interface AsesorExternoRepository extends JpaRepository<AsesorExterno, Integer> {

	List<AsesorExterno> findByEstatusOrderByIdDesc(Integer estatus);

	AsesorExterno findByIdAndEstatus(Integer id, Integer estatus);

	List<AsesorExterno> findByEstatusAndNombreContainingIgnoreCaseOrEstatusAndApellidosContainingIgnoreCaseOrEstatusAndEmpresaContainingIgnoreCase(
			Integer estatus1, String nombre,
			Integer estatus2, String apellidos,
			Integer estatus3, String empresa);
}