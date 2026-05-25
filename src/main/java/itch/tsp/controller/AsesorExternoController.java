package itch.tsp.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import itch.tsp.model.AsesorExterno;
import itch.tsp.model.Usuario;
import itch.tsp.repository.UsuarioRepository;
import itch.tsp.service.IEmpresaService;
import itch.tsp.service.IAsesorExternoService;

@Controller
public class AsesorExternoController {

	@Autowired
	private IAsesorExternoService serviceAsesorExterno;

	@Autowired
	private IEmpresaService serviceEmpresa;

	@Autowired
	private UsuarioRepository repoUsuario;

	@Value("${app.ruta.base}")
	private String rutaBase;

	@Value("${app.carpeta.asesores.externos}")
	private String carpetaAsesoresExternos;

	@GetMapping("/asesores-externos/index")
	public String mostrarIndex(
			@RequestParam(name = "texto", required = false) String texto,
			Model model) {

		List<AsesorExterno> asesoresExternos = serviceAsesorExterno.buscarAsesoresExternos(texto);

		model.addAttribute("asesoresExternos", asesoresExternos);
		model.addAttribute("textoBusqueda", texto);

		return "asesoresExternos/listaAsesorExterno";
	}

	@GetMapping("/asesores-externos/search")
	public String buscar(
			@RequestParam(name = "texto", required = false) String texto,
			Model model) {

		List<AsesorExterno> asesoresExternos = serviceAsesorExterno.buscarAsesoresExternos(texto);

		model.addAttribute("asesoresExternos", asesoresExternos);
		model.addAttribute("textoBusqueda", texto);

		return "asesoresExternos/listaAsesorExterno";
	}

	@GetMapping("/asesores-externos/create")
	public String crear(Model model) {

		model.addAttribute("asesorExterno", new AsesorExterno());
		model.addAttribute("empresasRegistradas", serviceEmpresa.buscarTodasActivas());

		return "asesoresExternos/formAsesorExterno";
	}

	@PostMapping("/asesores-externos/save")
	public String guardar(
			AsesorExterno asesorExterno,
			@RequestParam(value = "foto", required = false) MultipartFile foto,
			@RequestParam(value = "documento", required = false) MultipartFile documento,
			RedirectAttributes flash,
			Model model) {

		try {

			asesorExterno.setEstatus(1);

			serviceAsesorExterno.guardarAsesorExternoConArchivos(
					asesorExterno,
					foto,
					documento);

			Usuario usuarioCreado = repoUsuario.findByAsesorExterno_Id(asesorExterno.getId());
			String usuarioGenerado = usuarioCreado != null ? usuarioCreado.getUsername() : null;

			flash.addFlashAttribute(
					"msgSuccess",
					"Asesor externo guardado correctamente.");

			if (usuarioGenerado != null && !usuarioGenerado.trim().isEmpty()) {
				flash.addFlashAttribute(
						"usuarioGenerado",
						usuarioGenerado);

				flash.addFlashAttribute(
						"passwordGenerado",
						"123");
			}

			return "redirect:/asesores-externos/index";

		} catch (Exception e) {

			model.addAttribute(
					"msgError",
					"Error al guardar el asesor externo: " + e.getMessage());

			model.addAttribute(
					"asesorExterno",
					asesorExterno);
			model.addAttribute("empresasRegistradas", serviceEmpresa.buscarTodasActivas());

			return "asesoresExternos/formAsesorExterno";
		}
	}

	@GetMapping("/asesores-externos/edit/{id}")
	public String editar(
			@PathVariable("id") Integer idAsesorExterno,
			Model model) {

		AsesorExterno asesorExterno =
				serviceAsesorExterno.buscarPorIdAsesorExterno(idAsesorExterno);

		if (asesorExterno == null) {
			return "redirect:/asesores-externos/index";
		}

		model.addAttribute("asesorExterno", asesorExterno);
		model.addAttribute("empresasRegistradas", serviceEmpresa.buscarTodasActivas());

		return "asesoresExternos/formAsesorExterno";
	}

	@PostMapping("/asesores-externos/update")
	public String actualizar(
			AsesorExterno asesorExterno,
			@RequestParam(value = "foto", required = false) MultipartFile foto,
			@RequestParam(value = "documento", required = false) MultipartFile documento,
			RedirectAttributes flash,
			Model model) {

		try {

			serviceAsesorExterno.guardarAsesorExternoConArchivos(
					asesorExterno,
					foto,
					documento);

			flash.addFlashAttribute(
					"msgSuccess",
					"Asesor externo actualizado correctamente.");

			return "redirect:/asesores-externos/index";

		} catch (Exception e) {

			model.addAttribute(
					"msgError",
					"Error al actualizar el asesor externo: " + e.getMessage());

			model.addAttribute(
					"asesorExterno",
					asesorExterno);
			model.addAttribute("empresasRegistradas", serviceEmpresa.buscarTodasActivas());

			return "asesoresExternos/formAsesorExterno";
		}
	}

	@GetMapping("/asesores-externos/delete/{id}")
	public String eliminar(
			@PathVariable("id") Integer idAsesorExterno,
			RedirectAttributes flash) {

		serviceAsesorExterno.eliminar(idAsesorExterno);

		flash.addFlashAttribute(
				"msgSuccess",
				"Asesor externo eliminado correctamente.");

		return "redirect:/asesores-externos/index";
	}

	@GetMapping("/asesores-externos/inactivos")
	public String mostrarInactivos(Model model) {

		List<AsesorExterno> asesoresExternos =
				serviceAsesorExterno.buscarTodosInactivos();

		model.addAttribute(
				"asesoresExternos",
				asesoresExternos);

		return "asesoresExternos/recuperarAsesorExterno";
	}

	@GetMapping("/asesores-externos/recuperar/{id}")
	public String recuperar(
			@PathVariable("id") Integer idAsesorExterno,
			RedirectAttributes flash) {

		serviceAsesorExterno.recuperar(idAsesorExterno);

		flash.addFlashAttribute(
				"msgSuccess",
				"Asesor externo recuperado correctamente.");

		return "redirect:/asesores-externos/inactivos";
	}

	@GetMapping("/asesores-externos-archivos/{nombreArchivo:.+}")
	public ResponseEntity<Resource> mostrarFotoAsesorExterno(
			@PathVariable String nombreArchivo) {

		try {

			Path rutaArchivo = Paths.get(rutaBase)
					.resolve(carpetaAsesoresExternos)
					.resolve(nombreArchivo)
					.normalize()
					.toAbsolutePath();

			Resource recurso = new UrlResource(rutaArchivo.toUri());

			if (!recurso.exists() || !recurso.isReadable()) {
				return ResponseEntity.notFound().build();
			}

			String tipoContenido =
					Files.probeContentType(rutaArchivo);

			if (tipoContenido == null) {
				tipoContenido = "application/octet-stream";
			}

			return ResponseEntity.ok()
					.header("Content-Type", tipoContenido)
					.body(recurso);

		} catch (Exception e) {

			return ResponseEntity.notFound().build();
		}
	}
}
