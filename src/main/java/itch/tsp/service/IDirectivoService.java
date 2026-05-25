package itch.tsp.service;

import java.util.List;

import itch.tsp.model.Directivo;
import itch.tsp.model.TipoDirectivo;

public interface IDirectivoService {

	List<Directivo> buscarTodosActivos();

	List<Directivo> buscarTodosInactivos();

	Directivo buscarPorId(Integer id);

	Directivo buscarPorTipoActivo(TipoDirectivo tipoDirectivo);
	
	Directivo buscarPorDocente(Integer idDocente);

	Directivo buscarJefeDepartamentoPorCarrera(String carrera);

	void guardar(Directivo directivo);

	void eliminar(Integer id);

	void recuperar(Integer id);
}
