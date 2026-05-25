package itch.tsp.service;

import java.util.List;

import itch.tsp.model.Perfil;
import itch.tsp.model.Usuario;
import itch.tsp.model.UsuarioPerfil;

public interface IUsuarioService {

	Usuario buscarPorUsername(String username);

	void guardarUsuarioConPerfil(Usuario usuario, String nombrePerfil);

	List<UsuarioPerfil> buscarPerfilesDeUsuario(Integer idUsuario);

	void crearUsuarioParaDocente(Integer idDocente, String nombrePerfil);

	void crearUsuarioParaResidente(Integer idResidente);

	void crearUsuarioParaAsesorExterno(Integer idAsesorExterno);
	
	List<Usuario> buscarTodos();

	List<Usuario> buscarPorTexto(String texto);

	List<Usuario> buscarPorTextoYPerfil(String texto, String perfil);
	
	Usuario buscarPorId(Integer idUsuario);

	void eliminar(Integer idUsuario);

	void restablecerPassword(Integer idUsuario);
	
	List<Perfil> buscarPerfilesActivos();

	List<Usuario> buscarInactivos();

	void recuperar(Integer idUsuario);
	
}
