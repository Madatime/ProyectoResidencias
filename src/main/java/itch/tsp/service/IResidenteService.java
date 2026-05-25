package itch.tsp.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import itch.tsp.model.Residente;

public interface IResidenteService {

	List<Residente> buscarTodosActivos();

	List<Residente> buscarTodosInactivos();

	List<Residente> buscarResidentes(String texto);

	void guardarResidente(Residente residente);

	void guardarResidenteConArchivos(Residente residente, MultipartFile foto, MultipartFile documento);

	Residente buscarPorIdResidente(Integer idResidente);

	boolean existeMatricula(String matricula);

	boolean existeMatriculaParaOtroRegistro(String matricula, Integer id);

	String normalizarMatricula(String matricula);

	boolean matriculaValida(String matricula);

	void eliminar(Integer idResidente);

	void recuperar(Integer idResidente);
}