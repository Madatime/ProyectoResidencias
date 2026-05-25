package itch.tsp.model;

import java.time.LocalDateTime;

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
@Table(name = "documento_residencia")
public class DocumentoResidencia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Enumerated(EnumType.STRING)
	private TipoDocumentoResidencia tipoDocumento;

	private String nombreArchivo;
	private String rutaArchivo;

	@Enumerated(EnumType.STRING)
	private EstatusDocumento estatus = EstatusDocumento.PENDIENTE;

	private String observaciones;
	private LocalDateTime fechaCarga;
	private LocalDateTime fechaRevision;
	private Integer estatusRegistro = 1;

	@ManyToOne
	@JoinColumn(name = "idResidencia")
	private Residencia residencia;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public TipoDocumentoResidencia getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(TipoDocumentoResidencia tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public String getNombreArchivo() {
		return nombreArchivo;
	}

	public void setNombreArchivo(String nombreArchivo) {
		this.nombreArchivo = nombreArchivo;
	}

	public String getRutaArchivo() {
		return rutaArchivo;
	}

	public void setRutaArchivo(String rutaArchivo) {
		this.rutaArchivo = rutaArchivo;
	}

	public EstatusDocumento getEstatus() {
		return estatus;
	}

	public void setEstatus(EstatusDocumento estatus) {
		this.estatus = estatus;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}

	public LocalDateTime getFechaCarga() {
		return fechaCarga;
	}

	public void setFechaCarga(LocalDateTime fechaCarga) {
		this.fechaCarga = fechaCarga;
	}

	public LocalDateTime getFechaRevision() {
		return fechaRevision;
	}

	public void setFechaRevision(LocalDateTime fechaRevision) {
		this.fechaRevision = fechaRevision;
	}

	public Integer getEstatusRegistro() {
		return estatusRegistro;
	}

	public void setEstatusRegistro(Integer estatusRegistro) {
		this.estatusRegistro = estatusRegistro;
	}

	public Residencia getResidencia() {
		return residencia;
	}

	public void setResidencia(Residencia residencia) {
		this.residencia = residencia;
	}

	@Override
	public String toString() {
		return "DocumentoResidencia [id=" + id + ", tipoDocumento=" + tipoDocumento + ", nombreArchivo=" + nombreArchivo
				+ ", rutaArchivo=" + rutaArchivo + ", estatus=" + estatus + ", fechaCarga=" + fechaCarga + "]";
	}
}