import java.sql.*;
public class TmpFindRes {
  public static void main(String[] args) throws Exception {
    String url="jdbc:mysql://localhost:3306/proyectoResidencia?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true";
    try (Connection c=DriverManager.getConnection(url,"root","12345"); Statement st=c.createStatement()) {
      ResultSet rs=st.executeQuery("select r.id, r.nombreProyecto, r.periodo, e.nombre as empresa, est.nombre, est.apellidos, est.matricula from residencia r left join residente res on res.id=r.idResidente left join estudiante est on est.id=res.idEstudiante left join empresa e on e.id=r.idEmpresa where r.estatus=1 and (r.nombreProyecto like '%Seguimiento Empresarial%' or est.nombre like '%Daniela%' or est.apellidos like '%Morales%' or e.nombre like '%WebCore%') order by r.id");
      while (rs.next()) System.out.println(rs.getInt(1)+" | "+rs.getString(2)+" | "+rs.getString(3)+" | "+rs.getString(4)+" | "+rs.getString(5)+" "+rs.getString(6)+" | "+rs.getString(7));
    }
  }
}
