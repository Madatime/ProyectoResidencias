package itch.tsp.model;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "asesor_externo")
public class AsesorExterno {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String nombre;
	private String apellidos;
	private String empresa;
	private String cargo;
	private String telefono;
	private String correo;
	private String fotoPath;
	private Integer estatus = 1;

	@OneToMany(mappedBy = "asesorExterno")
	private List<Residencia> residencias;

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

	public String getApellidos() {
		return apellidos;
	}

	public void setApellidos(String apellidos) {
		this.apellidos = apellidos;
	}

	public String getEmpresa() {
		return empresa;
	}

	public void setEmpresa(String empresa) {
		this.empresa = empresa;
	}

	public String getCargo() {
		return cargo;
	}

	public void setCargo(String cargo) {
		this.cargo = cargo;
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

	public String getNombreCompleto() {
		return (nombre != null ? nombre : "") + " " + (apellidos != null ? apellidos : "");
	}

	@Override
	public String toString() {
		return "AsesorExterno [id=" + id + ", nombre=" + nombre + ", apellidos=" + apellidos + ", empresa=" + empresa
				+ ", cargo=" + cargo + ", telefono=" + telefono + ", correo=" + correo + ", fotoPath=" + fotoPath
				+ ", estatus=" + estatus + ", residencias=" + residencias + "]";
	}
	
}