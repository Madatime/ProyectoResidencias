import java.sql.*;
public class DbUserAudit {
  static final String URL = "jdbc:mysql://localhost:3306/proyectoResidencia?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true";
  static final String USER = "root";
  static final String PASS = "12345";
  public static void main(String[] args) throws Exception {
    try (Connection cn = DriverManager.getConnection(URL, USER, PASS); Statement st = cn.createStatement()) {
      System.out.println("== perfiles ==");
      try (ResultSet rs = st.executeQuery("select id,nombre,estatus from perfil order by id")) {
        while (rs.next()) System.out.println(rs.getInt(1)+" | "+rs.getString(2)+" | "+rs.getInt(3));
      }
      System.out.println("== usuarios ultimos ==");
      try (ResultSet rs = st.executeQuery("select id,username,email,rol,estatus,idDocente,idResidente,idAsesorExterno from usuario order by id desc limit 20")) {
        while (rs.next()) System.out.println(rs.getInt(1)+" | "+rs.getString(2)+" | "+rs.getString(3)+" | "+rs.getString(4)+" | e="+rs.getInt(5)+" | d="+rs.getString(6)+" | r="+rs.getString(7)+" | ae="+rs.getString(8));
      }
      System.out.println("== residentes sin usuario ==");
      try (ResultSet rs = st.executeQuery("select r.id, e.matricula, concat(e.nombre,' ',e.apellidos) from residente r join estudiante e on e.id=r.idEstudiante left join usuario u on u.idResidente=r.id and u.estatus=1 where r.estatus=1 and u.id is null order by r.id")) {
        while (rs.next()) System.out.println(rs.getInt(1)+" | "+rs.getString(2)+" | "+rs.getString(3));
      }
      System.out.println("== asesores internos sin usuario ==");
      try (ResultSet rs = st.executeQuery("select ai.id, d.noEmpleado, concat(d.nombre,' ',d.apellidos) from asesor_interno ai join docentes d on d.id=ai.idDocente left join usuario u on u.idDocente=d.id and u.estatus=1 where ai.estatus=1 and u.id is null order by ai.id")) {
        while (rs.next()) System.out.println(rs.getInt(1)+" | "+rs.getString(2)+" | "+rs.getString(3));
      }
      System.out.println("== asesores externos sin usuario ==");
      try (ResultSet rs = st.executeQuery("select ae.id, ae.correo, concat(ae.nombre,' ',ae.apellidos) from asesor_externo ae left join usuario u on u.idAsesorExterno=ae.id and u.estatus=1 where ae.estatus=1 and u.id is null order by ae.id")) {
        while (rs.next()) System.out.println(rs.getInt(1)+" | "+rs.getString(2)+" | "+rs.getString(3));
      }
    }
  }
}
