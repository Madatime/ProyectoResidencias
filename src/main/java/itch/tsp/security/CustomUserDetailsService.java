package itch.tsp.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import itch.tsp.model.Usuario;
import itch.tsp.model.UsuarioPerfil;
import itch.tsp.service.IUsuarioService;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private IUsuarioService serviceUsuario;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		Usuario usuario = serviceUsuario.buscarPorUsername(username);

		if (usuario == null) {
			throw new UsernameNotFoundException("Usuario no encontrado");
		}

		List<UsuarioPerfil> perfiles = serviceUsuario.buscarPerfilesDeUsuario(usuario.getId());
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();

		for (UsuarioPerfil usuarioPerfil : perfiles) {

			if (usuarioPerfil.getPerfil() != null && usuarioPerfil.getPerfil().getNombre() != null) {

				String nombrePerfil = usuarioPerfil.getPerfil().getNombre().trim().toUpperCase();

				if (!nombrePerfil.startsWith("ROLE_")) {
					nombrePerfil = "ROLE_" + nombrePerfil;
				}

				authorities.add(new SimpleGrantedAuthority(nombrePerfil));
			}
		}

		return new UsuarioPrincipal(usuario, authorities);
	}
}
