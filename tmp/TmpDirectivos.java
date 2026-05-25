import java.sql.*;
public class TmpDirectivos {
  public static void main(String[] args) throws Exception {
    String url="jdbc:mysql://localhost:3306/proyectoResidencia?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true";
    try (Connection c=DriverManager.getConnection(url,"root","12345"); Statement st=c.createStatement()) {
      ResultSet rs=st.executeQuery("select d.id,d.tipoDirectivo,d.puesto,d.departamento,doc.nombre,doc.apellidos from directivos d left join docente doc on doc.id=d.idDocente where d.estatus=1");
      while(rs.next()) System.out.println(rs.getInt(1)+" | "+rs.getString(2)+" | "+rs.getString(3)+" | "+rs.getString(4)+" | "+rs.getString(5)+" "+rs.getString(6));
    }
  }
}
