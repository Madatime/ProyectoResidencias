import java.sql.*;
public class TmpRes10Data {
  public static void main(String[] args) throws Exception {
    String url="jdbc:mysql://localhost:3306/proyectoResidencia?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true";
    try (Connection c=DriverManager.getConnection(url,"root","12345"); Statement st=c.createStatement()) {
      ResultSet rs=st.executeQuery("select r.id,r.nombreProyecto,r.periodo,r.fechaInicio,r.fechaFin,r.estadoAutorizacion,e.nombre empresa,e.representante,e.puestoRepresentante,ae.nombre aeNombre,ae.apellidos aeAp,ai.nombre aiNombre,ai.apellidos aiAp,est.nombre estNom,est.apellidos estAp,est.matricula,car.nombre carrera from residencia r left join empresa e on e.id=r.idEmpresa left join asesor_externo ae on ae.id=r.idAsesorExterno left join asesor_interno ai on ai.id=r.idAsesorInterno left join residente res on res.id=r.idResidente left join estudiante est on est.id=res.idEstudiante left join carrera car on car.id=est.idCarrera where r.id=10");
      ResultSetMetaData md=rs.getMetaData();
      if(rs.next()){
        for(int i=1;i<=md.getColumnCount();i++) System.out.println(md.getColumnLabel(i)+" = "+rs.getString(i));
      }
    }
  }
}
