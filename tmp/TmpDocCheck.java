import java.sql.*;
public class TmpDocCheck {
  public static void main(String[] args) throws Exception {
    String url="jdbc:mysql://localhost:3306/proyectoResidencia?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true";
    try (Connection c=DriverManager.getConnection(url,"root","12345")) {
      try (Statement st=c.createStatement()) {
        ResultSet rs=st.executeQuery("select r.id, r.nombreProyecto, e.nombre as empresa, est.nombre as residenteNombre, est.apellidos, est.matricula from residencia r left join residente res on res.id=r.idResidente left join estudiante est on est.id=res.idEstudiante left join empresa e on e.id=r.idEmpresa where r.estatus=1 order by r.id desc limit 15");
        while (rs.next()) {
          System.out.println(rs.getInt(1)+" | "+rs.getString(2)+" | "+rs.getString(3)+" | "+rs.getString(4)+" "+rs.getString(5)+" | "+rs.getString(6));
        }
        System.out.println("--- docs ---");
        rs=st.executeQuery("select id,idResidencia,tipoDocumento,nombreArchivo,rutaArchivo,estatus,observaciones from documento_residencia order by idResidencia,id");
        while (rs.next()) {
          System.out.println(rs.getInt(1)+" | res="+rs.getInt(2)+" | "+rs.getString(3)+" | "+rs.getString(4)+" | "+rs.getString(5)+" | "+rs.getString(6)+" | "+rs.getString(7));
        }
      }
    }
  }
}
