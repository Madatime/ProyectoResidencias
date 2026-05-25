package itch.tsp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import itch.tsp.model.Directivo;
import itch.tsp.model.TipoDirectivo;
import itch.tsp.service.ICarreraService;
import itch.tsp.service.IDirectivoService;
import itch.tsp.service.IDocenteService;

@Controller
public class DirectivoController {

	@Autowired
	private IDirectivoService serviceDirectivo;

	@Autowired
	private IDocenteService serviceDocente;

	@Autowired
	private ICarreraService serviceCarrera;

	@GetMapping("/directivos/index")
	public String mostrarIndex(Model model) {
		model.addAttribute("directivos", serviceDirectivo.buscarTodosActivos());
		return "directivos/listaDirectivos";
	}

	@GetMapping("/directivos/create")
	public String crear(Model model) {
		model.addAttribute("directivo", new Directivo());
		cargarCatalogos(model);
		return "directivos/formDirectivo";
	}

	@PostMapping("/directivos/save")
	public String guardar(
			@ModelAttribute Directivo directivo,
			Model model,
			RedirectAttributes flash) {

		try {
			serviceDirectivo.guardar(directivo);

			String usuarioGenerado = "Sin usuario";

			if (directivo.getDocente() != null && directivo.getDocente().getId() != null) {
				Integer idDocente = directivo.getDocente().getId();

				for (var docente : serviceDocente.buscarTodosActivos()) {
					if (docente.getId() != null && docente.getId().equals(idDocente)) {
						usuarioGenerado = docente.getNoEmpleado();
						break;
					}
				}
			}

			flash.addFlashAttribute("msgSuccess", "Directivo guardado correctamente.");
			flash.addFlashAttribute("usuarioGenerado", usuarioGenerado);
			flash.addFlashAttribute("passwordGenerado", "123");

			return "redirect:/directivos/index";

		} catch (RuntimeException e) {
			model.addAttribute("msgError", e.getMessage());
			model.addAttribute("directivo", directivo);
			cargarCatalogos(model);
			return "directivos/formDirectivo";
		}
	}

	@GetMapping("/directivos/edit/{id}")
	public String editar(@PathVariable("id") Integer id, Model model, RedirectAttributes flash) {
		Directivo directivo = serviceDirectivo.buscarPorId(id);

		if (directivo == null) {
			flash.addFlashAttribute("msgError", "El directivo no existe o está inactivo.");
			return "redirect:/directivos/index";
		}

		model.addAttribute("directivo", directivo);
		cargarCatalogos(model);

		return "directivos/formDirectivo";
	}

	@PostMapping("/directivos/update")
	public String actualizar(
			@ModelAttribute Directivo directivo,
			Model model,
			RedirectAttributes flash) {

		try {
			serviceDirectivo.guardar(directivo);
			flash.addFlashAttribute("msgSuccess", "Directivo actualizado correctamente.");
			return "redirect:/directivos/index";

		} catch (RuntimeException e) {
			model.addAttribute("msgError", e.getMessage());
			model.addAttribute("directivo", directivo);
			cargarCatalogos(model);
			return "directivos/formDirectivo";
		}
	}

	@GetMapping("/directivos/delete/{id}")
	public String eliminar(@PathVariable("id") Integer id, RedirectAttributes flash) {
		serviceDirectivo.eliminar(id);
		flash.addFlashAttribute("msgSuccess", "Directivo enviado a inactivos correctamente.");
		return "redirect:/directivos/index";
	}

	@GetMapping("/directivos/inactivos")
	public String mostrarInactivos(Model model) {
		model.addAttribute("directivos", serviceDirectivo.buscarTodosInactivos());
		return "directivos/recuperarDirectivos";
	}

	@GetMapping("/directivos/recuperar/{id}")
	public String recuperar(@PathVariable("id") Integer id, RedirectAttributes flash) {
		serviceDirectivo.recuperar(id);
		flash.addFlashAttribute("msgSuccess", "Directivo recuperado correctamente.");
		return "redirect:/directivos/inactivos";
	}

	private void cargarCatalogos(Model model) {
		model.addAttribute("docentes", serviceDocente.buscarTodosActivos());
		model.addAttribute("carreras", serviceCarrera.buscarTodasActivas());
		model.addAttribute("tiposDirectivo", TipoDirectivo.values());
	}
}
