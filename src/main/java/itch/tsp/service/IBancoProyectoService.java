package itch.tsp.service;

import java.util.List;

import itch.tsp.model.BancoProyecto;
import itch.tsp.model.EstadoBancoProyecto;

public interface IBancoProyectoService {

	List<BancoProyecto> buscarTodosActivos();

	List<BancoProyecto> buscarTodosInactivos();

	List<BancoProyecto> buscarDisponibles();

	List<BancoProyecto> buscarPendientesRevision();

	List<BancoProyecto> buscarPorTexto(String texto);

	BancoProyecto buscarPorId(Integer id);

	void guardar(BancoProyecto proyecto);

	void proponerProyecto(BancoProyecto proyecto, Integer idResidente);

	void revisarProyecto(Integer idProyecto, EstadoBancoProyecto estado, String observaciones);

	void marcarAsignado(Integer idProyecto);

	void eliminar(Integer id);

	void recuperar(Integer id);
}