package itch.tsp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "directivos")
public class Directivo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "claveDirectivo", unique = true, length = 25)
	private String claveDirectivo;

	@ManyToOne
	@JoinColumn(name = "idDocente")
	private Docente docente;

	@Enumerated(EnumType.STRING)
	private TipoDirectivo tipoDirectivo;

	@Column(length = 120)
	private String puesto;

	@Column(length = 120)
	private String departamento;

	private String firmaPath;

	private String selloPath;

	private Integer estatus = 1;

	@PrePersist
	public void prePersist() {
		if (estatus == null) {
			estatus = 1;
		}
	}

	@Transient
	public String getNombreCompleto() {
		return docente != null ? docente.getNombreCompleto() : "";
	}

	@Transient
	public String getNoEmpleado() {
		return docente != null ? docente.getNoEmpleado() : "";
	}

	@Transient
	public String getCorreo() {
		return docente != null ? docente.getCorreo() : "";
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getClaveDirectivo() {
		return claveDirectivo;
	}

	public void setClaveDirectivo(String claveDirectivo) {
		this.claveDirectivo = claveDirectivo;
	}

	public Docente getDocente() {
		return docente;
	}

	public void setDocente(Docente docente) {
		this.docente = docente;
	}

	public TipoDirectivo getTipoDirectivo() {
		return tipoDirectivo;
	}

	public void setTipoDirectivo(TipoDirectivo tipoDirectivo) {
		this.tipoDirectivo = tipoDirectivo;
	}

	public String getPuesto() {
		return puesto;
	}

	public void setPuesto(String puesto) {
		this.puesto = puesto;
	}

	public String getDepartamento() {
		return departamento;
	}

	public void setDepartamento(String departamento) {
		this.departamento = departamento;
	}

	public String getFirmaPath() {
		return firmaPath;
	}

	public void setFirmaPath(String firmaPath) {
		this.firmaPath = firmaPath;
	}

	public String getSelloPath() {
		return selloPath;
	}

	public void setSelloPath(String selloPath) {
		this.selloPath = selloPath;
	}

	public Integer getEstatus() {
		return estatus;
	}

	public void setEstatus(Integer estatus) {
		this.estatus = estatus;
	}
}