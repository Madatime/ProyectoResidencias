package itch.tsp.model;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "residencia")
public class Residencia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String nombreProyecto;
	private String descripcion;
	private String objetivo;
	private Integer totalRechazos = 0;

	@ManyToOne
	@JoinColumn(name = "idEmpresa")
	private Empresa empresa;

	private String periodo;
	private LocalDate fechaInicio;
	private LocalDate fechaFin;
	private Integer estatus = 1;

	private String estatusProceso = "EN_PROCESO";
	private LocalDate fechaCierre;
	
	private String idProyectoCarrera;
	private String estadoAutorizacion; 
	private LocalDate fechaAutorizacion;
	
	private String origenProyecto = "PROYECTO";
	private String carreraJefeArea;
	private String observacionesAutorizacion;
	
	@Transient
	private Boolean prorrogaAutorizada;

	@ManyToOne
	@JoinColumn(name = "idResidente")
	private Residente residente;
	
	@ManyToOne
	@JoinColumn(name = "idAsesorInterno", nullable = true)
	private AsesorInterno asesorInterno;

	@ManyToOne
	@JoinColumn(name = "idAsesorExterno")
	private AsesorExterno asesorExterno;

	@OneToMany(mappedBy = "residencia")
	private List<DocumentoResidencia> documentos;

	@OneToMany(mappedBy = "residencia")
	private List<EvaluacionResidencia> evaluaciones;

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

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public String getPeriodo() {
		return periodo;
	}

	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}

	public LocalDate getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(LocalDate fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public LocalDate getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(LocalDate fechaFin) {
		this.fechaFin = fechaFin;
	}

	public Integer getEstatus() {
		return estatus;
	}

	public void setEstatus(Integer estatus) {
		this.estatus = estatus;
	}

	public String getEstatusProceso() {
		return estatusProceso;
	}

	public void setEstatusProceso(String estatusProceso) {
		this.estatusProceso = estatusProceso;
	}

	public LocalDate getFechaCierre() {
		return fechaCierre;
	}

	public void setFechaCierre(LocalDate fechaCierre) {
		this.fechaCierre = fechaCierre;
	}

	public Residente getResidente() {
		return residente;
	}

	public void setResidente(Residente residente) {
		this.residente = residente;
	}

	public AsesorInterno getAsesorInterno() {
		return asesorInterno;
	}

	public void setAsesorInterno(AsesorInterno asesorInterno) {
		this.asesorInterno = asesorInterno;
	}

	public AsesorExterno getAsesorExterno() {
		return asesorExterno;
	}

	public void setAsesorExterno(AsesorExterno asesorExterno) {
		this.asesorExterno = asesorExterno;
	}

	public List<DocumentoResidencia> getDocumentos() {
		return documentos;
	}

	public void setDocumentos(List<DocumentoResidencia> documentos) {
		this.documentos = documentos;
	}

	public List<EvaluacionResidencia> getEvaluaciones() {
		return evaluaciones;
	}

	public void setEvaluaciones(List<EvaluacionResidencia> evaluaciones) {
		this.evaluaciones = evaluaciones;
	}

	public String getIdProyectoCarrera() {
		return idProyectoCarrera;
	}

	public void setIdProyectoCarrera(String idProyectoCarrera) {
		this.idProyectoCarrera = idProyectoCarrera;
	}

	public String getEstadoAutorizacion() {
		return estadoAutorizacion;
	}

	public void setEstadoAutorizacion(String estadoAutorizacion) {
		this.estadoAutorizacion = estadoAutorizacion;
	}

	public LocalDate getFechaAutorizacion() {
		return fechaAutorizacion;
	}

	public void setFechaAutorizacion(LocalDate fechaAutorizacion) {
		this.fechaAutorizacion = fechaAutorizacion;
	}
	
	public String getOrigenProyecto() {
		return origenProyecto;
	}

	public void setOrigenProyecto(String origenProyecto) {
		this.origenProyecto = origenProyecto;
	}

	public String getCarreraJefeArea() {
		return carreraJefeArea;
	}

	public void setCarreraJefeArea(String carreraJefeArea) {
		this.carreraJefeArea = carreraJefeArea;
	}

	public String getObservacionesAutorizacion() {
		return observacionesAutorizacion;
	}

	public void setObservacionesAutorizacion(String observacionesAutorizacion) {
		this.observacionesAutorizacion = observacionesAutorizacion;
	}

	public Boolean getProrrogaAutorizada() {
		if (prorrogaAutorizada != null) {
			return prorrogaAutorizada;
		}

		if (fechaInicio != null && fechaFin != null) {
			return fechaFin.isAfter(fechaInicio.plusMonths(4));
		}

		return false;
	}

	public void setProrrogaAutorizada(Boolean prorrogaAutorizada) {
		this.prorrogaAutorizada = prorrogaAutorizada;
	}
	
	public Integer getTotalRechazos() {
		return totalRechazos;
	}

	public void setTotalRechazos(Integer totalRechazos) {
		this.totalRechazos = totalRechazos;
	}

	@Override
	public String toString() {
		return "Residencia [id=" + id + ", nombreProyecto=" + nombreProyecto + ", descripcion=" + descripcion
				+ ", objetivo=" + objetivo + ", totalRechazos=" + totalRechazos + ", empresa=" + empresa + ", periodo="
				+ periodo + ", fechaInicio=" + fechaInicio + ", fechaFin=" + fechaFin + ", estatus=" + estatus
				+ ", estatusProceso=" + estatusProceso + ", fechaCierre=" + fechaCierre + ", idProyectoCarrera="
				+ idProyectoCarrera + ", estadoAutorizacion=" + estadoAutorizacion + ", fechaAutorizacion="
				+ fechaAutorizacion + ", origenProyecto=" + origenProyecto + ", carreraJefeArea=" + carreraJefeArea
				+ ", observacionesAutorizacion=" + observacionesAutorizacion + ", residente=" + residente
				+ ", asesorInterno=" + asesorInterno + ", asesorExterno=" + asesorExterno + ", documentos=" + documentos
				+ ", evaluaciones=" + evaluaciones + "]";
	}

	

}
