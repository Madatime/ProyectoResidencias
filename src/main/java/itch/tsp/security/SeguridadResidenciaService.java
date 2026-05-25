package itch.tsp.security;

import java.util.ArrayList;
import java.util.List;
import java.text.Normalizer;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import itch.tsp.model.Carrera;
import itch.tsp.model.Directivo;
import itch.tsp.model.Residencia;
import itch.tsp.service.IDirectivoService;

@Service
public class SeguridadResidenciaService {

	@Autowired
	private IDirectivoService serviceDirectivo;

	public void validarAccesoResidencia(Residencia residencia, Authentication authentication) {
		if (!puedeVerResidencia(residencia, authentication)) {
			throw new AccessDeniedException("No tienes permiso para acceder a esta residencia.");
		}
	}

	public void validarEdicionResidencia(Residencia residencia, Authentication authentication) {
		if (residencia == null) {
			throw new AccessDeniedException("La residencia no existe.");
		}

		if (tieneRol(authentication, "ADMINISTRADOR") || tieneRol(authentication, "DIVISION_ESTUDIOS")) {
			return;
		}

		throw new AccessDeniedException("No tienes permiso para editar esta residencia.");
	}

	public void validarDictamenResidencia(Residencia residencia, Authentication authentication) {
		if (residencia == null) {
			throw new AccessDeniedException("La residencia no existe.");
		}

		if (tieneRol(authentication, "ADMINISTRADOR") || tieneRol(authentication, "DIVISION_ESTUDIOS")) {
			return;
		}

		if (!tieneRol(authentication, "JEFE_DEPARTAMENTO")) {
			throw new AccessDeniedException("No tienes permiso para dictaminar esta residencia.");
		}

		UsuarioPrincipal principal = obtenerPrincipal(authentication);

		if (principal == null || principal.getIdDocente() == null) {
			throw new AccessDeniedException("No se pudo identificar al jefe de departamento.");
		}

		Directivo directivo = serviceDirectivo.buscarPorDocente(principal.getIdDocente());

		if (directivo == null) {
			throw new AccessDeniedException("Tu usuario no esta vinculado a un directivo activo.");
		}

		if (directivo.getTipoDirectivo() != itch.tsp.model.TipoDirectivo.JEFE_DEPARTAMENTO) {
			throw new AccessDeniedException("Solo un jefe de departamento puede dictaminar residencias.");
		}

		String carreraResidencia = obtenerCarreraResidencia(residencia);
		String departamentoJefe = directivo.getDepartamento();

		if (carreraResidencia == null || departamentoJefe == null) {
			throw new AccessDeniedException("No se pudo validar la carrera del dictamen.");
		}

		if (!coincideJefeConCarreraResidencia(directivo, carreraResidencia)) {
			throw new AccessDeniedException("Solo puedes dictaminar proyectos de tu departamento.");
		}
	}

	private String normalizar(String texto) {
		if (texto == null) {
			return "";
		}

		return texto.trim()
				.toUpperCase()
				.replace("Á", "A")
				.replace("É", "E")
				.replace("Í", "I")
				.replace("Ó", "O")
				.replace("Ú", "U");
	}

	private String obtenerCarreraResidencia(Residencia residencia) {
		if (residencia != null
				&& residencia.getResidente() != null
				&& residencia.getResidente().getEstudiante() != null
				&& residencia.getResidente().getEstudiante().getCarrera() != null
				&& residencia.getResidente().getEstudiante().getCarrera().getNombre() != null
				&& !residencia.getResidente().getEstudiante().getCarrera().getNombre().trim().isEmpty()) {
			return residencia.getResidente().getEstudiante().getCarrera().getNombre();
		}

		if (residencia != null
				&& residencia.getCarreraJefeArea() != null
				&& !residencia.getCarreraJefeArea().trim().isEmpty()) {
			return residencia.getCarreraJefeArea();
		}

		return null;
	}

	public List<Residencia> filtrarResidenciasPermitidas(List<Residencia> residencias, Authentication authentication) {
		if (residencias == null) {
			return new ArrayList<>();
		}

		if (tieneRol(authentication, "ADMINISTRADOR")
				|| tieneRol(authentication, "DIVISION_ESTUDIOS")
				|| tieneRol(authentication, "SERVICIOS_ESCOLARES")) {
			return residencias;
		}

		if (tieneRol(authentication, "JEFE_DEPARTAMENTO")) {
			return residencias.stream()
					.filter(residencia -> esJefeDeCarreraResidencia(residencia, authentication))
					.collect(Collectors.toList());
		}

		return residencias.stream()
				.filter(residencia -> puedeVerResidencia(residencia, authentication))
				.collect(Collectors.toList());
	}

	public void validarEvaluacionInterna(Residencia residencia, Authentication authentication) {
		if (tieneRol(authentication, "ADMINISTRADOR")) {
			return;
		}

		if (!esAsesorInternoAsignado(residencia, authentication)) {
			throw new AccessDeniedException("Solo el asesor interno asignado puede evaluar esta residencia.");
		}
	}

	public void validarEvaluacionExterna(Residencia residencia, Authentication authentication) {
		if (tieneRol(authentication, "ADMINISTRADOR")) {
			return;
		}

		if (!esAsesorExternoAsignado(residencia, authentication)) {
			throw new AccessDeniedException("Solo el asesor externo asignado puede evaluar esta residencia.");
		}
	}

	public boolean puedeVerResidencia(Residencia residencia, Authentication authentication) {
		if (residencia == null || authentication == null || !authentication.isAuthenticated()) {
			return false;
		}

		if (tieneRol(authentication, "ADMINISTRADOR")
				|| tieneRol(authentication, "DIVISION_ESTUDIOS")
				|| tieneRol(authentication, "SERVICIOS_ESCOLARES")) {
			return true;
		}

		if (tieneRol(authentication, "JEFE_DEPARTAMENTO")) {
			return esJefeDeCarreraResidencia(residencia, authentication);
		}

		if (esResidentePropietario(residencia, authentication)) {
			return true;
		}

		if (esAsesorInternoAsignado(residencia, authentication)) {
			return true;
		}

		if (esAsesorExternoAsignado(residencia, authentication)) {
			return true;
		}

		return false;
	}

	private boolean esResidentePropietario(Residencia residencia, Authentication authentication) {
		UsuarioPrincipal principal = obtenerPrincipal(authentication);

		if (principal == null || principal.getIdResidente() == null) {
			return false;
		}

		return residencia.getResidente() != null
				&& residencia.getResidente().getId() != null
				&& residencia.getResidente().getId().equals(principal.getIdResidente());
	}

	private boolean esAsesorInternoAsignado(Residencia residencia, Authentication authentication) {
		UsuarioPrincipal principal = obtenerPrincipal(authentication);

		if (principal == null || principal.getIdDocente() == null) {
			return false;
		}

		return residencia.getAsesorInterno() != null
				&& residencia.getAsesorInterno().getDocente() != null
				&& residencia.getAsesorInterno().getDocente().getId() != null
				&& residencia.getAsesorInterno().getDocente().getId().equals(principal.getIdDocente());
	}

	private boolean esAsesorExternoAsignado(Residencia residencia, Authentication authentication) {
		UsuarioPrincipal principal = obtenerPrincipal(authentication);

		if (principal == null || principal.getIdAsesorExterno() == null) {
			return false;
		}

		return residencia.getAsesorExterno() != null
				&& residencia.getAsesorExterno().getId() != null
				&& residencia.getAsesorExterno().getId().equals(principal.getIdAsesorExterno());
	}

	private UsuarioPrincipal obtenerPrincipal(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioPrincipal principal)) {
			return null;
		}

		return principal;
	}

	private boolean esJefeDeCarreraResidencia(Residencia residencia, Authentication authentication) {
		UsuarioPrincipal principal = obtenerPrincipal(authentication);

		if (principal == null || principal.getIdDocente() == null) {
			return false;
		}

		Directivo directivo = serviceDirectivo.buscarPorDocente(principal.getIdDocente());

		if (directivo == null
				|| directivo.getTipoDirectivo() != itch.tsp.model.TipoDirectivo.JEFE_DEPARTAMENTO) {
			return false;
		}

		return coincideJefeConCarreraResidencia(directivo, obtenerCarreraResidencia(residencia));
	}

	private boolean coincideJefeConCarreraResidencia(Directivo directivo, String carreraResidencia) {
		if (directivo == null || carreraResidencia == null || carreraResidencia.trim().isEmpty()) {
			return false;
		}

		if (coincideDepartamentoConCarrera(directivo.getDepartamento(), carreraResidencia)) {
			return true;
		}

		if (directivo.getDocente() == null || directivo.getDocente().getCarrerasHabilitadas() == null) {
			return false;
		}

		for (Carrera carrera : directivo.getDocente().getCarrerasHabilitadas()) {
			if (carrera != null && coincideDepartamentoConCarrera(carrera.getNombre(), carreraResidencia)) {
				return true;
			}
		}

		return false;
	}

	private boolean coincideDepartamentoConCarrera(String departamentoJefe, String carreraResidencia) {
		if (departamentoJefe == null || carreraResidencia == null) {
			return false;
		}

		String departamentoNormalizado = normalizarTexto(departamentoJefe);
		String carreraNormalizada = normalizarTexto(carreraResidencia);

		if (departamentoNormalizado.isEmpty() || carreraNormalizada.isEmpty()) {
			return false;
		}

		return departamentoNormalizado.equals(carreraNormalizada)
				|| departamentoNormalizado.contains(carreraNormalizada)
				|| carreraNormalizada.contains(departamentoNormalizado);
	}

	private String normalizarTexto(String texto) {
		if (texto == null) {
			return "";
		}

		String valor = Normalizer.normalize(texto.trim().toUpperCase(), Normalizer.Form.NFD);
		return valor.replaceAll("\\p{M}", "");
	}

	private boolean tieneRol(Authentication authentication, String rol) {
		if (authentication == null || authentication.getAuthorities() == null) {
			return false;
		}

		String rolSpring = "ROLE_" + rol;

		for (GrantedAuthority authority : authentication.getAuthorities()) {
			if (authority.getAuthority().equals(rolSpring)) {
				return true;
			}
		}

		return false;
	}
}
