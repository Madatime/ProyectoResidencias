package itch.tsp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import itch.tsp.model.Docente;
import itch.tsp.service.ICarreraService;
import itch.tsp.service.IDocenteService;

@Controller
public class DocenteController {

	@Autowired
	private IDocenteService serviceDocente;

	@Autowired
	private ICarreraService serviceCarrera;

	@GetMapping("/docentes/index")
	public String mostrarIndex(Model model) {
		model.addAttribute("docentes", serviceDocente.buscarTodosActivos());
		return "Docentes/listaDocentes";
	}

	@GetMapping("/docentes/create")
	public String crear(Model model) {
		model.addAttribute("docente", new Docente());
		model.addAttribute("carreras", serviceCarrera.buscarTodasActivas());
		return "Docentes/formDocente";
	}

	@PostMapping("/docentes/save")
	public String guardar(
			Docente docente,
			@RequestParam(name = "idsCarreras", required = false) List<Integer> idsCarreras,
			RedirectAttributes flash) {

		serviceDocente.guardar(docente, idsCarreras);
		flash.addFlashAttribute("msgSuccess", "Docente guardado correctamente.");

		return "redirect:/docentes/index";
	}

	@GetMapping("/docentes/edit/{id}")
	public String editar(@PathVariable("id") Integer id, Model model, RedirectAttributes flash) {
		Docente docente = serviceDocente.buscarPorId(id);

		if (docente == null) {
			flash.addFlashAttribute("msgError", "El docente no existe o está inactivo.");
			return "redirect:/docentes/index";
		}

		model.addAttribute("docente", docente);
		model.addAttribute("carreras", serviceCarrera.buscarTodosActivos());

		return "Docentes/formDocente";
	}

	@GetMapping("/docentes/delete/{id}")
	public String eliminar(@PathVariable("id") Integer id, RedirectAttributes flash) {
		serviceDocente.eliminar(id);
		flash.addFlashAttribute("msgSuccess", "Docente enviado a inactivos correctamente.");
		return "redirect:/docentes/index";
	}

	@GetMapping("/docentes/inactivos")
	public String mostrarInactivos(Model model) {
		model.addAttribute("docentes", serviceDocente.buscarTodosInactivos());
		return "Docentes/recuperarDocentes";
	}

	@GetMapping("/docentes/recuperar/{id}")
	public String recuperar(@PathVariable("id") Integer id, RedirectAttributes flash) {
		serviceDocente.recuperar(id);
		flash.addFlashAttribute("msgSuccess", "Docente recuperado correctamente.");
		return "redirect:/docentes/inactivos";
	}
}
