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

import itch.tsp.model.AsesorExterno;
import itch.tsp.repository.AsesorExternoRepository;
import itch.tsp.service.IAsesorExternoService;
import itch.tsp.service.IUsuarioService;

@Primary
@Service
public class AsesorExternoServiceJpa implements IAsesorExternoService {

	@Autowired
	private AsesorExternoRepository repoAsesorExterno;

	@Autowired
	private IUsuarioService serviceUsuario;

	@Value("${app.ruta.base}")
	private String rutaBase;

	@Value("${app.carpeta.asesores.externos}")
	private String carpetaAsesoresExternos;

	@Override
	public List<AsesorExterno> buscarTodosActivos() {
		return repoAsesorExterno.findByEstatusOrderByIdDesc(1);
	}

	@Override
	public List<AsesorExterno> buscarTodosInactivos() {
		return repoAsesorExterno.findByEstatusOrderByIdDesc(0);
	}

	@Override
	public List<AsesorExterno> buscarAsesoresExternos(String texto) {

		if (texto == null || texto.trim().isEmpty()) {
			return buscarTodosActivos();
		}

		String textoBusqueda = texto.trim();

		return repoAsesorExterno
				.findByEstatusAndNombreContainingIgnoreCaseOrEstatusAndApellidosContainingIgnoreCaseOrEstatusAndEmpresaContainingIgnoreCase(
						1, textoBusqueda,
						1, textoBusqueda,
						1, textoBusqueda);
	}

	@Override
	public void guardarAsesorExterno(AsesorExterno asesorExterno) {

		boolean nuevoRegistro = asesorExterno.getId() == null;

		normalizarDatos(asesorExterno);

		if (asesorExterno.getEstatus() == null) {
			asesorExterno.setEstatus(1);
		}

		repoAsesorExterno.save(asesorExterno);

		if (nuevoRegistro) {
			crearUsuarioAutomatico(asesorExterno);
		}
	}

	@Override
	public void guardarAsesorExternoConArchivos(
			AsesorExterno asesorExterno,
			MultipartFile foto,
			MultipartFile documento) {

		boolean nuevoRegistro = asesorExterno.getId() == null;

		AsesorExterno asesorBD = null;

		if (asesorExterno.getId() != null) {
			asesorBD = repoAsesorExterno.findById(asesorExterno.getId()).orElse(null);
		}

		normalizarDatos(asesorExterno);

		if (asesorExterno.getEstatus() == null) {
			asesorExterno.setEstatus(asesorBD != null ? asesorBD.getEstatus() : 1);
		}

		String rutaCompleta = rutaBase + carpetaAsesoresExternos;
		File directorio = new File(rutaCompleta);

		if (!directorio.exists()) {
			directorio.mkdirs();
		}

		try {
			if (foto != null && !foto.isEmpty()) {
				asesorExterno.setFotoPath(guardarArchivoEnDisco(foto, directorio));
			} else if (asesorBD != null) {
				asesorExterno.setFotoPath(asesorBD.getFotoPath());
			}

		} catch (IOException e) {
			throw new RuntimeException("Error al guardar la foto: " + e.getMessage());
		}

		repoAsesorExterno.save(asesorExterno);

		if (nuevoRegistro) {
			crearUsuarioAutomatico(asesorExterno);
		}
	}

	private void normalizarDatos(AsesorExterno asesorExterno) {

		if (asesorExterno.getNombre() != null) {
			asesorExterno.setNombre(asesorExterno.getNombre().trim());
		}

		if (asesorExterno.getApellidos() != null) {
			asesorExterno.setApellidos(asesorExterno.getApellidos().trim());
		}

		if (asesorExterno.getEmpresa() != null) {
			asesorExterno.setEmpresa(asesorExterno.getEmpresa().trim());
		}

		if (asesorExterno.getCargo() != null) {
			asesorExterno.setCargo(asesorExterno.getCargo().trim());
		}

		if (asesorExterno.getCorreo() != null) {
			asesorExterno.setCorreo(asesorExterno.getCorreo().trim());
		}

		if (asesorExterno.getTelefono() != null) {
			asesorExterno.setTelefono(asesorExterno.getTelefono().trim());
		}
	}

	private void crearUsuarioAutomatico(AsesorExterno asesorExterno) {
		serviceUsuario.crearUsuarioParaAsesorExterno(asesorExterno.getId());
	}

	private String guardarArchivoEnDisco(MultipartFile archivo, File directorio) throws IOException {

		String nombreOriginal = archivo.getOriginalFilename();
		String extension = "";

		if (nombreOriginal != null && nombreOriginal.contains(".")) {
			extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
		}

		String nombreNuevo = UUID.randomUUID().toString() + extension;

		if (!directorio.exists()) {
			directorio.mkdirs();
		}

		File destino = new File(directorio.getAbsolutePath(), nombreNuevo);

		archivo.transferTo(destino);

		return nombreNuevo;
	}

	@Override
	public AsesorExterno buscarPorIdAsesorExterno(Integer idAsesorExterno) {
		return repoAsesorExterno.findByIdAndEstatus(idAsesorExterno, 1);
	}

	@Override
	public void eliminar(Integer idAsesorExterno) {

		Optional<AsesorExterno> optional = repoAsesorExterno.findById(idAsesorExterno);

		if (optional.isPresent()) {
			AsesorExterno asesor = optional.get();
			asesor.setEstatus(0);
			repoAsesorExterno.save(asesor);
		}
	}

	@Override
	public void recuperar(Integer idAsesorExterno) {

		Optional<AsesorExterno> optional = repoAsesorExterno.findById(idAsesorExterno);

		if (optional.isPresent()) {
			AsesorExterno asesor = optional.get();
			asesor.setEstatus(1);
			repoAsesorExterno.save(asesor);
		}
	}
}
