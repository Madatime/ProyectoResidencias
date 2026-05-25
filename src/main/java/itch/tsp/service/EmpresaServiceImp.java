package itch.tsp.service;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import itch.tsp.model.Empresa;

@Service
public class EmpresaServiceImp implements IEmpresaService {

	private List<Empresa> listaEmpresas;

	public EmpresaServiceImp() {
		listaEmpresas = new LinkedList<>();
	}

	@Override
	public List<Empresa> buscarTodasActivas() {
		List<Empresa> activas = new LinkedList<>();

		for (Empresa empresa : listaEmpresas) {
			if (empresa.getEstatus() != null && empresa.getEstatus() == 1) {
				activas.add(empresa);
			}
		}

		return activas;
	}

	@Override
	public List<Empresa> buscarTodasInactivas() {
		List<Empresa> inactivas = new LinkedList<>();

		for (Empresa empresa : listaEmpresas) {
			if (empresa.getEstatus() != null && empresa.getEstatus() == 0) {
				inactivas.add(empresa);
			}
		}

		return inactivas;
	}

	@Override
	public List<Empresa> buscarEmpresas(String texto) {
		List<Empresa> resultado = new LinkedList<>();

		if (texto == null || texto.trim().isEmpty()) {
			return buscarTodasActivas();
		}

		String textoBusqueda = texto.trim().toLowerCase();

		for (Empresa empresa : listaEmpresas) {
			if (empresa.getEstatus() != null && empresa.getEstatus() == 1) {

				boolean coincideNombre = empresa.getNombre() != null
						&& empresa.getNombre().toLowerCase().contains(textoBusqueda);

				boolean coincideGiro = empresa.getGiro() != null
						&& empresa.getGiro().toLowerCase().contains(textoBusqueda);

				boolean coincideRepresentante = empresa.getRepresentante() != null
						&& empresa.getRepresentante().toLowerCase().contains(textoBusqueda);

				boolean coincideDueno = empresa.getDueno() != null
						&& empresa.getDueno().toLowerCase().contains(textoBusqueda);

				if (coincideNombre || coincideGiro || coincideRepresentante || coincideDueno) {
					resultado.add(empresa);
				}
			}
		}

		return resultado;
	}

	@Override
	public Empresa buscarPorIdEmpresa(Integer idEmpresa) {
		for (Empresa empresa : listaEmpresas) {
			if (empresa.getId() != null && empresa.getId().equals(idEmpresa) && empresa.getEstatus() != null
					&& empresa.getEstatus() == 1) {
				return empresa;
			}
		}

		return null;
	}

	private Empresa buscarPorIdGeneral(Integer idEmpresa) {
		for (Empresa empresa : listaEmpresas) {
			if (empresa.getId() != null && empresa.getId().equals(idEmpresa)) {
				return empresa;
			}
		}

		return null;
	}

	@Override
	public void guardarEmpresa(Empresa empresa) {
		if (empresa.getNombre() != null) {
			empresa.setNombre(normalizarNombre(empresa.getNombre()));
		}

		if (empresa.getGiro() != null) {
			empresa.setGiro(empresa.getGiro().trim());
		}

		if (empresa.getDireccion() != null) {
			empresa.setDireccion(empresa.getDireccion().trim());
		}

		if (empresa.getTelefono() != null) {
			empresa.setTelefono(empresa.getTelefono().trim());
		}

		if (empresa.getCorreo() != null) {
			empresa.setCorreo(empresa.getCorreo().trim());
		}

		if (empresa.getRepresentante() != null) {
			empresa.setRepresentante(empresa.getRepresentante().trim());
		}

		if (empresa.getPuestoRepresentante() != null) {
			empresa.setPuestoRepresentante(empresa.getPuestoRepresentante().trim());
		}

		if (empresa.getDueno() != null) {
			empresa.setDueno(empresa.getDueno().trim());
		}

		if (empresa.getConvenio() != null) {
			empresa.setConvenio(empresa.getConvenio().trim().toUpperCase());
		}

		if (empresa.getId() != null && empresa.getId() > 0) {
			Empresa existente = buscarPorIdGeneral(empresa.getId());

			if (existente != null) {
				int indice = listaEmpresas.indexOf(existente);
				listaEmpresas.set(indice, empresa);
				return;
			}
		}

		Integer nuevoId = 1;

		for (Empresa e : listaEmpresas) {
			if (e.getId() != null && e.getId() >= nuevoId) {
				nuevoId = e.getId() + 1;
			}
		}

		empresa.setId(nuevoId);

		if (empresa.getEstatus() == null) {
			empresa.setEstatus(1);
		}

		listaEmpresas.add(empresa);
	}

	@Override
	public boolean existeNombre(String nombre) {
		String nombreNormalizado = normalizarNombre(nombre);

		for (Empresa empresa : listaEmpresas) {
			if (empresa.getEstatus() != null && empresa.getEstatus() == 1 && empresa.getNombre() != null
					&& empresa.getNombre().equals(nombreNormalizado)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean existeNombreParaOtroRegistro(String nombre, Integer id) {
		String nombreNormalizado = normalizarNombre(nombre);

		for (Empresa empresa : listaEmpresas) {
			if (empresa.getEstatus() != null && empresa.getEstatus() == 1 && empresa.getNombre() != null
					&& empresa.getNombre().equals(nombreNormalizado) && empresa.getId() != null
					&& !empresa.getId().equals(id)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String normalizarNombre(String nombre) {
		if (nombre == null) {
			return null;
		}

		return nombre.trim().toUpperCase();
	}

	@Override
	public void eliminar(Integer idEmpresa) {
		Iterator<Empresa> iterator = listaEmpresas.iterator();

		while (iterator.hasNext()) {
			Empresa empresa = iterator.next();

			if (empresa.getId() != null && empresa.getId().equals(idEmpresa)) {
				empresa.setEstatus(0);
				break;
			}
		}
	}

	@Override
	public void recuperar(Integer idEmpresa) {
		Empresa empresa = buscarPorIdGeneral(idEmpresa);

		if (empresa != null) {
			empresa.setEstatus(1);
		}
	}
}