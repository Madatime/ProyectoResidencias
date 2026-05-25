package itch.tsp.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import itch.tsp.model.Residencia;
import itch.tsp.security.SeguridadResidenciaService;
import itch.tsp.service.IAsesorExternoService;
import itch.tsp.service.IAsesorInternoService;
import itch.tsp.service.IEmpresaService;
import itch.tsp.service.IResidenciaService;
import itch.tsp.service.IResidenteService;

@Controller
public class AuthController {

	@Autowired
	private IResidenteService serviceResidente;

	@Autowired
	private IResidenciaService serviceResidencia;

	@Autowired
	private IAsesorInternoService serviceAsesorInterno;

	@Autowired
	private IAsesorExternoService serviceAsesorExterno;

	@Autowired	
	private IEmpresaService serviceEmpresa;

	@Autowired
	private SeguridadResidenciaService seguridadResidenciaService;

	@GetMapping("/inicio")
	public String inicio() {
		return "redirect:/dashboard";
	}

	@GetMapping("/dashboard")
	public String dashboard(Authentication authentication, Model model) {

		List<Residencia> residencias = serviceResidencia.buscarTodasActivas();
		List<Residencia> residenciasVisibles =
				seguridadResidenciaService.filtrarResidenciasPermitidas(residencias, authentication);

		int enProceso = 0;
		int cerradas = 0;
		int autorizadas = 0;
		int pendientesAutorizacion = 0;
		int rechazadas = 0;

		for (Residencia r : residenciasVisibles) {
			String estatusProceso = r.getEstatusProceso() != null ? r.getEstatusProceso().trim().toUpperCase() : "";
			String estadoAutorizacion = r.getEstadoAutorizacion() != null ? r.getEstadoAutorizacion().trim().toUpperCase() : "";

			if ("CERRADO".equals(estatusProceso) || "FINALIZADO".equals(estatusProceso)) {
				cerradas++;
			} else {
				enProceso++;
			}

			if ("AUTORIZADO".equals(estadoAutorizacion)) {
				autorizadas++;
			} else if ("RECHAZADO".equals(estadoAutorizacion)) {
				rechazadas++;
			} else {
				pendientesAutorizacion++;
			}
		}

		List<Residencia> actividadReciente = new ArrayList<>();
		for (int i = 0; i < residenciasVisibles.size() && i < 5; i++) {
			actividadReciente.add(residenciasVisibles.get(i));
		}

		if (esVistaInstitucional(authentication)) {
			model.addAttribute("totalResidentes", serviceResidente.buscarTodosActivos().size());
			model.addAttribute("totalResidencias", residencias.size());
			model.addAttribute("totalAsesoresInternos", serviceAsesorInterno.buscarTodosActivos().size());
			model.addAttribute("totalAsesoresExternos", serviceAsesorExterno.buscarTodosActivos().size());
			model.addAttribute("totalEmpresas", serviceEmpresa.buscarTodasActivas().size());
		} else {
			model.addAttribute("totalResidentes", contarResidentesUnicos(residenciasVisibles));
			model.addAttribute("totalResidencias", residenciasVisibles.size());
			model.addAttribute("totalAsesoresInternos", contarAsesoresInternosUnicos(residenciasVisibles));
			model.addAttribute("totalAsesoresExternos", contarAsesoresExternosUnicos(residenciasVisibles));
			model.addAttribute("totalEmpresas", contarEmpresasUnicas(residenciasVisibles));
		}

		model.addAttribute("enProceso", enProceso);
		model.addAttribute("cerradas", cerradas);
		model.addAttribute("autorizadas", autorizadas);
		model.addAttribute("pendientesAutorizacion", pendientesAutorizacion);
		model.addAttribute("rechazadas", rechazadas);

		model.addAttribute("actividadReciente", actividadReciente);

		return "dashboard/index";
	}

	private boolean esVistaInstitucional(Authentication authentication) {
		return tieneRol(authentication, "ADMINISTRADOR")
				|| tieneRol(authentication, "DIVISION_ESTUDIOS")
				|| tieneRol(authentication, "SERVICIOS_ESCOLARES");
	}

	private boolean tieneRol(Authentication authentication, String rol) {
		if (authentication == null || authentication.getAuthorities() == null) {
			return false;
		}

		String rolSpring = "ROLE_" + rol;

		return authentication.getAuthorities().stream()
				.anyMatch(authority -> rolSpring.equals(authority.getAuthority()));
	}

	private int contarResidentesUnicos(List<Residencia> residencias) {
		Set<Integer> ids = residencias.stream()
				.filter(residencia -> residencia.getResidente() != null && residencia.getResidente().getId() != null)
				.map(residencia -> residencia.getResidente().getId())
				.collect(Collectors.toSet());

		return ids.size();
	}

	private int contarAsesoresInternosUnicos(List<Residencia> residencias) {
		Set<Integer> ids = residencias.stream()
				.filter(residencia -> residencia.getAsesorInterno() != null && residencia.getAsesorInterno().getId() != null)
				.map(residencia -> residencia.getAsesorInterno().getId())
				.collect(Collectors.toSet());

		return ids.size();
	}

	private int contarAsesoresExternosUnicos(List<Residencia> residencias) {
		Set<Integer> ids = residencias.stream()
				.filter(residencia -> residencia.getAsesorExterno() != null && residencia.getAsesorExterno().getId() != null)
				.map(residencia -> residencia.getAsesorExterno().getId())
				.collect(Collectors.toSet());

		return ids.size();
	}

	private int contarEmpresasUnicas(List<Residencia> residencias) {
		Set<Integer> ids = residencias.stream()
				.filter(residencia -> residencia.getEmpresa() != null && residencia.getEmpresa().getId() != null)
				.map(residencia -> residencia.getEmpresa().getId())
				.collect(Collectors.toSet());

		return ids.size();
	}
}
