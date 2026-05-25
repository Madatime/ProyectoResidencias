package itch.tsp.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import itch.tsp.model.Carrera;
import itch.tsp.model.Docente;
import itch.tsp.repository.CarreraRepository;
import itch.tsp.repository.DocenteRepository;

@Service
public class DocenteServiceImp implements IDocenteService {

	@Autowired
	private DocenteRepository repoDocente;

	@Autowired
	private CarreraRepository repoCarrera;

	@Override
	public List<Docente> buscarTodosActivos() {
		return repoDocente.findByEstatusOrderByIdAsc(1);
	}

	@Override
	public List<Docente> buscarTodosInactivos() {
		return repoDocente.findByEstatusOrderByIdAsc(0);
	}

	@Override
	public Docente buscarPorId(Integer id) {
		return repoDocente.findByIdAndEstatus(id, 1);
	}

	@Override
	public void guardar(Docente docente, List<Integer> idsCarreras) {

		if (docente.getId() == null) {
			docente.setNoEmpleado(generarNoEmpleado());
		} else {
			Docente existente = repoDocente.findById(docente.getId()).orElse(null);

			if (existente != null) {
				docente.setNoEmpleado(existente.getNoEmpleado());
				docente.setFotoPath(existente.getFotoPath());
			}
		}

		if (docente.getEstatus() == null) {
			docente.setEstatus(1);
		}

		List<Carrera> carreras = new ArrayList<>();

		if (idsCarreras != null) {
			for (Integer idCarrera : idsCarreras) {
				Carrera carrera = repoCarrera.findById(idCarrera).orElse(null);
				if (carrera != null) {
					carreras.add(carrera);
				}
			}
		}

		docente.setCarrerasHabilitadas(carreras);

		repoDocente.save(docente);
	}

	@Override
	public void eliminar(Integer id) {
		Docente docente = repoDocente.findById(id).orElse(null);

		if (docente != null) {
			docente.setEstatus(0);
			repoDocente.save(docente);
		}
	}

	@Override
	public void recuperar(Integer id) {
		Docente docente = repoDocente.findById(id).orElse(null);

		if (docente != null) {
			docente.setEstatus(1);
			repoDocente.save(docente);
		}
	}

	private String generarNoEmpleado() {
		List<Docente> docentes = repoDocente.findAll();

		int consecutivo = docentes.size() + 1;
		String clave;

		do {
			clave = String.format("ITCH-DOC-%04d", consecutivo);
			consecutivo++;
		} while (repoDocente.existsByNoEmpleado(clave));

		return clave;
	}
}