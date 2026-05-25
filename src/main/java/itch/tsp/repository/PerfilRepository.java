package itch.tsp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import itch.tsp.model.Perfil;

public interface PerfilRepository extends JpaRepository<Perfil, Integer> {

	Perfil findByNombreAndEstatus(String nombre, Integer estatus);

	List<Perfil> findByEstatusOrderByIdAsc(Integer estatus);
}