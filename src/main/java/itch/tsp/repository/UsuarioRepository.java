package itch.tsp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import itch.tsp.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

	Usuario findByUsernameAndEstatus(String username, Integer estatus);

	Usuario findByEmailAndEstatus(String email, Integer estatus);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	boolean existsByDocente_Id(Integer idDocente);

	boolean existsByResidente_Id(Integer idResidente);

	boolean existsByAsesorExterno_Id(Integer idAsesorExterno);

	Usuario findByAsesorExterno_Id(Integer idAsesorExterno);
}
