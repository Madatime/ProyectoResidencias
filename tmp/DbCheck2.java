import java.sql.*;
public class DbCheck2 {
  public static void main(String[] args) throws Exception {
    try (Connection cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/proyectoResidencia?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true", "root", "12345");
         Statement st = cn.createStatement()) {
      try (ResultSet rs = st.executeQuery("select id,estatus,periodo,fechaInicio,fechaFin,idProyectoCarrera from residencia order by id")) {
        while (rs.next()) {
          System.out.println(rs.getInt(1)+" | "+rs.getString(2)+" | "+rs.getString(3)+" | "+rs.getString(4)+" | "+rs.getString(5)+" | "+rs.getString(6));
        }
      }
    }
  }
}
