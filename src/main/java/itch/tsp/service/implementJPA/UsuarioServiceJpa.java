package itch.tsp.service.implementJPA;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import itch.tsp.model.AsesorExterno;
import itch.tsp.model.Docente;
import itch.tsp.model.Perfil;
import itch.tsp.model.Residente;
import itch.tsp.model.Usuario;
import itch.tsp.model.UsuarioPerfil;
import itch.tsp.repository.AsesorExternoRepository;
import itch.tsp.repository.DocenteRepository;
import itch.tsp.repository.PerfilRepository;
import itch.tsp.repository.ResidenteRepository;
import itch.tsp.repository.UsuarioPerfilRepository;
import itch.tsp.repository.UsuarioRepository;
import itch.tsp.service.IUsuarioService;


@Primary
@Service
public class UsuarioServiceJpa implements IUsuarioService {

	@Autowired
	private UsuarioRepository repoUsuario;

	@Autowired
	private PerfilRepository repoPerfil;

	@Autowired
	private UsuarioPerfilRepository repoUsuarioPerfil;

	@Autowired
	private DocenteRepository repoDocente;

	@Autowired
	private ResidenteRepository repoResidente;

	@Autowired
	private AsesorExternoRepository repoAsesorExterno;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private static final String PASSWORD_TEMPORAL = "123";

	@Override
	public Usuario buscarPorUsername(String username) {
		return repoUsuario.findByUsernameAndEstatus(username, 1);
	}

	@Override
	public void guardarUsuarioConPerfil(Usuario usuario, String nombrePerfil) {
		if (usuario == null) {
			throw new RuntimeException("No se recibió información del usuario.");
		}

		if (usuario.getUsername() != null) {
			usuario.setUsername(usuario.getUsername().trim());
		}

		if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {
			throw new RuntimeException("El nombre de usuario es obligatorio.");
		}

		if (usuario.getNombreCompleto() != null) {
			usuario.setNombreCompleto(usuario.getNombreCompleto().trim());
		}

		if (usuario.getNombreCompleto() == null || usuario.getNombreCompleto().isBlank()) {
			throw new RuntimeException("El nombre completo es obligatorio.");
		}

		prepararNombreMostrar(usuario);
		prepararEmail(usuario);
		prepararRol(usuario, nombrePerfil);

		Usuario existente = usuario.getId() != null
				? repoUsuario.findById(usuario.getId()).orElse(null)
				: null;

		String passwordCapturado = usuario.getPassword() != null ? usuario.getPassword().trim() : "";

		if (existente != null) {
			if (passwordCapturado.isEmpty()) {
				usuario.setPassword(existente.getPassword());
			} else if (!passwordCapturado.startsWith("$2")) {
				usuario.setPassword(passwordEncoder.encode(passwordCapturado));
			}
		} else {
			if (passwordCapturado.isEmpty()) {
				throw new RuntimeException("La contraseña es obligatoria.");
			}
			if (!passwordCapturado.startsWith("$2")) {
				usuario.setPassword(passwordEncoder.encode(passwordCapturado));
			}
		}

		Usuario usuarioConMismoUsername = repoUsuario.findByUsernameAndEstatus(usuario.getUsername(), 1);
		if (usuarioConMismoUsername != null
				&& (usuario.getId() == null || !usuarioConMismoUsername.getId().equals(usuario.getId()))) {
			throw new RuntimeException("Ya existe un usuario activo con ese nombre de usuario.");
		}

		if (usuario.getEstatus() == null) {
			usuario.setEstatus(1);
		}

		repoUsuario.save(usuario);

		asignarPerfil(usuario, nombrePerfil);
	}

	@Override
	public List<UsuarioPerfil> buscarPerfilesDeUsuario(Integer idUsuario) {
		return repoUsuarioPerfil.findByUsuario_IdAndEstatus(idUsuario, 1);
	}

	@Override
	public void crearUsuarioParaDocente(Integer idDocente, String nombrePerfil) {

		Docente docente = repoDocente.findByIdAndEstatus(idDocente, 1);

		if (docente == null) {
			throw new RuntimeException("El docente no existe o está inactivo.");
		}

		if (repoUsuario.existsByDocente_Id(idDocente)) {
			throw new RuntimeException("Este docente ya tiene un usuario asignado.");
		}

		String username = docente.getNoEmpleado();

		if (username == null || username.trim().isEmpty()) {
			throw new RuntimeException("El docente no tiene número de empleado.");
		}

		username = username.trim();

		if (repoUsuario.existsByUsername(username)) {
			throw new RuntimeException("Ya existe un usuario con el nombre: " + username);
		}

		Usuario usuario = new Usuario();
		usuario.setUsername(username);
		usuario.setPassword(passwordEncoder.encode(PASSWORD_TEMPORAL));
		usuario.setNombreCompleto(docente.getNombreCompleto());
		usuario.setNombreMostrar(docente.getNombreCompleto());
		usuario.setEmail(docente.getCorreo());
		usuario.setRol(nombrePerfil != null ? nombrePerfil.trim().toUpperCase() : null);
		usuario.setDocente(docente);
		usuario.setEstatus(1);

		repoUsuario.save(usuario);

		asignarPerfil(usuario, nombrePerfil);
	}

	@Override
	public void crearUsuarioParaResidente(Integer idResidente) {

		Residente residente = repoResidente.findByIdAndEstatus(idResidente, 1);

		if (residente == null) {
			throw new RuntimeException("El residente no existe o está inactivo.");
		}

		if (repoUsuario.existsByResidente_Id(idResidente)) {
			throw new RuntimeException("Este residente ya tiene un usuario asignado.");
		}

		String username = residente.getMatricula();

		if (username == null || username.trim().isEmpty()) {
			throw new RuntimeException("El residente no tiene matrícula.");
		}

		username = username.trim();

		if (repoUsuario.existsByUsername(username)) {
			throw new RuntimeException("Ya existe un usuario con el nombre: " + username);
		}

		Usuario usuario = new Usuario();
		usuario.setUsername(username);
		usuario.setPassword(passwordEncoder.encode(PASSWORD_TEMPORAL));
		usuario.setNombreCompleto(residente.getNombreCompleto());
		usuario.setNombreMostrar(residente.getNombreCompleto());
		usuario.setEmail(residente.getCorreo());
		usuario.setRol("ESTUDIANTE");
		usuario.setResidente(residente);
		usuario.setEstatus(1);

		repoUsuario.save(usuario);

		asignarPerfil(usuario, "ESTUDIANTE");
	}

	@Override
	public void crearUsuarioParaAsesorExterno(Integer idAsesorExterno) {

		AsesorExterno asesorExterno = repoAsesorExterno.findByIdAndEstatus(idAsesorExterno, 1);

		if (asesorExterno == null) {
			throw new RuntimeException("El asesor externo no existe o está inactivo.");
		}

		if (repoUsuario.existsByAsesorExterno_Id(idAsesorExterno)) {
			throw new RuntimeException("Este asesor externo ya tiene un usuario asignado.");
		}

		String username = generarUsernameAsesorExterno(asesorExterno);

		Usuario usuario = new Usuario();
		usuario.setUsername(username);
		usuario.setPassword(passwordEncoder.encode(PASSWORD_TEMPORAL));
		usuario.setNombreCompleto(asesorExterno.getNombreCompleto());
		usuario.setNombreMostrar(asesorExterno.getNombreCompleto());
		usuario.setEmail(generarEmailAsesorExterno(asesorExterno, username));
		usuario.setRol("ASESOR_EXTERNO");
		usuario.setAsesorExterno(asesorExterno);
		usuario.setEstatus(1);

		repoUsuario.save(usuario);

		asignarPerfil(usuario, "ASESOR_EXTERNO");
	}

	private String generarUsernameAsesorExterno(AsesorExterno asesorExterno) {
		String correo = asesorExterno.getCorreo() != null ? asesorExterno.getCorreo().trim() : "";

		if (!correo.isEmpty() && !repoUsuario.existsByUsername(correo)) {
			return correo;
		}

		String base = "asesorext" + asesorExterno.getId();
		String candidato = base;
		int sufijo = 1;

		while (repoUsuario.existsByUsername(candidato)) {
			candidato = base + "_" + sufijo++;
		}

		return candidato;
	}

	private String generarEmailAsesorExterno(AsesorExterno asesorExterno, String username) {
		String correo = asesorExterno.getCorreo() != null ? asesorExterno.getCorreo().trim() : "";

		if (!correo.isEmpty() && !repoUsuario.existsByEmail(correo)) {
			return correo;
		}

		String baseLocal = (username != null && !username.isBlank())
				? username.trim()
				: "asesorext" + asesorExterno.getId();

		baseLocal = baseLocal.replaceAll("[^A-Za-z0-9._-]", "_");
		String candidato = baseLocal + "@local.invalid";
		int sufijo = 1;

		while (repoUsuario.existsByEmail(candidato)) {
			candidato = baseLocal + "_" + sufijo++ + "@local.invalid";
		}

		return candidato;
	}

	private void asignarPerfil(Usuario usuario, String nombrePerfil) {

		if (nombrePerfil == null || nombrePerfil.trim().isEmpty()) {
			throw new RuntimeException("Debe indicar un perfil.");
		}

		String perfilNormalizado = nombrePerfil.trim().toUpperCase();

		Perfil perfil = repoPerfil.findByNombreAndEstatus(perfilNormalizado, 1);

		if (perfil == null) {
			throw new RuntimeException("El perfil " + perfilNormalizado + " no existe.");
		}

		List<UsuarioPerfil> perfilesActivos = repoUsuarioPerfil.findByUsuario_IdAndEstatus(usuario.getId(), 1);
		for (UsuarioPerfil perfilActivo : perfilesActivos) {
			if (perfilActivo.getPerfil() != null
					&& perfilActivo.getPerfil().getId() != null
					&& perfilActivo.getPerfil().getId().equals(perfil.getId())) {
				return;
			}
		}

		for (UsuarioPerfil perfilActivo : perfilesActivos) {
			perfilActivo.setEstatus(0);
			repoUsuarioPerfil.save(perfilActivo);
		}

		UsuarioPerfil usuarioPerfil = new UsuarioPerfil();
		usuarioPerfil.setUsuario(usuario);
		usuarioPerfil.setPerfil(perfil);
		usuarioPerfil.setEstatus(1);

		repoUsuarioPerfil.save(usuarioPerfil);
	}
	
	@Override
	public List<Usuario> buscarTodos() {
		return repoUsuario.findAll();
	}
	
	@Override
	public List<Usuario> buscarPorTexto(String texto) {
		return buscarPorTextoYPerfil(texto, null);
	}

	@Override
	public List<Usuario> buscarPorTextoYPerfil(String texto, String perfil) {

		List<Usuario> resultado = new ArrayList<>();
		String textoBusqueda = texto != null ? texto.trim().toLowerCase() : "";
		String perfilBusqueda = perfil != null ? perfil.trim().toUpperCase() : "";
		boolean filtrarTexto = !textoBusqueda.isEmpty();
		boolean filtrarPerfil = !perfilBusqueda.isEmpty();

		for (Usuario usuario : repoUsuario.findAll()) {
			cargarPerfilNombre(usuario);

			boolean coincideTexto = !filtrarTexto
					|| (usuario.getUsername() != null
							&& usuario.getUsername().toLowerCase().contains(textoBusqueda))
					|| (usuario.getNombreCompleto() != null
							&& usuario.getNombreCompleto().toLowerCase().contains(textoBusqueda));

			boolean coincidePerfil = !filtrarPerfil
					|| (usuario.getPerfilNombre() != null
							&& usuario.getPerfilNombre().trim().toUpperCase().equals(perfilBusqueda));

			if (coincideTexto && coincidePerfil) {
				resultado.add(usuario);
			}
		}

		return resultado;
	}
	
	@Override
	public Usuario buscarPorId(Integer idUsuario) {
		return repoUsuario.findById(idUsuario).orElse(null);
	}

	@Override
	public List<Perfil> buscarPerfilesActivos() {
		List<Perfil> perfilesActivos = new ArrayList<>();

		for (Perfil perfil : repoPerfil.findAll()) {
			if (perfil.getEstatus() != null && perfil.getEstatus() == 1) {
				perfilesActivos.add(perfil);
			}
		}

		return perfilesActivos;
	}

	@Override
	public void eliminar(Integer idUsuario) {
		Usuario usuario = buscarPorId(idUsuario);

		if (usuario == null) {
			throw new RuntimeException("El usuario no existe.");
		}

		if (usuario.getEstatus() == null || usuario.getEstatus() != 1) {
			throw new RuntimeException("El usuario ya está desactivado.");
		}

		if (esAdministrador(usuario)) {
			int totalAdminsActivos = contarAdministradoresActivos();

			if (totalAdminsActivos <= 1) {
				throw new RuntimeException("No se puede desactivar este usuario porque debe existir al menos un administrador activo.");
			}
		}

		usuario.setEstatus(0);
		repoUsuario.save(usuario);
	}

	@Override
	public void restablecerPassword(Integer idUsuario) {
		Usuario usuario = buscarPorId(idUsuario);

		if (usuario != null) {
			usuario.setPassword(passwordEncoder.encode(PASSWORD_TEMPORAL));
			repoUsuario.save(usuario);
		}
	}
	
	@Override
	public List<Usuario> buscarInactivos() {
		List<Usuario> inactivos = new ArrayList<>();

		for (Usuario usuario : repoUsuario.findAll()) {
			if (usuario.getEstatus() != null && usuario.getEstatus() == 0) {
				inactivos.add(usuario);
			}
		}

		return inactivos;
	}

	@Override
	public void recuperar(Integer idUsuario) {
		Usuario usuario = buscarPorId(idUsuario);

		if (usuario != null) {
			usuario.setEstatus(1);
			repoUsuario.save(usuario);
		}
	}
	
	private boolean esAdministrador(Usuario usuario) {
		List<UsuarioPerfil> perfiles = repoUsuarioPerfil.findByUsuario_IdAndEstatus(usuario.getId(), 1);

		for (UsuarioPerfil usuarioPerfil : perfiles) {
			if (usuarioPerfil.getPerfil() != null
					&& "ADMINISTRADOR".equalsIgnoreCase(usuarioPerfil.getPerfil().getNombre())) {
				return true;
			}
		}

		return false;
	}

	private void cargarPerfilNombre(Usuario usuario) {
		List<UsuarioPerfil> perfiles = repoUsuarioPerfil.findByUsuario_IdAndEstatus(usuario.getId(), 1);

		if (perfiles != null && !perfiles.isEmpty()
				&& perfiles.get(0).getPerfil() != null) {
			usuario.setPerfilNombre(perfiles.get(0).getPerfil().getNombre());
		}
	}

	private void prepararNombreMostrar(Usuario usuario) {
		if (usuario == null) {
			return;
		}

		if (usuario.getNombreMostrar() != null) {
			usuario.setNombreMostrar(usuario.getNombreMostrar().trim());
		}

		if (usuario.getNombreMostrar() == null || usuario.getNombreMostrar().isBlank()) {
			usuario.setNombreMostrar(usuario.getNombreCompleto());
		}

		if (usuario.getNombreMostrar() != null) {
			usuario.setNombreMostrar(usuario.getNombreMostrar().trim());
		}
	}

	private void prepararEmail(Usuario usuario) {
		if (usuario == null) {
			return;
		}

		if (usuario.getEmail() != null) {
			usuario.setEmail(usuario.getEmail().trim());
		}

		if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
			if (usuario.getDocente() != null && usuario.getDocente().getCorreo() != null) {
				usuario.setEmail(usuario.getDocente().getCorreo().trim());
			} else if (usuario.getResidente() != null && usuario.getResidente().getCorreo() != null) {
				usuario.setEmail(usuario.getResidente().getCorreo().trim());
			} else if (usuario.getAsesorExterno() != null && usuario.getAsesorExterno().getCorreo() != null) {
				usuario.setEmail(usuario.getAsesorExterno().getCorreo().trim());
			}
		}

		if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
			usuario.setEmail(usuario.getUsername().trim() + "@local.invalid");
		}
	}

	private void prepararRol(Usuario usuario, String nombrePerfil) {
		if (usuario == null) {
			return;
		}

		if (usuario.getRol() != null) {
			usuario.setRol(usuario.getRol().trim().toUpperCase());
		}

		if ((usuario.getRol() == null || usuario.getRol().isBlank())
				&& nombrePerfil != null
				&& !nombrePerfil.trim().isEmpty()) {
			usuario.setRol(nombrePerfil.trim().toUpperCase());
		}
	}

	private int contarAdministradoresActivos() {
		int total = 0;

		for (Usuario usuario : repoUsuario.findAll()) {
			if (usuario.getEstatus() != null
					&& usuario.getEstatus() == 1
					&& esAdministrador(usuario)) {
				total++;
			}
		}

		return total;
	}
	
}
