package itch.tsp.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "asesor_interno")
public class AsesorInterno {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "claveAsesor", unique = true, length = 25)
	private String claveAsesor;

	@ManyToOne
	@JoinColumn(name = "idDocente")
	private Docente docente;

	private Integer estatus = 1;

	@OneToMany(mappedBy = "asesorInterno")
	private List<Residencia> residencias;

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

	public String getClaveAsesor() {
		return claveAsesor;
	}

	public void setClaveAsesor(String claveAsesor) {
		this.claveAsesor = claveAsesor;
	}

	public Docente getDocente() {
		return docente;
	}

	public void setDocente(Docente docente) {
		this.docente = docente;
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

	@Transient
	public String getNoEmpleado() {
		return docente != null ? docente.getNoEmpleado() : "";
	}

	@Transient
	public String getNombre() {
		return docente != null ? docente.getNombre() : "";
	}

	@Transient
	public String getApellidos() {
		return docente != null ? docente.getApellidos() : "";
	}

	@Transient
	public String getTelefono() {
		return docente != null ? docente.getTelefono() : "";
	}

	@Transient
	public String getCorreo() {
		return docente != null ? docente.getCorreo() : "";
	}

	@Transient
	public String getFotoPath() {
		return docente != null ? docente.getFotoPath() : "";
	}

	@Transient
	public String getNombreCompleto() {
		return docente != null ? docente.getNombreCompleto() : "";
	}

	@Override
	public String toString() {
		return "AsesorInterno [id=" + id + ", claveAsesor=" + claveAsesor + ", docente=" + docente
				+ ", estatus=" + estatus + "]";
	}
}