package itch.tsp.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import itch.tsp.model.Usuario;

public class UsuarioPrincipal extends User {

	private static final long serialVersionUID = 1L;

	private final Integer idUsuario;
	private final Integer idDocente;
	private final Integer idResidente;
	private final Integer idAsesorExterno;
	private final String nombreCompleto;
	private final String nombreMostrar;

	public UsuarioPrincipal(
			Usuario usuario,
			Collection<? extends GrantedAuthority> authorities) {

		super(
				usuario.getUsername(),
				usuario.getPassword(),
				usuario.getEstatus() != null && usuario.getEstatus() == 1,
				true,
				true,
				true,
				authorities
		);

		this.idUsuario = usuario.getId();

		this.idDocente = usuario.getDocente() != null
				? usuario.getDocente().getId()
				: null;

		this.idResidente = usuario.getResidente() != null
				? usuario.getResidente().getId()
				: null;

		this.idAsesorExterno = usuario.getAsesorExterno() != null
				? usuario.getAsesorExterno().getId()
				: null;

		this.nombreCompleto = usuario.getNombreCompleto();
		this.nombreMostrar = usuario.getNombreMostrar();
	}

	public Integer getIdUsuario() {
		return idUsuario;
	}

	public Integer getIdDocente() {
		return idDocente;
	}

	public Integer getIdResidente() {
		return idResidente;
	}

	public Integer getIdAsesorExterno() {
		return idAsesorExterno;
	}

	public String getNombreCompleto() {
		return nombreCompleto;
	}

	public String getNombreMostrar() {
		return nombreMostrar;
	}

	public String getNombreVisible() {
		if (nombreMostrar != null && !nombreMostrar.trim().isEmpty()) {
			return nombreMostrar;
		}

		if (nombreCompleto != null && !nombreCompleto.trim().isEmpty()) {
			return nombreCompleto;
		}

		return getUsername();
	}
}
