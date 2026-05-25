package itch.tsp.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import itch.tsp.model.Usuario;
import itch.tsp.model.UsuarioPerfil;
import itch.tsp.service.IUsuarioService;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

	@Autowired
	private IUsuarioService serviceUsuario;

	@GetMapping("/index")
	public String index(
			@RequestParam(value = "texto", required = false) String texto,
			@RequestParam(value = "perfil", required = false) String perfil,
			Model model) {

		List<Usuario> usuarios = new ArrayList<>();
		boolean hayTexto = texto != null && !texto.trim().isEmpty();
		boolean hayPerfil = perfil != null && !perfil.trim().isEmpty();

		if (hayTexto || hayPerfil) {
			usuarios = serviceUsuario.buscarPorTextoYPerfil(texto, perfil);
		} else {
			usuarios = serviceUsuario.buscarTodos();
		}

		for (Usuario usuario : usuarios) {
			cargarPerfil(usuario);
		}

		int totalAdmins = 0;

		for (Usuario usuario : usuarios) {
			if ("ADMINISTRADOR".equals(usuario.getPerfilNombre())) {
				totalAdmins++;
			}
		}

		model.addAttribute("usuarios", usuarios);
		model.addAttribute("texto", texto);
		model.addAttribute("perfil", perfil);
		model.addAttribute("totalUsuarios", usuarios.size());
		model.addAttribute("totalAdmins", totalAdmins);

		return "usuarios/listaUsuarios";
	}

	@GetMapping("/create")
	public String crear(Model model) {
		model.addAttribute("usuario", new Usuario());
		model.addAttribute("perfiles", serviceUsuario.buscarPerfilesActivos());
		model.addAttribute("accion", "Crear Usuario");

		return "usuarios/formUsuario";
	}

	@GetMapping("/edit/{id}")
	public String editar(
			@PathVariable("id") Integer idUsuario,
			Model model,
			RedirectAttributes flash) {

		Usuario usuario = serviceUsuario.buscarPorId(idUsuario);

		if (usuario == null) {
			flash.addFlashAttribute("msgError", "El usuario no existe.");
			return "redirect:/usuarios/index";
		}

		cargarPerfil(usuario);

		model.addAttribute("usuario", usuario);
		model.addAttribute("perfiles", serviceUsuario.buscarPerfilesActivos());
		model.addAttribute("accion", "Editar Usuario");

		return "usuarios/formUsuario";
	}

	@PostMapping("/save")
	public String guardar(
			Usuario usuario,
			@RequestParam("nombrePerfil") String nombrePerfil,
			Model model,
			RedirectAttributes flash) {

		try {
			serviceUsuario.guardarUsuarioConPerfil(usuario, nombrePerfil);
			flash.addFlashAttribute("msgSuccess", "Usuario guardado correctamente.");
			return "redirect:/usuarios/index";
		} catch (RuntimeException e) {
			usuario.setPerfilNombre(nombrePerfil);
			model.addAttribute("msgError", e.getMessage());
			model.addAttribute("usuario", usuario);
			model.addAttribute("perfiles", serviceUsuario.buscarPerfilesActivos());
			model.addAttribute("accion", usuario.getId() != null ? "Editar Usuario" : "Crear Usuario");
			return "usuarios/formUsuario";
		}
	}

	@GetMapping("/reset-password/{id}")
	public String restablecerPassword(
			@PathVariable("id") Integer idUsuario,
			RedirectAttributes flash) {

		serviceUsuario.restablecerPassword(idUsuario);

		flash.addFlashAttribute(
				"msgSuccess",
				"Contraseña restablecida correctamente. Nueva contraseña: 123");

		return "redirect:/usuarios/index";
	}

	@GetMapping("/delete/{id}")
	public String eliminar(
			@PathVariable("id") Integer idUsuario,
			RedirectAttributes flash) {

		try {
			serviceUsuario.eliminar(idUsuario);
			flash.addFlashAttribute("msgSuccess", "Usuario desactivado correctamente.");
		} catch (RuntimeException e) {
			flash.addFlashAttribute("msgError", e.getMessage());
		}

		return "redirect:/usuarios/index";
	}

	private void cargarPerfil(Usuario usuario) {

		List<UsuarioPerfil> perfiles =
				serviceUsuario.buscarPerfilesDeUsuario(usuario.getId());

		if (perfiles != null && !perfiles.isEmpty()
				&& perfiles.get(0).getPerfil() != null) {

			usuario.setPerfilNombre(perfiles.get(0).getPerfil().getNombre());
			return;
		}

		if (usuario.getRol() != null && !usuario.getRol().trim().isEmpty()) {
			usuario.setPerfilNombre(usuario.getRol().trim().toUpperCase());
		}
	}
	
	@GetMapping("/inactivos")
	public String inactivos(Model model) {
		List<Usuario> usuarios = serviceUsuario.buscarInactivos();

		for (Usuario usuario : usuarios) {
			cargarPerfil(usuario);
		}

		model.addAttribute("usuarios", usuarios);
		model.addAttribute("totalUsuarios", usuarios.size());
		model.addAttribute("totalAdmins", 0);
		model.addAttribute("texto", "");

		return "usuarios/listaUsuarios";
	}

	@GetMapping("/recuperar/{id}")
	public String recuperar(
			@PathVariable("id") Integer idUsuario,
			RedirectAttributes flash) {

		serviceUsuario.recuperar(idUsuario);
		flash.addFlashAttribute("msgSuccess", "Usuario recuperado correctamente.");

		return "redirect:/usuarios/inactivos";
	}
	
}
