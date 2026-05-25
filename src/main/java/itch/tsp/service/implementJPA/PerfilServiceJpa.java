package itch.tsp.service.implementJPA;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import itch.tsp.model.Perfil;
import itch.tsp.repository.PerfilRepository;
import itch.tsp.service.IPerfilService;

@Primary
@Service
public class PerfilServiceJpa implements IPerfilService {

	@Autowired
	private PerfilRepository repoPerfil;

	@Override
	public List<Perfil> buscarTodosActivos() {
		return repoPerfil.findByEstatusOrderByIdAsc(1);
	}

	@Override
	public Perfil buscarPorNombre(String nombre) {
		return repoPerfil.findByNombreAndEstatus(nombre, 1);
	}

	@Override
	public void guardar(Perfil perfil) {
		if (perfil.getNombre() != null) {
			perfil.setNombre(perfil.getNombre().trim().toUpperCase());
		}

		if (perfil.getEstatus() == null) {
			perfil.setEstatus(1);
		}

		repoPerfil.save(perfil);
	}
}