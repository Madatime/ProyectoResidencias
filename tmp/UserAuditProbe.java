import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserAuditProbe {
    private static final String URL =
            "jdbc:mysql://localhost:3306/proyectoResidencia?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "12345";

    public static void main(String[] args) throws Exception {
        String like = args.length > 0 ? args[0] : "audit_%";
        Class.forName("com.mysql.cj.jdbc.Driver");

        try (Connection cn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = cn.prepareStatement(
                     """
                     select u.id, u.username, u.nombreCompleto, u.estatus,
                            p.nombre perfil
                     from usuario u
                     left join usuarioPerfil up on up.idUsuario = u.id and up.estatus = 1
                     left join perfil p on p.id = up.idPerfil
                     where u.username like ?
                     order by u.id desc
                     """)) {
            ps.setString(1, like);

            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.println(
                            "id=" + rs.getInt("id")
                                    + " | username=" + rs.getString("username")
                                    + " | nombre=" + rs.getString("nombreCompleto")
                                    + " | estatus=" + rs.getString("estatus")
                                    + " | perfil=" + rs.getString("perfil"));
                }
                if (!found) {
                    System.out.println("(sin coincidencias)");
                }
            }
        }
    }
}
