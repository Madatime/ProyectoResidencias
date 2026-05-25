package itch.tsp.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import itch.tsp.model.Estudiante;
import itch.tsp.model.Carrera;
import itch.tsp.service.ICarreraService;
import itch.tsp.service.IEstudianteService;

@Controller
@RequestMapping("/estudiantes")
public class EstudianteController {

	@Autowired
	private IEstudianteService serviceEstudiante;

	@Autowired
	private ICarreraService serviceCarrera;

	@GetMapping("/index")
	public String listar(
			@RequestParam(name = "texto", required = false) String texto,
			Model model) {

		model.addAttribute("estudiantes", serviceEstudiante.buscarEstudiantes(texto));
		model.addAttribute("texto", texto);

		return "estudiantes/listaEstudiantes";
	}

	@GetMapping("/search")
	public String buscar(
			@RequestParam(name = "texto", required = false) String texto,
			Model model) {

		model.addAttribute("estudiantes", serviceEstudiante.buscarEstudiantes(texto));
		model.addAttribute("texto", texto);

		return "estudiantes/listaEstudiantes";
	}

	@GetMapping("/create")
	public String crear(
			@RequestParam(name = "matricula", required = false) String matricula,
			Model model) {
		Estudiante estudiante = new Estudiante();
		estudiante.setCarrera(new Carrera());
		if (matricula != null && !matricula.isBlank()) {
			estudiante.setMatricula(matricula.trim().toUpperCase());
		}
		model.addAttribute("estudiante", estudiante);
		model.addAttribute("carreras", serviceCarrera.buscarTodas());
		return "estudiantes/formEstudiante";
	}

	@GetMapping("/edit/{id}")
	public String editar(@PathVariable("id") Integer id, Model model) {
		Estudiante estudiante = serviceEstudiante.buscarPorIdEstudiante(id);

		if (estudiante == null) {
			return "redirect:/estudiantes/index";
		}

		if (estudiante.getCarrera() == null) {
			estudiante.setCarrera(new Carrera());
		}

		model.addAttribute("estudiante", estudiante);
		model.addAttribute("carreras", serviceCarrera.buscarTodas());
		return "estudiantes/formEstudiante";
	}

	@PostMapping("/save")
	public String guardar(
			Estudiante estudiante,
			Model model,
			RedirectAttributes flash) {

		try {
			serviceEstudiante.guardarEstudiante(estudiante);
			flash.addFlashAttribute("msgSuccess",
					estudiante.getId() == null
							? "Estudiante registrado correctamente."
							: "Estudiante actualizado correctamente.");
			return "redirect:/estudiantes/index";
		} catch (RuntimeException e) {
			model.addAttribute("msgError", e.getMessage());
			model.addAttribute("estudiante", estudiante);
			model.addAttribute("carreras", serviceCarrera.buscarTodas());
			return "estudiantes/formEstudiante";
		}
	}

	@GetMapping("/delete/{id}")
	public String eliminar(
			@PathVariable("id") Integer id,
			RedirectAttributes flash) {

		try {
			serviceEstudiante.eliminar(id);
			flash.addFlashAttribute("msgSuccess", "Estudiante eliminado correctamente.");
		} catch (RuntimeException e) {
			flash.addFlashAttribute("msgError", e.getMessage());
		}

		return "redirect:/estudiantes/index";
	}

	@GetMapping("/inactivos")
	public String mostrarInactivos(Model model) {
		model.addAttribute("estudiantes", serviceEstudiante.buscarTodosInactivos());
		return "estudiantes/recuperarEstudiantes";
	}

	@GetMapping("/recuperar/{id}")
	public String recuperar(
			@PathVariable("id") Integer id,
			RedirectAttributes flash) {

		try {
			serviceEstudiante.recuperar(id);
			flash.addFlashAttribute("msgSuccess", "Estudiante recuperado correctamente.");
		} catch (RuntimeException e) {
			flash.addFlashAttribute("msgError", e.getMessage());
		}

		return "redirect:/estudiantes/inactivos";
	}

	@GetMapping("/buscar-por-matricula")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> buscarPorMatricula(
			@RequestParam("matricula") String matricula) {

		Estudiante estudiante = serviceEstudiante.buscarPorMatricula(matricula);

		if (estudiante == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		Map<String, Object> respuesta = new LinkedHashMap<>();
		respuesta.put("id", estudiante.getId());
		respuesta.put("matricula", estudiante.getMatricula());
		respuesta.put("nombre", estudiante.getNombre());
		respuesta.put("apellidos", estudiante.getApellidos());
		respuesta.put("semestre", estudiante.getSemestre());
		respuesta.put("telefono", estudiante.getTelefono());
		respuesta.put("correo", estudiante.getCorreo());

		return ResponseEntity.ok(respuesta);
	}
}
