package itch.tsp.service;

import java.util.List;

import itch.tsp.model.Empresa;

public interface IEmpresaService {

	List<Empresa> buscarTodasActivas();

	List<Empresa> buscarTodasInactivas();

	List<Empresa> buscarEmpresas(String texto);

	Empresa buscarPorIdEmpresa(Integer idEmpresa);

	void guardarEmpresa(Empresa empresa);

	boolean existeNombre(String nombre);

	boolean existeNombreParaOtroRegistro(String nombre, Integer id);

	String normalizarNombre(String nombre);

	void eliminar(Integer idEmpresa);

	void recuperar(Integer idEmpresa);
}