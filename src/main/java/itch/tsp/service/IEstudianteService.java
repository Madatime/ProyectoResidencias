package itch.tsp.service;

import java.util.List;

import itch.tsp.model.Estudiante;

public interface IEstudianteService {

	List<Estudiante> buscarTodosActivos();

	List<Estudiante> buscarTodosInactivos();

	List<Estudiante> buscarEstudiantes(String texto);

	Estudiante buscarPorIdEstudiante(Integer idEstudiante);

	Estudiante buscarPorMatricula(String matricula);

	void guardarEstudiante(Estudiante estudiante);

	void eliminar(Integer idEstudiante);

	void recuperar(Integer idEstudiante);

	String normalizarMatricula(String matricula);

	boolean existeMatricula(String matricula);

	boolean existeMatriculaParaOtroRegistro(String matricula, Integer id);

	List<Estudiante> buscarEstudiantesParaResidencia(String texto, String filtroProyectoResidencia);
}
