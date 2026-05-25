package itch.tsp.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class EstadoBancoProyectoConverter implements AttributeConverter<EstadoBancoProyecto, String> {

	@Override
	public String convertToDatabaseColumn(EstadoBancoProyecto attribute) {
		return attribute != null ? attribute.name() : null;
	}

	@Override
	public EstadoBancoProyecto convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.trim().isEmpty()) {
			return EstadoBancoProyecto.DISPONIBLE;
		}

		String valor = dbData.trim().toUpperCase();

		return switch (valor) {
		case "AUTORIZADO", "AUTORIZADO_CON_OBSERVACIONES" -> EstadoBancoProyecto.DISPONIBLE;
		case "DISPONIBLE" -> EstadoBancoProyecto.DISPONIBLE;
		case "ASIGNADO" -> EstadoBancoProyecto.ASIGNADO;
		case "INACTIVO" -> EstadoBancoProyecto.INACTIVO;
		case "PENDIENTE_REVISION" -> EstadoBancoProyecto.PENDIENTE_REVISION;
		case "RECHAZADO" -> EstadoBancoProyecto.RECHAZADO;
		default -> throw new IllegalArgumentException("Estado de banco de proyectos no soportado: " + dbData);
		};
	}
}
