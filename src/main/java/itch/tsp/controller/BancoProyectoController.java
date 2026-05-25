package itch.tsp.controller;

import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import itch.tsp.model.BancoProyecto;
import itch.tsp.model.Carrera;
import itch.tsp.model.Empresa;
import itch.tsp.model.EstadoBancoProyecto;
import itch.tsp.service.IBancoProyectoService;
import itch.tsp.service.ICarreraService;
import itch.tsp.service.IEmpresaService;

@Controller
public class BancoProyectoController {

	@Autowired
	private IBancoProyectoService serviceBancoProyecto;

	@Autowired
	private IEmpresaService serviceEmpresa;

	@Autowired
	private ICarreraService serviceCarrera;

	@GetMapping("/banco-proyectos/index")
	public String index(Model model) {
		model.addAttribute("proyectos", serviceBancoProyecto.buscarTodosActivos());
		model.addAttribute("estados", EstadoBancoProyecto.values());
		model.addAttribute("estadoBusqueda", null);
		return "bancoProyectos/listaBancoProyectos";
	}

	@GetMapping("/banco-proyectos/search")
	public String buscar(
			@RequestParam(name = "texto", required = false) String texto,
			@RequestParam(name = "estado", required = false) EstadoBancoProyecto estado,
			Model model) {

		List<BancoProyecto> proyectos = serviceBancoProyecto.buscarPorTexto(texto);

		if (estado != null) {
			proyectos = proyectos.stream()
					.filter(proyecto -> proyecto.getEstado() == estado)
					.collect(Collectors.toList());
		}

		model.addAttribute("proyectos", proyectos);
		model.addAttribute("textoBusqueda", texto);
		model.addAttribute("estadoBusqueda", estado != null ? estado.name() : null);
		model.addAttribute("estados", EstadoBancoProyecto.values());

		return "bancoProyectos/listaBancoProyectos";
	}

	@GetMapping("/banco-proyectos/create")
	public String crear(Model model) {
		BancoProyecto proyecto = new BancoProyecto();
		proyecto.setEmpresa(new Empresa());
		proyecto.setCarrera(new Carrera());
		proyecto.setPeriodo(obtenerPeriodoActual());
		proyecto.setEstado(EstadoBancoProyecto.DISPONIBLE);

		model.addAttribute("proyecto", proyecto);
		model.addAttribute("periodoActual", obtenerPeriodoActual());
		cargarCatalogos(model);

		return "bancoProyectos/formBancoProyecto";
	}

	@PostMapping("/banco-proyectos/save")
	public String guardar(
	        @ModelAttribute BancoProyecto proyecto,
	        Model model,
	        RedirectAttributes flash) {

	    try {
	        proyecto.setPeriodo(obtenerPeriodoActual());

	        serviceBancoProyecto.guardar(proyecto);

	        flash.addFlashAttribute("msgSuccess", "Proyecto guardado correctamente.");
	        return "redirect:/banco-proyectos/index";

	    } catch (RuntimeException e) {
	        model.addAttribute("msgError", e.getMessage());
	        model.addAttribute("proyecto", proyecto);
	        model.addAttribute("periodoActual", obtenerPeriodoActual());
	        cargarCatalogos(model);
	        return "bancoProyectos/formBancoProyecto";
	    }
	}

	@GetMapping("/banco-proyectos/edit/{id}")
	public String editar(@PathVariable("id") Integer id, Model model, RedirectAttributes flash) {
		BancoProyecto proyecto = serviceBancoProyecto.buscarPorId(id);

		if (proyecto == null) {
			flash.addFlashAttribute("msgError", "El proyecto no existe o está inactivo.");
			return "redirect:/banco-proyectos/index";
		}

		if (proyecto.getEmpresa() == null) {
			proyecto.setEmpresa(new Empresa());
		}

		if (proyecto.getCarrera() == null) {
			proyecto.setCarrera(new Carrera());
		}

		model.addAttribute("proyecto", proyecto);
		model.addAttribute("periodoActual",
				proyecto.getPeriodo() != null ? proyecto.getPeriodo() : obtenerPeriodoActual());

		cargarCatalogos(model);

		return "bancoProyectos/formBancoProyecto";
	}

	@PostMapping("/banco-proyectos/update")
	public String actualizar(
	        @ModelAttribute BancoProyecto proyecto,
	        Model model,
	        RedirectAttributes flash) {

	    try {
	        if (proyecto.getPeriodo() == null || proyecto.getPeriodo().trim().isEmpty()) {
	            proyecto.setPeriodo(obtenerPeriodoActual());
	        }

	        serviceBancoProyecto.guardar(proyecto);

	        flash.addFlashAttribute("msgSuccess", "Proyecto actualizado correctamente.");
	        return "redirect:/banco-proyectos/index";

	    } catch (RuntimeException e) {
	        model.addAttribute("msgError", e.getMessage());
	        model.addAttribute("proyecto", proyecto);
	        model.addAttribute("periodoActual", obtenerPeriodoActual());
	        cargarCatalogos(model);
	        return "bancoProyectos/formBancoProyecto";
	    }
	}

	@GetMapping("/banco-proyectos/pendientes")
	public String pendientes(Model model) {
		model.addAttribute("proyectos", serviceBancoProyecto.buscarPendientesRevision());
		return "bancoProyectos/propuestasPendientes";
	}

	@GetMapping("/banco-proyectos/revisar/{id}")
	public String revisar(@PathVariable("id") Integer id, Model model, RedirectAttributes flash) {
		BancoProyecto proyecto = serviceBancoProyecto.buscarPorId(id);

		if (proyecto == null) {
			flash.addFlashAttribute("msgError", "La propuesta no existe o está inactiva.");
			return "redirect:/banco-proyectos/pendientes";
		}

		model.addAttribute("proyecto", proyecto);
		model.addAttribute("estadosRevision", new EstadoBancoProyecto[] {
				EstadoBancoProyecto.DISPONIBLE,
				EstadoBancoProyecto.RECHAZADO
		});

		return "bancoProyectos/revisarPropuesta";
	}

	@PostMapping("/banco-proyectos/revisar")
	public String guardarRevision(
			@RequestParam("idProyecto") Integer idProyecto,
			@RequestParam("estado") EstadoBancoProyecto estado,
			@RequestParam(name = "observaciones", required = false) String observaciones,
			RedirectAttributes flash) {

		try {
			serviceBancoProyecto.revisarProyecto(idProyecto, estado, observaciones);
			flash.addFlashAttribute("msgSuccess", "Propuesta revisada correctamente.");
		} catch (RuntimeException e) {
			flash.addFlashAttribute("msgError", e.getMessage());
		}

		return "redirect:/banco-proyectos/pendientes";
	}

	@GetMapping("/banco-proyectos/delete/{id}")
	public String eliminar(@PathVariable("id") Integer id, RedirectAttributes flash) {
		serviceBancoProyecto.eliminar(id);
		flash.addFlashAttribute("msgSuccess", "Proyecto enviado a inactivos correctamente.");
		return "redirect:/banco-proyectos/index";
	}

	@GetMapping("/banco-proyectos/inactivos")
	public String inactivos(Model model) {
		model.addAttribute("proyectos", serviceBancoProyecto.buscarTodosInactivos());
		return "bancoProyectos/recuperarBancoProyectos";
	}

	@GetMapping("/banco-proyectos/recuperar/{id}")
	public String recuperar(@PathVariable("id") Integer id, RedirectAttributes flash) {
		serviceBancoProyecto.recuperar(id);
		flash.addFlashAttribute("msgSuccess", "Proyecto recuperado correctamente.");
		return "redirect:/banco-proyectos/inactivos";
	}

	private void cargarCatalogos(Model model) {
		model.addAttribute("empresas", serviceEmpresa.buscarTodasActivas());
		model.addAttribute("carreras", serviceCarrera.buscarTodas());
		model.addAttribute("estados", EstadoBancoProyecto.values());
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
