package itch.tsp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import itch.tsp.model.Carrera;
import itch.tsp.service.ICarreraService;

@Controller
@RequestMapping("/carreras")
public class CarreraController {

	@Autowired
	private ICarreraService service;

	@GetMapping("/index")
	public String listar(Model model) {
		model.addAttribute("carreras", service.buscarTodas());
		return "carreras/listaCarreras";
	}

	@GetMapping("/create")
	public String crear(Model model) {
		model.addAttribute("carrera", new Carrera());
		return "carreras/formCarrera";
	}

	@PostMapping("/save")
	public String guardar(Carrera carrera) {
		service.guardar(carrera);
		return "redirect:/carreras/index";
	}

	@GetMapping("/edit/{id}")
	public String editar(@PathVariable Integer id, Model model) {
		model.addAttribute("carrera", service.buscarPorId(id));
		return "carreras/formCarrera";
	}

	@GetMapping("/delete/{id}")
	public String eliminar(@PathVariable Integer id) {
		service.eliminar(id);
		return "redirect:/carreras/index";
	}
}