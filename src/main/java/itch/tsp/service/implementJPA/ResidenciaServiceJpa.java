package itch.tsp.service.implementJPA;

import java.lang.reflect.Method;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import itch.tsp.model.AsesorExterno;
import itch.tsp.model.AsesorInterno;
import itch.tsp.model.Empresa;
import itch.tsp.model.Residencia;
import itch.tsp.model.Residente;
import itch.tsp.service.CalendarioResidenciaReglaService;
import itch.tsp.repository.AsesorExternoRepository;
import itch.tsp.repository.AsesorInternoRepository;
import itch.tsp.repository.EmpresaRepository;
import itch.tsp.repository.ResidenciaRepository;
import itch.tsp.repository.ResidenteRepository;
import itch.tsp.service.IResidenciaService;

@Primary
@Service
public class ResidenciaServiceJpa implements IResidenciaService {

	@Autowired
	private ResidenciaRepository repoResidencia;

	@Autowired
	private ResidenteRepository repoResidente;

	@Autowired
	private AsesorInternoRepository repoAsesorInterno;

	@Autowired
	private AsesorExternoRepository repoAsesorExterno;

	@Autowired
	private EmpresaRepository repoEmpresa;

	@Autowired
	private CalendarioResidenciaReglaService calendarioResidenciaReglaService;

	@Override
	public List<Residencia> buscarTodasActivas() {
		List<Residencia> lista = repoResidencia.findByEstatusOrderByIdDesc(1);
		ordenarPorIdProyectoCarreraDesc(lista);
		return lista;
	}

	@Override
	public List<Residencia> buscarTodasInactivas() {
		List<Residencia> lista = repoResidencia.findByEstatusOrderByIdDesc(0);
		ordenarPorIdProyectoCarreraDesc(lista);
		return lista;
	}

	@Override
	public List<Residencia> buscarResidenciasPorPeriodo(String periodo) {
		if (periodo == null || periodo.trim().isEmpty()) {
			return buscarTodasActivas();
		}

		List<Residencia> lista = repoResidencia.findByPeriodoAndEstatusOrderByIdDesc(periodo.trim(), 1);
		ordenarPorIdProyectoCarreraDesc(lista);
		return lista;
	}

	@Override
	public List<Residencia> buscarResidenciasPorTexto(String texto) {
		if (texto == null || texto.trim().isEmpty()) {
			return buscarTodasActivas();
		}

		String textoBusqueda = texto.trim();

		List<Residencia> lista = buscarTodasActivas().stream()
				.filter(residencia -> coincideTextoBusqueda(residencia, textoBusqueda))
				.collect(Collectors.toList());

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

		String textoBusqueda = texto.trim();

		List<Residencia> lista = buscarResidenciasPorPeriodo(periodo).stream()
				.filter(residencia -> coincideTextoBusqueda(residencia, textoBusqueda))
				.collect(Collectors.toList());

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

		String textoBusqueda = texto.trim();

		List<Residencia> lista = repoResidencia
				.findByEstatusAndResidente_Estudiante_MatriculaContainingIgnoreCaseOrEstatusAndResidente_Estudiante_NombreContainingIgnoreCaseOrEstatusAndResidente_Estudiante_ApellidosContainingIgnoreCase(
						1, textoBusqueda,
						1, textoBusqueda,
						1, textoBusqueda);

		ordenarPorIdProyectoCarreraDesc(lista);
		return lista;
	}

	@Override
	public List<Residencia> buscarAsesoresInternosPorPeriodo(String periodo) {
		List<Residencia> lista;

		if (periodo == null || periodo.trim().isEmpty()) {
			lista = repoResidencia.findByEstatusAndAsesorInternoIsNotNullOrderByIdDesc(1);
		} else {
			lista = repoResidencia.findByPeriodoAndEstatusAndAsesorInternoIsNotNullOrderByIdDesc(periodo.trim(), 1);
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
		if (idResidencia == null) {
			return null;
		}

		return repoResidencia.findByIdAndEstatus(idResidencia, 1);
	}

	@Override
	public void guardarResidencia(Residencia residencia) {

		if (residencia == null) {
			throw new RuntimeException("No se recibió información de la residencia.");
		}

		boolean esNueva = residencia.getId() == null || residencia.getId() == 0;

		normalizarDatos(residencia);

		cargarRelacionesDesdeBD(residencia);
		calendarioResidenciaReglaService.validarCalendarioResidencia(residencia);

		if (residencia.getEstatus() == null) {
			residencia.setEstatus(1);
		}

		if (residencia.getEstatusProceso() == null || residencia.getEstatusProceso().trim().isEmpty()) {
			residencia.setEstatusProceso("EN_PROCESO");
		}

		if (residencia.getOrigenProyecto() == null || residencia.getOrigenProyecto().trim().isEmpty()) {
			residencia.setOrigenProyecto("PROYECTO");
		}

		if (esNueva) {

			if (residencia.getIdProyectoCarrera() == null || residencia.getIdProyectoCarrera().trim().isEmpty()) {
				asignarIdProyectoCarrera(residencia);
			}

			asignarEstadoAutorizacionInicial(residencia);
			asignarCarreraJefeArea(residencia);

		} else {

			conservarDatosNoEditables(residencia);
		}

		repoResidencia.save(residencia);
	}

	private void normalizarDatos(Residencia residencia) {
		if (residencia.getNombreProyecto() != null) {
			residencia.setNombreProyecto(residencia.getNombreProyecto().trim());
		}

		if (residencia.getDescripcion() != null) {
			residencia.setDescripcion(residencia.getDescripcion().trim());
		}

		if (residencia.getObjetivo() != null) {
			residencia.setObjetivo(residencia.getObjetivo().trim());
		}

		if (residencia.getPeriodo() != null) {
			residencia.setPeriodo(residencia.getPeriodo().trim());
		}
	}

	private void cargarRelacionesDesdeBD(Residencia residencia) {
		asignarEmpresa(residencia);
		asignarResidente(residencia);
		asignarAsesorInterno(residencia);
		asignarAsesorExterno(residencia);
	}

	private void asignarEmpresa(Residencia residencia) {
		if (residencia.getEmpresa() == null || residencia.getEmpresa().getId() == null) {
			throw new RuntimeException("Debes seleccionar una empresa.");
		}

		Empresa empresa = repoEmpresa.findByIdAndEstatus(residencia.getEmpresa().getId(), 1);

		if (empresa == null) {
			throw new RuntimeException("La empresa seleccionada no existe o está inactiva.");
		}

		if (!empresa.isConvenioActivo()) {
			throw new RuntimeException("La empresa tiene el convenio vencido o inactivo.");
		}

		residencia.setEmpresa(empresa);
	}

	private void asignarResidente(Residencia residencia) {
		if (residencia.getResidente() == null || residencia.getResidente().getId() == null) {
			throw new RuntimeException("Debes seleccionar un residente.");
		}

		Residente residente = repoResidente.findById(residencia.getResidente().getId()).orElse(null);

		if (residente == null || residente.getEstatus() == null || residente.getEstatus() != 1) {
			throw new RuntimeException("El residente seleccionado no existe o está inactivo.");
		}

		validarResidenciaUnicaPorResidente(residencia, residente);
		residencia.setResidente(residente);
	}

	private void validarResidenciaUnicaPorResidente(Residencia residencia, Residente residente) {
		List<Residencia> existentes = repoResidencia.findByResidenteAndEstatusOrderByIdDesc(residente, 1);

		boolean duplicada = existentes.stream()
				.anyMatch(item -> item != null
						&& item.getId() != null
						&& (residencia.getId() == null || !item.getId().equals(residencia.getId())));

		if (duplicada) {
			String nombre = residente.getNombreCompleto() != null && !residente.getNombreCompleto().trim().isEmpty()
					? residente.getNombreCompleto()
					: "El residente seleccionado";
			throw new RuntimeException(nombre + " ya tiene una residencia activa registrada.");
		}
	}

	private void asignarAsesorInterno(Residencia residencia) {
		if (residencia.getAsesorInterno() == null || residencia.getAsesorInterno().getId() == null) {
			return;
		}

		AsesorInterno asesorInterno = repoAsesorInterno.findById(residencia.getAsesorInterno().getId()).orElse(null);

		if (asesorInterno == null || asesorInterno.getEstatus() == null || asesorInterno.getEstatus() != 1) {
			throw new RuntimeException("El asesor interno seleccionado no existe o está inactivo.");
		}

		residencia.setAsesorInterno(asesorInterno);
	}

	private void asignarAsesorExterno(Residencia residencia) {
		if (residencia.getAsesorExterno() == null || residencia.getAsesorExterno().getId() == null) {
			return;
		}

		AsesorExterno asesorExterno = repoAsesorExterno.findById(residencia.getAsesorExterno().getId()).orElse(null);

		if (asesorExterno == null || asesorExterno.getEstatus() == null || asesorExterno.getEstatus() != 1) {
			throw new RuntimeException("El asesor externo seleccionado no existe o está inactivo.");
		}

		if (residencia.getEmpresa() == null || residencia.getEmpresa().getNombre() == null) {
			throw new RuntimeException("Debes seleccionar una empresa valida antes de asignar asesor externo.");
		}

		if (!coincideEmpresaAsesor(asesorExterno.getEmpresa(), residencia.getEmpresa().getNombre())) {
			throw new RuntimeException("El asesor externo seleccionado no pertenece a la empresa elegida.");
		}

		residencia.setAsesorExterno(asesorExterno);
	}

	private boolean coincideEmpresaAsesor(String empresaAsesor, String empresaResidencia) {
		String empresaAsesorNormalizada = normalizarEmpresa(empresaAsesor);
		String empresaResidenciaNormalizada = normalizarEmpresa(empresaResidencia);

		return !empresaAsesorNormalizada.isEmpty()
				&& empresaAsesorNormalizada.equals(empresaResidenciaNormalizada);
	}

	private String normalizarEmpresa(String valor) {
		if (valor == null) {
			return "";
		}

		return Normalizer.normalize(valor.trim().toUpperCase(), Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "");
	}

	private void conservarDatosNoEditables(Residencia residencia) {
		Residencia existente = repoResidencia.findById(residencia.getId()).orElse(null);

		if (existente == null) {
			return;
		}

		if (residencia.getIdProyectoCarrera() == null || residencia.getIdProyectoCarrera().trim().isEmpty()) {
			residencia.setIdProyectoCarrera(existente.getIdProyectoCarrera());
		}

		if (residencia.getEstadoAutorizacion() == null || residencia.getEstadoAutorizacion().trim().isEmpty()) {
			residencia.setEstadoAutorizacion(existente.getEstadoAutorizacion());
		}

		if (residencia.getFechaAutorizacion() == null) {
			residencia.setFechaAutorizacion(existente.getFechaAutorizacion());
		}

		if (residencia.getOrigenProyecto() == null || residencia.getOrigenProyecto().trim().isEmpty()) {
			residencia.setOrigenProyecto(existente.getOrigenProyecto());
		}

		if (residencia.getCarreraJefeArea() == null || residencia.getCarreraJefeArea().trim().isEmpty()) {
			residencia.setCarreraJefeArea(existente.getCarreraJefeArea());
		}

		if (residencia.getObservacionesAutorizacion() == null) {
			residencia.setObservacionesAutorizacion(existente.getObservacionesAutorizacion());
		}
	}

	private void asignarIdProyectoCarrera(Residencia residencia) {
		String periodo = residencia.getPeriodo();
		String carrera = obtenerCarreraResidencia(residencia);

		if (periodo == null || periodo.trim().isEmpty()) {
			throw new RuntimeException("El periodo no fue asignado correctamente.");
		}

		if (carrera == null || carrera.trim().isEmpty()) {
			throw new RuntimeException("El residente no tiene carrera asignada.");
		}

		long consecutivo = contarProyectosPorCarreraYPeriodo(carrera.trim(), periodo.trim()) + 1;

		residencia.setIdProyectoCarrera(String.valueOf(consecutivo));
	}

	private long contarProyectosPorCarreraYPeriodo(String carrera, String periodo) {
		List<Residencia> residencias = repoResidencia.findByPeriodoAndEstatusOrderByIdDesc(periodo, 1);
		long contador = 0;

		for (Residencia residencia : residencias) {
			String carreraActual = obtenerCarreraResidencia(residencia);

			if (carreraActual != null && carreraActual.equalsIgnoreCase(carrera)) {
				contador++;
			}
		}

		return contador;
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
		if (residencia == null || residencia.getResidente() == null) {
			return null;
		}

		Object residente = residencia.getResidente();

		String carreraDesdeEstudiante = obtenerCarreraDesdeEstudiante(residente);
		if (carreraDesdeEstudiante != null && !carreraDesdeEstudiante.trim().isEmpty()) {
			return carreraDesdeEstudiante.trim();
		}

		String carreraDesdeResidente = obtenerTextoPorGetter(residente, "getCarrera");
		if (carreraDesdeResidente != null && !carreraDesdeResidente.trim().isEmpty()) {
			return carreraDesdeResidente.trim();
		}

		return null;
	}

	private String obtenerCarreraDesdeEstudiante(Object residente) {
		try {
			Method metodoEstudiante = residente.getClass().getMethod("getEstudiante");
			Object estudiante = metodoEstudiante.invoke(residente);

			if (estudiante == null) {
				return null;
			}

			Object carrera = estudiante.getClass().getMethod("getCarrera").invoke(estudiante);

			if (carrera == null) {
				return null;
			}

			if (carrera instanceof String) {
				return (String) carrera;
			}

			try {
				Object nombre = carrera.getClass().getMethod("getNombre").invoke(carrera);
				return nombre != null ? nombre.toString() : null;
			} catch (Exception e) {
				return carrera.toString();
			}

		} catch (Exception e) {
			return null;
		}
	}

	private String obtenerTextoPorGetter(Object objeto, String nombreGetter) {
		try {
			Method metodo = objeto.getClass().getMethod(nombreGetter);
			Object valor = metodo.invoke(objeto);

			return valor != null ? valor.toString() : null;
		} catch (Exception e) {
			return null;
		}
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

		String valor = residencia.getIdProyectoCarrera().trim();

		try {
			return Integer.parseInt(valor);
		} catch (NumberFormatException e) {
			String soloNumeros = valor.replaceAll("\\D+", "");

			if (soloNumeros.isEmpty()) {
				return 0;
			}

			try {
				return Integer.parseInt(soloNumeros);
			} catch (NumberFormatException ex) {
				return 0;
			}
		}
	}

	@Override
	public void dictaminarProyecto(Integer idResidencia, String estadoAutorizacion,
			String observacionesAutorizacion) {

		Residencia residencia = repoResidencia.findByIdAndEstatus(idResidencia, 1);

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

		Integer totalRechazos = residencia.getTotalRechazos();

		if (totalRechazos == null) {
			totalRechazos = 0;
		}

		if (estado.equals("RECHAZADO")) {
			totalRechazos++;
			residencia.setTotalRechazos(totalRechazos);

			residencia.setEstadoAutorizacion("RECHAZADO");
			residencia.setObservacionesAutorizacion(observacionesAutorizacion);
			residencia.setFechaAutorizacion(LocalDate.now());

			if (totalRechazos >= 2) {
				residencia.setEstatus(0);
				repoResidencia.save(residencia);

				throw new RuntimeException(
					"La propuesta fue rechazada por segunda ocasión y fue eliminada automáticamente."
				);
			}

			repoResidencia.save(residencia);
			return;
		}

		if (estado.equals("AUTORIZADO")) {
			residencia.setTotalRechazos(0);
		}

		residencia.setEstadoAutorizacion(estado);
		residencia.setObservacionesAutorizacion(observacionesAutorizacion);
		residencia.setFechaAutorizacion(LocalDate.now());

		repoResidencia.save(residencia);
	}

	@Override
	public void cerrarExpediente(Integer idResidencia) {
		Residencia residencia = repoResidencia.findById(idResidencia).orElse(null);

		if (residencia != null) {
			residencia.setEstatusProceso("CERRADO");
			residencia.setFechaCierre(LocalDate.now());
			repoResidencia.save(residencia);
		}
	}

	@Override
	public void reabrirExpediente(Integer idResidencia) {
		Residencia residencia = repoResidencia.findById(idResidencia).orElse(null);

		if (residencia != null) {
			residencia.setEstatusProceso("EN_PROCESO");
			residencia.setFechaCierre(null);
			repoResidencia.save(residencia);
		}
	}

	@Override
	public void eliminar(Integer idResidencia) {
		Residencia residencia = repoResidencia.findById(idResidencia).orElse(null);

		if (residencia != null) {
			residencia.setEstatus(0);
			repoResidencia.save(residencia);
		}
	}

	@Override
	public void recuperar(Integer idResidencia) {
		Residencia residencia = repoResidencia.findById(idResidencia).orElse(null);

		if (residencia != null) {
			residencia.setEstatus(1);
			repoResidencia.save(residencia);
		}
	}
}
