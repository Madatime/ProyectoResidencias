package itch.tsp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import itch.tsp.model.UsuarioPerfil;

public interface UsuarioPerfilRepository extends JpaRepository<UsuarioPerfil, Integer> {

	List<UsuarioPerfil> findByUsuario_IdAndEstatus(Integer idUsuario, Integer estatus);
}