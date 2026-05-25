import java.sql.*;
public class DbSchemaProbe {
  static final String URL = "jdbc:mysql://localhost:3306/proyectoResidencia?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true";
  static final String USER = "root";
  static final String PASS = "12345";
  public static void main(String[] args) throws Exception {
    try (Connection cn = DriverManager.getConnection(URL, USER, PASS)) {
      String[] tables = {"perfil","usuario","usuarioPerfil","carreras","empresa","docentes","asesor_interno","asesor_externo","estudiante","residente","residencia","banco_proyectos"};
      for (String t : tables) {
        System.out.println("== " + t + " ==");
        try (PreparedStatement ps = cn.prepareStatement("select COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_KEY from information_schema.columns where table_schema = database() and table_name = ? order by ordinal_position")) {
          ps.setString(1, t);
          try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
              System.out.println(rs.getString(1)+" | "+rs.getString(2)+" | nullable="+rs.getString(3)+" | key="+rs.getString(4));
            }
          }
        }
        try (Statement st = cn.createStatement(); ResultSet rs = st.executeQuery("select count(*) from " + t)) {
          rs.next();
          System.out.println("count=" + rs.getInt(1));
        }
      }
    }
  }
}
