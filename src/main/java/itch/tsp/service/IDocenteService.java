package itch.tsp.service;

import java.util.List;

import itch.tsp.model.Docente;

public interface IDocenteService {

	List<Docente> buscarTodosActivos();

	List<Docente> buscarTodosInactivos();

	Docente buscarPorId(Integer id);

	void guardar(Docente docente, List<Integer> idsCarreras);

	void eliminar(Integer id);

	void recuperar(Integer id);
}