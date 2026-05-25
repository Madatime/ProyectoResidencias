package itch.tsp.config;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import itch.tsp.model.BancoProyecto;
import itch.tsp.model.Carrera;
import itch.tsp.model.Empresa;
import itch.tsp.model.EstadoBancoProyecto;
import itch.tsp.model.OrigenBancoProyecto;
import itch.tsp.repository.BancoProyectoRepository;
import itch.tsp.repository.CarreraRepository;
import itch.tsp.repository.EmpresaRepository;

@Configuration
public class BancoProyectoBootstrapInitializer {

	@Bean
	CommandLineRunner inicializarBancoProyectos(
			BancoProyectoRepository bancoProyectoRepository,
			EmpresaRepository empresaRepository,
			CarreraRepository carreraRepository) {

		return args -> {
			List<Empresa> empresas = empresaRepository.findByEstatusOrderByIdDesc(1);
			List<Carrera> carreras = carreraRepository.findByEstatusOrderByIdAsc(1);

			if (empresas.isEmpty() || carreras.isEmpty()) {
				return;
			}

			Set<String> nombresExistentes = bancoProyectoRepository.findByEstatusOrderByIdDesc(1)
					.stream()
					.map(BancoProyecto::getNombreProyecto)
					.filter(nombre -> nombre != null && !nombre.trim().isEmpty())
					.map(nombre -> nombre.trim().toUpperCase())
					.collect(Collectors.toSet());

			List<ProyectoSemilla> semillas = construirSemillas();
			List<BancoProyecto> nuevos = new ArrayList<>();

			for (int i = 0; i < semillas.size(); i++) {
				ProyectoSemilla semilla = semillas.get(i);
				String clave = semilla.nombreProyecto().trim().toUpperCase();

				if (nombresExistentes.contains(clave)) {
					continue;
				}

				BancoProyecto proyecto = new BancoProyecto();
				proyecto.setNombreProyecto(semilla.nombreProyecto());
				proyecto.setDescripcion(semilla.descripcion());
				proyecto.setObjetivo(semilla.objetivo());
				proyecto.setPeriodo(semilla.periodo());
				proyecto.setEstado(EstadoBancoProyecto.DISPONIBLE);
				proyecto.setOrigen(OrigenBancoProyecto.BANCO);
				proyecto.setObservaciones("Proyecto sembrado automaticamente para ampliar el banco institucional.");
				proyecto.setFechaPropuesta(LocalDate.now().minusDays(i + 1));
				proyecto.setEstatus(1);
				proyecto.setEmpresa(empresas.get(i % empresas.size()));
				proyecto.setCarrera(carreras.get(i % carreras.size()));
				nuevos.add(proyecto);
			}

			if (!nuevos.isEmpty()) {
				bancoProyectoRepository.saveAll(nuevos);
			}
		};
	}

	private List<ProyectoSemilla> construirSemillas() {
		return List.of(
				new ProyectoSemilla(
						"Panel inteligente para seguimiento de indicadores academicos",
						"Desarrollo de una plataforma web para centralizar indicadores de rendimiento escolar, asistencia y seguimiento institucional.",
						"Facilitar la toma de decisiones con informacion consolidada y visualizaciones oportunas.",
						"ENE-JUN 2026"),
				new ProyectoSemilla(
						"Sistema de control de inventario con alertas operativas",
						"Implementacion de un modulo de inventario con alertas de minimos, entradas, salidas y trazabilidad por responsable.",
						"Reducir errores en el control de activos y agilizar la administracion de almacen.",
						"ENE-JUN 2026"),
				new ProyectoSemilla(
						"Aplicacion movil para seguimiento de servicio al cliente",
						"Creacion de una aplicacion para registrar visitas, incidencias, evidencias fotograficas y tiempos de respuesta.",
						"Mejorar la atencion al cliente y el monitoreo de compromisos operativos.",
						"ENE-JUN 2026"),
				new ProyectoSemilla(
						"Tablero de monitoreo para procesos administrativos",
						"Construccion de un tablero con metricas de procesos, semaforizacion de pendientes y reportes exportables.",
						"Visibilizar cuellos de botella y acelerar el seguimiento de tramites internos.",
						"AGO-DIC 2026"),
				new ProyectoSemilla(
						"Optimizacion digital del flujo de requisiciones internas",
						"Digitalizacion del proceso de requisiciones con validaciones, historial y autorizaciones por etapas.",
						"Disminuir tiempos de respuesta y mejorar la trazabilidad documental.",
						"AGO-DIC 2026"),
				new ProyectoSemilla(
						"Sistema de agenda y seguimiento para visitas tecnicas",
						"Desarrollo de un sistema para calendarizar visitas, asignar responsables y capturar resultados por evidencia.",
						"Ordenar la operacion de campo y asegurar cumplimiento de actividades programadas.",
						"AGO-DIC 2026"),
				new ProyectoSemilla(
						"Portal de reportes ejecutivos para area de vinculacion",
						"Generacion de un portal con concentrado de convenios, empresas, estatus y seguimiento de colaboraciones.",
						"Fortalecer la gestion tecnologica y la relacion con el sector productivo.",
						"AGO-DIC 2026"),
				new ProyectoSemilla(
						"Control inteligente de mantenimiento preventivo",
						"Implementacion de un sistema para programar mantenimientos, evidencias de ejecucion y alertas por vencimiento.",
						"Incrementar la disponibilidad operativa del equipo institucional.",
						"ENE-JUN 2025"),
				new ProyectoSemilla(
						"Sistema de evaluacion y seguimiento de proyectos institucionales",
						"Plataforma para registrar objetivos, avances, responsables y resultados de proyectos de mejora continua.",
						"Dar seguimiento formal al cumplimiento de metas institucionales.",
						"ENE-JUN 2025"),
				new ProyectoSemilla(
						"Aplicacion para captura y analisis de incidencias escolares",
						"Herramienta para registrar incidencias, clasificar causas, tiempos de atencion y generar reportes estadisticos.",
						"Contar con informacion accionable para prevenir recurrencias.",
						"ENE-JUN 2025"),
				new ProyectoSemilla(
						"Modulo de control documental para expedientes operativos",
						"Desarrollo de un repositorio con versionado, busqueda y consulta de expedientes digitales.",
						"Asegurar orden, disponibilidad y consulta rapida de documentacion clave.",
						"AGO-DIC 2025"),
				new ProyectoSemilla(
						"Sistema de gestion de solicitudes y atencion interna",
						"Plataforma de tickets internos con prioridades, asignaciones y seguimiento hasta cierre.",
						"Mejorar la respuesta entre areas y medir niveles de servicio.",
						"AGO-DIC 2025"),
				new ProyectoSemilla(
						"Tablero de productividad para operaciones y supervision",
						"Desarrollo de indicadores de productividad por area, responsable y periodo con metas comparativas.",
						"Impulsar la supervision basada en datos y el seguimiento de resultados.",
						"AGO-DIC 2025"),
				new ProyectoSemilla(
						"Plataforma de seguimiento para practicas y colaboraciones empresariales",
						"Registro de empresas, estudiantes, avances y resultados de colaboraciones con el sector externo.",
						"Integrar informacion clave para fortalecer la vinculacion academica.",
						"AGO-DIC 2024"),
				new ProyectoSemilla(
						"Sistema web de mejora continua para procesos de calidad",
						"Implementacion de un sistema para registrar hallazgos, acciones correctivas y seguimiento de auditorias internas.",
						"Apoyar la gestion de calidad y el cierre oportuno de observaciones.",
						"AGO-DIC 2024"));
	}

	private record ProyectoSemilla(
			String nombreProyecto,
			String descripcion,
			String objetivo,
			String periodo) {
	}
}
