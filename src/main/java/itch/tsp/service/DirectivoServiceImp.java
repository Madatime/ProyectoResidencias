package itch.tsp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import itch.tsp.model.Carrera;
import itch.tsp.model.Directivo;
import itch.tsp.model.Docente;
import itch.tsp.model.TipoDirectivo;
import itch.tsp.repository.CarreraRepository;
import itch.tsp.repository.DirectivoRepository;
import itch.tsp.repository.DocenteRepository;

@Service
public class DirectivoServiceImp implements IDirectivoService {

	@Autowired
	private DirectivoRepository repoDirectivo;

	@Autowired
	private DocenteRepository repoDocente;

	@Autowired
	private CarreraRepository repoCarrera;

	@Autowired
	private IUsuarioService serviceUsuario;

	@Override
	public List<Directivo> buscarTodosActivos() {
		return repoDirectivo.findByEstatusOrderByIdAsc(1);
	}

	@Override
	public List<Directivo> buscarTodosInactivos() {
		return repoDirectivo.findByEstatusOrderByIdAsc(0);
	}

	@Override
	public Directivo buscarPorId(Integer id) {
		return repoDirectivo.findByIdAndEstatus(id, 1);
	}
	
	@Override
	public Directivo buscarPorDocente(Integer idDocente) {

		if (idDocente == null) {
			return null;
		}

		return repoDirectivo
				.findByDocente_IdAndEstatusOrderByIdDesc(idDocente, 1)
				.stream()
				.findFirst()
				.orElse(null);
	}

	@Override
	public Directivo buscarPorTipoActivo(TipoDirectivo tipoDirectivo) {
		if (tipoDirectivo == null) {
			return null;
		}

		return repoDirectivo.findFirstByTipoDirectivoAndEstatusOrderByIdDesc(tipoDirectivo, 1);
	}

	@Override
	public Directivo buscarJefeDepartamentoPorCarrera(String carrera) {
		if (carrera == null || carrera.trim().isEmpty()) {
			return null;
		}

		String carreraNormalizada = normalizar(carrera);

		return buscarTodosActivos().stream()
				.filter(item -> item.getTipoDirectivo() == TipoDirectivo.JEFE_DEPARTAMENTO)
				.filter(item -> coincideJefeConCarrera(item, carreraNormalizada))
				.findFirst()
				.orElse(null);
	}

	@Override
	public void guardar(Directivo directivo) {
		if (directivo == null || directivo.getDocente() == null || directivo.getDocente().getId() == null) {
			throw new RuntimeException("Debes seleccionar un docente.");
		}

		boolean nuevoRegistro = directivo.getId() == null;

		Docente docente = repoDocente.findByIdAndEstatus(directivo.getDocente().getId(), 1);

		if (docente == null) {
			throw new RuntimeException("El docente seleccionado no existe o está inactivo.");
		}

		Directivo directivoGuardar;

		if (directivo.getId() != null) {
			directivoGuardar = repoDirectivo.findById(directivo.getId()).orElse(new Directivo());

			if (directivoGuardar.getClaveDirectivo() == null
					|| directivoGuardar.getClaveDirectivo().trim().isEmpty()) {
				directivoGuardar.setClaveDirectivo(generarClaveDirectivo());
			}
		} else {
			if (repoDirectivo.existsByDocenteAndEstatus(docente, 1)) {
				throw new RuntimeException("Este docente ya está registrado como directivo o jefe.");
			}

			directivoGuardar = new Directivo();
			directivoGuardar.setClaveDirectivo(generarClaveDirectivo());
		}

		directivoGuardar.setDocente(docente);
		directivoGuardar.setTipoDirectivo(directivo.getTipoDirectivo());
		directivoGuardar.setPuesto(directivo.getPuesto());
		directivoGuardar.setDepartamento(resolverDepartamento(directivo));

		Directivo directivoActivoMismoDocente = buscarPorDocente(docente.getId());
		if (directivoActivoMismoDocente != null
				&& (directivo.getId() == null || !directivoActivoMismoDocente.getId().equals(directivo.getId()))) {
			throw new RuntimeException("Este docente ya esta registrado como directivo activo.");
		}

		if (directivo.getFirmaPath() != null) {
			directivoGuardar.setFirmaPath(directivo.getFirmaPath());
		}

		if (directivo.getSelloPath() != null) {
			directivoGuardar.setSelloPath(directivo.getSelloPath());
		}

		if (directivoGuardar.getEstatus() == null) {
			directivoGuardar.setEstatus(1);
		}

		repoDirectivo.save(directivoGuardar);

		if (nuevoRegistro) {
			crearUsuarioAutomatico(directivoGuardar);
		}
	}

	private void crearUsuarioAutomatico(Directivo directivo) {
		try {
			if (directivo == null || directivo.getDocente() == null || directivo.getDocente().getId() == null) {
				return;
			}

			String perfil = obtenerPerfilPorTipoDirectivo(directivo.getTipoDirectivo());

			serviceUsuario.crearUsuarioParaDocente(directivo.getDocente().getId(), perfil);

		} catch (Exception e) {
			System.out.println("Error al crear usuario automático para directivo: " + e.getMessage());
		}
	}

	private String obtenerPerfilPorTipoDirectivo(TipoDirectivo tipoDirectivo) {
		if (tipoDirectivo == null) {
			return "JEFE_DEPARTAMENTO";
		}

		String tipo = tipoDirectivo.name().toUpperCase();

		if (tipo.contains("DIVISION")) {
			return "DIVISION_ESTUDIOS";
		}

		return "JEFE_DEPARTAMENTO";
	}

	private String resolverDepartamento(Directivo directivo) {
		String departamento = directivo.getDepartamento() != null ? directivo.getDepartamento().trim() : "";

		if (directivo.getTipoDirectivo() != TipoDirectivo.JEFE_DEPARTAMENTO) {
			return departamento;
		}

		if (departamento.isEmpty()) {
			throw new RuntimeException("Debes seleccionar la carrera del jefe de departamento.");
		}

		Carrera carrera = repoCarrera.findByEstatusOrderByIdAsc(1)
				.stream()
				.filter(item -> item != null
						&& item.getNombre() != null
						&& normalizar(item.getNombre()).equals(normalizar(departamento)))
				.findFirst()
				.orElse(null);

		if (carrera == null) {
			throw new RuntimeException("La carrera seleccionada no es valida para jefe de departamento.");
		}

		Directivo existente = repoDirectivo.findByTipoDirectivoAndDepartamentoAndEstatus(
				TipoDirectivo.JEFE_DEPARTAMENTO,
				carrera.getNombre(),
				1);

		if (existente != null && (directivo.getId() == null || !existente.getId().equals(directivo.getId()))) {
			throw new RuntimeException("Ya existe un jefe de departamento activo para la carrera " + carrera.getNombre() + ".");
		}

		return carrera.getNombre();
	}

	private String normalizar(String valor) {
		if (valor == null) {
			return "";
		}

		return java.text.Normalizer.normalize(valor.trim().toUpperCase(), java.text.Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "");
	}

	private boolean coincideJefeConCarrera(Directivo directivo, String carreraNormalizada) {
		if (directivo == null || carreraNormalizada == null || carreraNormalizada.isEmpty()) {
			return false;
		}

		if (coincideDepartamentoConCarrera(directivo.getDepartamento(), carreraNormalizada)) {
			return true;
		}

		if (directivo.getDocente() == null || directivo.getDocente().getCarrerasHabilitadas() == null) {
			return false;
		}

		return directivo.getDocente().getCarrerasHabilitadas().stream()
				.filter(carrera -> carrera != null && carrera.getNombre() != null)
				.anyMatch(carrera -> coincideDepartamentoConCarrera(carrera.getNombre(), carreraNormalizada));
	}

	private boolean coincideDepartamentoConCarrera(String departamento, String carreraNormalizada) {
		String departamentoNormalizado = normalizar(departamento);

		if (departamentoNormalizado.isEmpty() || carreraNormalizada == null || carreraNormalizada.isEmpty()) {
			return false;
		}

		return departamentoNormalizado.equals(carreraNormalizada)
				|| departamentoNormalizado.contains(carreraNormalizada)
				|| carreraNormalizada.contains(departamentoNormalizado);
	}

	@Override
	public void eliminar(Integer id) {
		Directivo directivo = repoDirectivo.findById(id).orElse(null);

		if (directivo != null) {
			directivo.setEstatus(0);
			repoDirectivo.save(directivo);
		}
	}

	@Override
	public void recuperar(Integer id) {
		Directivo directivo = repoDirectivo.findById(id).orElse(null);

		if (directivo != null) {
			directivo.setEstatus(1);
			repoDirectivo.save(directivo);
		}
	}

	private String generarClaveDirectivo() {
		List<Directivo> directivos = repoDirectivo.findAll();

		int consecutivo = directivos.size() + 1;
		String clave;

		do {
			clave = String.format("ITCH-DIR-%04d", consecutivo);
			consecutivo++;
		} while (repoDirectivo.existsByClaveDirectivo(clave));

		return clave;
	}
}
