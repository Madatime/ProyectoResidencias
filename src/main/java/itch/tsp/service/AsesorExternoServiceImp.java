package itch.tsp.service;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import itch.tsp.model.AsesorExterno;

@Service
public class AsesorExternoServiceImp implements IAsesorExternoService {

	private List<AsesorExterno> listaAsesoresExternos;

	public AsesorExternoServiceImp() {
		listaAsesoresExternos = new LinkedList<>();
	}

	@Override
	public List<AsesorExterno> buscarTodosActivos() {
		List<AsesorExterno> listaActivos = new LinkedList<>();

		for (AsesorExterno asesor : listaAsesoresExternos) {
			if (asesor.getEstatus() != null && asesor.getEstatus() == 1) {
				listaActivos.add(asesor);
			}
		}

		return listaActivos;
	}

	@Override
	public List<AsesorExterno> buscarTodosInactivos() {
		List<AsesorExterno> listaInactivos = new LinkedList<>();

		for (AsesorExterno asesor : listaAsesoresExternos) {
			if (asesor.getEstatus() != null && asesor.getEstatus() == 0) {
				listaInactivos.add(asesor);
			}
		}

		return listaInactivos;
	}

	@Override
	public List<AsesorExterno> buscarAsesoresExternos(String texto) {
		List<AsesorExterno> resultado = new LinkedList<>();

		if (texto == null || texto.trim().isEmpty()) {
			return buscarTodosActivos();
		}

		String textoBusqueda = texto.trim().toLowerCase();

		for (AsesorExterno asesor : listaAsesoresExternos) {
			if (asesor.getEstatus() != null && asesor.getEstatus() == 1) {

				boolean coincideNombre = asesor.getNombre() != null
						&& asesor.getNombre().toLowerCase().contains(textoBusqueda);

				boolean coincideApellidos = asesor.getApellidos() != null
						&& asesor.getApellidos().toLowerCase().contains(textoBusqueda);

				boolean coincideEmpresa = asesor.getEmpresa() != null
						&& asesor.getEmpresa().toLowerCase().contains(textoBusqueda);

				if (coincideNombre || coincideApellidos || coincideEmpresa) {
					resultado.add(asesor);
				}
			}
		}

		return resultado;
	}

	@Override
	public void guardarAsesorExterno(AsesorExterno asesorExterno) {
		if (asesorExterno.getId() != null && asesorExterno.getId() > 0) {
			AsesorExterno existente = buscarPorIdGeneral(asesorExterno.getId());

			if (existente != null) {
				int indice = listaAsesoresExternos.indexOf(existente);
				listaAsesoresExternos.set(indice, asesorExterno);
				return;
			}
		}

		Integer nuevoId = 1;

		for (AsesorExterno asesor : listaAsesoresExternos) {
			if (asesor.getId() != null && asesor.getId() >= nuevoId) {
				nuevoId = asesor.getId() + 1;
			}
		}

		asesorExterno.setId(nuevoId);

		if (asesorExterno.getEstatus() == null) {
			asesorExterno.setEstatus(1);
		}

		listaAsesoresExternos.add(asesorExterno);
	}

	@Override
	public void guardarAsesorExternoConArchivos(
			AsesorExterno asesorExterno,
			MultipartFile foto,
			MultipartFile documento) {

		guardarAsesorExterno(asesorExterno);
	}

	@Override
	public AsesorExterno buscarPorIdAsesorExterno(Integer idAsesorExterno) {
		for (AsesorExterno asesor : listaAsesoresExternos) {
			if (asesor.getId() != null
					&& asesor.getId().equals(idAsesorExterno)
					&& asesor.getEstatus() != null
					&& asesor.getEstatus() == 1) {
				return asesor;
			}
		}

		return null;
	}

	private AsesorExterno buscarPorIdGeneral(Integer idAsesorExterno) {
		for (AsesorExterno asesor : listaAsesoresExternos) {
			if (asesor.getId() != null && asesor.getId().equals(idAsesorExterno)) {
				return asesor;
			}
		}

		return null;
	}

	@Override
	public void eliminar(Integer idAsesorExterno) {
		AsesorExterno asesor = buscarPorIdGeneral(idAsesorExterno);

		if (asesor != null) {
			asesor.setEstatus(0);
		}
	}

	@Override
	public void recuperar(Integer idAsesorExterno) {
		AsesorExterno asesor = buscarPorIdGeneral(idAsesorExterno);

		if (asesor != null) {
			asesor.setEstatus(1);
		}
	}
}