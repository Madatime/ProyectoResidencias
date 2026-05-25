package itch.tsp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import itch.tsp.model.Empresa;
import itch.tsp.service.IEmpresaService;

@Controller
public class EmpresaController {

	@Autowired
	private IEmpresaService serviceEmpresa;

	@GetMapping("/empresas/index")
	public String mostrarIndex(
			@RequestParam(name = "texto", required = false) String texto,
			Model model) {

		List<Empresa> empresas = serviceEmpresa.buscarEmpresas(texto);
		model.addAttribute("empresas", empresas);
		model.addAttribute("textoBusqueda", texto);

		return "empresas/listaEmpresas";
	}

	@GetMapping("/empresas/search")
	public String buscar(
			@RequestParam(name = "texto", required = false) String texto,
			Model model) {

		List<Empresa> empresas = serviceEmpresa.buscarEmpresas(texto);
		model.addAttribute("empresas", empresas);
		model.addAttribute("textoBusqueda", texto);

		return "empresas/listaEmpresas";
	}

	@GetMapping("/empresas/create")
	public String crear(Model model) {
		model.addAttribute("empresa", new Empresa());
		return "empresas/formEmpresa";
	}

	@PostMapping("/empresas/save")
	public String guardar(Empresa empresa, RedirectAttributes flash, Model model) {

		String error = validarEmpresa(empresa, false);

		if (error != null) {
			model.addAttribute("msgError", error);
			model.addAttribute("empresa", empresa);
			return "empresas/formEmpresa";
		}

		String nombreNormalizado = serviceEmpresa.normalizarNombre(empresa.getNombre());

		if (serviceEmpresa.existeNombre(nombreNormalizado)) {
			model.addAttribute("msgError", "Ya existe una empresa activa con ese nombre.");
			model.addAttribute("empresa", empresa);
			return "empresas/formEmpresa";
		}

		empresa.setNombre(nombreNormalizado);
		empresa.setEstatus(1);

		serviceEmpresa.guardarEmpresa(empresa);

		flash.addFlashAttribute("msgSuccess", "Empresa guardada correctamente.");
		return "redirect:/empresas/index";
	}

	@GetMapping("/empresas/edit/{id}")
	public String editar(@PathVariable("id") Integer idEmpresa, Model model) {
		Empresa empresa = serviceEmpresa.buscarPorIdEmpresa(idEmpresa);

		if (empresa == null) {
			return "redirect:/empresas/index";
		}

		model.addAttribute("empresa", empresa);
		return "empresas/formEmpresa";
	}

	@PostMapping("/empresas/update")
	public String actualizar(Empresa empresa, RedirectAttributes flash, Model model) {

		String error = validarEmpresa(empresa, true);

		if (error != null) {
			model.addAttribute("msgError", error);
			model.addAttribute("empresa", empresa);
			return "empresas/formEmpresa";
		}

		String nombreNormalizado = serviceEmpresa.normalizarNombre(empresa.getNombre());

		if (serviceEmpresa.existeNombreParaOtroRegistro(nombreNormalizado, empresa.getId())) {
			model.addAttribute("msgError", "Ya existe otra empresa activa con ese nombre.");
			model.addAttribute("empresa", empresa);
			return "empresas/formEmpresa";
		}

		empresa.setNombre(nombreNormalizado);

		serviceEmpresa.guardarEmpresa(empresa);

		flash.addFlashAttribute("msgSuccess", "Empresa actualizada correctamente.");
		return "redirect:/empresas/index";
	}

	@GetMapping("/empresas/delete/{id}")
	public String eliminar(@PathVariable("id") Integer idEmpresa, RedirectAttributes flash) {
		serviceEmpresa.eliminar(idEmpresa);
		flash.addFlashAttribute("msgSuccess", "Empresa eliminada correctamente.");
		return "redirect:/empresas/index";
	}

	@GetMapping("/empresas/inactivos")
	public String mostrarInactivos(Model model) {
		List<Empresa> empresas = serviceEmpresa.buscarTodasInactivas();
		model.addAttribute("empresas", empresas);
		return "empresas/recuperarEmpresas";
	}

	@GetMapping("/empresas/recuperar/{id}")
	public String recuperar(@PathVariable("id") Integer idEmpresa, RedirectAttributes flash) {
		serviceEmpresa.recuperar(idEmpresa);
		flash.addFlashAttribute("msgSuccess", "Empresa recuperada correctamente.");
		return "redirect:/empresas/inactivos";
	}

	private String validarEmpresa(Empresa empresa, boolean esActualizacion) {

		if (esActualizacion && empresa.getId() == null) {
			return "No se encontró el ID de la empresa.";
		}

		if (estaVacio(empresa.getNombre())) {
			return "El nombre de la empresa es obligatorio.";
		}

		if (!soloLetrasEspacios(empresa.getNombre())) {
			return "El nombre de la empresa solo debe contener letras y espacios.";
		}

		if (!estaVacio(empresa.getGiro()) && !soloLetrasEspacios(empresa.getGiro())) {
			return "El giro solo debe contener letras y espacios.";
		}

		if (!estaVacio(empresa.getDueno()) && !soloLetrasEspacios(empresa.getDueno())) {
			return "El dueño solo debe contener letras y espacios.";
		}

		if (!estaVacio(empresa.getRepresentante()) && !soloLetrasEspacios(empresa.getRepresentante())) {
			return "El representante solo debe contener letras y espacios.";
		}

		if (!estaVacio(empresa.getPuestoRepresentante()) && !soloLetrasEspacios(empresa.getPuestoRepresentante())) {
			return "El puesto del representante solo debe contener letras y espacios.";
		}

		if (!estaVacio(empresa.getTelefono()) && !telefonoValido(empresa.getTelefono())) {
			return "El teléfono debe contener exactamente 10 dígitos y no debe incluir letras.";
		}
		
		if ("ACTIVO".equalsIgnoreCase(empresa.getConvenio())) {

			if (empresa.getVigenciaConvenio() == null) {
				return "Debes seleccionar la vigencia del convenio.";
			}

			if (!(empresa.getVigenciaConvenio() == 2
					|| empresa.getVigenciaConvenio() == 3
					|| empresa.getVigenciaConvenio() == 5)) {

				return "La vigencia del convenio debe ser de 2, 3 o 5 años.";
			}
		}

		return null;
	}

	private boolean estaVacio(String valor) {
		return valor == null || valor.trim().isEmpty();
	}

	private boolean soloLetrasEspacios(String valor) {
		return valor != null && valor.trim().matches("^[A-Za-zÁÉÍÓÚáéíóúÑñÜü\\s\\.]+$");
	}

	private boolean telefonoValido(String telefono) {
		return telefono != null && telefono.trim().matches("^\\d{10}$");
	}
}