import java.sql.*;
public class DbCountsAfter {
  public static void main(String[] args) throws Exception {
    try (Connection cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/proyectoResidencia?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true", "root", "12345"); Statement st = cn.createStatement()) {
      String[] tables = {"usuario","docentes","asesor_interno","asesor_externo","estudiante","residente","residencia","banco_proyectos"};
      for (String t : tables) {
        try (ResultSet rs = st.executeQuery("select count(*) from " + t)) { rs.next(); System.out.println(t + ": " + rs.getInt(1)); }
      }
    }
  }
}
