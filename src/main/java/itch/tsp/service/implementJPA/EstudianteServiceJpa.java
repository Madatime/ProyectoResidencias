package itch.tsp.service.implementJPA;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import itch.tsp.model.Estudiante;
import itch.tsp.model.Residencia;
import itch.tsp.model.Residente;
import itch.tsp.repository.EstudianteRepository;
import itch.tsp.repository.ResidenciaRepository;
import itch.tsp.repository.ResidenteRepository;
import itch.tsp.service.IEstudianteService;

@Primary
@Service
public class EstudianteServiceJpa implements IEstudianteService {

	@Autowired
	private EstudianteRepository repoEstudiante;

	@Autowired
	private ResidenteRepository repoResidente;

	@Autowired
	private ResidenciaRepository repoResidencia;

	@Override
	public List<Estudiante> buscarTodosActivos() {
		return repoEstudiante.findByEstatusOrderByIdDesc(1);
	}

	@Override
	public List<Estudiante> buscarTodosInactivos() {
		return repoEstudiante.findByEstatusOrderByIdDesc(0);
	}

	@Override
	public List<Estudiante> buscarEstudiantes(String texto) {
		if (texto == null || texto.trim().isEmpty()) {
			return buscarTodosActivos();
		}

		String textoBusqueda = texto.trim();

		return repoEstudiante
				.findByEstatusAndMatriculaContainingIgnoreCaseOrEstatusAndNombreContainingIgnoreCaseOrEstatusAndApellidosContainingIgnoreCase(
						1, textoBusqueda,
						1, textoBusqueda,
						1, textoBusqueda);
	}

	@Override
	public Estudiante buscarPorIdEstudiante(Integer idEstudiante) {
		if (idEstudiante == null) {
			return null;
		}

		return repoEstudiante.findByIdAndEstatus(idEstudiante, 1);
	}

	@Override
	public Estudiante buscarPorMatricula(String matricula) {
		String matriculaNormalizada = normalizarMatricula(matricula);

		if (matriculaNormalizada == null || matriculaNormalizada.isEmpty()) {
			return null;
		}

		return repoEstudiante.findByMatriculaAndEstatus(matriculaNormalizada, 1);
	}

	@Override
	public void guardarEstudiante(Estudiante estudiante) {
		if (estudiante == null) {
			throw new RuntimeException("No se recibio informacion del estudiante.");
		}

		normalizarDatos(estudiante);
		validarDuplicados(estudiante);

		if (estudiante.getEstatus() == null) {
			estudiante.setEstatus(1);
		}

		repoEstudiante.save(estudiante);
	}

	private void normalizarDatos(Estudiante estudiante) {
		estudiante.setMatricula(normalizarMatricula(estudiante.getMatricula()));
		estudiante.setNombre(normalizarTexto(estudiante.getNombre()));
		estudiante.setApellidos(normalizarTexto(estudiante.getApellidos()));

		if (estudiante.getSexo() != null) {
			estudiante.setSexo(estudiante.getSexo().trim().toUpperCase());
		}

		if (estudiante.getSemestre() != null) {
			estudiante.setSemestre(estudiante.getSemestre().trim());
		}

		if (estudiante.getTelefono() != null) {
			estudiante.setTelefono(estudiante.getTelefono().trim());
		}

		if (estudiante.getCorreo() != null) {
			estudiante.setCorreo(estudiante.getCorreo().trim());
		}
	}

	private void validarDuplicados(Estudiante estudiante) {
		if (estudiante.getMatricula() == null || estudiante.getMatricula().isEmpty()) {
			throw new RuntimeException("La matricula es obligatoria.");
		}

		validarMatriculaPermitida(estudiante.getMatricula());

		if (estudiante.getNombre() == null || estudiante.getNombre().isEmpty()) {
			throw new RuntimeException("El nombre es obligatorio.");
		}

		if (estudiante.getApellidos() == null || estudiante.getApellidos().isEmpty()) {
			throw new RuntimeException("Los apellidos son obligatorios.");
		}

		if (estudiante.getCarrera() == null || estudiante.getCarrera().getId() == null) {
			throw new RuntimeException("Debes seleccionar una carrera.");
		}

		if (estudiante.getSexo() == null || estudiante.getSexo().trim().isEmpty()) {
			throw new RuntimeException("Debes seleccionar el sexo del estudiante.");
		}

		if (estudiante.getCorreo() == null || estudiante.getCorreo().trim().isEmpty()) {
			throw new RuntimeException("El correo electronico es obligatorio.");
		}

		validarSemestrePermitido(estudiante.getSemestre());
		validarTelefonoPermitido(estudiante.getTelefono());

		Integer id = estudiante.getId();

		if (existeMatriculaParaOtroRegistro(estudiante.getMatricula(), id)) {
			throw new RuntimeException("Ya existe un estudiante registrado con esa matricula.");
		}

		boolean nombreDuplicado;

		if (id == null) {
			nombreDuplicado = repoEstudiante.existsByNombreIgnoreCaseAndApellidosIgnoreCaseAndEstatus(
					estudiante.getNombre(),
					estudiante.getApellidos(),
					1);
		} else {
			nombreDuplicado = repoEstudiante.existsByNombreIgnoreCaseAndApellidosIgnoreCaseAndEstatusAndIdNot(
					estudiante.getNombre(),
					estudiante.getApellidos(),
					1,
					id);
		}

		if (nombreDuplicado) {
			throw new RuntimeException("Ya existe un estudiante registrado con ese nombre y apellidos.");
		}
	}

	private void validarMatriculaPermitida(String matricula) {
		if (matricula == null || matricula.isBlank()) {
			return;
		}

		String valor = normalizarMatricula(matricula);

		if (!valor.matches("^C?\\d{8}$")) {
			throw new RuntimeException("La matricula debe tener 8 digitos o iniciar con C seguida de 8 digitos.");
		}
	}

	private void validarSemestrePermitido(String semestre) {
		if (semestre == null || semestre.trim().isEmpty()) {
			throw new RuntimeException("El semestre es obligatorio.");
		}

		int semestreNumerico;

		try {
			semestreNumerico = Integer.parseInt(semestre.trim());
		} catch (NumberFormatException e) {
			throw new RuntimeException("El semestre debe ser un numero entero.");
		}

		if (semestreNumerico < 8) {
			throw new RuntimeException("El semestre minimo permitido es 8.");
		}

		if (semestreNumerico > 13) {
			throw new RuntimeException("El semestre maximo permitido es 13.");
		}
	}

	private void validarTelefonoPermitido(String telefono) {
		if (telefono == null || telefono.trim().isEmpty()) {
			throw new RuntimeException("El telefono es obligatorio.");
		}

		String valor = telefono.trim();

		if (!valor.matches("^\\d{10}$")) {
			throw new RuntimeException("El telefono debe contener exactamente 10 digitos.");
		}
	}

	@Override
	public void eliminar(Integer idEstudiante) {
		if (idEstudiante == null) {
			return;
		}

		Estudiante estudiante = repoEstudiante.findById(idEstudiante).orElse(null);

		if (estudiante != null) {
			Residente residenteActivo = repoResidente.findByEstudiante_IdAndEstatus(idEstudiante, 1);

			if (residenteActivo != null) {
				throw new RuntimeException("No puedes eliminar un estudiante que ya esta registrado como residente activo.");
			}

			estudiante.setEstatus(0);
			repoEstudiante.save(estudiante);
		}
	}

	@Override
	public void recuperar(Integer idEstudiante) {
		if (idEstudiante == null) {
			return;
		}

		Estudiante estudiante = repoEstudiante.findById(idEstudiante).orElse(null);

		if (estudiante != null) {
			normalizarDatos(estudiante);
			validarDuplicados(estudiante);

			estudiante.setEstatus(1);
			repoEstudiante.save(estudiante);
		}
	}

	@Override
	public String normalizarMatricula(String matricula) {
		if (matricula == null) {
			return null;
		}

		return matricula.trim().toUpperCase();
	}

	private String normalizarTexto(String texto) {
		if (texto == null) {
			return null;
		}

		return texto.trim().replaceAll("\\s+", " ");
	}

	@Override
	public boolean existeMatricula(String matricula) {
		Estudiante estudiante = buscarPorMatricula(matricula);
		return estudiante != null;
	}

	@Override
	public boolean existeMatriculaParaOtroRegistro(String matricula, Integer id) {
		String matriculaNormalizada = normalizarMatricula(matricula);

		if (matriculaNormalizada == null || matriculaNormalizada.isEmpty()) {
			return false;
		}

		if (id == null) {
			return existeMatricula(matriculaNormalizada);
		}

		List<Estudiante> lista = repoEstudiante.findByMatriculaAndEstatusAndIdNot(
				matriculaNormalizada,
				1,
				id);

		return lista != null && !lista.isEmpty();
	}

	@Override
	public List<Estudiante> buscarEstudiantesParaResidencia(String texto, String filtroProyectoResidencia) {
		List<Estudiante> estudiantes = buscarEstudiantes(texto);
		List<Estudiante> resultado = new ArrayList<>();

		boolean filtrarCon = "con".equalsIgnoreCase(filtroProyectoResidencia);
		boolean filtrarSin = "sin".equalsIgnoreCase(filtroProyectoResidencia);

		if (!filtrarCon && !filtrarSin) {
			return estudiantes;
		}

		for (Estudiante estudiante : estudiantes) {
			Residente residente = repoResidente.findByEstudiante_IdAndEstatus(estudiante.getId(), 1);
			boolean tieneProyectoActivo = false;

			if (residente != null) {
				List<Residencia> residencias = repoResidencia.findByResidenteAndEstatusOrderByIdDesc(residente, 1);
				tieneProyectoActivo = residencias != null && !residencias.isEmpty();
			}

			if (filtrarCon && tieneProyectoActivo) {
				resultado.add(estudiante);
			}

			if (filtrarSin && !tieneProyectoActivo) {
				resultado.add(estudiante);
			}
		}

		return resultado;
	}
}
