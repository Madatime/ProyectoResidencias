package itch.tsp.controller;

import java.time.LocalDate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.access.AccessDeniedException;

import jakarta.servlet.http.HttpServletResponse;

import itch.tsp.security.SeguridadResidenciaService;
import itch.tsp.security.UsuarioPrincipal;

import itch.tsp.model.AsesorExterno;
import itch.tsp.model.AsesorInterno;
import itch.tsp.model.BancoProyecto;
import itch.tsp.model.Carrera;

import itch.tsp.model.DocumentoResidencia;
import itch.tsp.model.Empresa;
import itch.tsp.model.EstatusDocumento;
import itch.tsp.model.Residencia;
import itch.tsp.model.Residente;
import itch.tsp.model.TipoDocumentoResidencia;
import itch.tsp.service.DictamenPdfService;
import itch.tsp.service.IAsesorExternoService;
import itch.tsp.service.IAsesorInternoService;
import itch.tsp.service.IBancoProyectoService;
import itch.tsp.service.ICarreraService;
import itch.tsp.service.CalendarioResidenciaReglaService;
import itch.tsp.service.IDocumentoResidenciaService;
import itch.tsp.service.IEmpresaService;
import itch.tsp.service.IEvaluacionResidenciaService;
import itch.tsp.service.IResidenciaService;
import itch.tsp.service.IResidenteService;

@Controller
public class ResidenciaController {

	@Autowired
	private IResidenciaService serviceResidencia;

	@Autowired
	private IEmpresaService serviceEmpresa;

	@Autowired
	private IResidenteService serviceResidente;

	@Autowired
	private IAsesorInternoService serviceAsesorInterno;

	@Autowired
	private IAsesorExternoService serviceAsesorExterno;

	@Autowired
	private IBancoProyectoService serviceBancoProyecto;

	@Autowired
	private IDocumentoResidenciaService serviceDocumentoResidencia;

	@Autowired
	private IEvaluacionResidenciaService serviceEvaluacionResidencia;

	@Autowired
	private ICarreraService serviceCarrera;
	
	@Autowired
	private SeguridadResidenciaService seguridadResidenciaService;

	@Autowired
	private DictamenPdfService dictamenPdfService;

	@Autowired
	private CalendarioResidenciaReglaService calendarioResidenciaReglaService;

	private static final TipoDocumentoResidencia[] DOCUMENTOS_OBLIGATORIOS = {
			TipoDocumentoResidencia.REPORTE_PRELIMINAR,
			TipoDocumentoResidencia.PROYECTO_RESIDENCIA,
			TipoDocumentoResidencia.PRIMER_INFORME,
			TipoDocumentoResidencia.SEGUNDO_INFORME,
			TipoDocumentoResidencia.EVALUACION_FINAL,
			TipoDocumentoResidencia.LIBERACION_ASESOR_INTERNO,
			TipoDocumentoResidencia.ENTREGA_REPORTE_EMPRESA,
			TipoDocumentoResidencia.OFICIO_ENTREGA_DIVISION
	};
	@GetMapping("/residencias/index")
	public String mostrarIndex(
			@RequestParam(name = "texto", required = false) String texto,
			@RequestParam(name = "periodo", required = false) String periodo,
			@RequestParam(name = "idCarrera", required = false) Integer idCarrera, 
			Authentication authentication, Model model) {

		List<Residencia> lista;

		if ((texto != null && !texto.trim().isEmpty()) || (periodo != null && !periodo.trim().isEmpty())) {
			lista = serviceResidencia.buscarResidenciasPorPeriodoYTexto(periodo, texto);
		} else {
			lista = serviceResidencia.buscarTodasActivas();
		}

		lista = filtrarPorCarrera(lista, idCarrera);

		model.addAttribute("textoBusqueda", texto);
		model.addAttribute("periodoBusqueda", periodo);
		model.addAttribute("idCarrera", idCarrera);
		model.addAttribute("carreras", serviceCarrera.buscarTodasActivas());

		lista = seguridadResidenciaService.filtrarResidenciasPermitidas(lista, authentication);
		cargarResumenResidencias(model, lista);

		return "residencias/listaResidencia";
	}
	@GetMapping("/residencias/searchPeriodoTexto")
	public String buscar(
			@RequestParam(name = "texto", required = false) String texto,
			@RequestParam(name = "periodo", required = false) String periodo,
			@RequestParam(name = "idCarrera", required = false) Integer idCarrera,
			Authentication authentication, Model model) {

		List<Residencia> lista = serviceResidencia.buscarResidenciasPorPeriodoYTexto(periodo, texto);
		lista = filtrarPorCarrera(lista, idCarrera);

		model.addAttribute("textoBusqueda", texto);
		model.addAttribute("periodoBusqueda", periodo);
		model.addAttribute("idCarrera", idCarrera);
		model.addAttribute("carreras", serviceCarrera.buscarTodasActivas());

		lista = seguridadResidenciaService.filtrarResidenciasPermitidas(lista, authentication);
		cargarResumenResidencias(model, lista);

		return "residencias/listaResidencia";
	}

	@GetMapping("/residencias/dictamen/pdf")
	public void descargarDictamenPdf(
			@RequestParam(name = "texto", required = false) String texto,
			@RequestParam(name = "periodo", required = false) String periodo,
			Authentication authentication,
			HttpServletResponse response) throws Exception {

		List<Residencia> lista;

		if ((texto != null && !texto.trim().isEmpty()) || (periodo != null && !periodo.trim().isEmpty())) {
			lista = serviceResidencia.buscarResidenciasPorPeriodoYTexto(periodo, texto);
		} else {
			lista = serviceResidencia.buscarTodasActivas();
		}

		lista = seguridadResidenciaService.filtrarResidenciasPermitidas(lista, authentication);

		lista = lista.stream()
				.sorted(Comparator
						.comparing(this::obtenerNombreCarreraResidencia, String.CASE_INSENSITIVE_ORDER.reversed())
						.thenComparing(Residencia::getId, Comparator.nullsLast(Comparator.reverseOrder())))
				.collect(Collectors.toList());

		String nombreArchivo = "dictamen-residencias-" + LocalDate.now() + ".pdf";
		response.setContentType("application/pdf");
		response.setHeader(
				"Content-Disposition",
				"inline; filename*=UTF-8''" + URLEncoder.encode(nombreArchivo, StandardCharsets.UTF_8));

		dictamenPdfService.generarDictamen(response.getOutputStream(), lista);
	}
	@GetMapping("/residencias/create")
	public String crear(
			Model model,
			RedirectAttributes flash,
			Authentication authentication) {
		Residencia residencia = new Residencia();
		residencia.setEmpresa(new Empresa());
		residencia.setPeriodo(obtenerPeriodoActual());
		calendarioResidenciaReglaService.prepararFechasPorDefecto(residencia);

		if (esEstudiante(authentication)) {
			Residente residenteActual = obtenerResidenteAutenticado(authentication);

			if (residenteActual == null) {
				flash.addFlashAttribute("msgError", "No se encontro un residente asociado a tu usuario.");
				return "redirect:/residencias/index";
			}

			if (tieneResidenciaActiva(residenteActual.getId(), null)) {
				flash.addFlashAttribute("msgError", "Ya tienes una residencia activa registrada.");
				return "redirect:/residencias/index";
			}

			residencia.setResidente(residenteActual);
			model.addAttribute("modoEstudiante", true);
			model.addAttribute("residenteActual", residenteActual);
		}

		model.addAttribute("residencia", residencia);
		model.addAttribute("periodoActual", obtenerPeriodoActual());
		cargarPoliticaCalendario(model, residencia.getPeriodo());
		cargarCatalogosFormulario(
				model,
				residencia.getResidente() != null ? residencia.getResidente().getId() : null,
				false);

		return "residencias/formResidencia";
	}
	@PostMapping("/residencias/save")
	public String guardar(
			@ModelAttribute Residencia residencia,
			@RequestParam("idEmpresa") Integer idEmpresa,
			@RequestParam("idResidente") Integer idResidente,
			@RequestParam(name = "idAsesorInterno", required = false) Integer idAsesorInterno,
			@RequestParam(name = "idAsesorExterno", required = false) Integer idAsesorExterno,
			@RequestParam(name = "idBancoProyecto", required = false) Integer idBancoProyecto,
			Model model,
			RedirectAttributes flash,
			Authentication authentication) {

		try {
			boolean modoEstudiante = esEstudiante(authentication);
			Integer idResidenteEfectivo = modoEstudiante ? obtenerIdResidenteAutenticado(authentication) : idResidente;

			if (modoEstudiante && idResidenteEfectivo == null) {
				throw new RuntimeException("No se encontro un residente asociado a tu usuario.");
			}

			if (modoEstudiante && tieneResidenciaActiva(idResidenteEfectivo, null)) {
				throw new RuntimeException("Ya tienes una residencia activa registrada.");
			}

			asignarRelaciones(
					residencia,
					idEmpresa,
					idResidenteEfectivo,
					modoEstudiante ? null : idAsesorInterno,
					idAsesorExterno,
					!modoEstudiante);

			residencia.setPeriodo(obtenerPeriodoActual());
			residencia.setProrrogaAutorizada(false);
			calendarioResidenciaReglaService.prepararFechasPorDefecto(residencia);
			prepararEstadoInicialResidencia(residencia, idBancoProyecto, modoEstudiante);

			if (residencia.getEstatus() == null) {
				residencia.setEstatus(1);
			}

			if (!modoEstudiante) {
				validarAsignacionAsesorInterno(residencia);
			}

			serviceResidencia.guardarResidencia(residencia);

			if (idBancoProyecto != null) {
				serviceBancoProyecto.marcarAsignado(idBancoProyecto);
			}

			flash.addFlashAttribute(
					"msgSuccess",
					modoEstudiante
							? "Solicitud de residencia enviada correctamente."
							: "Residencia guardada correctamente.");
			return "redirect:/residencias/index";

		} catch (RuntimeException e) {
			model.addAttribute("msgError", e.getMessage());
			model.addAttribute("residencia", residencia);
			model.addAttribute("idBancoProyecto", idBancoProyecto);
			model.addAttribute("periodoActual", obtenerPeriodoActual());
			model.addAttribute("modoEstudiante", esEstudiante(authentication));
			model.addAttribute("residenteActual", obtenerResidenteAutenticado(authentication));
			cargarPoliticaCalendario(model, residencia.getPeriodo() != null ? residencia.getPeriodo() : obtenerPeriodoActual());
			cargarCatalogosFormulario(
					model,
					esEstudiante(authentication) && obtenerIdResidenteAutenticado(authentication) != null
							? obtenerIdResidenteAutenticado(authentication)
							: null,
					idBancoProyecto != null);
			return "residencias/formResidencia";
		}
	}
	@GetMapping("/residencias/edit/{id}")
	public String editar(
			@PathVariable("id") Integer idResidencia,
			Model model,
			RedirectAttributes flash,
			Authentication authentication) {

		Residencia residencia =
				serviceResidencia.buscarPorIdResidencia(idResidencia);

		if (residencia == null) {
			flash.addFlashAttribute(
					"msgError",
					"La residencia no existe o está inactiva.");

			return "redirect:/residencias/index";
		}

			seguridadResidenciaService
				.validarEdicionResidencia(residencia, authentication);

		if (residencia.getEmpresa() == null) {
			residencia.setEmpresa(new Empresa());
		}

		residencia.setPeriodo(obtenerPeriodoActual());
		calendarioResidenciaReglaService.prepararFechasPorDefecto(residencia);

		model.addAttribute("residencia", residencia);
		model.addAttribute("periodoActual", obtenerPeriodoActual());
		cargarPoliticaCalendario(model, residencia.getPeriodo());

		cargarCatalogosFormulario(
				model,
				residencia.getResidente() != null ? residencia.getResidente().getId() : null,
				false);

		return "residencias/formResidencia";
	}
	@PostMapping("/residencias/update")
	public String actualizar(
			@ModelAttribute Residencia residencia,
			@RequestParam("idEmpresa") Integer idEmpresa,
			@RequestParam("idResidente") Integer idResidente,
			@RequestParam(name = "idAsesorInterno", required = false) Integer idAsesorInterno,
			@RequestParam("idAsesorExterno") Integer idAsesorExterno,
			@RequestParam(name = "idBancoProyecto", required = false) Integer idBancoProyecto,
			Model model,
			RedirectAttributes flash,
			Authentication authentication) {

		try {

			Residencia residenciaBD =
					serviceResidencia.buscarPorIdResidencia(residencia.getId());

			if (residenciaBD == null) {

				flash.addFlashAttribute(
						"msgError",
						"La residencia no existe.");

				return "redirect:/residencias/index";
			}

				seguridadResidenciaService
					.validarEdicionResidencia(residenciaBD, authentication);

			residencia.setEstadoAutorizacion(
					residenciaBD.getEstadoAutorizacion());

			residencia.setFechaAutorizacion(
					residenciaBD.getFechaAutorizacion());

			residencia.setIdProyectoCarrera(
					residenciaBD.getIdProyectoCarrera());

			residencia.setOrigenProyecto(
					residenciaBD.getOrigenProyecto());

			residencia.setCarreraJefeArea(
					residenciaBD.getCarreraJefeArea());

			residencia.setObservacionesAutorizacion(
					residenciaBD.getObservacionesAutorizacion());

			asignarRelaciones(
					residencia,
					idEmpresa,
					idResidente,
					idAsesorInterno,
					idAsesorExterno,
					true);

			validarAsignacionAsesorInterno(residencia);

			residencia.setPeriodo(obtenerPeriodoActual());
			residencia.setProrrogaAutorizada(tieneProrrogaCalendario(residenciaBD));
			calendarioResidenciaReglaService.prepararFechasPorDefecto(residencia);

			if (residencia.getEstatus() == null) {
				residencia.setEstatus(1);
			}

			serviceResidencia.guardarResidencia(residencia);

			if (idBancoProyecto != null) {
				serviceBancoProyecto.marcarAsignado(idBancoProyecto);
			}

			flash.addFlashAttribute(
					"msgSuccess",
					"Residencia actualizada correctamente.");

			return "redirect:/residencias/index";

		} catch (AccessDeniedException e) {

			flash.addFlashAttribute(
					"msgError",
					"No tienes permiso para modificar esta residencia.");

			return "redirect:/residencias/index";

		} catch (RuntimeException e) {

			model.addAttribute("msgError", e.getMessage());
			model.addAttribute("residencia", residencia);
			model.addAttribute("idBancoProyecto", idBancoProyecto);
			model.addAttribute("periodoActual", obtenerPeriodoActual());
			cargarPoliticaCalendario(model, residencia.getPeriodo() != null ? residencia.getPeriodo() : obtenerPeriodoActual());

			cargarCatalogosFormulario(model, idResidente, idBancoProyecto != null);

			return "residencias/formResidencia";
		}
	}
	@GetMapping("/residencias/asesores-afines/{idResidente}")
	@ResponseBody
	public Map<String, Object> obtenerAsesoresAfines(
			@PathVariable("idResidente") Integer idResidente,
			Authentication authentication) {
		Map<String, Object> respuesta = new LinkedHashMap<>();

		Residente residente = serviceResidente.buscarPorIdResidente(idResidente);

		if (residente == null) {
			respuesta.put("dictamenAutorizado", false);
			respuesta.put("mensaje", "El residente seleccionado no existe o está inactivo.");
			respuesta.put("asesores", new ArrayList<>());
			return respuesta;
		}

		Residencia residenciaAutorizada = obtenerResidenciaAutorizadaDelResidente(idResidente);
		
		if (residenciaAutorizada != null) {
			seguridadResidenciaService.validarAccesoResidencia(residenciaAutorizada, authentication);
		}

		if (residenciaAutorizada == null) {
			respuesta.put("dictamenAutorizado", false);
			respuesta.put("mensaje", "El dictamen del proyecto todavía no está autorizado.");
			respuesta.put("asesores", new ArrayList<>());
			return respuesta;
		}

		if (residente.getEstudiante() == null || residente.getEstudiante().getCarrera() == null) {
			respuesta.put("dictamenAutorizado", true);
			respuesta.put("mensaje", "El residente no tiene carrera asignada.");
			respuesta.put("asesores", new ArrayList<>());
			return respuesta;
		}

		Integer idCarreraResidente = residente.getEstudiante().getCarrera().getId();

		List<Map<String, Object>> asesores = serviceAsesorInterno.buscarTodosActivos()
				.stream()
				.filter(asesor -> asesor != null
						&& asesor.getDocente() != null
						&& asesor.getDocente().getCarrerasHabilitadas() != null
						&& asesor.getDocente().getCarrerasHabilitadas()
								.stream()
								.anyMatch(carrera -> carrera != null
										&& carrera.getId() != null
										&& carrera.getId().equals(idCarreraResidente)))
				.map(asesor -> {
					Map<String, Object> item = new LinkedHashMap<>();
					item.put("id", asesor.getId());
					item.put("noEmpleado", asesor.getNoEmpleado());
					item.put("nombre", asesor.getNombre());
					item.put("apellidos", asesor.getApellidos());
					return item;
				})
				.collect(Collectors.toList());

		respuesta.put("dictamenAutorizado", true);
		respuesta.put("mensaje", "Asesores cargados correctamente.");
		respuesta.put("asesores", asesores);

		return respuesta;
	}

	@GetMapping("/residencias/dictamen/{id:\\d+}")
	public String mostrarDictamenProyecto(
			@PathVariable("id") Integer idResidencia,
			Model model,
			RedirectAttributes flash,
			Authentication authentication) {

		Residencia residencia =
				serviceResidencia.buscarPorIdResidencia(idResidencia);
		
		seguridadResidenciaService
				.validarDictamenResidencia(residencia, authentication);

		if (residencia == null) {

			flash.addFlashAttribute(
					"msgError",
					"La residencia no existe o está inactiva.");

			return "redirect:/residencias/index";
		}

		seguridadResidenciaService
				.validarAccesoResidencia(residencia, authentication);

		DocumentoResidencia reportePreliminar = serviceDocumentoResidencia.buscarPorResidenciaYTipo(
				idResidencia,
				TipoDocumentoResidencia.REPORTE_PRELIMINAR);
		boolean reportePreliminarDisponible = reportePreliminar != null
				&& reportePreliminar.getRutaArchivo() != null
				&& !reportePreliminar.getRutaArchivo().trim().isEmpty()
				&& !"GENERADO_EN_LINEA".equalsIgnoreCase(reportePreliminar.getRutaArchivo());

		model.addAttribute("residencia", residencia);
		model.addAttribute("reportePreliminar", reportePreliminar);
		model.addAttribute("reportePreliminarDisponible", reportePreliminarDisponible);

		return "residencias/dictamenProyecto";
	}

	@PostMapping("/residencias/dictamen")
	public String dictaminarProyecto(
			@RequestParam("idResidencia") Integer idResidencia,
			@RequestParam("estadoAutorizacion") String estadoAutorizacion,
			@RequestParam(value = "observacionesAutorizacion", required = false) String observaciones,
			Authentication authentication,
			RedirectAttributes flash) {

		try {

			Residencia residencia =
					serviceResidencia.buscarPorIdResidencia(idResidencia);

			if (residencia == null) {

				flash.addFlashAttribute(
						"msgError",
						"La residencia no existe o está inactiva.");

				return "redirect:/residencias/index";
			}

			seguridadResidenciaService
					.validarDictamenResidencia(residencia, authentication);

			serviceResidencia.dictaminarProyecto(
					idResidencia,
					estadoAutorizacion,
					observaciones);

			flash.addFlashAttribute(
					"msgSuccess",
					"Dictamen guardado correctamente.");

		} catch (AccessDeniedException e) {

			flash.addFlashAttribute(
					"msgError",
					"No tienes permiso para dictaminar esta residencia.");

		} catch (RuntimeException e) {

			flash.addFlashAttribute(
					"msgError",
					e.getMessage());
		}

		return "redirect:/residencias/index";
	}
	@GetMapping("/residencias/delete/{id}")
	public String eliminar(@PathVariable("id") Integer idResidencia, RedirectAttributes flash) {
		serviceResidencia.eliminar(idResidencia);
		flash.addFlashAttribute("msgSuccess", "Residencia enviada a inactivos correctamente.");
		return "redirect:/residencias/index";
	}
	@GetMapping("/residencias/inactivos")
	public String mostrarInactivos(Model model) {
		List<Residencia> residencias = serviceResidencia.buscarTodasInactivas();
		model.addAttribute("residencias", residencias);
		return "residencias/recuperarResidencias";
	}

	@GetMapping("/residencias/recuperar/{id}")
	public String recuperar(@PathVariable("id") Integer idResidencia, RedirectAttributes flash) {
		serviceResidencia.recuperar(idResidencia);
		flash.addFlashAttribute("msgSuccess", "Residencia recuperada correctamente.");
		return "redirect:/residencias/inactivos";
	}

	@GetMapping("/residencias/create-desde-banco/{idBancoProyecto}")
	public String crearDesdeBanco(
			@PathVariable("idBancoProyecto") Integer idBancoProyecto,
			Model model,
			RedirectAttributes flash,
			Authentication authentication) {

		BancoProyecto banco = serviceBancoProyecto.buscarPorId(idBancoProyecto);

		if (banco == null) {
			flash.addFlashAttribute("msgError", "El proyecto del banco no existe.");
			return "redirect:/banco-proyectos/index";
		}

		boolean modoEstudiante = esEstudiante(authentication);
		Residente residenteActual = modoEstudiante ? obtenerResidenteAutenticado(authentication) : null;

		if (modoEstudiante && residenteActual == null) {
			flash.addFlashAttribute("msgError", "No se encontro un residente asociado a tu usuario.");
			return "redirect:/banco-proyectos/index";
		}

		if (modoEstudiante && tieneResidenciaActiva(residenteActual.getId(), null)) {
			flash.addFlashAttribute("msgError", "Ya tienes una residencia activa registrada.");
			return "redirect:/residencias/index";
		}

		Residencia residencia = new Residencia();
		aplicarBancoProyecto(residencia, banco, modoEstudiante);
		residencia.setPeriodo(obtenerPeriodoActual());
		calendarioResidenciaReglaService.prepararFechasPorDefecto(residencia);

		if (residenteActual != null) {
			residencia.setResidente(residenteActual);
		}

		model.addAttribute("residencia", residencia);
		model.addAttribute("idBancoProyecto", banco.getId());
		model.addAttribute("periodoActual", obtenerPeriodoActual());
		model.addAttribute("modoEstudiante", modoEstudiante);
		model.addAttribute("residenteActual", residenteActual);
		cargarPoliticaCalendario(model, residencia.getPeriodo());
		cargarCatalogosFormulario(
				model,
				residencia.getResidente() != null ? residencia.getResidente().getId() : null,
				true);

		return "residencias/formResidencia";
	}

	private void asignarRelaciones(
			Residencia residencia,
			Integer idEmpresa,
			Integer idResidente,
			Integer idAsesorInterno,
			Integer idAsesorExterno,
			boolean requiereAsesorExterno) {

		if (idEmpresa == null) {
			throw new RuntimeException("Debes seleccionar una empresa.");
		}

		if (idResidente == null) {
			throw new RuntimeException("Debes seleccionar un residente.");
		}

		if (requiereAsesorExterno && idAsesorExterno == null) {
			throw new RuntimeException("Debes seleccionar un asesor externo.");
		}

		Empresa empresa = new Empresa();
		empresa.setId(idEmpresa);
		residencia.setEmpresa(empresa);

		Residente residente = serviceResidente.buscarPorIdResidente(idResidente);

		if (residente == null) {
			throw new RuntimeException("El residente seleccionado no existe o está inactivo.");
		}

		residencia.setResidente(residente);

		if (idAsesorInterno != null) {
			AsesorInterno asesorInterno = serviceAsesorInterno.buscarPorIdAsesorInterno(idAsesorInterno);

			if (asesorInterno == null) {
				throw new RuntimeException("El asesor interno seleccionado no existe o está inactivo.");
			}

			residencia.setAsesorInterno(asesorInterno);
		} else {
			residencia.setAsesorInterno(null);
		}

		if (idAsesorExterno != null) {
			AsesorExterno asesorExterno = new AsesorExterno();
			asesorExterno.setId(idAsesorExterno);
			residencia.setAsesorExterno(asesorExterno);
		} else {
			residencia.setAsesorExterno(null);
		}
	}

	private void prepararEstadoInicialResidencia(Residencia residencia, Integer idBancoProyecto, boolean modoEstudiante) {
		if (idBancoProyecto != null) {
			BancoProyecto banco = serviceBancoProyecto.buscarPorId(idBancoProyecto);

			if (banco == null) {
				throw new RuntimeException("El proyecto del banco no existe o ya no esta disponible.");
			}

			aplicarBancoProyecto(residencia, banco, modoEstudiante);
			return;
		}

		if (modoEstudiante) {
			residencia.setEstadoAutorizacion("PENDIENTE");
			residencia.setFechaAutorizacion(null);

			if (residencia.getOrigenProyecto() == null || residencia.getOrigenProyecto().trim().isEmpty()) {
				residencia.setOrigenProyecto("PROYECTO");
			}
		}
	}

	private void aplicarBancoProyecto(Residencia residencia, BancoProyecto banco, boolean modoEstudiante) {
		residencia.setNombreProyecto(banco.getNombreProyecto());
		residencia.setDescripcion(banco.getDescripcion());
		residencia.setObjetivo(banco.getObjetivo());
		residencia.setEmpresa(banco.getEmpresa());
		residencia.setOrigenProyecto("BANCO_PROYECTOS");
		residencia.setEstadoAutorizacion(modoEstudiante ? "PENDIENTE" : "AUTORIZADO");
		residencia.setFechaAutorizacion(modoEstudiante ? null : LocalDate.now());
	}

	private boolean esEstudiante(Authentication authentication) {
		if (authentication == null) {
			return false;
		}

		for (GrantedAuthority authority : authentication.getAuthorities()) {
			if (authority != null && "ROLE_ESTUDIANTE".equals(authority.getAuthority())) {
				return true;
			}
		}

		return false;
	}

	private Integer obtenerIdResidenteAutenticado(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioPrincipal usuarioPrincipal)) {
			return null;
		}

		return usuarioPrincipal.getIdResidente();
	}

	private Residente obtenerResidenteAutenticado(Authentication authentication) {
		Integer idResidente = obtenerIdResidenteAutenticado(authentication);

		if (idResidente == null) {
			return null;
		}

		return serviceResidente.buscarPorIdResidente(idResidente);
	}

	private boolean tieneResidenciaActiva(Integer idResidente, Integer idResidenciaExcluir) {
		if (idResidente == null) {
			return false;
		}

		for (Residencia residencia : serviceResidencia.buscarTodasActivas()) {
			if (residencia == null
					|| residencia.getResidente() == null
					|| residencia.getResidente().getId() == null) {
				continue;
			}

			if (!residencia.getResidente().getId().equals(idResidente)) {
				continue;
			}

			if (idResidenciaExcluir != null
					&& residencia.getId() != null
					&& residencia.getId().equals(idResidenciaExcluir)) {
				continue;
			}

			return true;
		}

		return false;
	}

	private void validarAsignacionAsesorInterno(Residencia residencia) {
		boolean dictamenAutorizado = dictamenPermiteAsignarAsesor(residencia.getEstadoAutorizacion());

		if (!dictamenAutorizado && residencia.getAsesorInterno() != null) {
			throw new RuntimeException("No puedes asignar asesor interno hasta que el dictamen esté autorizado.");
		}

		if (dictamenAutorizado && residencia.getAsesorInterno() == null) {
			throw new RuntimeException("Debes seleccionar un asesor interno porque el dictamen ya está autorizado.");
		}

		if (dictamenAutorizado) {
			validarAsesorCompatibleConCarrera(residencia);
		}
	}

	private void validarAsesorCompatibleConCarrera(Residencia residencia) {
		if (residencia == null) {
			throw new RuntimeException("No se pudo validar la residencia.");
		}

		if (residencia.getResidente() == null
				|| residencia.getResidente().getEstudiante() == null
				|| residencia.getResidente().getEstudiante().getCarrera() == null) {
			throw new RuntimeException("El residente no tiene una carrera asignada.");
		}

		if (residencia.getAsesorInterno() == null
				|| residencia.getAsesorInterno().getDocente() == null) {
			throw new RuntimeException("El asesor interno seleccionado no tiene docente asociado.");
		}

		if (residencia.getAsesorInterno().getDocente().getCarrerasHabilitadas() == null
				|| residencia.getAsesorInterno().getDocente().getCarrerasHabilitadas().isEmpty()) {
			throw new RuntimeException("El asesor interno no tiene carreras habilitadas.");
		}

		Integer idCarreraResidente = residencia.getResidente()
				.getEstudiante()
				.getCarrera()
				.getId();

		boolean compatible = residencia.getAsesorInterno()
				.getDocente()
				.getCarrerasHabilitadas()
				.stream()
				.anyMatch(carrera -> carrera != null
						&& carrera.getId() != null
						&& carrera.getId().equals(idCarreraResidente));

		if (!compatible) {
			throw new RuntimeException("El asesor interno no está habilitado para la carrera del residente.");
		}
	}

	private Residencia obtenerResidenciaAutorizadaDelResidente(Integer idResidente) {
		List<Residencia> residencias = serviceResidencia.buscarTodasActivas();

		for (Residencia residencia : residencias) {
			if (residencia.getResidente() != null
					&& residencia.getResidente().getId() != null
					&& residencia.getResidente().getId().equals(idResidente)
					&& dictamenPermiteAsignarAsesor(residencia.getEstadoAutorizacion())) {
				return residencia;
			}
		}

		return null;
	}

	private boolean dictamenPermiteAsignarAsesor(String estadoAutorizacion) {
		if (estadoAutorizacion == null) {
			return false;
		}

		String estado = estadoAutorizacion.trim().toUpperCase();

		return estado.equals("AUTORIZADO")
				|| estado.equals("AUTORIZADO_CON_OBSERVACIONES");
	}

	private List<Residencia> filtrarPorCarrera(List<Residencia> lista, Integer idCarrera) {
		if (idCarrera == null) {
			return lista;
		}

		return lista.stream()
				.filter(residencia -> residencia.getResidente() != null
						&& residencia.getResidente().getEstudiante() != null
						&& residencia.getResidente().getEstudiante().getCarrera() != null
						&& residencia.getResidente().getEstudiante().getCarrera().getId() != null
						&& residencia.getResidente().getEstudiante().getCarrera().getId().equals(idCarrera))
				.collect(Collectors.toList());
	}

	private void cargarCatalogosFormulario(Model model, Integer idResidenteActual, boolean incluirTodosLosResidentes) {
		model.addAttribute("residentes", obtenerResidentesDisponibles(idResidenteActual, incluirTodosLosResidentes));
		model.addAttribute("asesoresInternos", serviceAsesorInterno.buscarTodosActivos());
		model.addAttribute("asesoresExternos", serviceAsesorExterno.buscarTodosActivos());
		model.addAttribute("empresas", serviceEmpresa.buscarTodasActivas());
		model.addAttribute("carreras", serviceCarrera.buscarTodasActivas());
	}

	private List<Residente> obtenerResidentesDisponibles(Integer idResidenteActual, boolean incluirTodosLosResidentes) {
		List<Residente> residentes = serviceResidente.buscarTodosActivos();

		if (incluirTodosLosResidentes) {
			return residentes.stream()
					.filter(residente -> residente != null && residente.getId() != null)
					.collect(Collectors.toList());
		}

		List<Residencia> residenciasActivas = serviceResidencia.buscarTodasActivas();

		return residentes.stream()
				.filter(residente -> residente != null && residente.getId() != null)
				.filter(residente -> residente.getId().equals(idResidenteActual)
						|| residenciasActivas.stream().noneMatch(residencia -> residencia != null
								&& residencia.getResidente() != null
								&& residencia.getResidente().getId() != null
								&& residencia.getResidente().getId().equals(residente.getId())))
				.collect(Collectors.toList());
	}

	private void cargarPoliticaCalendario(Model model, String periodo) {
		model.addAttribute("fechaInicioOficial", calendarioResidenciaReglaService.obtenerFechaInicioOficial(periodo));
		model.addAttribute("fechaFinBase", calendarioResidenciaReglaService.obtenerFechaFinBase(periodo));
		model.addAttribute("fechaFinMaxima", calendarioResidenciaReglaService.obtenerFechaFinMaxima(periodo));
	}

	private boolean tieneProrrogaCalendario(Residencia residencia) {
		if (residencia == null || residencia.getPeriodo() == null || residencia.getFechaFin() == null) {
			return false;
		}

		LocalDate fechaFinBase = calendarioResidenciaReglaService.obtenerFechaFinBase(residencia.getPeriodo());
		return residencia.getFechaFin().isAfter(fechaFinBase);
	}

	private void cargarResumenResidencias(Model model, List<Residencia> lista) {
		List<Residencia> listaOrdenada = lista.stream()
				.sorted(Comparator
						.comparing(this::obtenerNombreCarreraResidencia, String.CASE_INSENSITIVE_ORDER.reversed())
						.thenComparing(Residencia::getId, Comparator.nullsLast(Comparator.reverseOrder())))
				.collect(Collectors.toList());

		Map<Integer, String> mapaEstadoExpediente = new LinkedHashMap<>();
		Map<Integer, Double> mapaPromedios = new LinkedHashMap<>();
		Map<Integer, Integer> mapaTotalDocumentos = new LinkedHashMap<>();
		Map<Integer, Integer> mapaAvanceExpediente = new LinkedHashMap<>();
		Map<Integer, Integer> mapaDocumentosObligatorios = new LinkedHashMap<>();

		for (Residencia residencia : listaOrdenada) {
			Integer idResidencia = residencia.getId();

			List<DocumentoResidencia> documentos = serviceDocumentoResidencia.buscarPorResidencia(idResidencia);
			Double promedioFinal = serviceEvaluacionResidencia.calcularPromedioFinal(idResidencia);

			int totalDocumentos = documentos != null ? documentos.size() : 0;
			int totalObligatorios = DOCUMENTOS_OBLIGATORIOS.length;
			int avanceExpediente = calcularAvanceExpediente(idResidencia);
			String estadoExpediente = calcularEstadoExpediente(avanceExpediente, promedioFinal);

			mapaEstadoExpediente.put(idResidencia, estadoExpediente);
			mapaPromedios.put(idResidencia, promedioFinal);
			mapaTotalDocumentos.put(idResidencia, totalDocumentos);
			mapaAvanceExpediente.put(idResidencia, avanceExpediente);
			mapaDocumentosObligatorios.put(idResidencia, totalObligatorios);
		}

		model.addAttribute("residencias", listaOrdenada);
		model.addAttribute("residenciasAgrupadas", agruparResidenciasPorCarrera(listaOrdenada));
		model.addAttribute("mapaEstadoExpediente", mapaEstadoExpediente);
		model.addAttribute("mapaPromedios", mapaPromedios);
		model.addAttribute("mapaTotalDocumentos", mapaTotalDocumentos);
		model.addAttribute("mapaAvanceExpediente", mapaAvanceExpediente);
		model.addAttribute("mapaDocumentosObligatorios", mapaDocumentosObligatorios);
	}

	private Map<String, List<Residencia>> agruparResidenciasPorCarrera(List<Residencia> lista) {
		return lista.stream()
				.collect(Collectors.groupingBy(
						this::obtenerNombreCarreraResidencia,
						LinkedHashMap::new,
						Collectors.toList()));
	}

	private String obtenerNombreCarreraResidencia(Residencia residencia) {
		if (residencia != null
				&& residencia.getResidente() != null
				&& residencia.getResidente().getEstudiante() != null
				&& residencia.getResidente().getEstudiante().getCarrera() != null
				&& residencia.getResidente().getEstudiante().getCarrera().getNombre() != null
				&& !residencia.getResidente().getEstudiante().getCarrera().getNombre().trim().isEmpty()) {
			return residencia.getResidente().getEstudiante().getCarrera().getNombre().trim();
		}

		return "Sin carrera asignada";
	}

	private String calcularEstadoExpediente(int avanceExpediente, Double promedioFinal) {
		if (avanceExpediente == 0) {
			return "SIN_AVANCE";
		}

		if (promedioFinal != null && promedioFinal >= 70 && avanceExpediente >= 80) {
			return "LISTO_PARA_CIERRE";
		}

		return "EN_SEGUIMIENTO";
	}

	private int calcularAvanceExpediente(Integer idResidencia) {
		Residencia residencia = serviceResidencia.buscarPorIdResidencia(idResidencia);

		if (residencia == null || !dictamenPermiteAsignarAsesor(residencia.getEstadoAutorizacion())) {
			return 0;
		}

		int suma = 0;

		for (TipoDocumentoResidencia tipo : DOCUMENTOS_OBLIGATORIOS) {
			DocumentoResidencia documento = serviceDocumentoResidencia.buscarPorResidenciaYTipo(idResidencia, tipo);

			if (documento == null || documento.getEstatus() == null) {
				suma += 0;
			} else if (documento.getEstatus() == EstatusDocumento.APROBADO) {
				suma += 100;
			} else if (documento.getEstatus() == EstatusDocumento.EN_REVISION) {
				suma += 75;
			} else if (documento.getEstatus() == EstatusDocumento.CARGADO) {
				suma += 50;
			} else if (documento.getEstatus() == EstatusDocumento.RECHAZADO) {
				suma += 25;
			}
		}

		return suma / DOCUMENTOS_OBLIGATORIOS.length;
	}

	private String obtenerPeriodoActual() {
		LocalDate hoy = LocalDate.now();
		int mes = hoy.getMonthValue();
		int anio = hoy.getYear();

		if (mes >= 1 && mes <= 6) {
			return "ENE-JUN " + anio;
		}

		return "AGO-DIC " + anio;
	}
}
