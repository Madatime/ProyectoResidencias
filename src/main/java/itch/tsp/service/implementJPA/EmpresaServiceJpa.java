package itch.tsp.service.implementJPA;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import itch.tsp.model.Empresa;
import itch.tsp.repository.EmpresaRepository;
import itch.tsp.service.IEmpresaService;

@Primary
@Service
public class EmpresaServiceJpa implements IEmpresaService {

	@Autowired
	private EmpresaRepository repoEmpresa;

	@Override
	public List<Empresa> buscarTodasActivas() {
		return repoEmpresa.findByEstatusOrderByIdDesc(1);
	}

	@Override
	public List<Empresa> buscarTodasInactivas() {
		return repoEmpresa.findByEstatusOrderByIdDesc(0);
	}

	@Override
	public List<Empresa> buscarEmpresas(String texto) {

		if (texto == null || texto.trim().isEmpty()) {
			return buscarTodasActivas();
		}

		String t = texto.trim();

		return repoEmpresa
				.findByEstatusAndNombreContainingIgnoreCaseOrEstatusAndGiroContainingIgnoreCaseOrEstatusAndRepresentanteContainingIgnoreCaseOrEstatusAndDuenoContainingIgnoreCase(
						1, t,
						1, t,
						1, t,
						1, t);
	}

	@Override
	public Empresa buscarPorIdEmpresa(Integer idEmpresa) {
		Optional<Empresa> optional = repoEmpresa.findById(idEmpresa);

		if (optional.isPresent()) {
			Empresa empresa = optional.get();

			if (empresa.getEstatus() != null && empresa.getEstatus() == 1) {
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

		if (empresa.getEstatus() == null) {
			empresa.setEstatus(1);
		}
		
		if ("ACTIVO".equalsIgnoreCase(empresa.getConvenio())) {
			if (empresa.getVigenciaConvenio() == null
					|| !(empresa.getVigenciaConvenio() == 2
					|| empresa.getVigenciaConvenio() == 3
					|| empresa.getVigenciaConvenio() == 5)) {
				throw new IllegalArgumentException("La vigencia del convenio debe ser de 2, 3 o 5 años.");
			}
		} else {
			empresa.setVigenciaConvenio(null);
			empresa.setAnioConvenio(null);
			empresa.setAnioFinConvenio(null);
		}

		repoEmpresa.save(empresa);
	}

	@Override
	public boolean existeNombre(String nombre) {
		String nombreNormalizado = normalizarNombre(nombre);
		List<Empresa> lista = repoEmpresa.findByNombreAndEstatus(nombreNormalizado, 1);
		return !lista.isEmpty();
	}

	@Override
	public boolean existeNombreParaOtroRegistro(String nombre, Integer id) {
		String nombreNormalizado = normalizarNombre(nombre);
		List<Empresa> lista = repoEmpresa.findByNombreAndEstatusAndIdNot(nombreNormalizado, 1, id);
		return !lista.isEmpty();
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
		Optional<Empresa> optional = repoEmpresa.findById(idEmpresa);

		if (optional.isPresent()) {
			Empresa empresa = optional.get();
			empresa.setEstatus(0);
			repoEmpresa.save(empresa);
		}
	}

	@Override
	public void recuperar(Integer idEmpresa) {
		Optional<Empresa> optional = repoEmpresa.findById(idEmpresa);

		if (optional.isPresent()) {
			Empresa empresa = optional.get();
			empresa.setEstatus(1);
			repoEmpresa.save(empresa);
		}
	}
}