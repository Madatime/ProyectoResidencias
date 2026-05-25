package itch.tsp.service;

import java.util.List;

import itch.tsp.model.Carrera;

public interface ICarreraService {

	List<Carrera> buscarTodas();

	List<Carrera> buscarTodosActivos();

	List<Carrera> buscarTodasActivas();

	List<Carrera> buscarTodasInactivas();

	Carrera buscarPorId(Integer id);

	void guardar(Carrera carrera);

	void eliminar(Integer id);

	void recuperar(Integer id);
}