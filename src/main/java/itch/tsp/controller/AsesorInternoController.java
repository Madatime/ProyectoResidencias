package itch.tsp.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import itch.tsp.model.AsesorInterno;
import itch.tsp.model.Residencia;
import itch.tsp.security.UsuarioPrincipal;
import itch.tsp.service.IAsesorInternoService;
import itch.tsp.service.IDocenteService;
import itch.tsp.service.IResidenciaService;

@Controller
public class AsesorInternoController {

	@Autowired
	private IAsesorInternoService serviceAsesorInterno;

	@Autowired
	private IDocenteService serviceDocente;

	@Autowired
	private IResidenciaService serviceResidencia;

	@GetMapping({
			"/asesoresInternos",
			"/asesoresInternos/index",
			"/asesores-internos",
			"/asesores-internos/index"
	})
	public String mostrarIndex(Authentication authentication, Model model) {
		if (esAsesorInterno(authentication)) {
			return "redirect:/asesores-internos/mis-proyectos";
		}

		model.addAttribute("asesoresInternos", serviceAsesorInterno.buscarTodosActivos());
		model.addAttribute("periodosDisponibles", obtenerPeriodosDisponibles());
		return "asesoresInternos/listaAsesorInterno";
	}

	@GetMapping({
			"/asesoresInternos/search",
			"/asesores-internos/search"
	})
	public String buscar(
			@RequestParam(name = "texto", required = false) String texto,
			@RequestParam(name = "periodo", required = false) String periodo,
			Authentication authentication,
			Model model) {

		if (esAsesorInterno(authentication)) {
			return "redirect:/asesores-internos/mis-proyectos";
		}

		List<AsesorInterno> asesores;

		boolean textoVacio = texto == null || texto.trim().isEmpty();
		boolean periodoVacio = periodo == null || periodo.trim().isEmpty();

		if (textoVacio && periodoVacio) {
			asesores = serviceAsesorInterno.buscarTodosActivos();
		} else if (!periodoVacio) {
			asesores = serviceAsesorInterno.buscarAsesoresInternosConProyectoPorPeriodoYTexto(periodo, texto);
		} else {
			asesores = serviceAsesorInterno.buscarAsesoresInternos(texto);
		}

		model.addAttribute("asesoresInternos", asesores);
		model.addAttribute("textoBusqueda", texto);
		model.addAttribute("periodoBusqueda", periodo);
		model.addAttribute("periodosDisponibles", obtenerPeriodosDisponibles());

		return "asesoresInternos/listaAsesorInterno";
	}

	@GetMapping("/asesores-internos/mis-proyectos")
	public String misProyectos(Authentication authentication, Model model, RedirectAttributes flash) {
		UsuarioPrincipal principal = obtenerPrincipal(authentication);

		if (principal == null || principal.getIdDocente() == null) {
			flash.addFlashAttribute("msgError", "No se pudo identificar al asesor interno autenticado.");
			return "redirect:/dashboard";
		}

		AsesorInterno asesorInterno = serviceAsesorInterno.buscarPorIdDocente(principal.getIdDocente());

		if (asesorInterno == null) {
			flash.addFlashAttribute("msgError", "Tu usuario no está vinculado a un asesor interno activo.");
			return "redirect:/dashboard";
		}

		model.addAttribute("asesorInterno", asesorInterno);
		model.addAttribute("proyectos", serviceAsesorInterno.buscarProyectosAsignados(asesorInterno.getId()));

		return "asesoresInternos/proyectosAsesorInterno";
	}

	@GetMapping({
			"/asesoresInternos/create",
			"/asesores-internos/create"
	})
	public String crear(Model model) {
		model.addAttribute("asesorInterno", new AsesorInterno());
		model.addAttribute("docentes", serviceDocente.buscarTodosActivos());
		return "asesoresInternos/formAsesorInterno";
	}

	@PostMapping({
			"/asesoresInternos/save",
			"/asesores-internos/save"
	})
	public String guardar(
			@ModelAttribute AsesorInterno asesorInterno,
			Model model,
			RedirectAttributes flash) {

		try {
			serviceAsesorInterno.guardarAsesorInterno(asesorInterno);

			String usuarioGenerado = "Sin usuario";

			if (asesorInterno.getDocente() != null && asesorInterno.getDocente().getNoEmpleado() != null) {
				usuarioGenerado = asesorInterno.getDocente().getNoEmpleado().trim();
			}

			flash.addFlashAttribute("msgSuccess", "Asesor interno guardado correctamente.");
			flash.addFlashAttribute("usuarioGenerado", usuarioGenerado);
			flash.addFlashAttribute("passwordGenerado", "123");

			return "redirect:/asesoresInternos/index";

		} catch (RuntimeException e) {
			model.addAttribute("msgError", e.getMessage());
			model.addAttribute("asesorInterno", asesorInterno);
			model.addAttribute("docentes", serviceDocente.buscarTodosActivos());
			return "asesoresInternos/formAsesorInterno";
		}
	}

	@GetMapping({
			"/asesoresInternos/edit/{id}",
			"/asesores-internos/edit/{id}"
	})
	public String editar(
			@PathVariable("id") Integer idAsesorInterno,
			Model model,
			RedirectAttributes flash) {

		AsesorInterno asesorInterno = serviceAsesorInterno.buscarPorIdAsesorInterno(idAsesorInterno);

		if (asesorInterno == null) {
			flash.addFlashAttribute("msgError", "El asesor interno no existe o está inactivo.");
			return "redirect:/asesoresInternos/index";
		}

		model.addAttribute("asesorInterno", asesorInterno);
		model.addAttribute("docentes", serviceDocente.buscarTodosActivos());

		return "asesoresInternos/formAsesorInterno";
	}

	@PostMapping({
			"/asesoresInternos/update",
			"/asesores-internos/update"
	})
	public String actualizar(
			@ModelAttribute AsesorInterno asesorInterno,
			Model model,
			RedirectAttributes flash) {

		try {
			serviceAsesorInterno.guardarAsesorInterno(asesorInterno);
			flash.addFlashAttribute("msgSuccess", "Asesor interno actualizado correctamente.");
			return "redirect:/asesoresInternos/index";

		} catch (RuntimeException e) {
			model.addAttribute("msgError", e.getMessage());
			model.addAttribute("asesorInterno", asesorInterno);
			model.addAttribute("docentes", serviceDocente.buscarTodosActivos());
			return "asesoresInternos/formAsesorInterno";
		}
	}

	@GetMapping({
			"/asesoresInternos/delete/{id}",
			"/asesores-internos/delete/{id}"
	})
	public String eliminar(
			@PathVariable("id") Integer idAsesorInterno,
			RedirectAttributes flash) {

		serviceAsesorInterno.eliminar(idAsesorInterno);
		flash.addFlashAttribute("msgSuccess", "Asesor interno enviado a inactivos correctamente.");
		return "redirect:/asesoresInternos/index";
	}

	@GetMapping({
			"/asesoresInternos/inactivos",
			"/asesores-internos/inactivos"
	})
	public String mostrarInactivos(Model model) {
		model.addAttribute("asesoresInternos", serviceAsesorInterno.buscarTodosInactivos());
		return "asesoresInternos/recuperarAsesorInterno";
	}

	@GetMapping({
			"/asesoresInternos/recuperar/{id}",
			"/asesores-internos/recuperar/{id}"
	})
	public String recuperar(
			@PathVariable("id") Integer idAsesorInterno,
			RedirectAttributes flash) {

		serviceAsesorInterno.recuperar(idAsesorInterno);
		flash.addFlashAttribute("msgSuccess", "Asesor interno recuperado correctamente.");
		return "redirect:/asesoresInternos/inactivos";
	}

	@GetMapping({
			"/asesoresInternos/proyectos/{id}",
			"/asesores-internos/proyectos/{id}"
	})
	public String proyectosAsignados(
			@PathVariable("id") Integer idAsesorInterno,
			Authentication authentication,
			Model model,
			RedirectAttributes flash) {

		AsesorInterno asesorInterno = serviceAsesorInterno.buscarPorIdAsesorInterno(idAsesorInterno);

		if (asesorInterno == null) {
			flash.addFlashAttribute("msgError", "El asesor interno no existe o está inactivo.");
			return "redirect:/asesoresInternos/index";
		}

		if (esAsesorInterno(authentication) && !perteneceAlUsuario(asesorInterno, authentication)) {
			throw new AccessDeniedException("Solo puedes consultar tus propios proyectos asignados.");
		}

		List<Residencia> proyectos = serviceAsesorInterno.buscarProyectosAsignados(idAsesorInterno);

		model.addAttribute("asesorInterno", asesorInterno);
		model.addAttribute("proyectos", proyectos);

		return "asesoresInternos/proyectosAsesorInterno";
	}

	private boolean esAsesorInterno(Authentication authentication) {
		if (authentication == null || authentication.getAuthorities() == null) {
			return false;
		}

		return authentication.getAuthorities().stream()
				.anyMatch(authority -> "ROLE_ASESOR_INTERNO".equals(authority.getAuthority()));
	}

	private boolean perteneceAlUsuario(AsesorInterno asesorInterno, Authentication authentication) {
		UsuarioPrincipal principal = obtenerPrincipal(authentication);

		return principal != null
				&& principal.getIdDocente() != null
				&& asesorInterno != null
				&& asesorInterno.getDocente() != null
				&& asesorInterno.getDocente().getId() != null
				&& asesorInterno.getDocente().getId().equals(principal.getIdDocente());
	}

	private UsuarioPrincipal obtenerPrincipal(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioPrincipal principal)) {
			return null;
		}

		return principal;
	}

	private List<String> obtenerPeriodosDisponibles() {
		return serviceResidencia.buscarTodasActivas()
				.stream()
				.map(Residencia::getPeriodo)
				.filter(periodo -> periodo != null && !periodo.trim().isEmpty())
				.map(String::trim)
				.distinct()
				.sorted((a, b) -> b.compareToIgnoreCase(a))
				.collect(Collectors.toList());
	}
}
