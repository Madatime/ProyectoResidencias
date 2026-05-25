import java.sql.*;
public class CheckMatricula02250024 {
  public static void main(String[] args) throws Exception {
    try (Connection cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/proyectoResidencia?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true", "root", "12345");
         PreparedStatement ps = cn.prepareStatement("select id, matricula, nombre, apellidos, semestre, telefono, correo, estatus from estudiante where matricula = ?")) {
      ps.setString(1, "02250024");
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          System.out.println(rs.getInt("id") + " | " + rs.getString("matricula") + " | " + rs.getString("nombre") + " | " + rs.getString("apellidos") + " | sem=" + rs.getString("semestre") + " | tel=" + rs.getString("telefono") + " | correo=" + rs.getString("correo") + " | estatus=" + rs.getInt("estatus"));
        }
      }
    }
  }
}
