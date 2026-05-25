package itch.tsp.config;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import itch.tsp.model.AsesorExterno;
import itch.tsp.model.AsesorInterno;
import itch.tsp.model.Residencia;
import itch.tsp.repository.AsesorExternoRepository;
import itch.tsp.repository.AsesorInternoRepository;
import itch.tsp.repository.ResidenciaRepository;
import itch.tsp.repository.UsuarioRepository;
import itch.tsp.service.IUsuarioService;

@Configuration
public class DemoRelacionesInitializer {

	@Bean
	CommandLineRunner inicializarRelacionesDemo(
			AsesorExternoRepository asesorExternoRepository,
			AsesorInternoRepository asesorInternoRepository,
			ResidenciaRepository residenciaRepository,
			UsuarioRepository usuarioRepository,
			IUsuarioService usuarioService) {

		return args -> {
			List<AsesorExterno> asesoresExternos = asesorExternoRepository.findByEstatusOrderByIdDesc(1);
			List<AsesorInterno> asesores = asesorInternoRepository.findByEstatusOrderByIdAsc(1);
			List<Residencia> residencias = residenciaRepository.findByEstatusOrderByIdDesc(1);

			crearUsuariosFaltantesAsesoresExternos(asesoresExternos, usuarioRepository, usuarioService);

			if (asesores.isEmpty()) {
				return;
			}

			crearUsuariosFaltantes(asesores, usuarioRepository, usuarioService);

			if (residencias.isEmpty()) {
				return;
			}

			List<Residencia> elegiblesSinAsesor = new ArrayList<>();

			for (Residencia residencia : residencias) {
				if (residencia.getAsesorInterno() == null && permiteAsignacionDemo(residencia)) {
					elegiblesSinAsesor.add(residencia);
				}
			}

			if (elegiblesSinAsesor.isEmpty()) {
				return;
			}

			List<AsesorInterno> asesoresOrdenados = new ArrayList<>(asesores);
			asesoresOrdenados.sort(Comparator.comparingInt(asesor -> contarAsignaciones(residencias, asesor.getId())));

			int indiceResidencia = 0;

			for (AsesorInterno asesor : asesoresOrdenados) {
				if (indiceResidencia >= elegiblesSinAsesor.size()) {
					break;
				}

				if (contarAsignaciones(residencias, asesor.getId()) > 0) {
					continue;
				}

				Residencia residencia = elegiblesSinAsesor.get(indiceResidencia++);
				residencia.setAsesorInterno(asesor);
				residenciaRepository.save(residencia);
			}

			while (indiceResidencia < elegiblesSinAsesor.size()) {
				asesoresOrdenados.sort(Comparator.comparingInt(asesor -> contarAsignaciones(residencias, asesor.getId())));

				AsesorInterno asesor = asesoresOrdenados.get(0);
				Residencia residencia = elegiblesSinAsesor.get(indiceResidencia++);
				residencia.setAsesorInterno(asesor);
				residenciaRepository.save(residencia);
			}
		};
	}

	private void crearUsuariosFaltantes(
			List<AsesorInterno> asesores,
			UsuarioRepository usuarioRepository,
			IUsuarioService usuarioService) {

		for (AsesorInterno asesor : asesores) {
			if (asesor == null || asesor.getDocente() == null || asesor.getDocente().getId() == null) {
				continue;
			}

			Integer idDocente = asesor.getDocente().getId();

			if (!usuarioRepository.existsByDocente_Id(idDocente)) {
				usuarioService.crearUsuarioParaDocente(idDocente, "ASESOR_INTERNO");
			}
		}
	}

	private void crearUsuariosFaltantesAsesoresExternos(
			List<AsesorExterno> asesoresExternos,
			UsuarioRepository usuarioRepository,
			IUsuarioService usuarioService) {

		for (AsesorExterno asesor : asesoresExternos) {
			if (asesor == null || asesor.getId() == null) {
				continue;
			}

			if (!usuarioRepository.existsByAsesorExterno_Id(asesor.getId())) {
				usuarioService.crearUsuarioParaAsesorExterno(asesor.getId());
			}
		}
	}

	private boolean permiteAsignacionDemo(Residencia residencia) {
		if (residencia == null || residencia.getResidente() == null) {
			return false;
		}

		String estado = residencia.getEstadoAutorizacion();

		if (estado == null || estado.trim().isEmpty()) {
			return true;
		}

		return !"RECHAZADO".equalsIgnoreCase(estado.trim());
	}

	private int contarAsignaciones(List<Residencia> residencias, Integer idAsesorInterno) {
		if (idAsesorInterno == null) {
			return 0;
		}

		int total = 0;

		for (Residencia residencia : residencias) {
			if (residencia != null
					&& residencia.getAsesorInterno() != null
					&& idAsesorInterno.equals(residencia.getAsesorInterno().getId())) {
				total++;
			}
		}

		return total;
	}
}
