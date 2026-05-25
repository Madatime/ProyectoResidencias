package itch.tsp.config;

import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import itch.tsp.model.AsesorExterno;
import itch.tsp.model.Directivo;
import itch.tsp.model.Docente;
import itch.tsp.model.Empresa;
import itch.tsp.model.TipoDirectivo;
import itch.tsp.repository.AsesorExternoRepository;
import itch.tsp.repository.DirectivoRepository;
import itch.tsp.repository.EmpresaRepository;
import itch.tsp.repository.UsuarioRepository;
import itch.tsp.service.IUsuarioService;

@Configuration
public class EmpresaInstitucionalBootstrapInitializer {

	private static final String NOMBRE_INSTITUCION = "INSTITUTO TECNOLOGICO DE MEXICO - CAMPUS CHILPANCINGO";

	@Bean
	CommandLineRunner inicializarEmpresaInstitucional(
			EmpresaRepository empresaRepository,
			DirectivoRepository directivoRepository,
			AsesorExternoRepository asesorExternoRepository,
			UsuarioRepository usuarioRepository,
			IUsuarioService usuarioService) {

		return args -> {
			Empresa institucion = asegurarInstitucion(empresaRepository);
			asegurarJefesComoAsesoresExternos(
					institucion,
					directivoRepository.findByEstatusOrderByIdAsc(1),
					asesorExternoRepository,
					usuarioRepository,
					usuarioService);
		};
	}

	private Empresa asegurarInstitucion(EmpresaRepository empresaRepository) {
		List<Empresa> existentes = empresaRepository.findByNombreAndEstatus(NOMBRE_INSTITUCION, 1);
		Empresa empresa = existentes.isEmpty() ? new Empresa() : existentes.get(0);

		empresa.setNombre(NOMBRE_INSTITUCION);
		empresa.setGiro("INSTITUCION EDUCATIVA");
		empresa.setDireccion("CHILPANCINGO, GUERRERO");
		empresa.setTelefono("7470000000");
		empresa.setCorreo("contacto@itchilpancingo.edu.mx");
		empresa.setRepresentante("Aldo Olivar Herrera");
		empresa.setPuestoRepresentante("Administrador");
		empresa.setDueno("Gobierno de Mexico");
		empresa.setConvenio("ACTIVO");
		empresa.setAnioConvenio(LocalDate.now().getYear());
		empresa.setVigenciaConvenio(3);
		empresa.setEstatus(1);

		return empresaRepository.save(empresa);
	}

	private void asegurarJefesComoAsesoresExternos(
			Empresa institucion,
			List<Directivo> directivos,
			AsesorExternoRepository asesorExternoRepository,
			UsuarioRepository usuarioRepository,
			IUsuarioService usuarioService) {

		List<AsesorExterno> externosActivos = asesorExternoRepository.findByEstatusOrderByIdDesc(1);

		for (Directivo directivo : directivos) {
			if (directivo == null || directivo.getTipoDirectivo() != TipoDirectivo.JEFE_DEPARTAMENTO) {
				continue;
			}

			Docente docente = directivo.getDocente();
			if (docente == null) {
				continue;
			}

			AsesorExterno asesorExistente = externosActivos.stream()
					.filter(asesor -> asesor != null)
					.filter(asesor -> coincide(asesor.getNombre(), docente.getNombre()))
					.filter(asesor -> coincide(asesor.getApellidos(), docente.getApellidos()))
					.filter(asesor -> coincide(asesor.getEmpresa(), institucion.getNombre()))
					.findFirst()
					.orElse(null);

			if (asesorExistente == null) {
				asesorExistente = new AsesorExterno();
				asesorExistente.setNombre(docente.getNombre());
				asesorExistente.setApellidos(docente.getApellidos());
				asesorExistente.setEmpresa(institucion.getNombre());
				asesorExistente.setCargo(directivo.getPuesto() != null && !directivo.getPuesto().trim().isEmpty()
						? directivo.getPuesto().trim()
						: "Jefe de departamento");
				asesorExistente.setTelefono(docente.getTelefono());
				asesorExistente.setCorreo(docente.getCorreo());
				asesorExistente.setFotoPath(docente.getFotoPath());
				asesorExistente.setEstatus(1);
				asesorExistente = asesorExternoRepository.save(asesorExistente);
				externosActivos.add(asesorExistente);
			}

			if (docente.getCorreo() != null && !docente.getCorreo().trim().isEmpty()) {
				if (usuarioRepository.findByEmailAndEstatus(docente.getCorreo().trim(), 1) != null) {
					continue;
				}
			}

			if (!usuarioRepository.existsByAsesorExterno_Id(asesorExistente.getId())) {
				usuarioService.crearUsuarioParaAsesorExterno(asesorExistente.getId());
			}
		}
	}

	private boolean coincide(String a, String b) {
		return normalizar(a).equals(normalizar(b));
	}

	private String normalizar(String valor) {
		return valor == null ? "" : valor.trim().toUpperCase();
	}
}
