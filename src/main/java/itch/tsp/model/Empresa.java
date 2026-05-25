package itch.tsp.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "empresa")
public class Empresa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String nombre;
	private String giro;
	private String direccion;
	private String telefono;
	private String correo;
	private String representante;
	private String puestoRepresentante;
	private String dueno;

	private String convenio = "INACTIVO";

	private Integer anioConvenio;

	private Integer vigenciaConvenio;

	private Integer anioFinConvenio;

	private Integer estatus = 1;

	public Empresa() {
		this.estatus = 1;
		this.convenio = "INACTIVO";
	}

	@PrePersist
	@PreUpdate
	public void actualizarConvenioAutomaticamente() {
		if (estatus == null) {
			estatus = 1;
		}

		if (convenio == null || convenio.trim().isEmpty()) {
			convenio = "INACTIVO";
		}

		convenio = convenio.trim().toUpperCase();

		if ("ACTIVO".equals(convenio)) {

			if (anioConvenio == null) {
				anioConvenio = LocalDate.now().getYear();
			}

			if (vigenciaConvenio == null || !(vigenciaConvenio == 2 || vigenciaConvenio == 3 || vigenciaConvenio == 5)) {
				vigenciaConvenio = 2;
			}

			anioFinConvenio = anioConvenio + vigenciaConvenio;

		} else {
			vigenciaConvenio = null;
			anioConvenio = null;
			anioFinConvenio = null;
		}
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getGiro() {
		return giro;
	}

	public void setGiro(String giro) {
		this.giro = giro;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getRepresentante() {
		return representante;
	}

	public void setRepresentante(String representante) {
		this.representante = representante;
	}

	public String getPuestoRepresentante() {
		return puestoRepresentante;
	}

	public void setPuestoRepresentante(String puestoRepresentante) {
		this.puestoRepresentante = puestoRepresentante;
	}

	public String getDueno() {
		return dueno;
	}

	public void setDueno(String dueno) {
		this.dueno = dueno;
	}

	public String getConvenio() {
		return convenio;
	}

	public void setConvenio(String convenio) {
		this.convenio = convenio;
	}

	public Integer getAnioConvenio() {
		return anioConvenio;
	}

	public void setAnioConvenio(Integer anioConvenio) {
		this.anioConvenio = anioConvenio;
	}

	public Integer getVigenciaConvenio() {
		return vigenciaConvenio;
	}

	public void setVigenciaConvenio(Integer vigenciaConvenio) {
		this.vigenciaConvenio = vigenciaConvenio;
	}

	public Integer getAnioFinConvenio() {
		return anioFinConvenio;
	}

	public void setAnioFinConvenio(Integer anioFinConvenio) {
		this.anioFinConvenio = anioFinConvenio;
	}

	public Integer getEstatus() {
		return estatus;
	}

	public void setEstatus(Integer estatus) {
		this.estatus = estatus;
	}

	@Transient
	public String getConvenioVisible() {
		if (!"ACTIVO".equalsIgnoreCase(convenio)) {
			return "INACTIVO";
		}

		if (anioConvenio == null || anioFinConvenio == null) {
			return "INACTIVO";
		}

		int anioActual = LocalDate.now().getYear();

		if (anioActual >= anioConvenio && anioActual <= anioFinConvenio) {
			return "ACTIVO";
		}

		return "VENCIDO";
	}

	@Transient
	public boolean isConvenioActivo() {
		return "ACTIVO".equalsIgnoreCase(getConvenioVisible());
	}

	@Transient
	public boolean isConvenioVencido() {
		return "VENCIDO".equalsIgnoreCase(getConvenioVisible());
	}

	@Transient
	public String getConvenioConAnio() {
		if (anioConvenio == null || anioFinConvenio == null) {
			return getConvenioVisible();
		}

		return getConvenioVisible() + " - " + anioConvenio + " a " + anioFinConvenio;
	}
}