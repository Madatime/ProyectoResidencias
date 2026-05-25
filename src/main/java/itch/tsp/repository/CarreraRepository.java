package itch.tsp.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import itch.tsp.model.Carrera;

public interface CarreraRepository extends JpaRepository<Carrera, Integer> {

	List<Carrera> findByEstatus(Integer estatus);
	
	Carrera findByIdAndEstatus(Integer id, Integer estatus);
	
	List<Carrera> findByEstatusOrderByIdAsc(Integer estatus);

}