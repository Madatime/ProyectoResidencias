package itch.tsp.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import itch.tsp.model.Perfil;
import itch.tsp.model.Usuario;
import itch.tsp.model.UsuarioPerfil;
import itch.tsp.repository.PerfilRepository;
import itch.tsp.repository.UsuarioPerfilRepository;
import itch.tsp.repository.UsuarioRepository;

@Configuration
public class SecurityBootstrapInitializer {

	private static final List<String> PERFILES_BASE = List.of(
			"ADMINISTRADOR",
			"DIVISION_ESTUDIOS",
			"JEFE_DEPARTAMENTO",
			"VINCULACION",
			"SERVICIOS_ESCOLARES",
			"ASESOR_INTERNO",
			"ASESOR_EXTERNO",
			"ESTUDIANTE"
	);

	@Bean
	CommandLineRunner inicializarSeguridadBase(
			PerfilRepository perfilRepository,
			UsuarioRepository usuarioRepository,
			UsuarioPerfilRepository usuarioPerfilRepository,
			PasswordEncoder passwordEncoder,
			@Value("${app.bootstrap.admin.enabled:true}") boolean adminHabilitado,
			@Value("${app.bootstrap.admin.username:admin}") String adminUsername,
			@Value("${app.bootstrap.admin.password:admin123}") String adminPassword,
			@Value("${app.bootstrap.admin.nombre:Aldo Olivar Herrera}") String adminNombre) {

		return args -> {
			crearPerfilesBase(perfilRepository);

			if (adminHabilitado) {
				asegurarAdministradorInicial(
						perfilRepository,
						usuarioRepository,
						usuarioPerfilRepository,
						passwordEncoder,
						adminUsername,
						adminPassword,
						adminNombre
				);
			}
		};
	}

	private void crearPerfilesBase(PerfilRepository perfilRepository) {
		for (String nombrePerfil : PERFILES_BASE) {
			Perfil perfilExistente = perfilRepository.findByNombreAndEstatus(nombrePerfil, 1);

			if (perfilExistente != null) {
				continue;
			}

			Perfil perfil = new Perfil();
			perfil.setNombre(nombrePerfil);
			perfil.setEstatus(1);
			perfilRepository.save(perfil);
		}
	}

	private void asegurarAdministradorInicial(
			PerfilRepository perfilRepository,
			UsuarioRepository usuarioRepository,
			UsuarioPerfilRepository usuarioPerfilRepository,
			PasswordEncoder passwordEncoder,
			String adminUsername,
			String adminPassword,
			String adminNombre) {

		Usuario administradorActivo = buscarAdministradorActivo(usuarioRepository, usuarioPerfilRepository);

		Usuario admin = usuarioRepository.findByUsernameAndEstatus(adminUsername, 1);

		if (admin == null && administradorActivo == null) {
			admin = new Usuario();
			admin.setUsername(adminUsername.trim());
			admin.setPassword(passwordEncoder.encode(adminPassword));
			admin.setNombreCompleto(adminNombre.trim());
			admin.setNombreMostrar(adminNombre.trim());
			admin.setEmail(adminUsername.trim() + "@local.invalid");
			admin.setRol("ADMINISTRADOR");
			admin.setEstatus(1);
			admin = usuarioRepository.save(admin);
		} else if (admin != null) {
			admin.setNombreCompleto(adminNombre.trim());
			admin.setNombreMostrar(adminNombre.trim());
			if (admin.getRol() == null || admin.getRol().trim().isEmpty()) {
				admin.setRol("ADMINISTRADOR");
			}
			admin = usuarioRepository.save(admin);
		} else {
			admin = administradorActivo;
		}

		Perfil perfilAdmin = perfilRepository.findByNombreAndEstatus("ADMINISTRADOR", 1);
		if (perfilAdmin == null) {
			throw new RuntimeException("No se pudo inicializar el perfil ADMINISTRADOR.");
		}

		boolean yaTienePerfil = usuarioPerfilRepository.findByUsuario_IdAndEstatus(admin.getId(), 1).stream()
				.anyMatch(usuarioPerfil -> usuarioPerfil.getPerfil() != null
						&& "ADMINISTRADOR".equalsIgnoreCase(usuarioPerfil.getPerfil().getNombre()));

		if (yaTienePerfil) {
			return;
		}

		UsuarioPerfil usuarioPerfil = new UsuarioPerfil();
		usuarioPerfil.setUsuario(admin);
		usuarioPerfil.setPerfil(perfilAdmin);
		usuarioPerfil.setEstatus(1);
		usuarioPerfilRepository.save(usuarioPerfil);
	}

	private Usuario buscarAdministradorActivo(
			UsuarioRepository usuarioRepository,
			UsuarioPerfilRepository usuarioPerfilRepository) {

		for (Usuario usuario : usuarioRepository.findAll()) {
			if (usuario.getEstatus() == null || usuario.getEstatus() != 1) {
				continue;
			}

			boolean esAdmin = usuarioPerfilRepository.findByUsuario_IdAndEstatus(usuario.getId(), 1).stream()
					.anyMatch(usuarioPerfil -> usuarioPerfil.getPerfil() != null
							&& "ADMINISTRADOR".equalsIgnoreCase(usuarioPerfil.getPerfil().getNombre()));

			if (esAdmin) {
				return usuario;
			}
		}

		return null;
	}
}
