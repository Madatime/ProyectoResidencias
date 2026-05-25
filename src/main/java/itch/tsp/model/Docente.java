package itch.tsp.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "docentes")
public class Docente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "noEmpleado", unique = true, nullable = false, length = 20)
	private String noEmpleado;

	@Column(nullable = false, length = 80)
	private String nombre;

	@Column(nullable = false, length = 120)
	private String apellidos;

	@Column(length = 120)
	private String correo;

	@Column(length = 20)
	private String telefono;

	private String fotoPath;

	private Integer estatus = 1;

	@ManyToMany
	@JoinTable(
		name = "docente_carrera",
		joinColumns = @JoinColumn(name = "idDocente"),
		inverseJoinColumns = @JoinColumn(name = "idCarrera")
	)
	private List<Carrera> carrerasHabilitadas = new ArrayList<>();

	public String getNombreCompleto() {
		return (nombre != null ? nombre : "") + " " + (apellidos != null ? apellidos : "");
	}

	@PrePersist
	public void prePersist() {
		if (estatus == null) {
			estatus = 1;
		}
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNoEmpleado() {
		return noEmpleado;
	}

	public void setNoEmpleado(String noEmpleado) {
		this.noEmpleado = noEmpleado;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellidos() {
		return apellidos;
	}

	public void setApellidos(String apellidos) {
		this.apellidos = apellidos;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
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

	public List<Carrera> getCarrerasHabilitadas() {
		return carrerasHabilitadas;
	}

	public void setCarrerasHabilitadas(List<Carrera> carrerasHabilitadas) {
		this.carrerasHabilitadas = carrerasHabilitadas;
	}
}