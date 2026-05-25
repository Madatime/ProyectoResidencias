package itch.tsp.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import itch.tsp.model.Residencia;
import itch.tsp.model.Carrera;
import itch.tsp.repository.ResidenciaRepository;

@Service("residenciaServiceMemoria")
public class ResidenciaServiceImp implements IResidenciaService {

	@Autowired
	private ResidenciaRepository residenciaRepository;

	@Override
	public List<Residencia> buscarTodasActivas() {
		List<Residencia> lista = residenciaRepository.findByEstatusOrderByIdDesc(1);
		ordenarPorIdProyectoCarreraDesc(lista);
		return lista;
	}

	@Override
	public List<Residencia> buscarTodasInactivas() {
		List<Residencia> lista = residenciaRepository.findByEstatusOrderByIdDesc(0);
		ordenarPorIdProyectoCarreraDesc(lista);
		return lista;
	}

	@Override
	public List<Residencia> buscarResidenciasPorPeriodo(String periodo) {
		if (periodo == null || periodo.trim().isEmpty()) {
			return buscarTodasActivas();
		}

		List<Residencia> lista = residenciaRepository.findByPeriodoAndEstatusOrderByIdDesc(periodo.trim(), 1);
		ordenarPorIdProyectoCarreraDesc(lista);
		return lista;
	}

	@Override
	public List<Residencia> buscarResidenciasPorTexto(String texto) {
		if (texto == null || texto.trim().isEmpty()) {
			return buscarTodasActivas();
		}

		String busqueda = texto.trim();

		List<Residencia> lista = buscarTodasActivas().stream()
				.filter(residencia -> coincideTextoBusqueda(residencia, busqueda))
				.toList();

		ordenarPorIdProyectoCarreraDesc(lista);
		return lista;
	}

	@Override
	public List<Residencia> buscarResidenciasPorPeriodoYTexto(String periodo, String texto) {
		boolean periodoVacio = periodo == null || periodo.trim().isEmpty();
		boolean textoVacio = texto == null || texto.trim().isEmpty();

		if (periodoVacio && textoVacio) {
			return buscarTodasActivas();
		}

		if (!periodoVacio && textoVacio) {
			return buscarResidenciasPorPeriodo(periodo);
		}

		if (periodoVacio) {
			return buscarResidenciasPorTexto(texto);
		}

		String periodoBusqueda = periodo.trim();
		String textoBusqueda = texto.trim();

		List<Residencia> lista = buscarResidenciasPorPeriodo(periodoBusqueda).stream()
				.filter(residencia -> coincideTextoBusqueda(residencia, textoBusqueda))
				.toList();

		ordenarPorIdProyectoCarreraDesc(lista);
		return lista;
	}

	private boolean coincideTextoBusqueda(Residencia residencia, String textoBusqueda) {
		String filtro = normalizarTexto(textoBusqueda);

		return contiene(residencia != null ? residencia.getNombreProyecto() : null, filtro)
				|| contiene(residencia != null && residencia.getEmpresa() != null ? residencia.getEmpresa().getNombre() : null, filtro)
				|| contiene(
						residencia != null
								&& residencia.getResidente() != null
								&& residencia.getResidente().getEstudiante() != null
										? residencia.getResidente().getEstudiante().getMatricula()
										: null,
						filtro)
				|| contiene(
						residencia != null
								&& residencia.getResidente() != null
								&& residencia.getResidente().getEstudiante() != null
										? residencia.getResidente().getEstudiante().getNombre()
										: null,
						filtro)
				|| contiene(
						residencia != null
								&& residencia.getResidente() != null
								&& residencia.getResidente().getEstudiante() != null
										? residencia.getResidente().getEstudiante().getApellidos()
										: null,
						filtro)
				|| contiene(
						residencia != null
								&& residencia.getAsesorInterno() != null
								&& residencia.getAsesorInterno().getDocente() != null
										? residencia.getAsesorInterno().getDocente().getNombre()
										: null,
						filtro)
				|| contiene(
						residencia != null
								&& residencia.getAsesorInterno() != null
								&& residencia.getAsesorInterno().getDocente() != null
										? residencia.getAsesorInterno().getDocente().getApellidos()
										: null,
						filtro)
				|| contiene(
						residencia != null
								&& residencia.getAsesorExterno() != null
										? residencia.getAsesorExterno().getNombre()
										: null,
						filtro)
				|| contiene(
						residencia != null
								&& residencia.getAsesorExterno() != null
										? residencia.getAsesorExterno().getApellidos()
										: null,
						filtro);
	}

	private boolean contiene(String valor, String filtroNormalizado) {
		return normalizarTexto(valor).contains(filtroNormalizado);
	}

	private String normalizarTexto(String valor) {
		return valor == null ? "" : valor.trim().toUpperCase();
	}

	@Override
	public List<Residencia> buscarAlumnosConProyecto(String texto) {
		if (texto == null || texto.trim().isEmpty()) {
			return buscarTodasActivas();
		}

		String busqueda = texto.trim();

		List<Residencia> lista = residenciaRepository
				.findByEstatusAndResidente_Estudiante_MatriculaContainingIgnoreCaseOrEstatusAndResidente_Estudiante_NombreContainingIgnoreCaseOrEstatusAndResidente_Estudiante_ApellidosContainingIgnoreCase(
						1, busqueda,
						1, busqueda,
						1, busqueda);

		ordenarPorIdProyectoCarreraDesc(lista);
		return lista;
	}

	@Override
	public List<Residencia> buscarAsesoresInternosPorPeriodo(String periodo) {
		List<Residencia> lista;

		if (periodo == null || periodo.trim().isEmpty()) {
			lista = residenciaRepository.findByEstatusAndAsesorInternoIsNotNullOrderByIdDesc(1);
		} else {
			lista = residenciaRepository.findByPeriodoAndEstatusAndAsesorInternoIsNotNullOrderByIdDesc(periodo.trim(), 1);
		}

		ordenarPorIdProyectoCarreraDesc(lista);
		return lista;
	}

	@Override
	public List<Residencia> buscarProyectosPorPeriodo(String periodo) {
		return buscarResidenciasPorPeriodo(periodo);
	}

	@Override
	public Residencia buscarPorIdResidencia(Integer idResidencia) {
		return residenciaRepository.findByIdAndEstatus(idResidencia, 1);
	}

	@Override
	public void guardarResidencia(Residencia residencia) {
		boolean esNueva = residencia.getId() == null || residencia.getId() == 0;

		if (residencia.getEstatus() == null) {
			residencia.setEstatus(1);
		}

		if (residencia.getEstatusProceso() == null || residencia.getEstatusProceso().trim().isEmpty()) {
			residencia.setEstatusProceso("EN_PROCESO");
		}

		if (esNueva) {
			asignarIdProyectoCarrera(residencia);
			asignarEstadoAutorizacionInicial(residencia);
			asignarCarreraJefeArea(residencia);
		} else {
			Residencia existente = residenciaRepository.findById(residencia.getId()).orElse(null);

			if (existente != null) {
				if (residencia.getIdProyectoCarrera() == null || residencia.getIdProyectoCarrera().trim().isEmpty()) {
					residencia.setIdProyectoCarrera(existente.getIdProyectoCarrera());
				}

				if (residencia.getEstadoAutorizacion() == null || residencia.getEstadoAutorizacion().trim().isEmpty()) {
					residencia.setEstadoAutorizacion(existente.getEstadoAutorizacion());
				}

				if (residencia.getFechaAutorizacion() == null) {
					residencia.setFechaAutorizacion(existente.getFechaAutorizacion());
				}

				if (residencia.getCarreraJefeArea() == null || residencia.getCarreraJefeArea().trim().isEmpty()) {
					residencia.setCarreraJefeArea(existente.getCarreraJefeArea());
				}

				if (residencia.getOrigenProyecto() == null || residencia.getOrigenProyecto().trim().isEmpty()) {
					residencia.setOrigenProyecto(existente.getOrigenProyecto());
				}

				if (residencia.getObservacionesAutorizacion() == null) {
					residencia.setObservacionesAutorizacion(existente.getObservacionesAutorizacion());
				}
			}
		}

		residenciaRepository.save(residencia);
	}

	@Override
	public void dictaminarProyecto(Integer idResidencia, String estadoAutorizacion, String observacionesAutorizacion) {
		Residencia residencia = residenciaRepository.findByIdAndEstatus(idResidencia, 1);

		if (residencia == null) {
			throw new RuntimeException("No se encontró la residencia.");
		}

		if (estadoAutorizacion == null || estadoAutorizacion.trim().isEmpty()) {
			throw new RuntimeException("Debes seleccionar un estado de autorización.");
		}

		String estado = estadoAutorizacion.trim().toUpperCase();

		if (!estado.equals("AUTORIZADO")
				&& !estado.equals("AUTORIZADO_CON_OBSERVACIONES")
				&& !estado.equals("RECHAZADO")) {
			throw new RuntimeException("Estado de autorización no válido.");
		}

		residencia.setEstadoAutorizacion(estado);
		residencia.setObservacionesAutorizacion(observacionesAutorizacion);
		residencia.setFechaAutorizacion(LocalDate.now());

		residenciaRepository.save(residencia);
	}

	@Override
	public void cerrarExpediente(Integer idResidencia) {
		Residencia residencia = residenciaRepository.findByIdAndEstatus(idResidencia, 1);

		if (residencia != null) {
			residencia.setEstatusProceso("CERRADO");
			residencia.setFechaCierre(LocalDate.now());
			residenciaRepository.save(residencia);
		}
	}

	@Override
	public void reabrirExpediente(Integer idResidencia) {
		Residencia residencia = residenciaRepository.findByIdAndEstatus(idResidencia, 1);

		if (residencia != null) {
			residencia.setEstatusProceso("EN_PROCESO");
			residencia.setFechaCierre(null);
			residenciaRepository.save(residencia);
		}
	}

	@Override
	public void eliminar(Integer idResidencia) {
		Residencia residencia = residenciaRepository.findById(idResidencia).orElse(null);

		if (residencia != null) {
			residencia.setEstatus(0);
			residenciaRepository.save(residencia);
		}
	}

	@Override
	public void recuperar(Integer idResidencia) {
		Residencia residencia = residenciaRepository.findById(idResidencia).orElse(null);

		if (residencia != null) {
			residencia.setEstatus(1);
			residenciaRepository.save(residencia);
		}
	}

	private void asignarIdProyectoCarrera(Residencia residencia) {
		String periodo = residencia.getPeriodo();
		String carrera = obtenerCarreraResidencia(residencia);

		if (periodo == null || periodo.trim().isEmpty()) {
			return;
		}

		if (carrera == null || carrera.trim().isEmpty()) {
			return;
		}

		long total = residenciaRepository.countByPeriodoAndResidente_Estudiante_CarreraAndEstatus(
				periodo.trim(),
				carrera.trim(),
				1);

		residencia.setIdProyectoCarrera(String.valueOf(total + 1));
	}

	private void asignarEstadoAutorizacionInicial(Residencia residencia) {
		if (residencia.getEstadoAutorizacion() != null && !residencia.getEstadoAutorizacion().trim().isEmpty()) {
			return;
		}

		if ("BANCO_PROYECTOS".equalsIgnoreCase(residencia.getOrigenProyecto())) {
			residencia.setEstadoAutorizacion("AUTORIZADO");
			residencia.setFechaAutorizacion(LocalDate.now());
		} else {
			residencia.setEstadoAutorizacion("PENDIENTE");
			residencia.setFechaAutorizacion(null);
		}
	}

	private void asignarCarreraJefeArea(Residencia residencia) {
		String carrera = obtenerCarreraResidencia(residencia);

		if (carrera != null && !carrera.trim().isEmpty()) {
			residencia.setCarreraJefeArea(carrera.trim());
		}
	}

	private String obtenerCarreraResidencia(Residencia residencia) {

		if (residencia == null
				|| residencia.getResidente() == null
				|| residencia.getResidente().getEstudiante() == null
				|| residencia.getResidente().getEstudiante().getCarrera() == null) {
			return null;
		}

		Carrera carrera = residencia.getResidente()
				.getEstudiante()
				.getCarrera();

		if (carrera.getNombre() == null
				|| carrera.getNombre().trim().isEmpty()) {
			return null;
		}

		return carrera.getNombre().trim();
	}

	private void ordenarPorIdProyectoCarreraDesc(List<Residencia> lista) {
		lista.sort(
				Comparator.comparingInt(this::obtenerNumeroIdProyectoCarrera)
						.reversed());
	}

	private int obtenerNumeroIdProyectoCarrera(Residencia residencia) {
		if (residencia == null || residencia.getIdProyectoCarrera() == null) {
			return 0;
		}

		try {
			return Integer.parseInt(residencia.getIdProyectoCarrera().trim());
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
