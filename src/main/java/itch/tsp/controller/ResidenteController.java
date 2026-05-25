package itch.tsp.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import itch.tsp.model.Estudiante;
import itch.tsp.model.Residente;
import itch.tsp.service.IEstudianteService;
import itch.tsp.service.IResidenteService;

@Controller
public class ResidenteController {

	@Autowired
	private IResidenteService serviceResidente;

	@Autowired
	private IEstudianteService serviceEstudiante;

	private void prepararAltaEstudianteSugerida(Model model, String matricula) {
		model.addAttribute("mostrarCrearEstudiante", true);
		model.addAttribute("matriculaSugerida", matricula);
	}

	@GetMapping("/residentes/index")
	public String mostrarIndex(
			@RequestParam(name = "texto", required = false) String texto,
			@RequestParam(name = "filtroProyectoResidencia", required = false) String filtroProyectoResidencia,
			Model model) {

		cargarBandejaEstudiantes(model, texto, filtroProyectoResidencia);
		return "residentes/listaResidentes";
	}

	@GetMapping("/residentes/search")
	public String buscar(
			@RequestParam(name = "texto", required = false) String texto,
			@RequestParam(name = "filtroProyectoResidencia", required = false) String filtroProyectoResidencia,
			Model model) {

		cargarBandejaEstudiantes(model, texto, filtroProyectoResidencia);
		return "residentes/listaResidentes";
	}

	@GetMapping("/residentes/create")
	public String crear(
			@RequestParam(name = "matricula", required = false) String matricula,
			Model model) {

		Residente residente = new Residente();

		if (matricula != null && !matricula.trim().isEmpty()) {

			Estudiante estudiante = serviceEstudiante.buscarPorMatricula(matricula);

			if (estudiante != null) {
				residente.setEstudiante(estudiante);
			}
		}

		model.addAttribute("residente", residente);

		return "residentes/formResidente";
	}

	@PostMapping("/residentes/save")
	public String guardar(
			@RequestParam("matricula") String matricula,
			@RequestParam("nombre") String nombre,
			@RequestParam("apellidos") String apellidos,
			@RequestParam("semestre") String semestre,
			@RequestParam("telefono") String telefono,
			@RequestParam("correo") String correo,
			@RequestParam(value = "foto", required = false) MultipartFile foto,
			RedirectAttributes flash,
			Model model) {

		Residente residente = new Residente();

		try {

			String matriculaNormalizada = serviceResidente.normalizarMatricula(matricula);

			if (!serviceResidente.matriculaValida(matriculaNormalizada)) {

				model.addAttribute("msgError",
						"La matrícula debe tener máximo 8 números y opcionalmente una C al inicio.");

				model.addAttribute("residente", residente);

				return "residentes/formResidente";
			}

			Estudiante estudiante = serviceEstudiante.buscarPorMatricula(matriculaNormalizada);

			if (estudiante == null) {

				model.addAttribute("msgError",
						"No existe un estudiante activo con esa matrícula.");

				prepararAltaEstudianteSugerida(model, matriculaNormalizada);
				model.addAttribute("residente", residente);

				return "residentes/formResidente";
			}

			actualizarDatosEstudiante(
					estudiante,
					nombre,
					apellidos,
					semestre,
					telefono,
					correo);

			serviceEstudiante.guardarEstudiante(estudiante);

			if (serviceResidente.existeMatricula(matriculaNormalizada)) {

				model.addAttribute("msgError",
						"Ese estudiante ya está registrado como residente.");

				residente.setEstudiante(estudiante);

				prepararAltaEstudianteSugerida(model, matriculaNormalizada);
				model.addAttribute("residente", residente);

				return "residentes/formResidente";
			}

			residente.setEstudiante(estudiante);
			residente.setEstatus(1);

			serviceResidente.guardarResidenteConArchivos(residente, foto, null);

			String usuarioGenerado = estudiante.getMatricula();

			flash.addFlashAttribute("msgSuccess",
					"Residente guardado correctamente.");

			flash.addFlashAttribute("usuarioGenerado",
					usuarioGenerado);

			flash.addFlashAttribute("passwordGenerado",
					"123");

			return "redirect:/residentes/index";

		} catch (Exception e) {

			model.addAttribute("msgError",
					"Error al guardar el residente: " + e.getMessage());

			model.addAttribute("residente", residente);

			return "residentes/formResidente";
		}
	}

	@GetMapping("/residentes/edit/{id}")
	public String editar(@PathVariable("id") Integer idResidente,
			Model model) {

		Residente residente = serviceResidente.buscarPorIdResidente(idResidente);

		if (residente != null) {

			model.addAttribute("residente", residente);

			return "residentes/formResidente";
		}

		return "redirect:/residentes/index";
	}

	@PostMapping("/residentes/update")
	public String actualizar(
			@RequestParam("id") Integer id,

			@RequestParam("matricula") String matricula,

			@RequestParam("nombre") String nombre,

			@RequestParam("apellidos") String apellidos,

			@RequestParam("semestre") String semestre,

			@RequestParam("telefono") String telefono,

			@RequestParam("correo") String correo,

			@RequestParam(value = "foto", required = false) MultipartFile foto,

			RedirectAttributes flash,
			Model model) {

		Residente residente = serviceResidente.buscarPorIdResidente(id);

		try {

			if (residente == null) {

				flash.addFlashAttribute("msgError",
						"El residente no existe.");

				return "redirect:/residentes/index";
			}

			String matriculaNormalizada =
					serviceResidente.normalizarMatricula(matricula);

			if (!serviceResidente.matriculaValida(matriculaNormalizada)) {

				model.addAttribute("msgError",
						"La matrícula debe tener máximo 8 números y opcionalmente una C al inicio.");

				model.addAttribute("residente", residente);

				return "residentes/formResidente";
			}

			Estudiante estudiante =
					serviceEstudiante.buscarPorMatricula(matriculaNormalizada);

			if (estudiante == null) {

				model.addAttribute("msgError",
						"No existe un estudiante activo con esa matrícula.");

				prepararAltaEstudianteSugerida(model, matriculaNormalizada);
				model.addAttribute("residente", residente);

				return "residentes/formResidente";
			}

			if (serviceResidente.existeMatriculaParaOtroRegistro(
					matriculaNormalizada,
					id)) {

				model.addAttribute("msgError",
						"Ese estudiante ya está registrado en otro residente.");

				model.addAttribute("residente", residente);

				return "residentes/formResidente";
			}

			actualizarDatosEstudiante(
					estudiante,
					nombre,
					apellidos,
					semestre,
					telefono,
					correo);

			serviceEstudiante.guardarEstudiante(estudiante);

			residente.setEstudiante(estudiante);

			serviceResidente.guardarResidenteConArchivos(
					residente,
					foto,
					null);

			flash.addFlashAttribute("msgSuccess",
					"Residente actualizado correctamente.");

			return "redirect:/residentes/index";

		} catch (Exception e) {

			model.addAttribute("msgError",
					"Error al actualizar el residente: " + e.getMessage());

			model.addAttribute("residente", residente);

			return "residentes/formResidente";
		}
	}

	@GetMapping("/residentes/delete/{id}")
	public String eliminar(@PathVariable("id") Integer idResidente) {

		serviceResidente.eliminar(idResidente);

		return "redirect:/residentes/index";
	}

	private void cargarBandejaEstudiantes(
			Model model,
			String texto,
			String filtroProyectoResidencia) {

		List<Estudiante> estudiantes =
				serviceEstudiante.buscarEstudiantesParaResidencia(texto, filtroProyectoResidencia);

		List<Residente> residentesActivos =
				serviceResidente.buscarTodosActivos();

		Map<Integer, Residente> mapaResidencias =
				new LinkedHashMap<>();

		for (Estudiante estudiante : estudiantes) {

			Residente residente = null;

			if (estudiante.getId() != null) {

				residente = residentesActivos.stream()
						.filter(r ->
								r.getEstudiante() != null
								&& r.getEstudiante().getId() != null
								&& r.getEstudiante().getId().equals(estudiante.getId()))
						.findFirst()
						.orElse(null);
			}

			if (residente != null) {
				mapaResidencias.put(estudiante.getId(), residente);
			}
		}

		model.addAttribute("estudiantes", estudiantes);
		model.addAttribute("mapaResidencias", mapaResidencias);
		model.addAttribute("textoBusqueda", texto);
		model.addAttribute("filtroProyectoResidencia", filtroProyectoResidencia);
	}

	@GetMapping("/residentes/inactivos")
	public String mostrarInactivos(Model model) {

		List<Residente> residentes =
				serviceResidente.buscarTodosInactivos();

		model.addAttribute("residentes", residentes);

		return "residentes/recuperarResidentes";
	}

	@GetMapping("/residentes/recuperar/{id}")
	public String recuperar(
			@PathVariable("id") Integer idResidente,
			RedirectAttributes flash) {

		serviceResidente.recuperar(idResidente);

		flash.addFlashAttribute("msgSuccess",
				"Residente recuperado correctamente.");

		return "redirect:/residentes/inactivos";
	}

	private void actualizarDatosEstudiante(
			Estudiante estudiante,
			String nombre,
			String apellidos,
			String semestre,
			String telefono,
			String correo) {

		String nombreNormalizado = nombre != null ? nombre.trim().replaceAll("\\s+", " ") : "";
		String apellidosNormalizados = apellidos != null ? apellidos.trim().replaceAll("\\s+", " ") : "";
		String telefonoNormalizado = telefono != null ? telefono.trim() : "";
		String correoNormalizado = correo != null ? correo.trim() : "";

		if (nombreNormalizado.isEmpty() || !nombreNormalizado.matches("[A-Za-zÁÉÍÓÚáéíóúÑñ ]+")) {
			throw new RuntimeException("El nombre solo debe contener letras y espacios.");
		}

		if (apellidosNormalizados.isEmpty() || !apellidosNormalizados.matches("[A-Za-zÁÉÍÓÚáéíóúÑñ ]+")) {
			throw new RuntimeException("Los apellidos solo deben contener letras y espacios.");
		}

		if (!telefonoNormalizado.matches("\\d{10}")) {
			throw new RuntimeException("El telefono debe tener exactamente 10 digitos.");
		}

		int semestreNumerico;

		try {
			semestreNumerico = Integer.parseInt(semestre != null ? semestre.trim() : "");
		} catch (NumberFormatException e) {
			throw new RuntimeException("El semestre debe ser un numero entero.");
		}

		if (semestreNumerico < 8) {
			throw new RuntimeException("El semestre minimo permitido es 8.");
		}

		if (semestreNumerico > 13) {
			throw new RuntimeException("El semestre maximo permitido es 13.");
		}

		estudiante.setNombre(nombreNormalizado);
		estudiante.setApellidos(apellidosNormalizados);
		estudiante.setSemestre(String.valueOf(semestreNumerico));
		estudiante.setTelefono(telefonoNormalizado);
		estudiante.setCorreo(correoNormalizado);
	}
}
