import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class SeedDirectDbData {
  static final String URL = "jdbc:mysql://localhost:3306/proyectoResidencia?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true";
  static final String USER = "root";
  static final String PASS = "12345";
  static final String TEMP_PASSWORD = "123";
  static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

  public static void main(String[] args) throws Exception {
    try (Connection cn = DriverManager.getConnection(URL, USER, PASS)) {
      cn.setAutoCommit(false);
      try {
        int perfilEstudiante = perfilId(cn, "ESTUDIANTE");
        int perfilAsesorInterno = perfilId(cn, "ASESOR_INTERNO");
        int perfilAsesorExterno = perfilId(cn, "ASESOR_EXTERNO");

        List<String> resumen = new ArrayList<>();

        syncExistingResidentUsers(cn, perfilEstudiante, resumen);
        syncExistingExternalAdvisorUsers(cn, perfilAsesorExterno, resumen);

        List<Integer> carreras = activeIds(cn, "carreras");
        List<Integer> empresas = activeIds(cn, "empresa");
        if (carreras.isEmpty() || empresas.isEmpty()) {
          throw new IllegalStateException("No hay carreras o empresas activas suficientes.");
        }

        int[][] residentSpecs = {
          {22999031, 0}, {22999032, 1}, {22999033, 2}, {22999034, 3}, {22999035, 4}
        };
        String[][] residentNames = {
          {"Alma","Navarro Ruiz","F","9","7479903101","alma.navarro@demo.mx"},
          {"Bruno","Castillo Vela","M","10","7479903102","bruno.castillo@demo.mx"},
          {"Cecilia","Ortega Pineda","F","9","7479903103","cecilia.ortega@demo.mx"},
          {"Diego","Serrano Bello","M","10","7479903104","diego.serrano@demo.mx"},
          {"Elena","Fuentes Lara","F","9","7479903105","elena.fuentes@demo.mx"}
        };

        List<Integer> newResidentes = new ArrayList<>();
        for (int i = 0; i < residentSpecs.length; i++) {
          String matricula = String.valueOf(residentSpecs[i][0]);
          int carreraIndex = Math.min(residentSpecs[i][1], carreras.size() - 1);
          int idEstudiante = ensureStudent(cn, matricula, residentNames[i], carreras.get(carreraIndex));
          int idResidente = ensureResident(cn, idEstudiante, matricula, residentNames[i], carreraNombre(cn, carreras.get(carreraIndex)));
          ensureResidentUser(cn, perfilEstudiante, idResidente, matricula, residentNames[i][0] + " " + residentNames[i][1], residentNames[i][5], resumen);
          newResidentes.add(idResidente);
        }

        String[][] internalSpecs = {
          {"TST-DOC-031","Nadia","Campos Ruiz","nadia.campos@tecnm.mx","7479903201"},
          {"TST-DOC-032","Oscar","Mejia Torres","oscar.mejia@tecnm.mx","7479903202"},
          {"TST-DOC-033","Paula","Luna Salgado","paula.luna@tecnm.mx","7479903203"}
        };
        List<Integer> newAsesoresInternos = new ArrayList<>();
        for (int i = 0; i < internalSpecs.length; i++) {
          int carreraIndex = Math.min(i, carreras.size() - 1);
          int idDocente = ensureDocente(cn, internalSpecs[i], carreras.get(carreraIndex));
          int idAsesor = ensureInternalAdvisor(cn, idDocente, internalSpecs[i]);
          ensureDocenteUser(cn, perfilAsesorInterno, idDocente, internalSpecs[i][0], internalSpecs[i][1] + " " + internalSpecs[i][2], internalSpecs[i][3], "ASESOR_INTERNO", resumen);
          linkInternalAdvisorUser(cn, idAsesor, idDocente);
          newAsesoresInternos.add(idAsesor);
        }

        String[][] externalSpecs = {
          {"Rocio","Parra Molina","qa.ext1@demo.mx","Especialista QA","7479903301"},
          {"Tomas","Galeana Soto","qa.ext2@demo.mx","Analista de Procesos","7479903302"},
          {"Uriel","Sanchez Rios","qa.ext3@demo.mx","Administrador de Infraestructura","7479903303"}
        };
        List<Integer> newAsesoresExternos = new ArrayList<>();
        for (int i = 0; i < externalSpecs.length; i++) {
          int empresaIndex = Math.min(i, empresas.size() - 1);
          int idAsesor = ensureExternalAdvisor(cn, externalSpecs[i], empresaNombre(cn, empresas.get(empresaIndex)));
          ensureExternalAdvisorUser(cn, perfilAsesorExterno, idAsesor, externalSpecs[i][2], externalSpecs[i][0] + " " + externalSpecs[i][1], resumen);
          newAsesoresExternos.add(idAsesor);
        }

        String[][] residencias = {
          {"Plataforma de seguimiento de tutorias","Centralizar tutorias y alertas academicas.","Mejorar el seguimiento de alumnos.","ENE-JUN 2027","2027-01-20","2027-05-20","31","AUTORIZADO"},
          {"Control de inventario con trazabilidad","Registro de movimientos y reportes de almacen.","Reducir perdidas y faltantes.","ENE-JUN 2027","2027-02-01","2027-06-01","32","PENDIENTE"},
          {"Dashboard de productividad institucional","Indicadores de avance para areas operativas.","Apoyar decisiones con datos.","AGO-DIC 2027","2027-08-10","2027-12-10","33","AUTORIZADO"},
          {"Monitoreo de red y soporte preventivo","Alertas de disponibilidad y desempeno.","Elevar continuidad operativa.","AGO-DIC 2027","2027-08-15","2027-12-15","34","PENDIENTE"},
          {"Optimizacion del proceso de atencion","Mapeo y mejora de tiempos de respuesta.","Reducir cuellos de botella.","ENE-JUN 2028","2028-01-25","2028-05-25","35","AUTORIZADO"}
        };
        for (int i = 0; i < residencias.length; i++) {
          int empresaId = empresas.get(Math.min(i, empresas.size() - 1));
          int residenteId = newResidentes.get(i);
          int asesorInternoId = newAsesoresInternos.get(i % newAsesoresInternos.size());
          int asesorExternoId = newAsesoresExternos.get(i % newAsesoresExternos.size());
          ensureResidencia(cn, residencias[i], empresaId, residenteId, asesorInternoId, asesorExternoId);
        }

        String[][] banco = {
          {"App de evidencias de residencia","Carga de evidencias y validaciones.","Mejorar control documental.","ENE-JUN 2027","DISPONIBLE","BANCO"},
          {"Analitica de rutas de distribucion","Modelo de apoyo para planeacion logistica.","Optimizar entregas.","AGO-DIC 2027","DISPONIBLE","BANCO"},
          {"Gestor de mantenimiento preventivo","Calendario y seguimiento de incidencias.","Reducir paros no programados.","ENE-JUN 2028","DISPONIBLE","BANCO"},
          {"Repositorio de procesos hospitalarios","Consulta y control de procedimientos.","Disminuir errores operativos.","AGO-DIC 2027","PENDIENTE_REVISION","PROPUESTO"},
          {"Panel de servicios escolares","Visualizar cargas y tiempos de atencion.","Mejorar capacidad de respuesta.","ENE-JUN 2028","DISPONIBLE","BANCO"}
        };
        for (int i = 0; i < banco.length; i++) {
          Integer idResidente = "PROPUESTO".equals(banco[i][5]) ? newResidentes.get(i % newResidentes.size()) : null;
          ensureBancoProyecto(cn, banco[i], empresas.get(Math.min(i, empresas.size() - 1)), carreras.get(Math.min(i, carreras.size() - 1)), idResidente);
        }

        cn.commit();
        System.out.println("Registros y usuarios agregados/sincronizados. Password temporal: " + TEMP_PASSWORD);
        for (String s : resumen) System.out.println(s);
      } catch (Exception ex) {
        cn.rollback();
        throw ex;
      }
    }
  }

  static int perfilId(Connection cn, String nombre) throws Exception {
    try (PreparedStatement ps = cn.prepareStatement("select id from perfil where nombre=? and estatus=1 limit 1")) {
      ps.setString(1, nombre);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getInt(1);
      }
    }
    throw new IllegalStateException("Perfil no encontrado: " + nombre);
  }

  static List<Integer> activeIds(Connection cn, String table) throws Exception {
    List<Integer> ids = new ArrayList<>();
    try (Statement st = cn.createStatement(); ResultSet rs = st.executeQuery("select id from " + table + " where estatus=1 order by id")) {
      while (rs.next()) ids.add(rs.getInt(1));
    }
    return ids;
  }

  static String carreraNombre(Connection cn, int id) throws Exception {
    try (PreparedStatement ps = cn.prepareStatement("select nombre from carreras where id=?")) {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getString(1); }
    }
  }

  static String empresaNombre(Connection cn, int id) throws Exception {
    try (PreparedStatement ps = cn.prepareStatement("select nombre from empresa where id=?")) {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getString(1); }
    }
  }

  static int ensureStudent(Connection cn, String matricula, String[] data, int idCarrera) throws Exception {
    try (PreparedStatement ps = cn.prepareStatement("select id from estudiante where matricula=? limit 1")) {
      ps.setString(1, matricula);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getInt(1);
      }
    }
    try (PreparedStatement ps = cn.prepareStatement("insert into estudiante(matricula,nombre,apellidos,sexo,semestre,telefono,correo,estatus,idCarrera) values(?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, matricula); ps.setString(2, data[0]); ps.setString(3, data[1]); ps.setString(4, data[2]); ps.setString(5, data[3]); ps.setString(6, data[4]); ps.setString(7, data[5]); ps.setInt(8, 1); ps.setInt(9, idCarrera);
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getInt(1); }
    }
  }

  static int ensureResident(Connection cn, int idEstudiante, String matricula, String[] data, String carrera) throws Exception {
    try (PreparedStatement ps = cn.prepareStatement("select id from residente where idEstudiante=? limit 1")) {
      ps.setInt(1, idEstudiante);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getInt(1);
      }
    }
    try (PreparedStatement ps = cn.prepareStatement("insert into residente(matricula,nombre,apellidos,carrera,semestre,telefono,correo,fotoPath,estatus,idEstudiante) values(?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, matricula); ps.setString(2, data[0]); ps.setString(3, data[1]); ps.setString(4, carrera); ps.setString(5, data[3]); ps.setString(6, data[4]); ps.setString(7, data[5]); ps.setString(8, null); ps.setInt(9,1); ps.setInt(10,idEstudiante);
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getInt(1); }
    }
  }

  static int ensureDocente(Connection cn, String[] data, int idCarrera) throws Exception {
    try (PreparedStatement ps = cn.prepareStatement("select id from docentes where noEmpleado=? limit 1")) {
      ps.setString(1, data[0]);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getInt(1);
      }
    }
    int id;
    try (PreparedStatement ps = cn.prepareStatement("insert into docentes(noEmpleado,nombre,apellidos,correo,telefono,estatus) values(?,?,?,?,?,1)", Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, data[0]); ps.setString(2, data[1]); ps.setString(3, data[2]); ps.setString(4, data[3]); ps.setString(5, data[4]);
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); id = rs.getInt(1); }
    }
    try (PreparedStatement ps = cn.prepareStatement("insert into docente_carrera(idDocente,idCarrera) values(?,?)")) {
      ps.setInt(1, id); ps.setInt(2, idCarrera); ps.executeUpdate();
    }
    return id;
  }

  static int ensureInternalAdvisor(Connection cn, int idDocente, String[] data) throws Exception {
    try (PreparedStatement ps = cn.prepareStatement("select id from asesor_interno where idDocente=? limit 1")) {
      ps.setInt(1, idDocente);
      try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
    }
    String clave = "ITCH-AI-TST-" + data[0].substring(data[0].length()-3);
    try (PreparedStatement ps = cn.prepareStatement("insert into asesor_interno(noEmpleado,nombre,apellidos,area,telefono,correo,fotoPath,estatus,idUsuario,claveAsesor,idDocente) values(?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1,data[0]); ps.setString(2,data[1]); ps.setString(3,data[2]); ps.setString(4,"Departamento Academico"); ps.setString(5,data[4]); ps.setString(6,data[3]); ps.setString(7,null); ps.setInt(8,1); ps.setObject(9,null); ps.setString(10,clave); ps.setInt(11,idDocente);
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getInt(1); }
    }
  }

  static void linkInternalAdvisorUser(Connection cn, int idAsesor, int idDocente) throws Exception {
    Integer idUsuario = userByDocente(cn, idDocente);
    if (idUsuario == null) return;
    try (PreparedStatement ps = cn.prepareStatement("update asesor_interno set idUsuario=? where id=? and (idUsuario is null or idUsuario<>?)")) {
      ps.setInt(1, idUsuario); ps.setInt(2, idAsesor); ps.setInt(3, idUsuario); ps.executeUpdate();
    }
  }

  static int ensureExternalAdvisor(Connection cn, String[] data, String empresa) throws Exception {
    try (PreparedStatement ps = cn.prepareStatement("select id from asesor_externo where correo=? limit 1")) {
      ps.setString(1, data[2]);
      try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
    }
    try (PreparedStatement ps = cn.prepareStatement("insert into asesor_externo(nombre,apellidos,empresa,cargo,telefono,correo,fotoPath,estatus) values(?,?,?,?,?,?,?,1)", Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1,data[0]); ps.setString(2,data[1]); ps.setString(3,empresa); ps.setString(4,data[3]); ps.setString(5,data[4]); ps.setString(6,data[2]); ps.setString(7,null);
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getInt(1); }
    }
  }

  static void ensureResidencia(Connection cn, String[] data, int idEmpresa, int idResidente, int idAsesorInterno, int idAsesorExterno) throws Exception {
    try (PreparedStatement ps = cn.prepareStatement("select id from residencia where nombreProyecto=? limit 1")) {
      ps.setString(1, data[0]);
      try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return; }
    }
    String carrera = residentCareer(cn, idResidente);
    try (PreparedStatement ps = cn.prepareStatement("insert into residencia(nombreProyecto,descripcion,objetivo,periodo,fechaInicio,fechaFin,estatus,idResidente,idAsesorInterno,idAsesorExterno,estatusProceso,fechaCierre,estadoAutorizacion,fechaAutorizacion,idProyectoCarrera,idEmpresa,carreraJefeArea,observacionesAutorizacion,origenProyecto,totalRechazos) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
      ps.setString(1,data[0]); ps.setString(2,data[1]); ps.setString(3,data[2]); ps.setString(4,data[3]); ps.setDate(5, java.sql.Date.valueOf(data[4])); ps.setDate(6, java.sql.Date.valueOf(data[5])); ps.setInt(7,1); ps.setInt(8,idResidente); ps.setInt(9,idAsesorInterno); ps.setInt(10,idAsesorExterno); ps.setString(11,"EN_PROCESO"); ps.setObject(12,null); ps.setString(13,data[7]); ps.setObject(14,"AUTORIZADO".equals(data[7]) ? java.sql.Date.valueOf(LocalDate.now()) : null); ps.setString(15,data[6]); ps.setInt(16,idEmpresa); ps.setString(17,carrera); ps.setObject(18,null); ps.setString(19,"PROYECTO"); ps.setInt(20,0);
      ps.executeUpdate();
    }
  }

  static String residentCareer(Connection cn, int idResidente) throws Exception {
    try (PreparedStatement ps = cn.prepareStatement("select c.nombre from residente r join estudiante e on e.id=r.idEstudiante left join carreras c on c.id=e.idCarrera where r.id=?")) {
      ps.setInt(1,idResidente);
      try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getString(1); }
    }
  }

  static void ensureBancoProyecto(Connection cn, String[] data, int idEmpresa, int idCarrera, Integer idResidente) throws Exception {
    try (PreparedStatement ps = cn.prepareStatement("select id from banco_proyectos where nombreProyecto=? limit 1")) {
      ps.setString(1, data[0]);
      try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return; }
    }
    try (PreparedStatement ps = cn.prepareStatement("insert into banco_proyectos(descripcion,estado,estatus,fechaPropuesta,fechaRevision,nombreProyecto,objetivo,observaciones,origen,periodo,idCarrera,idEmpresa,idResidente) values(?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
      ps.setString(1,data[1]); ps.setString(2,data[4]); ps.setInt(3,1); ps.setDate(4, java.sql.Date.valueOf(LocalDate.now().minusDays(7))); ps.setObject(5, "PENDIENTE_REVISION".equals(data[4]) ? null : java.sql.Date.valueOf(LocalDate.now().minusDays(2))); ps.setString(6,data[0]); ps.setString(7,data[2]); ps.setObject(8,null); ps.setString(9,data[5]); ps.setString(10,data[3]); ps.setInt(11,idCarrera); ps.setInt(12,idEmpresa); if (idResidente == null) ps.setObject(13,null); else ps.setInt(13,idResidente);
      ps.executeUpdate();
    }
  }

  static void syncExistingResidentUsers(Connection cn, int perfilId, List<String> resumen) throws Exception {
    String sql = "select r.id, e.matricula, concat(e.nombre,' ',e.apellidos) nombre, coalesce(e.correo, concat(lower(e.matricula),'@tecnm.mx')) correo from residente r join estudiante e on e.id=r.idEstudiante left join usuario u on u.idResidente=r.id and u.estatus=1 where r.estatus=1 and u.id is null order by r.id";
    try (Statement st = cn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
      while (rs.next()) ensureResidentUser(cn, perfilId, rs.getInt("id"), rs.getString("matricula"), rs.getString("nombre"), rs.getString("correo"), resumen);
    }
  }

  static void syncExistingExternalAdvisorUsers(Connection cn, int perfilId, List<String> resumen) throws Exception {
    String sql = "select ae.id, coalesce(nullif(trim(ae.correo),''), concat('asesorext',ae.id)) username, concat(ae.nombre,' ',ae.apellidos) nombre from asesor_externo ae left join usuario u on u.idAsesorExterno=ae.id and u.estatus=1 where ae.estatus=1 and u.id is null order by ae.id";
    try (Statement st = cn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
      while (rs.next()) ensureExternalAdvisorUser(cn, perfilId, rs.getInt("id"), rs.getString("username"), rs.getString("nombre"), resumen);
    }
  }

  static void ensureResidentUser(Connection cn, int perfilId, int idResidente, String username, String nombre, String email, List<String> resumen) throws Exception {
    Integer idUsuario = userByResident(cn, idResidente);
    if (idUsuario == null) {
      idUsuario = insertUser(cn, username, nombre, sanitizeEmail(cn, email, username), "ESTUDIANTE", null, idResidente, null);
      ensureUsuarioPerfil(cn, idUsuario, perfilId);
      resumen.add("residente -> " + username + " / " + TEMP_PASSWORD);
    }
  }

  static void ensureDocenteUser(Connection cn, int perfilId, int idDocente, String username, String nombre, String email, String rol, List<String> resumen) throws Exception {
    Integer idUsuario = userByDocente(cn, idDocente);
    if (idUsuario == null) {
      idUsuario = insertUser(cn, username, nombre, sanitizeEmail(cn, email, username), rol, idDocente, null, null);
      ensureUsuarioPerfil(cn, idUsuario, perfilId);
      resumen.add(rol.toLowerCase() + " -> " + username + " / " + TEMP_PASSWORD);
    }
  }

  static void ensureExternalAdvisorUser(Connection cn, int perfilId, int idAsesorExterno, String username, String nombre, List<String> resumen) throws Exception {
    Integer idUsuario = userByExternal(cn, idAsesorExterno);
    if (idUsuario == null) {
      String email = username.contains("@") ? username : username + "@tecnm.mx";
      idUsuario = insertUser(cn, username, nombre, sanitizeEmail(cn, email, username), "ASESOR_EXTERNO", null, null, idAsesorExterno);
      ensureUsuarioPerfil(cn, idUsuario, perfilId);
      resumen.add("asesor_externo -> " + username + " / " + TEMP_PASSWORD);
    }
  }

  static String sanitizeEmail(Connection cn, String email, String username) throws Exception {
    String base = (email == null || email.isBlank()) ? username + "@tecnm.mx" : email.trim().toLowerCase();
    String candidate = base;
    int i = 1;
    while (emailExists(cn, candidate)) {
      int at = base.indexOf('@');
      if (at > 0) candidate = base.substring(0, at) + "+" + i + base.substring(at);
      else candidate = base + i;
      i++;
    }
    return candidate;
  }

  static boolean emailExists(Connection cn, String email) throws Exception {
    try (PreparedStatement ps = cn.prepareStatement("select count(*) from usuario where email=?")) {
      ps.setString(1,email);
      try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1) > 0; }
    }
  }

  static Integer userByResident(Connection cn, int idResidente) throws Exception {
    try (PreparedStatement ps = cn.prepareStatement("select id from usuario where idResidente=? and estatus=1 limit 1")) {
      ps.setInt(1,idResidente);
      try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : null; }
    }
  }
  static Integer userByDocente(Connection cn, int idDocente) throws Exception {
    try (PreparedStatement ps = cn.prepareStatement("select id from usuario where idDocente=? and estatus=1 limit 1")) {
      ps.setInt(1,idDocente);
      try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : null; }
    }
  }
  static Integer userByExternal(Connection cn, int idAe) throws Exception {
    try (PreparedStatement ps = cn.prepareStatement("select id from usuario where idAsesorExterno=? and estatus=1 limit 1")) {
      ps.setInt(1,idAe);
      try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : null; }
    }
  }

  static int insertUser(Connection cn, String username, String nombre, String email, String rol, Integer idDocente, Integer idResidente, Integer idAe) throws Exception {
    String uniqueUsername = username;
    int i = 1;
    while (usernameExists(cn, uniqueUsername)) { uniqueUsername = username + i; i++; }
    try (PreparedStatement ps = cn.prepareStatement("insert into usuario(username,password,nombreMostrar,email,rol,estatus,nombreCompleto,idDocente,idResidente,idAsesorExterno) values(?,?,?,?,?,1,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, uniqueUsername);
      ps.setString(2, ENCODER.encode(TEMP_PASSWORD));
      ps.setString(3, nombre);
      ps.setString(4, email);
      ps.setString(5, rol);
      ps.setString(6, nombre);
      if (idDocente == null) ps.setObject(7, null); else ps.setInt(7, idDocente);
      if (idResidente == null) ps.setObject(8, null); else ps.setInt(8, idResidente);
      if (idAe == null) ps.setObject(9, null); else ps.setInt(9, idAe);
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) { rs.next(); return rs.getInt(1); }
    }
  }

  static boolean usernameExists(Connection cn, String username) throws Exception {
    try (PreparedStatement ps = cn.prepareStatement("select count(*) from usuario where username=?")) {
      ps.setString(1, username);
      try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1) > 0; }
    }
  }

  static void ensureUsuarioPerfil(Connection cn, int idUsuario, int idPerfil) throws Exception {
    try (PreparedStatement ps = cn.prepareStatement("select id from usuarioPerfil where idUsuario=? and idPerfil=? and estatus=1 limit 1")) {
      ps.setInt(1,idUsuario); ps.setInt(2,idPerfil);
      try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return; }
    }
    try (PreparedStatement ps = cn.prepareStatement("insert into usuarioPerfil(idPerfil,idUsuario,estatus) values(?,?,1)")) {
      ps.setInt(1,idPerfil); ps.setInt(2,idUsuario); ps.executeUpdate();
    }
  }
}


