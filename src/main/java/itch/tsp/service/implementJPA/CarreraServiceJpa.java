package itch.tsp.service.implementJPA;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import itch.tsp.model.Carrera;
import itch.tsp.repository.CarreraRepository;
import itch.tsp.service.ICarreraService;

@Service
public class CarreraServiceJpa implements ICarreraService {

	@Autowired
	private CarreraRepository repo;

	@Override
	public List<Carrera> buscarTodas() {
		return buscarTodasActivas();
	}

	@Override
	public List<Carrera> buscarTodosActivos() {
		return buscarTodasActivas();
	}

	@Override
	public List<Carrera> buscarTodasActivas() {
		return repo.findByEstatusOrderByIdAsc(1);
	}

	@Override
	public List<Carrera> buscarTodasInactivas() {
		return repo.findByEstatusOrderByIdAsc(0);
	}

	@Override
	public Carrera buscarPorId(Integer id) {
		if (id == null) {
			return null;
		}

		return repo.findById(id).orElse(null);
	}

	@Override
	public void guardar(Carrera carrera) {
		if (carrera == null) {
			throw new RuntimeException("No se recibió información de la carrera.");
		}

		if (carrera.getEstatus() == null) {
			carrera.setEstatus(1);
		}

		repo.save(carrera);
	}

	@Override
	public void eliminar(Integer id) {
		Carrera carrera = buscarPorId(id);

		if (carrera != null) {
			carrera.setEstatus(0);
			repo.save(carrera);
		}
	}

	@Override
	public void recuperar(Integer id) {
		Carrera carrera = buscarPorId(id);

		if (carrera != null) {
			carrera.setEstatus(1);
			repo.save(carrera);
		}
	}
}