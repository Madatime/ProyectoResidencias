package itch.tsp.service;

import java.util.List;

import itch.tsp.model.Residencia;

public interface IResidenciaService {

	List<Residencia> buscarTodasActivas();

	List<Residencia> buscarTodasInactivas();

	List<Residencia> buscarResidenciasPorPeriodo(String periodo);

	List<Residencia> buscarResidenciasPorTexto(String texto);

	List<Residencia> buscarResidenciasPorPeriodoYTexto(String periodo, String texto);

	List<Residencia> buscarAlumnosConProyecto(String texto);

	List<Residencia> buscarAsesoresInternosPorPeriodo(String periodo);

	List<Residencia> buscarProyectosPorPeriodo(String periodo);

	Residencia buscarPorIdResidencia(Integer idResidencia);

	void guardarResidencia(Residencia residencia);

	void dictaminarProyecto(Integer idResidencia, String estadoAutorizacion, String observacionesAutorizacion);

	void cerrarExpediente(Integer idResidencia);

	void reabrirExpediente(Integer idResidencia);

	void eliminar(Integer idResidencia);

	void recuperar(Integer idResidencia);
}