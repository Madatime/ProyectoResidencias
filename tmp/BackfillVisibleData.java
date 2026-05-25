import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;

public class BackfillVisibleData {
    private static final String URL =
            "jdbc:mysql://localhost:3306/proyectoResidencia?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "12345";

    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection cn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            cn.setAutoCommit(false);

            completarConveniosEmpresa(cn);
            enriquecerResidenciasPendientes(cn);
            corregirTextos(cn);

            cn.commit();
        }
    }

    private static void completarConveniosEmpresa(Connection cn) throws Exception {
        String sql = """
                update empresa
                set anioFinConvenio = coalesce(anioFinConvenio, anioConvenio + 3),
                    vigenciaConvenio = case
                        when trim(coalesce(vigenciaConvenio, '')) = '' then
                            coalesce(anioFinConvenio, anioConvenio + 3)
                        else vigenciaConvenio
                    end
                where estatus = 1
                """;
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    private static void enriquecerResidenciasPendientes(Connection cn) throws Exception {
        try (Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery("""
                     select id
                     from residencia
                     where estatus = 1
                       and estadoAutorizacion = 'PENDIENTE'
                       and fechaAutorizacion is null
                     order by id
                     """)) {
            int index = 0;
            while (rs.next()) {
                int id = rs.getInt("id");
                index++;
                if (index % 4 == 0) {
                    continue;
                }

                String estado = (index % 3 == 0) ? "AUTORIZADO_CON_OBSERVACIONES" : "AUTORIZADO";
                LocalDate fecha = LocalDate.of(2026, 5, 10).plusDays(index);
                String observaciones = "AUTORIZADO_CON_OBSERVACIONES".equals(estado)
                        ? "Se autoriza con ajustes menores de redacción y alcance."
                        : "Proyecto autorizado conforme a revisión académica.";

                try (PreparedStatement ps = cn.prepareStatement("""
                        update residencia
                        set estadoAutorizacion = ?,
                            fechaAutorizacion = ?,
                            observacionesAutorizacion = ?,
                            estatusProceso = coalesce(nullif(estatusProceso, ''), 'EN_SEGUIMIENTO')
                        where id = ?
                        """)) {
                    ps.setString(1, estado);
                    ps.setObject(2, fecha);
                    ps.setString(3, observaciones);
                    ps.setInt(4, id);
                    ps.executeUpdate();
                }
            }
        }
    }

    private static void corregirTextos(Connection cn) throws Exception {
        try (PreparedStatement ps = cn.prepareStatement("""
                update residencia
                set nombreProyecto = 'Implementación de un Proyecto de IA'
                where nombreProyecto = 'Implementacion de un proyecto de IA'
                """)) {
            ps.executeUpdate();
        }
    }
}
