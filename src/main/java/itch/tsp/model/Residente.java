package itch.tsp.model;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "residente")
public class Residente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "idEstudiante")
	private Estudiante estudiante;

	private String fotoPath;
	private Integer estatus = 1;

	@OneToMany(mappedBy = "residente")
	private List<Residencia> residencias;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Estudiante getEstudiante() {
		return estudiante;
	}

	public void setEstudiante(Estudiante estudiante) {
		this.estudiante = estudiante;
	}

	public String getFotoPath() {
		return fotoPath;
	}

	public void setFotoPath(String fotoPath) {
		this.fotoPath = fotoPath;
	}

	public Integer getEstatus() {
		return estatus;
	}

	public void setEstatus(Integer estatus) {
		this.estatus = estatus;
	}

	public List<Residencia> getResidencias() {
		return residencias;
	}

	public void setResidencias(List<Residencia> residencias) {
		this.residencias = residencias;
	}

	public String getMatricula() {
		return estudiante != null ? estudiante.getMatricula() : "";
	}

	public String getNombre() {
		return estudiante != null ? estudiante.getNombre() : "";
	}

	public String getApellidos() {
		return estudiante != null ? estudiante.getApellidos() : "";
	}

	public String getNombreCompleto() {
		return estudiante != null ? estudiante.getNombreCompleto() : "";
	}

	public String getSexo() {
		return estudiante != null ? estudiante.getSexo() : "";
	}

	public String getSemestre() {
		return estudiante != null ? estudiante.getSemestre() : "";
	}

	public String getTelefono() {
		return estudiante != null ? estudiante.getTelefono() : "";
	}

	public String getCorreo() {
		return estudiante != null ? estudiante.getCorreo() : "";
	}
}