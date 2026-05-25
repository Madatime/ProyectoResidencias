package itch.tsp.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "banco_proyectos")
public class BancoProyecto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String nombreProyecto;

	@Column(length = 1500)
	private String descripcion;

	@Column(length = 1500)
	private String objetivo;

	private String periodo;

	@Convert(converter = EstadoBancoProyectoConverter.class)
	private EstadoBancoProyecto estado = EstadoBancoProyecto.DISPONIBLE;

	@Enumerated(EnumType.STRING)
	private OrigenBancoProyecto origen = OrigenBancoProyecto.BANCO;

	@Column(length = 1500)
	private String observaciones;

	private LocalDate fechaPropuesta;
	private LocalDate fechaRevision;

	private Integer estatus = 1;

	@ManyToOne
	@JoinColumn(name = "idEmpresa")
	private Empresa empresa;

	@ManyToOne
	@JoinColumn(name = "idCarrera")
	private Carrera carrera;

	@ManyToOne
	@JoinColumn(name = "idResidente")
	private Residente propuestoPor;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNombreProyecto() {
		return nombreProyecto;
	}

	public void setNombreProyecto(String nombreProyecto) {
		this.nombreProyecto = nombreProyecto;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getObjetivo() {
		return objetivo;
	}

	public void setObjetivo(String objetivo) {
		this.objetivo = objetivo;
	}

	public String getPeriodo() {
		return periodo;
	}

	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}

	public EstadoBancoProyecto getEstado() {
		return estado;
	}

	public void setEstado(EstadoBancoProyecto estado) {
		this.estado = estado;
	}

	public OrigenBancoProyecto getOrigen() {
		return origen;
	}

	public void setOrigen(OrigenBancoProyecto origen) {
		this.origen = origen;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}

	public LocalDate getFechaPropuesta() {
		return fechaPropuesta;
	}

	public void setFechaPropuesta(LocalDate fechaPropuesta) {
		this.fechaPropuesta = fechaPropuesta;
	}

	public LocalDate getFechaRevision() {
		return fechaRevision;
	}

	public void setFechaRevision(LocalDate fechaRevision) {
		this.fechaRevision = fechaRevision;
	}

	public Integer getEstatus() {
		return estatus;
	}

	public void setEstatus(Integer estatus) {
		this.estatus = estatus;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public Carrera getCarrera() {
		return carrera;
	}

	public void setCarrera(Carrera carrera) {
		this.carrera = carrera;
	}

	public Residente getPropuestoPor() {
		return propuestoPor;
	}

	public void setPropuestoPor(Residente propuestoPor) {
		this.propuestoPor = propuestoPor;
	}
}
