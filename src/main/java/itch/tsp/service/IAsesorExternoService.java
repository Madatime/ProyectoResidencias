package itch.tsp.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import itch.tsp.model.AsesorExterno;

public interface IAsesorExternoService {

	List<AsesorExterno> buscarTodosActivos();

	List<AsesorExterno> buscarTodosInactivos();

	List<AsesorExterno> buscarAsesoresExternos(String texto);

	void guardarAsesorExterno(AsesorExterno asesorExterno);

	void guardarAsesorExternoConArchivos(AsesorExterno asesorExterno, MultipartFile foto, MultipartFile documento);

	AsesorExterno buscarPorIdAsesorExterno(Integer idAsesorExterno);

	void eliminar(Integer idAsesorExterno);

	void recuperar(Integer idAsesorExterno);
}