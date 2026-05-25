package itch.tsp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import itch.tsp.model.BancoProyecto;
import itch.tsp.model.EstadoBancoProyecto;
import itch.tsp.model.OrigenBancoProyecto;

public interface BancoProyectoRepository extends JpaRepository<BancoProyecto, Integer> {

	List<BancoProyecto> findByEstatusOrderByIdDesc(Integer estatus);

	BancoProyecto findByIdAndEstatus(Integer id, Integer estatus);

	List<BancoProyecto> findByEstadoAndEstatusOrderByIdDesc(EstadoBancoProyecto estado, Integer estatus);

	List<BancoProyecto> findByOrigenAndEstatusOrderByIdDesc(OrigenBancoProyecto origen, Integer estatus);

	List<BancoProyecto> findByEstadoAndOrigenAndEstatusOrderByIdDesc(
			EstadoBancoProyecto estado,
			OrigenBancoProyecto origen,
			Integer estatus);

	List<BancoProyecto> findByEstatusAndNombreProyectoContainingIgnoreCaseOrEstatusAndEmpresa_NombreContainingIgnoreCaseOrEstatusAndCarrera_NombreContainingIgnoreCase(
			Integer estatus1, String nombreProyecto,
			Integer estatus2, String nombreEmpresa,
			Integer estatus3, String nombreCarrera);
}