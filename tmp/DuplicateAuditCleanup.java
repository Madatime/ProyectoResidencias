import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class DuplicateAuditCleanup {
    private static final String URL =
            "jdbc:mysql://localhost:3306/proyectoResidencia?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "12345";

    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");

        try (Connection cn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            cn.setAutoCommit(false);

            System.out.println("=== DUPLICADOS DE ESTUDIANTES POR MATRICULA ===");
            printDuplicates(cn,
                    """
                    select upper(trim(matricula)) llave,
                           count(*) total,
                           group_concat(id order by id separator ',') ids
                    from estudiante
                    where estatus = 1
                      and trim(coalesce(matricula, '')) <> ''
                    group by upper(trim(matricula))
                    having count(*) > 1
                    order by llave
                    """);

            System.out.println("=== DUPLICADOS DE ESTUDIANTES POR NOMBRE COMPLETO ===");
            printDuplicates(cn,
                    """
                    select upper(trim(concat(nombre, ' ', apellidos))) llave,
                           count(*) total,
                           group_concat(id order by id separator ',') ids
                    from estudiante
                    where estatus = 1
                      and trim(coalesce(nombre, '')) <> ''
                      and trim(coalesce(apellidos, '')) <> ''
                    group by upper(trim(concat(nombre, ' ', apellidos)))
                    having count(*) > 1
                    order by llave
                    """);

            System.out.println("=== DUPLICADOS DE USUARIOS POR USERNAME ===");
            printDuplicates(cn,
                    """
                    select upper(trim(username)) llave,
                           count(*) total,
                           group_concat(id order by id separator ',') ids
                    from usuario
                    where estatus = 1
                      and trim(coalesce(username, '')) <> ''
                    group by upper(trim(username))
                    having count(*) > 1
                    order by llave
                    """);

            int studentsByMatricula = cleanupStudentDuplicatesByKey(cn,
                    """
                    select upper(trim(matricula)) llave
                    from estudiante
                    where estatus = 1
                      and trim(coalesce(matricula, '')) <> ''
                    group by upper(trim(matricula))
                    having count(*) > 1
                    """,
                    """
                    select e.id,
                           case when exists (
                               select 1
                               from residente r
                               where r.idEstudiante = e.id
                                 and r.estatus = 1
                           ) then 1 else 0 end usado
                    from estudiante e
                    where e.estatus = 1
                      and upper(trim(e.matricula)) = ?
                    order by usado desc, e.id asc
                    """);

            int studentsByName = cleanupStudentDuplicatesByKey(cn,
                    """
                    select upper(trim(concat(nombre, ' ', apellidos))) llave
                    from estudiante
                    where estatus = 1
                      and trim(coalesce(nombre, '')) <> ''
                      and trim(coalesce(apellidos, '')) <> ''
                    group by upper(trim(concat(nombre, ' ', apellidos)))
                    having count(*) > 1
                    """,
                    """
                    select e.id,
                           case when exists (
                               select 1
                               from residente r
                               where r.idEstudiante = e.id
                                 and r.estatus = 1
                           ) then 1 else 0 end usado
                    from estudiante e
                    where e.estatus = 1
                      and upper(trim(concat(e.nombre, ' ', e.apellidos))) = ?
                    order by usado desc, e.id asc
                    """);

            int duplicateUsernames = cleanupUsersByUsername(cn);

            System.out.println("=== LIMPIEZA APLICADA ===");
            System.out.println("estudiantes desactivados por matricula duplicada=" + studentsByMatricula);
            System.out.println("estudiantes desactivados por nombre duplicado=" + studentsByName);
            System.out.println("usuarios desactivados por username duplicado=" + duplicateUsernames);

            System.out.println("=== DUPLICADOS RESTANTES DE ESTUDIANTES POR MATRICULA ===");
            printDuplicates(cn,
                    """
                    select upper(trim(matricula)) llave,
                           count(*) total,
                           group_concat(id order by id separator ',') ids
                    from estudiante
                    where estatus = 1
                      and trim(coalesce(matricula, '')) <> ''
                    group by upper(trim(matricula))
                    having count(*) > 1
                    order by llave
                    """);

            System.out.println("=== DUPLICADOS RESTANTES DE ESTUDIANTES POR NOMBRE COMPLETO ===");
            printDuplicates(cn,
                    """
                    select upper(trim(concat(nombre, ' ', apellidos))) llave,
                           count(*) total,
                           group_concat(id order by id separator ',') ids
                    from estudiante
                    where estatus = 1
                      and trim(coalesce(nombre, '')) <> ''
                      and trim(coalesce(apellidos, '')) <> ''
                    group by upper(trim(concat(nombre, ' ', apellidos)))
                    having count(*) > 1
                    order by llave
                    """);

            System.out.println("=== DUPLICADOS RESTANTES DE USUARIOS POR USERNAME ===");
            printDuplicates(cn,
                    """
                    select upper(trim(username)) llave,
                           count(*) total,
                           group_concat(id order by id separator ',') ids
                    from usuario
                    where estatus = 1
                      and trim(coalesce(username, '')) <> ''
                    group by upper(trim(username))
                    having count(*) > 1
                    order by llave
                    """);

            cn.commit();
        }
    }

    private static int cleanupStudentDuplicatesByKey(Connection cn, String duplicateKeysSql, String rowsSql) throws Exception {
        int cleaned = 0;

        try (PreparedStatement keyPs = cn.prepareStatement(duplicateKeysSql);
             ResultSet keyRs = keyPs.executeQuery();
             PreparedStatement rowPs = cn.prepareStatement(rowsSql);
             PreparedStatement deactivatePs = cn.prepareStatement("update estudiante set estatus = 0 where id = ?")) {

            while (keyRs.next()) {
                String key = keyRs.getString(1);
                rowPs.setString(1, key);

                try (ResultSet rowRs = rowPs.executeQuery()) {
                    boolean keepFirst = true;
                    while (rowRs.next()) {
                        int id = rowRs.getInt("id");
                        int used = rowRs.getInt("usado");

                        if (keepFirst) {
                            keepFirst = false;
                            System.out.println("conservando estudiante id=" + id + " usado=" + used + " llave=" + key);
                            continue;
                        }

                        if (used == 1) {
                            System.out.println("duplicado activo no desactivado por estar ligado a residente: id=" + id + " llave=" + key);
                            continue;
                        }

                        deactivatePs.setInt(1, id);
                        cleaned += deactivatePs.executeUpdate();
                        System.out.println("desactivando estudiante duplicado id=" + id + " llave=" + key);
                    }
                }
            }
        }

        return cleaned;
    }

    private static int cleanupUsersByUsername(Connection cn) throws Exception {
        int cleaned = 0;

        try (PreparedStatement keyPs = cn.prepareStatement(
                """
                select upper(trim(username)) llave
                from usuario
                where estatus = 1
                  and trim(coalesce(username, '')) <> ''
                group by upper(trim(username))
                having count(*) > 1
                """);
             ResultSet keyRs = keyPs.executeQuery();
             PreparedStatement rowPs = cn.prepareStatement(
                     """
                     select u.id,
                            case when exists (
                                select 1
                                from usuarioPerfil up
                                where up.idUsuario = u.id
                                  and up.estatus = 1
                            ) then 1 else 0 end conPerfil
                     from usuario u
                     where u.estatus = 1
                       and upper(trim(u.username)) = ?
                     order by conPerfil desc, u.id asc
                     """);
             PreparedStatement deactivatePs = cn.prepareStatement("update usuario set estatus = 0 where id = ?")) {

            while (keyRs.next()) {
                String key = keyRs.getString(1);
                rowPs.setString(1, key);

                try (ResultSet rowRs = rowPs.executeQuery()) {
                    boolean keepFirst = true;
                    while (rowRs.next()) {
                        int id = rowRs.getInt("id");

                        if (keepFirst) {
                            keepFirst = false;
                            System.out.println("conservando usuario id=" + id + " username=" + key);
                            continue;
                        }

                        deactivatePs.setInt(1, id);
                        cleaned += deactivatePs.executeUpdate();
                        System.out.println("desactivando usuario duplicado id=" + id + " username=" + key);
                    }
                }
            }
        }

        return cleaned;
    }

    private static void printDuplicates(Connection cn, String sql) throws Exception {
        try (Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("llave=" + rs.getString("llave")
                        + " | total=" + rs.getInt("total")
                        + " | ids=" + rs.getString("ids"));
            }
            if (!found) {
                System.out.println("(sin duplicados)");
            }
        }
    }
}
