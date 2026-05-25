package itch.tsp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import itch.tsp.model.BancoProyecto;
import itch.tsp.model.Residencia;
import itch.tsp.service.IBancoProyectoService;
import itch.tsp.service.IResidenciaService;

@Controller
public class PortalPublicoController {

	@Autowired
	private IResidenciaService serviceResidencia;

	@Autowired
	private IBancoProyectoService serviceBancoProyecto;

	@GetMapping("/")
	public String inicioPublico(Model model) {
		List<Residencia> residencias = serviceResidencia.buscarTodasActivas();
		cargarDatosPortal(model, residencias);
		return "publico/index";
	}

	@GetMapping("/residencias-publicas/buscar")
	public String buscarResidenciasPublicas(
			@RequestParam(name = "texto", required = false) String texto,
			@RequestParam(name = "periodo", required = false) String periodo,
			Model model) {

		List<Residencia> residencias = serviceResidencia.buscarResidenciasPorPeriodoYTexto(periodo, texto);

		cargarDatosPortal(model, residencias);
		model.addAttribute("textoBusqueda", texto);
		model.addAttribute("periodoBusqueda", periodo);

		return "publico/index";
	}
		
	@GetMapping("/residencias-publicas/detalle/{id}")
	public String detalleResidenciaPublica(
			@PathVariable("id") Integer id,
			Model model) {

		Residencia residencia = serviceResidencia.buscarPorIdResidencia(id);

		if (residencia == null) {
			return "redirect:/";
		}

		model.addAttribute("residencia", residencia);

		return "publico/detalleResidencia";
	}

	private void cargarDatosPortal(Model model, List<Residencia> residencias) {
		List<BancoProyecto> proyectosBanco = serviceBancoProyecto.buscarDisponibles();
		model.addAttribute("residencias", residencias);
		model.addAttribute("proyectosBanco", proyectosBanco);
	}
}
