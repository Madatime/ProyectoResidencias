package itch.tsp.service.implementJPA;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import itch.tsp.model.Residente;
import itch.tsp.repository.ResidenteRepository;
import itch.tsp.service.IResidenteService;
import itch.tsp.service.IUsuarioService;

@Primary
@Service
public class ResidenteServiceJpa implements IResidenteService {

	@Autowired
	private ResidenteRepository repoResidente;

	@Autowired
	private IUsuarioService serviceUsuario;

	@Value("${app.ruta.base}")
	private String rutaBase;

	@Value("${app.carpeta.residentes}")
	private String carpetaResidentes;

	@Override
	public List<Residente> buscarTodosActivos() {
		return repoResidente.findByEstatusOrderByIdDesc(1);
	}

	@Override
	public List<Residente> buscarTodosInactivos() {
		return repoResidente.findByEstatusOrderByIdDesc(0);
	}

	@Override
	public List<Residente> buscarResidentes(String texto) {
		if (texto == null || texto.trim().isEmpty()) {
			return repoResidente.findByEstatusOrderByIdDesc(1);
		}

		String textoBusqueda = texto.trim();

		return repoResidente
				.findByEstatusAndEstudiante_MatriculaContainingIgnoreCaseOrEstatusAndEstudiante_NombreContainingIgnoreCaseOrEstatusAndEstudiante_ApellidosContainingIgnoreCase(
						1, textoBusqueda,
						1, textoBusqueda,
						1, textoBusqueda);
	}

	@Override
	public void guardarResidente(Residente residente) {

		boolean nuevoRegistro = residente.getId() == null;

		if (nuevoRegistro) {

			if (residente.getEstatus() == null) {
				residente.setEstatus(1);
			}

			repoResidente.save(residente);

			try {
				serviceUsuario.crearUsuarioParaResidente(residente.getId());
			} catch (Exception e) {
				System.out.println("Error al crear usuario automático para residente: " + e.getMessage());
			}

			return;
		}

		Residente residenteBD = repoResidente.findById(residente.getId()).orElse(null);

		if (residenteBD != null) {

			if (residente.getEstatus() == null) {
				residente.setEstatus(residenteBD.getEstatus());
			}

			if (residente.getFotoPath() == null || residente.getFotoPath().trim().isEmpty()) {
				residente.setFotoPath(residenteBD.getFotoPath());
			}

			if (residente.getEstudiante() == null) {
				residente.setEstudiante(residenteBD.getEstudiante());
			}
		}

		repoResidente.save(residente);
	}

	@Override
	public void guardarResidenteConArchivos(Residente residente, MultipartFile foto, MultipartFile documento) {

		boolean nuevoRegistro = residente.getId() == null;

		Residente residenteBD = null;

		if (residente.getId() != null) {
			residenteBD = repoResidente.findById(residente.getId()).orElse(null);
		}

		if (residente.getEstatus() == null) {
			residente.setEstatus(residenteBD != null ? residenteBD.getEstatus() : 1);
		}

		if (residente.getEstudiante() == null && residenteBD != null) {
			residente.setEstudiante(residenteBD.getEstudiante());
		}

		String rutaCompleta = rutaBase + carpetaResidentes;
		File directorio = new File(rutaCompleta);

		if (!directorio.exists()) {
			directorio.mkdirs();
		}

		try {
			if (foto != null && !foto.isEmpty()) {
				String nombreFoto = guardarArchivoEnDisco(foto, directorio);
				residente.setFotoPath(nombreFoto);
			} else if (residenteBD != null) {
				residente.setFotoPath(residenteBD.getFotoPath());
			}
		} catch (IOException e) {
			throw new RuntimeException("Error al guardar la foto del residente: " + e.getMessage());
		}

		repoResidente.save(residente);

		if (nuevoRegistro) {
			try {
				serviceUsuario.crearUsuarioParaResidente(residente.getId());
			} catch (Exception e) {
				System.out.println("Error al crear usuario automático para residente: " + e.getMessage());
			}
		}
	}

	private String guardarArchivoEnDisco(MultipartFile archivo, File directorio) throws IOException {
		String nombreOriginal = archivo.getOriginalFilename();
		String extension = "";

		if (nombreOriginal != null && nombreOriginal.contains(".")) {
			extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
		}

		String nombreNuevo = UUID.randomUUID().toString() + extension;
		File destino = new File(directorio, nombreNuevo);
		archivo.transferTo(destino);

		return nombreNuevo;
	}

	@Override
	public Residente buscarPorIdResidente(Integer idResidente) {
		return repoResidente.findByIdAndEstatus(idResidente, 1);
	}

	@Override
	public boolean existeMatricula(String matricula) {
		String matriculaNormalizada = normalizarMatricula(matricula);
		List<Residente> lista = repoResidente.findByEstudiante_MatriculaAndEstatus(matriculaNormalizada, 1);
		return lista != null && !lista.isEmpty();
	}

	@Override
	public boolean existeMatriculaParaOtroRegistro(String matricula, Integer id) {
		String matriculaNormalizada = normalizarMatricula(matricula);
		List<Residente> lista = repoResidente.findByEstudiante_MatriculaAndEstatusAndIdNot(matriculaNormalizada, 1, id);
		return lista != null && !lista.isEmpty();
	}

	@Override
	public String normalizarMatricula(String matricula) {
		if (matricula == null) {
			return null;
		}

		return matricula.trim().toUpperCase();
	}

	@Override
	public boolean matriculaValida(String matricula) {
		if (matricula == null) {
			return false;
		}

		String valor = normalizarMatricula(matricula);
		return valor.matches("^(C\\d{8}|\\d{8})$");
	}

	@Override
	public void eliminar(Integer idResidente) {
		Optional<Residente> optional = repoResidente.findById(idResidente);

		if (optional.isPresent()) {
			Residente residente = optional.get();
			residente.setEstatus(0);
			repoResidente.save(residente);
		}
	}

	@Override
	public void recuperar(Integer idResidente) {
		Optional<Residente> optional = repoResidente.findById(idResidente);

		if (optional.isPresent()) {
			Residente residente = optional.get();
			residente.setEstatus(1);
			repoResidente.save(residente);
		}
	}
}
