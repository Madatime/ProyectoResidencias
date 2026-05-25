import java.sql.*;
public class TmpUpsertDocsR6 {
  public static void main(String[] args) throws Exception {
    String url="jdbc:mysql://localhost:3306/proyectoResidencia?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true";
    try (Connection c=DriverManager.getConnection(url,"root","12345")) {
      upsert(c, 6, "ENTREGA_REPORTE_EMPRESA", "Entrega de Reporte.pdf", "ENTREGA_REPORTE_EMPRESA_R6.pdf", "CARGADO", "Documento cargado manualmente para expediente final.");
      upsert(c, 6, "OFICIO_ENTREGA_DIVISION", "9 Oficio de entrega de Reporte.pdf", "OFICIO_ENTREGA_DIVISION_R6.pdf", "CARGADO", "Documento cargado manualmente para expediente final.");
      try (PreparedStatement ps = c.prepareStatement("select id,idResidencia,tipoDocumento,nombreArchivo,rutaArchivo,estatus from documento_residencia where idResidencia=6 order by id")) {
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
          System.out.println(rs.getInt(1)+" | res="+rs.getInt(2)+" | "+rs.getString(3)+" | "+rs.getString(4)+" | "+rs.getString(5)+" | "+rs.getString(6));
        }
      }
    }
  }
  static void upsert(Connection c, int idResidencia, String tipo, String nombre, String ruta, String estatus, String observaciones) throws Exception {
    Integer id = null;
    try (PreparedStatement ps = c.prepareStatement("select id from documento_residencia where idResidencia=? and tipoDocumento=? and estatusRegistro=1")) {
      ps.setInt(1, idResidencia);
      ps.setString(2, tipo);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) id = rs.getInt(1);
    }
    if (id == null) {
      try (PreparedStatement ps = c.prepareStatement("insert into documento_residencia (tipoDocumento,nombreArchivo,rutaArchivo,estatus,observaciones,fechaCarga,fechaRevision,estatusRegistro,idResidencia) values (?,?,?,?,?,now(),null,1,?)")) {
        ps.setString(1, tipo);
        ps.setString(2, nombre);
        ps.setString(3, ruta);
        ps.setString(4, estatus);
        ps.setString(5, observaciones);
        ps.setInt(6, idResidencia);
        ps.executeUpdate();
      }
    } else {
      try (PreparedStatement ps = c.prepareStatement("update documento_residencia set nombreArchivo=?, rutaArchivo=?, estatus=?, observaciones=?, fechaCarga=now(), estatusRegistro=1 where id=?")) {
        ps.setString(1, nombre);
        ps.setString(2, ruta);
        ps.setString(3, estatus);
        ps.setString(4, observaciones);
        ps.setInt(5, id);
        ps.executeUpdate();
      }
    }
  }
}
