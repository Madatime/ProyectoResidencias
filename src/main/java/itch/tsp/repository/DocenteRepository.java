package itch.tsp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import itch.tsp.model.Docente;

public interface DocenteRepository extends JpaRepository<Docente, Integer> {

	List<Docente> findByEstatusOrderByIdDesc(Integer estatus);

	List<Docente> findByEstatusOrderByIdAsc(Integer estatus);

	Docente findByIdAndEstatus(Integer id, Integer estatus);

	boolean existsByNoEmpleado(String noEmpleado);
}