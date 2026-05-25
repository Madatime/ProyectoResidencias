package itch.tsp.model;

import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String username;
	private String password;
	private String nombreCompleto;
	private String nombreMostrar;
	private String email;
	private String rol;
	private Integer estatus = 1;

	@OneToOne
	@JoinColumn(name = "idDocente")
	private Docente docente;

	@OneToOne
	@JoinColumn(name = "idResidente")
	private Residente residente;

	@OneToOne
	@JoinColumn(name = "idAsesorExterno")
	private AsesorExterno asesorExterno;

	@OneToMany(mappedBy = "usuario")
	private List<UsuarioPerfil> perfiles;
	
	@Transient
	private String perfilNombre;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNombreCompleto() {
		return nombreCompleto;
	}

	public void setNombreCompleto(String nombreCompleto) {
		this.nombreCompleto = nombreCompleto;
	}

	public String getNombreMostrar() {
		return nombreMostrar;
	}

	public void setNombreMostrar(String nombreMostrar) {
		this.nombreMostrar = nombreMostrar;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}

	public Integer getEstatus() {
		return estatus;
	}

	public void setEstatus(Integer estatus) {
		this.estatus = estatus;
	}

	public Docente getDocente() {
		return docente;
	}

	public void setDocente(Docente docente) {
		this.docente = docente;
	}

	public Residente getResidente() {
		return residente;
	}

	public void setResidente(Residente residente) {
		this.residente = residente;
	}

	public AsesorExterno getAsesorExterno() {
		return asesorExterno;
	}

	public void setAsesorExterno(AsesorExterno asesorExterno) {
		this.asesorExterno = asesorExterno;
	}

	public List<UsuarioPerfil> getPerfiles() {
		return perfiles;
	}

	public void setPerfiles(List<UsuarioPerfil> perfiles) {
		this.perfiles = perfiles;
	}
	
	public String getPerfilNombre() {
		return perfilNombre;
	}

	public void setPerfilNombre(String perfilNombre) {
		this.perfilNombre = perfilNombre;
	}

	@Override
	public String toString() {
		return "Usuario [id=" + id + ", username=" + username + ", nombreCompleto=" + nombreCompleto
				+ ", email=" + email + ", rol=" + rol + ", estatus=" + estatus + "]";
	}
}
