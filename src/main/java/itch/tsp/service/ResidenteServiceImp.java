package itch.tsp.service;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import itch.tsp.model.Residente;

@Service
public class ResidenteServiceImp implements IResidenteService {

	private List<Residente> listaResidentes = new LinkedList<>();

	@Override
	public List<Residente> buscarTodosActivos() {
		List<Residente> listaActivos = new LinkedList<>();

		for (Residente residente : listaResidentes) {
			if (residente.getEstatus() != null && residente.getEstatus() == 1) {
				listaActivos.add(residente);
			}
		}

		return listaActivos;
	}

	@Override
	public List<Residente> buscarTodosInactivos() {
		List<Residente> listaInactivos = new LinkedList<>();

		for (Residente residente : listaResidentes) {
			if (residente.getEstatus() != null && residente.getEstatus() == 0) {
				listaInactivos.add(residente);
			}
		}

		return listaInactivos;
	}

	@Override
	public List<Residente> buscarResidentes(String texto) {
		List<Residente> resultado = new LinkedList<>();

		if (texto == null || texto.trim().isEmpty()) {
			return buscarTodosActivos();
		}

		String textoBusqueda = texto.trim().toLowerCase();

		for (Residente residente : listaResidentes) {
			if (residente.getEstatus() != null && residente.getEstatus() == 1) {
				boolean coincideMatricula = residente.getMatricula() != null
						&& residente.getMatricula().toLowerCase().contains(textoBusqueda);

				boolean coincideNombre = residente.getNombre() != null
						&& residente.getNombre().toLowerCase().contains(textoBusqueda);

				boolean coincideApellidos = residente.getApellidos() != null
						&& residente.getApellidos().toLowerCase().contains(textoBusqueda);

				if (coincideMatricula || coincideNombre || coincideApellidos) {
					resultado.add(residente);
				}
			}
		}

		return resultado;
	}

	@Override
	public void guardarResidente(Residente residente) {
		if (residente.getId() != null && residente.getId() > 0) {
			Residente existente = buscarPorIdGeneral(residente.getId());

			if (existente != null) {
				int indice = listaResidentes.indexOf(existente);
				listaResidentes.set(indice, residente);
				return;
			}
		}

		Integer nuevoId = 1;
		for (Residente r : listaResidentes) {
			if (r.getId() != null && r.getId() >= nuevoId) {
				nuevoId = r.getId() + 1;
			}
		}

		residente.setId(nuevoId);

		if (residente.getEstatus() == null) {
			residente.setEstatus(1);
		}

		listaResidentes.add(residente);
	}

	@Override
	public void guardarResidenteConArchivos(Residente residente, MultipartFile foto, MultipartFile documento) {
		guardarResidente(residente);
	}

	@Override
	public Residente buscarPorIdResidente(Integer idResidente) {
		for (Residente residente : listaResidentes) {
			if (residente.getId() != null
					&& residente.getId().equals(idResidente)
					&& residente.getEstatus() != null
					&& residente.getEstatus() == 1) {
				return residente;
			}
		}
		return null;
	}

	private Residente buscarPorIdGeneral(Integer idResidente) {
		for (Residente residente : listaResidentes) {
			if (residente.getId() != null && residente.getId().equals(idResidente)) {
				return residente;
			}
		}
		return null;
	}

	@Override
	public boolean existeMatricula(String matricula) {
		String matriculaNormalizada = normalizarMatricula(matricula);

		for (Residente residente : listaResidentes) {
			if (residente.getEstatus() != null
					&& residente.getEstatus() == 1
					&& residente.getMatricula() != null
					&& residente.getMatricula().equals(matriculaNormalizada)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean existeMatriculaParaOtroRegistro(String matricula, Integer id) {
		String matriculaNormalizada = normalizarMatricula(matricula);

		for (Residente residente : listaResidentes) {
			if (residente.getEstatus() != null
					&& residente.getEstatus() == 1
					&& residente.getMatricula() != null
					&& residente.getMatricula().equals(matriculaNormalizada)
					&& residente.getId() != null
					&& !residente.getId().equals(id)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String normalizarMatricula(String matricula) {
		return matricula == null ? null : matricula.trim().toUpperCase();
	}

	@Override
	public boolean matriculaValida(String matricula) {
		if (matricula == null) {
			return false;
		}
		return normalizarMatricula(matricula).matches("^C?\\d{7,8}$");
	}

	@Override
	public void eliminar(Integer idResidente) {
		Iterator<Residente> iterator = listaResidentes.iterator();

		while (iterator.hasNext()) {
			Residente residente = iterator.next();
			if (residente.getId() != null && residente.getId().equals(idResidente)) {
				residente.setEstatus(0);
				break;
			}
		}
	}

	@Override
	public void recuperar(Integer idResidente) {
		Residente residente = buscarPorIdGeneral(idResidente);

		if (residente != null) {
			residente.setEstatus(1);
		}
	}
}