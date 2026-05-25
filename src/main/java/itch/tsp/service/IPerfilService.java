package itch.tsp.service;

import java.util.List;

import itch.tsp.model.Perfil;

public interface IPerfilService {

	List<Perfil> buscarTodosActivos();

	Perfil buscarPorNombre(String nombre);

	void guardar(Perfil perfil);
}