import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DbBackfill {
    private static final String URL =
            "jdbc:mysql://localhost:3306/proyectoResidencia?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "12345";

    private static final String RESIDENTE_PLACEHOLDER_PHOTO = "1b4652b6-1307-4bd6-971f-d3b9415ccc0f.png";

    public static void main(String[] args) throws Exception {
        try (Connection cn = DriverManager.getConnection(URL, USER, PASS)) {
            cn.setAutoCommit(false);

            try {
                int residentesConFoto = backfillResidentePhotos(cn);
                int residentesInactivos = inactivateOrphanResidents(cn);
                int residenciasGenerales = backfillResidenciasGeneral(cn);
                int residenciasTextos = backfillResidenciasText(cn);
                int residenciasIds = backfillResidenciaCareerIds(cn);

                cn.commit();

                System.out.println("Backfill aplicado correctamente.");
                System.out.println("residentes.fotoPath actualizados: " + residentesConFoto);
                System.out.println("residentes huérfanos inactivados: " + residentesInactivos);
                System.out.println("residencias generales actualizadas: " + residenciasGenerales);
                System.out.println("residencias con texto corregido: " + residenciasTextos);
                System.out.println("residencias con idProyectoCarrera generado: " + residenciasIds);
            } catch (Exception ex) {
                cn.rollback();
                throw ex;
            }
        }
    }

    private static int backfillResidentePhotos(Connection cn) throws Exception {
        String sql = "update residente set fotoPath=? " +
                "where estatus=1 and idEstudiante is not null and (fotoPath is null or trim(fotoPath)='')";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, RESIDENTE_PLACEHOLDER_PHOTO);
            return ps.executeUpdate();
        }
    }

    private static int inactivateOrphanResidents(Connection cn) throws Exception {
        String sql = "update residente r set r.estatus=0 " +
                "where r.estatus=1 and r.idEstudiante is null " +
                "and not exists (select 1 from residencia rr where rr.idResidente=r.id and rr.estatus=1)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            return ps.executeUpdate();
        }
    }

    private static int backfillResidenciasGeneral(Connection cn) throws Exception {
        String select = "select r.id,r.periodo,r.fechaInicio,r.fechaFin,r.estatusProceso,r.fechaCierre,r.idProyectoCarrera," +
                "r.estadoAutorizacion,r.fechaAutorizacion,r.origenProyecto,r.carreraJefeArea,r.observacionesAutorizacion," +
                "c.nombre as carrera " +
                "from residencia r " +
                "left join residente rr on rr.id=r.idResidente " +
                "left join estudiante e on e.id=rr.idEstudiante " +
                "left join carreras c on c.id=e.idCarrera " +
                "where r.estatus=1";

        String update = "update residencia set fechaInicio=?,fechaFin=?,estatusProceso=?,estadoAutorizacion=?,fechaAutorizacion=?," +
                "origenProyecto=?,carreraJefeArea=?,observacionesAutorizacion=? where id=?";

        int updated = 0;

        try (Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(select);
             PreparedStatement ps = cn.prepareStatement(update)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String periodo = rs.getString("periodo");
                LocalDate fechaInicio = toLocalDate(rs.getDate("fechaInicio"));
                LocalDate fechaFin = toLocalDate(rs.getDate("fechaFin"));
                String estatusProceso = rs.getString("estatusProceso");
                LocalDate fechaCierre = toLocalDate(rs.getDate("fechaCierre"));
                String estadoAutorizacion = rs.getString("estadoAutorizacion");
                LocalDate fechaAutorizacion = toLocalDate(rs.getDate("fechaAutorizacion"));
                String origenProyecto = rs.getString("origenProyecto");
                String carreraJefeArea = rs.getString("carreraJefeArea");
                String observaciones = rs.getString("observacionesAutorizacion");
                String carrera = rs.getString("carrera");

                LocalDate inicioOficial = obtenerFechaInicioOficial(periodo);
                LocalDate finBase = obtenerFechaFinBase(periodo);

                LocalDate nuevoInicio = inicioOficial;
                LocalDate nuevoFin = finBase;
                String nuevoEstatusProceso = isBlank(estatusProceso)
                        ? (fechaCierre != null ? "CERRADO" : "EN_PROCESO")
                        : estatusProceso.trim().toUpperCase(Locale.ROOT);
                String nuevoEstado = isBlank(estadoAutorizacion) ? "PENDIENTE"
                        : estadoAutorizacion.trim().toUpperCase(Locale.ROOT);
                LocalDate nuevaFechaAutorizacion = fechaAutorizacion;
                if (nuevaFechaAutorizacion == null && !"PENDIENTE".equals(nuevoEstado)) {
                    nuevaFechaAutorizacion = nuevoInicio.plusDays(15);
                }
                String nuevoOrigen = isBlank(origenProyecto) ? "PROYECTO" : origenProyecto.trim().toUpperCase(Locale.ROOT);
                String nuevaCarreraJefeArea = isBlank(carreraJefeArea) ? nullToEmpty(carrera) : carreraJefeArea.trim();
                String nuevasObservaciones = normalizeObservaciones(nuevoEstado, observaciones);

                ps.setDate(1, Date.valueOf(nuevoInicio));
                ps.setDate(2, Date.valueOf(nuevoFin));
                ps.setString(3, nuevoEstatusProceso);
                ps.setString(4, nuevoEstado);
                if (nuevaFechaAutorizacion != null) {
                    ps.setDate(5, Date.valueOf(nuevaFechaAutorizacion));
                } else {
                    ps.setNull(5, java.sql.Types.DATE);
                }
                ps.setString(6, nuevoOrigen);
                ps.setString(7, emptyToNull(nuevaCarreraJefeArea));
                ps.setString(8, emptyToNull(nuevasObservaciones));
                ps.setInt(9, id);
                updated += ps.executeUpdate();
            }
        }

        return updated;
    }

    private static int backfillResidenciasText(Connection cn) throws Exception {
        Map<Integer, ProjectText> textById = new HashMap<>();
        textById.put(8, new ProjectText(
                "Implementación de un proyecto de IA",
                "Diseño e implementación de un prototipo de inteligencia artificial para automatizar tareas de análisis y clasificación de información institucional.",
                "Desarrollar una solución basada en IA que mejore la toma de decisiones y reduzca tiempos de operación mediante automatización controlada."));
        textById.put(11, new ProjectText(
                "Sistema de Atención y Seguimiento de Incidencias",
                "Plataforma web para registrar, canalizar y dar seguimiento a incidencias operativas y solicitudes internas de diferentes áreas de la organización.",
                "Centralizar el control de incidencias y mejorar los tiempos de respuesta mediante trazabilidad y monitoreo de estatus."));
        textById.put(12, new ProjectText(
                "Plataforma Web para Control de Servicios Institucionales",
                "Sistema orientado a la administración de servicios institucionales, solicitudes de apoyo y control de atención a usuarios internos.",
                "Optimizar el registro, asignación y seguimiento de servicios institucionales mediante una plataforma web confiable y auditable."));
        textById.put(20, new ProjectText(
                "Panel de Indicadores para Gestión Académica",
                "Herramienta digital para concentrar indicadores operativos y académicos, con reportes de seguimiento para apoyo a la gestión departamental.",
                "Facilitar el análisis oportuno de indicadores académicos mediante visualización centralizada y seguimiento continuo."));

        String select = "select id,nombreProyecto,descripcion,objetivo from residencia where estatus=1 and id in (8,11,12,20)";
        String update = "update residencia set nombreProyecto=?,descripcion=?,objetivo=? where id=?";
        int updated = 0;

        try (Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(select);
             PreparedStatement ps = cn.prepareStatement(update)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                ProjectText projectText = textById.get(id);
                if (projectText == null) {
                    continue;
                }

                String nombreProyecto = rs.getString("nombreProyecto");
                String descripcion = rs.getString("descripcion");
                String objetivo = rs.getString("objetivo");

                String nuevoNombre = needsPlaceholderReplacement(nombreProyecto) ? projectText.nombre : nombreProyecto;
                String nuevaDescripcion = needsPlaceholderReplacement(descripcion) ? projectText.descripcion : descripcion;
                String nuevoObjetivo = needsPlaceholderReplacement(objetivo) ? projectText.objetivo : objetivo;

                ps.setString(1, nuevoNombre);
                ps.setString(2, nuevaDescripcion);
                ps.setString(3, nuevoObjetivo);
                ps.setInt(4, id);
                updated += ps.executeUpdate();
            }
        }

        return updated;
    }

    private static int backfillResidenciaCareerIds(Connection cn) throws Exception {
        String select = "select r.id,r.idProyectoCarrera,r.periodo,c.nombre as carrera " +
                "from residencia r " +
                "left join residente rr on rr.id=r.idResidente " +
                "left join estudiante e on e.id=rr.idEstudiante " +
                "left join carreras c on c.id=e.idCarrera " +
                "where r.estatus=1 " +
                "order by c.nombre, r.periodo, r.id";

        String update = "update residencia set idProyectoCarrera=? where id=?";
        int updated = 0;

        try (Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(select);
             PreparedStatement ps = cn.prepareStatement(update)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String periodo = rs.getString("periodo");
                String carrera = rs.getString("carrera");
                String careerCode = buildCareerCode(carrera);

                String year = obtenerAnio(periodo);
                String folio = careerCode + "-" + year + "-" + String.format("%03d", id);

                ps.setString(1, folio);
                ps.setInt(2, id);
                updated += ps.executeUpdate();
            }
        }

        return updated;
    }

    private static LocalDate obtenerFechaInicioOficial(String periodo) {
        if (periodo == null) {
            throw new IllegalArgumentException("Periodo nulo.");
        }

        String normalizado = periodo.trim().toUpperCase(Locale.ROOT);
        int anio = Integer.parseInt(obtenerAnio(periodo));

        if (normalizado.contains("ENE-JUN")) {
            return LocalDate.of(anio, 1, 26);
        }
        if (normalizado.contains("AGO-DIC")) {
            return LocalDate.of(anio, 8, 25);
        }

        throw new IllegalArgumentException("Periodo no soportado: " + periodo);
    }

    private static LocalDate obtenerFechaFinBase(String periodo) {
        return obtenerFechaInicioOficial(periodo).plusMonths(4);
    }

    private static String obtenerAnio(String periodo) {
        String[] partes = periodo.split(" ");
        for (String parte : partes) {
            String clean = parte.trim();
            if (clean.matches("\\d{4}")) {
                return clean;
            }
        }
        return "2026";
    }

    private static String buildCareerCode(String carrera) {
        if (carrera == null) {
            return "RES";
        }

        String normalizada = carrera.toUpperCase(Locale.ROOT);
        if (normalizada.contains("SISTEMAS")) {
            return "ISC";
        }
        if (normalizada.contains("INFORM")) {
            return "INF";
        }
        if (normalizada.contains("GESTI")) {
            return "IGE";
        }
        if (normalizada.contains("CIVIL")) {
            return "IC";
        }
        if (normalizada.contains("CONTADOR")) {
            return "CP";
        }
        return "RES";
    }

    private static boolean needsPlaceholderReplacement(String value) {
        if (value == null) {
            return true;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty()
                || normalized.equals("xxx")
                || normalized.equals("xxxx")
                || normalized.equals("xxcx");
    }

    private static String normalizeObservaciones(String estado, String observaciones) {
        if (!needsPlaceholderReplacement(observaciones)) {
            return observaciones.trim();
        }

        if ("AUTORIZADO".equals(estado)) {
            return "Proyecto autorizado para su desarrollo conforme al calendario institucional.";
        }
        if ("AUTORIZADO_CON_OBSERVACIONES".equals(estado)) {
            return "Proyecto autorizado con observaciones. Se solicita ajustar alcance, entregables y redacción técnica.";
        }
        if ("RECHAZADO".equals(estado)) {
            return "Proyecto rechazado. Debe replantearse el alcance, los objetivos y la planeación de entregables.";
        }
        return "Pendiente de revisión por la jefatura correspondiente.";
    }

    private static LocalDate toLocalDate(Date date) {
        return date != null ? date.toLocalDate() : null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String emptyToNull(String value) {
        return isBlank(value) ? null : value;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private record ProjectText(String nombre, String descripcion, String objetivo) {
    }
}
