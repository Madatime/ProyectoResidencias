package itch.tsp.service.implementJPA;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import itch.tsp.model.BancoProyecto;
import itch.tsp.model.Carrera;
import itch.tsp.model.Empresa;
import itch.tsp.model.EstadoBancoProyecto;
import itch.tsp.model.OrigenBancoProyecto;
import itch.tsp.model.Residente;
import itch.tsp.repository.BancoProyectoRepository;
import itch.tsp.repository.CarreraRepository;
import itch.tsp.repository.EmpresaRepository;
import itch.tsp.repository.ResidenteRepository;
import itch.tsp.service.IBancoProyectoService;

@Primary
@Service
public class BancoProyectoServiceJpa implements IBancoProyectoService {

	@Autowired
	private BancoProyectoRepository repoBancoProyecto;

	@Autowired
	private EmpresaRepository repoEmpresa;

	@Autowired
	private CarreraRepository repoCarrera;

	@Autowired
	private ResidenteRepository repoResidente;

	@Override
	public List<BancoProyecto> buscarTodosActivos() {
		return repoBancoProyecto.findByEstatusOrderByIdDesc(1);
	}

	@Override
	public List<BancoProyecto> buscarTodosInactivos() {
		return repoBancoProyecto.findByEstatusOrderByIdDesc(0);
	}

	@Override
	public List<BancoProyecto> buscarDisponibles() {
		return repoBancoProyecto.findByEstadoAndEstatusOrderByIdDesc(EstadoBancoProyecto.DISPONIBLE, 1);
	}

	@Override
	public List<BancoProyecto> buscarPendientesRevision() {
		return repoBancoProyecto.findByEstadoAndEstatusOrderByIdDesc(EstadoBancoProyecto.PENDIENTE_REVISION, 1);
	}

	@Override
	public List<BancoProyecto> buscarPorTexto(String texto) {
		if (texto == null || texto.trim().isEmpty()) {
			return buscarTodosActivos();
		}

		String busqueda = texto.trim();

		return repoBancoProyecto
				.findByEstatusAndNombreProyectoContainingIgnoreCaseOrEstatusAndEmpresa_NombreContainingIgnoreCaseOrEstatusAndCarrera_NombreContainingIgnoreCase(
						1, busqueda,
						1, busqueda,
						1, busqueda);
	}

	@Override
	public BancoProyecto buscarPorId(Integer id) {
		if (id == null) {
			return null;
		}

		return repoBancoProyecto.findByIdAndEstatus(id, 1);
	}

	@Override
	public void guardar(BancoProyecto proyecto) {
		if (proyecto == null) {
			throw new RuntimeException("No se recibió información del proyecto.");
		}

		normalizar(proyecto);
		asignarRelaciones(proyecto);

		if (proyecto.getEstatus() == null) {
			proyecto.setEstatus(1);
		}

		if (proyecto.getFechaPropuesta() == null) {
			proyecto.setFechaPropuesta(LocalDate.now());
		}

		if (proyecto.getOrigen() == null) {
			proyecto.setOrigen(OrigenBancoProyecto.BANCO);
		}

		if (proyecto.getEstado() == null) {
			proyecto.setEstado(EstadoBancoProyecto.DISPONIBLE);
		}

		repoBancoProyecto.save(proyecto);
	}

	@Override
	public void proponerProyecto(BancoProyecto proyecto, Integer idResidente) {
		if (proyecto == null) {
			throw new RuntimeException("No se recibió información de la propuesta.");
		}

		normalizar(proyecto);
		asignarRelaciones(proyecto);

		if (idResidente == null) {
			throw new RuntimeException("No se recibió el residente que propone el proyecto.");
		}

		Residente residente = repoResidente.findById(idResidente).orElse(null);

		if (residente == null || residente.getEstatus() == null || residente.getEstatus() != 1) {
			throw new RuntimeException("El residente no existe o está inactivo.");
		}

		proyecto.setPropuestoPor(residente);
		proyecto.setOrigen(OrigenBancoProyecto.PROPUESTO);
		proyecto.setEstado(EstadoBancoProyecto.PENDIENTE_REVISION);
		proyecto.setFechaPropuesta(LocalDate.now());
		proyecto.setFechaRevision(null);
		proyecto.setEstatus(1);

		repoBancoProyecto.save(proyecto);
	}

	@Override
	public void revisarProyecto(Integer idProyecto, EstadoBancoProyecto estado, String observaciones) {
		BancoProyecto proyecto = buscarPorId(idProyecto);

		if (proyecto == null) {
			throw new RuntimeException("El proyecto no existe o está inactivo.");
		}

		if (estado == null) {
			throw new RuntimeException("Debes seleccionar un estado de revisión.");
		}

		if (estado == EstadoBancoProyecto.ASIGNADO || estado == EstadoBancoProyecto.INACTIVO) {
			throw new RuntimeException("Estado de revisión no válido.");
		}

		proyecto.setEstado(estado);
		proyecto.setObservaciones(observaciones != null ? observaciones.trim() : null);
		proyecto.setFechaRevision(LocalDate.now());

		repoBancoProyecto.save(proyecto);
	}

	@Override
	public void marcarAsignado(Integer idProyecto) {
		BancoProyecto proyecto = buscarPorId(idProyecto);

		if (proyecto == null) {
			throw new RuntimeException("El proyecto no existe o está inactivo.");
		}

		proyecto.setEstado(EstadoBancoProyecto.ASIGNADO);
		repoBancoProyecto.save(proyecto);
	}

	@Override
	public void eliminar(Integer id) {
		BancoProyecto proyecto = repoBancoProyecto.findById(id).orElse(null);

		if (proyecto != null) {
			proyecto.setEstatus(0);
			repoBancoProyecto.save(proyecto);
		}
	}

	@Override
	public void recuperar(Integer id) {
		BancoProyecto proyecto = repoBancoProyecto.findById(id).orElse(null);

		if (proyecto != null) {
			proyecto.setEstatus(1);
			repoBancoProyecto.save(proyecto);
		}
	}

	private void normalizar(BancoProyecto proyecto) {
		if (proyecto.getNombreProyecto() != null) {
			proyecto.setNombreProyecto(proyecto.getNombreProyecto().trim());
		}

		if (proyecto.getDescripcion() != null) {
			proyecto.setDescripcion(proyecto.getDescripcion().trim());
		}

		if (proyecto.getObjetivo() != null) {
			proyecto.setObjetivo(proyecto.getObjetivo().trim());
		}

		if (proyecto.getPeriodo() != null) {
			proyecto.setPeriodo(proyecto.getPeriodo().trim());
		}

		if (proyecto.getObservaciones() != null) {
			proyecto.setObservaciones(proyecto.getObservaciones().trim());
		}
	}

	private void asignarRelaciones(BancoProyecto proyecto) {
		if (proyecto.getEmpresa() == null || proyecto.getEmpresa().getId() == null) {
			throw new RuntimeException("Debes seleccionar una empresa.");
		}

		Empresa empresa = repoEmpresa.findByIdAndEstatus(proyecto.getEmpresa().getId(), 1);

		if (empresa == null) {
			throw new RuntimeException("La empresa seleccionada no existe o está inactiva.");
		}

		proyecto.setEmpresa(empresa);

		if (proyecto.getCarrera() == null || proyecto.getCarrera().getId() == null) {
			throw new RuntimeException("Debes seleccionar una carrera.");
		}

		Carrera carrera = repoCarrera.findByIdAndEstatus(proyecto.getCarrera().getId(), 1);

		if (carrera == null) {
			throw new RuntimeException("La carrera seleccionada no existe o está inactiva.");
		}

		proyecto.setCarrera(carrera);
	}
}