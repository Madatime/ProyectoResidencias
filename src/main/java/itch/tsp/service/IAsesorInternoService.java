package itch.tsp.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import itch.tsp.model.AsesorInterno;
import itch.tsp.model.Residencia;

public interface IAsesorInternoService {

	List<AsesorInterno> buscarTodosActivos();

	List<AsesorInterno> buscarTodosInactivos();

	List<AsesorInterno> buscarAsesoresInternos(String texto);

	List<AsesorInterno> buscarAsesoresInternosConProyecto();

	List<AsesorInterno> buscarAsesoresInternosConProyectoPorPeriodo(String periodo);

	List<AsesorInterno> buscarAsesoresInternosConProyectoPorPeriodoYTexto(String periodo, String texto);

	List<Residencia> buscarProyectosAsignados(Integer idAsesorInterno);

	void guardarAsesorInterno(AsesorInterno asesorInterno);

	void guardarAsesorInternoConArchivos(AsesorInterno asesorInterno, MultipartFile foto, MultipartFile documento);

	AsesorInterno buscarPorIdAsesorInterno(Integer idAsesorInterno);

	AsesorInterno buscarPorIdDocente(Integer idDocente);

	boolean existeNoEmpleado(String noEmpleado);

	boolean existeNoEmpleadoParaOtroRegistro(String noEmpleado, Integer id);

	String normalizarNoEmpleado(String noEmpleado);

	void eliminar(Integer idAsesorInterno);

	void recuperar(Integer idAsesorInterno);
}
