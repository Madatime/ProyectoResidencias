package itch.tsp.model;

import java.time.LocalDate;

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
@Table(name = "evaluacion_residencia")
public class EvaluacionResidencia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Enumerated(EnumType.STRING)
	private TipoEvaluacionResidencia tipoEvaluacion;

	private Double calificacion;
	private String observaciones;
	private LocalDate fechaEvaluacion;
	private String evaluadorNombre;
	private String evaluadorRol;
	private Integer estatus = 1;

	private Double criterio1;
	private Double criterio2;
	private Double criterio3;
	private Double criterio4;
	private Double criterio5;
	private Double criterio6;
	private Double criterio7;
	private Double criterio8;
	private Double criterio9;
	private Double criterio10;
	private Double criterio11;
	private Double criterio12;
	private Double criterio13;

	@ManyToOne
	@JoinColumn(name = "idResidencia")
	private Residencia residencia;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public TipoEvaluacionResidencia getTipoEvaluacion() {
		return tipoEvaluacion;
	}

	public void setTipoEvaluacion(TipoEvaluacionResidencia tipoEvaluacion) {
		this.tipoEvaluacion = tipoEvaluacion;
	}

	public Double getCalificacion() {
		return calificacion;
	}

	public void setCalificacion(Double calificacion) {
		this.calificacion = calificacion;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}

	public LocalDate getFechaEvaluacion() {
		return fechaEvaluacion;
	}

	public void setFechaEvaluacion(LocalDate fechaEvaluacion) {
		this.fechaEvaluacion = fechaEvaluacion;
	}

	public String getEvaluadorNombre() {
		return evaluadorNombre;
	}

	public void setEvaluadorNombre(String evaluadorNombre) {
		this.evaluadorNombre = evaluadorNombre;
	}

	public String getEvaluadorRol() {
		return evaluadorRol;
	}

	public void setEvaluadorRol(String evaluadorRol) {
		this.evaluadorRol = evaluadorRol;
	}

	public Integer getEstatus() {
		return estatus;
	}

	public void setEstatus(Integer estatus) {
		this.estatus = estatus;
	}

	public Double getCriterio1() {
		return criterio1;
	}

	public void setCriterio1(Double criterio1) {
		this.criterio1 = criterio1;
	}

	public Double getCriterio2() {
		return criterio2;
	}

	public void setCriterio2(Double criterio2) {
		this.criterio2 = criterio2;
	}

	public Double getCriterio3() {
		return criterio3;
	}

	public void setCriterio3(Double criterio3) {
		this.criterio3 = criterio3;
	}

	public Double getCriterio4() {
		return criterio4;
	}

	public void setCriterio4(Double criterio4) {
		this.criterio4 = criterio4;
	}

	public Double getCriterio5() {
		return criterio5;
	}

	public void setCriterio5(Double criterio5) {
		this.criterio5 = criterio5;
	}

	public Double getCriterio6() {
		return criterio6;
	}

	public void setCriterio6(Double criterio6) {
		this.criterio6 = criterio6;
	}

	public Double getCriterio7() {
		return criterio7;
	}

	public void setCriterio7(Double criterio7) {
		this.criterio7 = criterio7;
	}

	public Double getCriterio8() {
		return criterio8;
	}

	public void setCriterio8(Double criterio8) {
		this.criterio8 = criterio8;
	}

	public Double getCriterio9() {
		return criterio9;
	}

	public void setCriterio9(Double criterio9) {
		this.criterio9 = criterio9;
	}

	public Double getCriterio10() {
		return criterio10;
	}

	public void setCriterio10(Double criterio10) {
		this.criterio10 = criterio10;
	}

	public Double getCriterio11() {
		return criterio11;
	}

	public void setCriterio11(Double criterio11) {
		this.criterio11 = criterio11;
	}

	public Double getCriterio12() {
		return criterio12;
	}

	public void setCriterio12(Double criterio12) {
		this.criterio12 = criterio12;
	}

	public Double getCriterio13() {
		return criterio13;
	}

	public void setCriterio13(Double criterio13) {
		this.criterio13 = criterio13;
	}

	public Residencia getResidencia() {
		return residencia;
	}

	public void setResidencia(Residencia residencia) {
		this.residencia = residencia;
	}

	@Override
	public String toString() {
		return "EvaluacionResidencia [id=" + id + ", tipoEvaluacion=" + tipoEvaluacion
				+ ", calificacion=" + calificacion + ", fechaEvaluacion=" + fechaEvaluacion + "]";
	}
}