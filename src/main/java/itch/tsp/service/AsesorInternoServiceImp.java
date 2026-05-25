package itch.tsp.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import itch.tsp.model.AsesorInterno;
import itch.tsp.model.Docente;
import itch.tsp.model.Residencia;
import itch.tsp.repository.AsesorInternoRepository;
import itch.tsp.repository.DocenteRepository;
import itch.tsp.repository.ResidenciaRepository;

@Service
public class AsesorInternoServiceImp implements IAsesorInternoService {

	@Autowired
	private AsesorInternoRepository repoAsesorInterno;

	@Autowired
	private DocenteRepository repoDocente;

	@Autowired
	private ResidenciaRepository repoResidencia;

	@Override
	public List<AsesorInterno> buscarTodosActivos() {
		return repoAsesorInterno.findByEstatusOrderByIdAsc(1);
	}

	@Override
	public List<AsesorInterno> buscarTodosInactivos() {
		return repoAsesorInterno.findByEstatusOrderByIdAsc(0);
	}

	@Override
	public List<AsesorInterno> buscarAsesoresInternos(String texto) {
		if (texto == null || texto.trim().isEmpty()) {
			return buscarTodosActivos();
		}

		String busqueda = texto.trim();

		return repoAsesorInterno
				.findByEstatusAndDocente_NoEmpleadoContainingIgnoreCaseOrEstatusAndDocente_NombreContainingIgnoreCaseOrEstatusAndDocente_ApellidosContainingIgnoreCase(
						1, busqueda,
						1, busqueda,
						1, busqueda);
	}

	@Override
	public List<AsesorInterno> buscarAsesoresInternosConProyecto() {
		List<AsesorInterno> asesores = buscarTodosActivos();
		List<AsesorInterno> resultado = new ArrayList<>();

		for (AsesorInterno asesor : asesores) {
			List<Residencia> proyectos = buscarProyectosAsignados(asesor.getId());

			if (proyectos != null && !proyectos.isEmpty()) {
				resultado.add(asesor);
			}
		}

		return resultado;
	}

	@Override
	public List<AsesorInterno> buscarAsesoresInternosConProyectoPorPeriodo(String periodo) {
		List<AsesorInterno> asesores = buscarTodosActivos();
		List<AsesorInterno> resultado = new ArrayList<>();

		for (AsesorInterno asesor : asesores) {
			List<Residencia> proyectos = buscarProyectosAsignados(asesor.getId());

			for (Residencia residencia : proyectos) {
				if (periodo == null || periodo.trim().isEmpty()
						|| (residencia.getPeriodo() != null && residencia.getPeriodo().equalsIgnoreCase(periodo.trim()))) {
					resultado.add(asesor);
					break;
				}
			}
		}

		return resultado;
	}

	@Override
	public List<AsesorInterno> buscarAsesoresInternosConProyectoPorPeriodoYTexto(String periodo, String texto) {
		List<AsesorInterno> asesores = buscarAsesoresInternosConProyectoPorPeriodo(periodo);

		if (texto == null || texto.trim().isEmpty()) {
			return asesores;
		}

		String busqueda = texto.trim().toLowerCase();
		List<AsesorInterno> resultado = new ArrayList<>();

		for (AsesorInterno asesor : asesores) {
			boolean coincideNoEmpleado = asesor.getNoEmpleado() != null
					&& asesor.getNoEmpleado().toLowerCase().contains(busqueda);

			boolean coincideNombre = asesor.getNombreCompleto() != null
					&& asesor.getNombreCompleto().toLowerCase().contains(busqueda);

			if (coincideNoEmpleado || coincideNombre) {
				resultado.add(asesor);
			}
		}

		return resultado;
	}

	@Override
	public List<Residencia> buscarProyectosAsignados(Integer idAsesorInterno) {
		AsesorInterno asesor = buscarPorIdAsesorInterno(idAsesorInterno);

		if (asesor == null) {
			return new ArrayList<>();
		}

		return repoResidencia.findByAsesorInternoAndEstatusOrderByIdDesc(asesor, 1);
	}

	@Override
	public void guardarAsesorInterno(AsesorInterno asesorInterno) {
		if (asesorInterno == null || asesorInterno.getDocente() == null || asesorInterno.getDocente().getId() == null) {
			throw new RuntimeException("Debes seleccionar un docente.");
		}

		Docente docente = repoDocente.findByIdAndEstatus(asesorInterno.getDocente().getId(), 1);

		if (docente == null) {
			throw new RuntimeException("El docente seleccionado no existe o está inactivo.");
		}

		AsesorInterno asesorGuardar;

		if (asesorInterno.getId() != null) {
			asesorGuardar = repoAsesorInterno.findById(asesorInterno.getId()).orElse(new AsesorInterno());

			if (asesorGuardar.getClaveAsesor() == null || asesorGuardar.getClaveAsesor().trim().isEmpty()) {
				asesorGuardar.setClaveAsesor(generarClaveAsesor());
			}

		} else {
			if (repoAsesorInterno.existsByDocenteAndEstatus(docente, 1)) {
				throw new RuntimeException("Este docente ya está registrado como asesor interno.");
			}

			asesorGuardar = new AsesorInterno();
			asesorGuardar.setClaveAsesor(generarClaveAsesor());
		}

		asesorGuardar.setDocente(docente);

		if (asesorGuardar.getEstatus() == null) {
			asesorGuardar.setEstatus(1);
		}

		repoAsesorInterno.save(asesorGuardar);
	}

	@Override
	public void guardarAsesorInternoConArchivos(AsesorInterno asesorInterno, MultipartFile foto, MultipartFile documento) {
		guardarAsesorInterno(asesorInterno);
	}

	@Override
	public AsesorInterno buscarPorIdAsesorInterno(Integer idAsesorInterno) {
		return repoAsesorInterno.findByIdAndEstatus(idAsesorInterno, 1);
	}

	@Override
	public AsesorInterno buscarPorIdDocente(Integer idDocente) {
		if (idDocente == null) {
			return null;
		}

		return repoAsesorInterno.findByDocente_IdAndEstatus(idDocente, 1);
	}

	@Override
	public boolean existeNoEmpleado(String noEmpleado) {
		String normalizado = normalizarNoEmpleado(noEmpleado);

		if (normalizado == null || normalizado.isEmpty()) {
			return false;
		}

		return !repoAsesorInterno.findByDocente_NoEmpleadoAndEstatus(normalizado, 1).isEmpty();
	}

	@Override
	public boolean existeNoEmpleadoParaOtroRegistro(String noEmpleado, Integer id) {
		String normalizado = normalizarNoEmpleado(noEmpleado);

		if (normalizado == null || normalizado.isEmpty() || id == null) {
			return false;
		}

		return !repoAsesorInterno.findByDocente_NoEmpleadoAndEstatusAndIdNot(normalizado, 1, id).isEmpty();
	}

	@Override
	public String normalizarNoEmpleado(String noEmpleado) {
		if (noEmpleado == null) {
			return "";
		}

		return noEmpleado.trim().toUpperCase();
	}

	@Override
	public void eliminar(Integer idAsesorInterno) {
		AsesorInterno asesor = repoAsesorInterno.findById(idAsesorInterno).orElse(null);

		if (asesor != null) {
			asesor.setEstatus(0);
			repoAsesorInterno.save(asesor);
		}
	}

	@Override
	public void recuperar(Integer idAsesorInterno) {
		AsesorInterno asesor = repoAsesorInterno.findById(idAsesorInterno).orElse(null);

		if (asesor != null) {
			asesor.setEstatus(1);
			repoAsesorInterno.save(asesor);
		}
	}

	private String generarClaveAsesor() {
		List<AsesorInterno> asesores = repoAsesorInterno.findAll();

		int consecutivo = asesores.size() + 1;
		String clave;

		do {
			clave = String.format("ITCH-AI-%04d", consecutivo);
			consecutivo++;
		} while (repoAsesorInterno.existsByClaveAsesor(clave));

		return clave;
	}
}
