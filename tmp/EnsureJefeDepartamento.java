import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class EnsureJefeDepartamento {

	private static final String URL = "jdbc:mysql://localhost:3306/proyectoResidencia?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true";
	private static final String USER = "root";
	private static final String PASSWORD = "12345";
	private static final String PASSWORD_TEMPORAL = "123";

	public static void main(String[] args) throws Exception {
		try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
			connection.setAutoCommit(false);

			try {
				Integer idPerfilJefe = buscarPerfilJefe(connection);
				if (idPerfilJefe == null) {
					throw new IllegalStateException("No existe el perfil JEFE_DEPARTAMENTO.");
				}

				List<CarreraInfo> carreras = buscarCarrerasActivas(connection);
				if (carreras.isEmpty()) {
					throw new IllegalStateException("No existen carreras activas.");
				}

				List<JefeInfo> jefesActivos = buscarJefesActivos(connection);
				Map<String, JefeInfo> jefesValidosPorCarrera = new HashMap<>();
				List<JefeInfo> jefesReciclables = new ArrayList<>();

				for (JefeInfo jefe : jefesActivos) {
					if (jefe.departamento != null && existeCarreraActiva(connection, jefe.departamento)) {
						jefesValidosPorCarrera.putIfAbsent(jefe.departamento, jefe);
					} else {
						jefesReciclables.add(jefe);
					}
				}

				Set<Integer> docentesReservados = new HashSet<>();
				for (JefeInfo jefe : jefesActivos) {
					docentesReservados.add(jefe.idDocente);
				}

				List<String> resumen = new ArrayList<>();

				for (CarreraInfo carrera : carreras) {
					JefeInfo jefe = jefesValidosPorCarrera.get(carrera.nombre);

					if (jefe != null) {
						asegurarUsuarioYPerfil(connection, jefe.idDocente, idPerfilJefe);
						resumen.add(formatearResumen(carrera.nombre, jefe.idDirectivo, jefe.idDocente, buscarUsernameDocente(connection, jefe.idDocente)));
						continue;
					}

					if (!jefesReciclables.isEmpty()) {
						JefeInfo reciclado = jefesReciclables.remove(0);
						actualizarDirectivoComoJefeValido(connection, reciclado.idDirectivo, carrera.nombre);
						asegurarUsuarioYPerfil(connection, reciclado.idDocente, idPerfilJefe);
						resumen.add(formatearResumen(carrera.nombre, reciclado.idDirectivo, reciclado.idDocente, buscarUsernameDocente(connection, reciclado.idDocente)));
						continue;
					}

					DocenteInfo docente = buscarDocenteDisponible(connection, docentesReservados);
					if (docente == null) {
						throw new IllegalStateException("No hay suficientes docentes disponibles para completar los jefes por carrera.");
					}

					int idDirectivo = insertarDirectivo(connection, docente.id, carrera.nombre);
					asegurarUsuarioYPerfil(connection, docente.id, idPerfilJefe);
					docentesReservados.add(docente.id);
					resumen.add(formatearResumen(carrera.nombre, idDirectivo, docente.id, buscarUsernameDocente(connection, docente.id)));
				}

				connection.commit();

				System.out.println("Jefes de departamento asegurados por carrera:");
				for (String linea : resumen) {
					System.out.println(linea);
				}
				System.out.println("Password para los accesos creados o corregidos: " + PASSWORD_TEMPORAL);
			} catch (Exception e) {
				connection.rollback();
				throw e;
			}
		}
	}

	private static Integer buscarPerfilJefe(Connection connection) throws Exception {
		String sql = "select id from perfil where nombre = 'JEFE_DEPARTAMENTO' and estatus = 1 limit 1";
		try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
			return rs.next() ? rs.getInt("id") : null;
		}
	}

	private static List<CarreraInfo> buscarCarrerasActivas(Connection connection) throws Exception {
		String sql = "select id, nombre from carreras where estatus = 1 order by id asc";
		List<CarreraInfo> carreras = new ArrayList<>();

		try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
			while (rs.next()) {
				CarreraInfo carrera = new CarreraInfo();
				carrera.id = rs.getInt("id");
				carrera.nombre = rs.getString("nombre");
				carreras.add(carrera);
			}
		}

		return carreras;
	}

	private static boolean existeCarreraActiva(Connection connection, String nombreCarrera) throws Exception {
		String sql = "select count(*) as total from carreras where estatus = 1 and nombre = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, nombreCarrera);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt("total") > 0;
			}
		}
	}

	private static List<JefeInfo> buscarJefesActivos(Connection connection) throws Exception {
		String sql = """
			select d.id as idDirectivo, d.idDocente, d.departamento
			from directivos d
			where d.estatus = 1 and d.tipoDirectivo = 'JEFE_DEPARTAMENTO'
			order by d.id asc
			""";

		List<JefeInfo> jefes = new ArrayList<>();
		try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
			while (rs.next()) {
				JefeInfo jefe = new JefeInfo();
				jefe.idDirectivo = rs.getInt("idDirectivo");
				jefe.idDocente = rs.getInt("idDocente");
				jefe.departamento = rs.getString("departamento");
				jefes.add(jefe);
			}
		}

		return jefes;
	}

	private static DocenteInfo buscarDocenteDisponible(Connection connection, Set<Integer> docentesReservados) throws Exception {
		String sql = """
			select d.id, d.noEmpleado, d.nombre, d.apellidos, d.correo
			from docentes d
			left join directivos dir on dir.idDocente = d.id and dir.estatus = 1
			where d.estatus = 1
			  and dir.id is null
			order by d.id asc
			""";

		try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
			while (rs.next()) {
				int idDocente = rs.getInt("id");
				if (docentesReservados.contains(idDocente)) {
					continue;
				}

				DocenteInfo docente = new DocenteInfo();
				docente.id = idDocente;
				docente.noEmpleado = rs.getString("noEmpleado");
				String nombre = rs.getString("nombre") != null ? rs.getString("nombre").trim() : "";
				String apellidos = rs.getString("apellidos") != null ? rs.getString("apellidos").trim() : "";
				docente.nombreCompleto = (nombre + " " + apellidos).trim();
				docente.correo = rs.getString("correo");
				return docente;
			}
		}

		return null;
	}

	private static int insertarDirectivo(Connection connection, int idDocente, String carreraNombre) throws Exception {
		String clave = generarClaveDirectivo(connection);
		String sql = """
			insert into directivos (claveDirectivo, idDocente, tipoDirectivo, puesto, departamento, estatus)
			values (?, ?, 'JEFE_DEPARTAMENTO', ?, ?, 1)
			""";

		try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, clave);
			ps.setInt(2, idDocente);
			ps.setString(3, "Jefe de Departamento de " + carreraNombre);
			ps.setString(4, carreraNombre);
			ps.executeUpdate();

			try (ResultSet keys = ps.getGeneratedKeys()) {
				if (keys.next()) {
					return keys.getInt(1);
				}
			}
		}

		throw new IllegalStateException("No se pudo crear el directivo.");
	}

	private static void actualizarDirectivoComoJefeValido(Connection connection, int idDirectivo, String carreraNombre) throws Exception {
		String sql = """
			update directivos
			set departamento = ?, puesto = ?, tipoDirectivo = 'JEFE_DEPARTAMENTO', estatus = 1
			where id = ?
			""";

		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, carreraNombre);
			ps.setString(2, "Jefe de Departamento de " + carreraNombre);
			ps.setInt(3, idDirectivo);
			ps.executeUpdate();
		}
	}

	private static void asegurarUsuarioYPerfil(Connection connection, int idDocente, int idPerfil) throws Exception {
		Integer idUsuario = buscarUsuarioPorDocente(connection, idDocente);
		if (idUsuario == null) {
			DocenteInfo docente = buscarDocentePorId(connection, idDocente);
			if (docente == null) {
				throw new IllegalStateException("No se encontro el docente del jefe.");
			}
			idUsuario = insertarUsuario(connection, docente);
		}

		actualizarUsuarioJefe(connection, idUsuario, idDocente);

		if (!tienePerfil(connection, idUsuario, idPerfil)) {
			insertarUsuarioPerfil(connection, idUsuario, idPerfil);
		}
	}

	private static int insertarUsuario(Connection connection, DocenteInfo docente) throws Exception {
		String sql = """
			insert into usuario (username, password, nombreCompleto, nombreMostrar, email, rol, estatus, idDocente)
			values (?, ?, ?, ?, ?, 'JEFE_DEPARTAMENTO', 1, ?)
			""";

		String hash = new BCryptPasswordEncoder().encode(PASSWORD_TEMPORAL);

		try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, docente.noEmpleado);
			ps.setString(2, hash);
			ps.setString(3, docente.nombreCompleto);
			ps.setString(4, docente.nombreCompleto);
			ps.setString(5, docente.correo != null && !docente.correo.isBlank() ? docente.correo : docente.noEmpleado + "@itc.local");
			ps.setInt(6, docente.id);
			ps.executeUpdate();

			try (ResultSet keys = ps.getGeneratedKeys()) {
				if (keys.next()) {
					return keys.getInt(1);
				}
			}
		}

		throw new IllegalStateException("No se pudo crear el usuario del jefe.");
	}

	private static void actualizarUsuarioJefe(Connection connection, int idUsuario, int idDocente) throws Exception {
		DocenteInfo docente = buscarDocentePorId(connection, idDocente);
		if (docente == null) {
			throw new IllegalStateException("No se encontro el docente del usuario jefe.");
		}

		String sql = """
			update usuario
			set username = ?, password = ?, nombreCompleto = ?, nombreMostrar = ?, email = ?, rol = 'JEFE_DEPARTAMENTO', estatus = 1, idDocente = ?
			where id = ?
			""";

		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, docente.noEmpleado);
			ps.setString(2, new BCryptPasswordEncoder().encode(PASSWORD_TEMPORAL));
			ps.setString(3, docente.nombreCompleto);
			ps.setString(4, docente.nombreCompleto);
			ps.setString(5, docente.correo != null && !docente.correo.isBlank() ? docente.correo : docente.noEmpleado + "@itc.local");
			ps.setInt(6, idDocente);
			ps.setInt(7, idUsuario);
			ps.executeUpdate();
		}
	}

	private static Integer buscarUsuarioPorDocente(Connection connection, int idDocente) throws Exception {
		String sql = "select id from usuario where idDocente = ? limit 1";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, idDocente);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getInt("id") : null;
			}
		}
	}

	private static DocenteInfo buscarDocentePorId(Connection connection, int idDocente) throws Exception {
		String sql = "select id, noEmpleado, nombre, apellidos, correo from docentes where id = ? and estatus = 1";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, idDocente);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return null;
				}

				DocenteInfo docente = new DocenteInfo();
				docente.id = rs.getInt("id");
				docente.noEmpleado = rs.getString("noEmpleado");
				String nombre = rs.getString("nombre") != null ? rs.getString("nombre").trim() : "";
				String apellidos = rs.getString("apellidos") != null ? rs.getString("apellidos").trim() : "";
				docente.nombreCompleto = (nombre + " " + apellidos).trim();
				docente.correo = rs.getString("correo");
				return docente;
			}
		}
	}

	private static String buscarUsernameDocente(Connection connection, int idDocente) throws Exception {
		String sql = "select username from usuario where idDocente = ? and estatus = 1 limit 1";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, idDocente);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getString("username") : null;
			}
		}
	}

	private static boolean tienePerfil(Connection connection, int idUsuario, int idPerfil) throws Exception {
		String sql = "select count(*) as total from usuarioPerfil where idUsuario = ? and idPerfil = ? and estatus = 1";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, idUsuario);
			ps.setInt(2, idPerfil);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt("total") > 0;
			}
		}
	}

	private static void insertarUsuarioPerfil(Connection connection, int idUsuario, int idPerfil) throws Exception {
		String sql = "insert into usuarioPerfil (idUsuario, idPerfil, estatus) values (?, ?, 1)";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setInt(1, idUsuario);
			ps.setInt(2, idPerfil);
			ps.executeUpdate();
		}
	}

	private static String generarClaveDirectivo(Connection connection) throws Exception {
		String sql = "select coalesce(max(id), 0) + 1 as siguiente from directivos";
		try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
			rs.next();
			int consecutivo = rs.getInt("siguiente");
			return String.format("ITCH-DIR-%04d", consecutivo);
		}
	}

	private static String formatearResumen(String carrera, int idDirectivo, int idDocente, String username) {
		return "- " + carrera + " | directivo=" + idDirectivo + " | docente=" + idDocente + " | usuario=" + username;
	}

	private static class CarreraInfo {
		int id;
		String nombre;
	}

	private static class DocenteInfo {
		int id;
		String noEmpleado;
		String nombreCompleto;
		String correo;
	}

	private static class JefeInfo {
		int idDirectivo;
		int idDocente;
		String departamento;
	}
}
