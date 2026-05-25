package itch.tsp.service;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import itch.tsp.model.Estudiante;
import itch.tsp.model.Residente;
import itch.tsp.repository.EstudianteRepository;
import itch.tsp.repository.ResidenteRepository;

public class EstudianteServiceImp implements IEstudianteService {

	@Autowired
	private EstudianteRepository repoEstudiante;

	@Autowired
	private ResidenteRepository repoResidente;

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

		return repoEstudiante.buscarActivosPorTexto(texto.trim());
	}

	@Override
	public Estudiante buscarPorIdEstudiante(Integer idEstudiante) {
		return repoEstudiante.findByIdAndEstatus(idEstudiante, 1);
	}

	@Override
	public Estudiante buscarPorMatricula(String matricula) {
		String matriculaNormalizada = normalizarMatricula(matricula);
		return repoEstudiante.findByMatriculaAndEstatus(matriculaNormalizada, 1);
	}

	@Override
	public void guardarEstudiante(Estudiante estudiante) {
		if (estudiante.getMatricula() != null) {
			estudiante.setMatricula(normalizarMatricula(estudiante.getMatricula()));
		}

		if (estudiante.getNombre() != null) {
			estudiante.setNombre(estudiante.getNombre().trim());
		}

		if (estudiante.getApellidos() != null) {
			estudiante.setApellidos(estudiante.getApellidos().trim());
		}

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
			estudiante.setCorreo(estudiante.getCorreo().trim().toLowerCase());
		}

		if (estudiante.getEstatus() == null) {
			estudiante.setEstatus(1);
		}

		repoEstudiante.save(estudiante);
	}

	@Override
	public void eliminar(Integer idEstudiante) {
		Estudiante estudiante = repoEstudiante.findById(idEstudiante).orElse(null);

		if (estudiante != null) {
			estudiante.setEstatus(0);
			repoEstudiante.save(estudiante);
		}
	}

	@Override
	public void recuperar(Integer idEstudiante) {
		Estudiante estudiante = repoEstudiante.findById(idEstudiante).orElse(null);

		if (estudiante != null) {
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

	@Override
	public boolean existeMatricula(String matricula) {
		return buscarPorMatricula(matricula) != null;
	}

	@Override
	public boolean existeMatriculaParaOtroRegistro(String matricula, Integer id) {
		String matriculaNormalizada = normalizarMatricula(matricula);

		List<Estudiante> lista = repoEstudiante.findByMatriculaAndEstatusAndIdNot(matriculaNormalizada, 1, id);

		return lista != null && !lista.isEmpty();
	}

	@Override
	public List<Estudiante> buscarEstudiantesParaResidencia(String texto, String filtroResidencia) {
		List<Estudiante> base = buscarEstudiantes(texto);
		List<Estudiante> resultado = new LinkedList<>();

		boolean filtrarConResidencia = filtroResidencia != null && filtroResidencia.equalsIgnoreCase("con");
		boolean filtrarSinResidencia = filtroResidencia != null && filtroResidencia.equalsIgnoreCase("sin");

		for (Estudiante estudiante : base) {
			boolean tieneResidencia = false;

			Residente residente = repoResidente.findByEstudiante_IdAndEstatus(estudiante.getId(), 1);

			if (residente != null) {
				tieneResidencia = true;
			}

			if (!filtrarConResidencia && !filtrarSinResidencia) {
				resultado.add(estudiante);
			} else if (filtrarConResidencia && tieneResidencia) {
				resultado.add(estudiante);
			} else if (filtrarSinResidencia && !tieneResidencia) {
				resultado.add(estudiante);
			}
		}

		return resultado;
	}
}