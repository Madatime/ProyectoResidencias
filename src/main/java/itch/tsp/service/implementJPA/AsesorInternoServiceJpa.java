package itch.tsp.service.implementJPA;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import itch.tsp.model.AsesorInterno;
import itch.tsp.model.Docente;
import itch.tsp.model.Residencia;
import itch.tsp.repository.AsesorInternoRepository;
import itch.tsp.repository.DocenteRepository;
import itch.tsp.repository.ResidenciaRepository;
import itch.tsp.service.IAsesorInternoService;
import itch.tsp.service.IUsuarioService;

@Primary
@Service
public class AsesorInternoServiceJpa implements IAsesorInternoService {

	@Autowired
	private AsesorInternoRepository repoAsesorInterno;

	@Autowired
	private DocenteRepository repoDocente;

	@Autowired
	private ResidenciaRepository repoResidencia;

	@Autowired
	private IUsuarioService serviceUsuario;

	@Override
	public List<AsesorInterno> buscarTodosActivos() {
		return repoAsesorInterno.findByEstatusOrderByIdDesc(1);
	}

	@Override
	public List<AsesorInterno> buscarTodosInactivos() {
		return repoAsesorInterno.findByEstatusOrderByIdDesc(0);
	}

	@Override
	public List<AsesorInterno> buscarAsesoresInternos(String texto) {
		if (texto == null || texto.trim().isEmpty()) {
			return buscarTodosActivos();
		}

		String textoBusqueda = texto.trim();

		return repoAsesorInterno
				.findByEstatusAndDocente_NoEmpleadoContainingIgnoreCaseOrEstatusAndDocente_NombreContainingIgnoreCaseOrEstatusAndDocente_ApellidosContainingIgnoreCase(
						1, textoBusqueda,
						1, textoBusqueda,
						1, textoBusqueda);
	}

	@Override
	public List<AsesorInterno> buscarAsesoresInternosConProyecto() {
		List<Residencia> residencias = repoResidencia.findByEstatusAndAsesorInternoIsNotNullOrderByIdDesc(1);
		return extraerAsesoresUnicos(residencias);
	}

	@Override
	public List<AsesorInterno> buscarAsesoresInternosConProyectoPorPeriodo(String periodo) {
		if (periodo == null || periodo.trim().isEmpty()) {
			return buscarAsesoresInternosConProyecto();
		}

		List<Residencia> residencias = repoResidencia
				.findByPeriodoAndEstatusAndAsesorInternoIsNotNullOrderByIdDesc(periodo.trim(), 1);

		return extraerAsesoresUnicos(residencias);
	}

	@Override
	public List<AsesorInterno> buscarAsesoresInternosConProyectoPorPeriodoYTexto(String periodo, String texto) {
		boolean periodoVacio = periodo == null || periodo.trim().isEmpty();
		boolean textoVacio = texto == null || texto.trim().isEmpty();

		if (periodoVacio && textoVacio) {
			return buscarAsesoresInternosConProyecto();
		}

		if (!periodoVacio && textoVacio) {
			return buscarAsesoresInternosConProyectoPorPeriodo(periodo);
		}

		String textoBusqueda = texto.trim().toLowerCase();
		List<AsesorInterno> asesores = buscarAsesoresInternosConProyectoPorPeriodo(periodo);
		List<AsesorInterno> resultado = new ArrayList<>();

		for (AsesorInterno asesor : asesores) {
			boolean coincideNoEmpleado = asesor.getNoEmpleado() != null
					&& asesor.getNoEmpleado().toLowerCase().contains(textoBusqueda);

			boolean coincideNombre = asesor.getNombreCompleto() != null
					&& asesor.getNombreCompleto().toLowerCase().contains(textoBusqueda);

			boolean coincideProyecto = false;
			List<Residencia> proyectos = buscarProyectosAsignados(asesor.getId());

			for (Residencia residencia : proyectos) {
				if (residencia.getNombreProyecto() != null
						&& residencia.getNombreProyecto().toLowerCase().contains(textoBusqueda)) {
					coincideProyecto = true;
					break;
				}
			}

			if (coincideNoEmpleado || coincideNombre || coincideProyecto) {
				resultado.add(asesor);
			}
		}

		return resultado;
	}

	private List<AsesorInterno> extraerAsesoresUnicos(List<Residencia> residencias) {
		Map<Integer, AsesorInterno> mapa = new LinkedHashMap<>();

		if (residencias == null) {
			return new ArrayList<>();
		}

		for (Residencia residencia : residencias) {
			if (residencia != null
					&& residencia.getAsesorInterno() != null
					&& residencia.getAsesorInterno().getId() != null) {

				AsesorInterno asesor = residencia.getAsesorInterno();

				if (asesor.getEstatus() == null || asesor.getEstatus() == 1) {
					mapa.put(asesor.getId(), asesor);
				}
			}
		}

		return new ArrayList<>(mapa.values());
	}

	@Override
	public void guardarAsesorInterno(AsesorInterno asesorInterno) {

		boolean nuevoRegistro = asesorInterno != null && asesorInterno.getId() == null;

		prepararDatos(asesorInterno);

		repoAsesorInterno.save(asesorInterno);

		if (nuevoRegistro) {
			crearUsuarioAutomatico(asesorInterno);
		}
	}

	@Override
	public void guardarAsesorInternoConArchivos(AsesorInterno asesorInterno, MultipartFile foto,
			MultipartFile documento) {
		guardarAsesorInterno(asesorInterno);
	}

	private void prepararDatos(AsesorInterno asesorInterno) {
		if (asesorInterno == null) {
			throw new RuntimeException("No se recibió información del asesor interno.");
		}

		if (asesorInterno.getDocente() == null || asesorInterno.getDocente().getId() == null) {
			throw new RuntimeException("Debes seleccionar un docente.");
		}

		Docente docente = repoDocente.findByIdAndEstatus(asesorInterno.getDocente().getId(), 1);

		if (docente == null) {
			throw new RuntimeException("El docente seleccionado no existe o está inactivo.");
		}

		AsesorInterno asesorBD = null;

		if (asesorInterno.getId() != null) {
			asesorBD = repoAsesorInterno.findById(asesorInterno.getId()).orElse(null);
		}

		if (asesorInterno.getId() == null) {
			if (repoAsesorInterno.existsByDocenteAndEstatus(docente, 1)) {
				throw new RuntimeException("Este docente ya está registrado como asesor interno.");
			}

			asesorInterno.setClaveAsesor(generarClaveAsesor());
		} else if (asesorBD != null) {
			asesorInterno.setClaveAsesor(asesorBD.getClaveAsesor());
		}

		asesorInterno.setDocente(docente);

		if (asesorInterno.getEstatus() == null) {
			asesorInterno.setEstatus(asesorBD != null ? asesorBD.getEstatus() : 1);
		}
	}

	private void crearUsuarioAutomatico(AsesorInterno asesorInterno) {
		try {
			if (asesorInterno != null && asesorInterno.getDocente() != null && asesorInterno.getDocente().getId() != null) {
				serviceUsuario.crearUsuarioParaDocente(asesorInterno.getDocente().getId(), "ASESOR_INTERNO");
			}
		} catch (Exception e) {
			System.out.println("Error al crear usuario automático para asesor interno: " + e.getMessage());
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

		List<AsesorInterno> lista = repoAsesorInterno.findByDocente_NoEmpleadoAndEstatus(normalizado, 1);
		return lista != null && !lista.isEmpty();
	}

	@Override
	public boolean existeNoEmpleadoParaOtroRegistro(String noEmpleado, Integer id) {
		String normalizado = normalizarNoEmpleado(noEmpleado);

		if (normalizado == null || normalizado.isEmpty() || id == null) {
			return false;
		}

		List<AsesorInterno> lista = repoAsesorInterno.findByDocente_NoEmpleadoAndEstatusAndIdNot(normalizado, 1, id);
		return lista != null && !lista.isEmpty();
	}

	@Override
	public String normalizarNoEmpleado(String noEmpleado) {
		if (noEmpleado == null) {
			return null;
		}

		return noEmpleado.trim().toUpperCase();
	}

	@Override
	public void eliminar(Integer idAsesorInterno) {
		Optional<AsesorInterno> optional = repoAsesorInterno.findById(idAsesorInterno);

		if (optional.isPresent()) {
			AsesorInterno asesor = optional.get();
			asesor.setEstatus(0);
			repoAsesorInterno.save(asesor);
		}
	}

	@Override
	public void recuperar(Integer idAsesorInterno) {
		Optional<AsesorInterno> optional = repoAsesorInterno.findById(idAsesorInterno);

		if (optional.isPresent()) {
			AsesorInterno asesor = optional.get();
			asesor.setEstatus(1);
			repoAsesorInterno.save(asesor);
		}
	}

	@Override
	public List<Residencia> buscarProyectosAsignados(Integer idAsesorInterno) {
		if (idAsesorInterno == null) {
			return new ArrayList<>();
		}

		return repoResidencia.findByAsesorInterno_IdAndEstatusOrderByIdDesc(idAsesorInterno, 1);
	}
}
