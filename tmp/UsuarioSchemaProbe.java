import java.sql.*;
public class UsuarioSchemaProbe {
  public static void main(String[] args) throws Exception {
    Class.forName("com.mysql.cj.jdbc.Driver");
    try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/proyectoResidencia?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true", "root", "12345")) {
      DatabaseMetaData md = c.getMetaData();
      try (ResultSet rs = md.getColumns(c.getCatalog(), null, "usuario", null)) {
        while (rs.next()) {
          System.out.println(rs.getString("COLUMN_NAME") + "|" + rs.getString("TYPE_NAME") + "|" + rs.getString("IS_NULLABLE") + "|" + rs.getString("COLUMN_DEF"));
        }
      }
    }
  }
}
