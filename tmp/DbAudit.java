import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbAudit {
    private static final String URL =
            "jdbc:mysql://localhost:3306/proyectoResidencia?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "12345";

    public static void main(String[] args) throws Exception {
        try (Connection cn = DriverManager.getConnection(URL, USER, PASS);
             Statement st = cn.createStatement()) {

            System.out.println("== COUNTS ==");
            dumpCount(st, "empresa");
            dumpCount(st, "asesor_externo");
            dumpCount(st, "docentes");
            dumpCount(st, "asesor_interno");
            dumpCount(st, "directivos");
            dumpCount(st, "estudiante");
            dumpCount(st, "residente");
            dumpCount(st, "residencia");
            dumpCount(st, "banco_proyectos");
            dumpCount(st, "evaluacion_residencia");
            dumpCount(st, "documento_residencia");

            System.out.println("\n== EMPRESAS WITH GAPS ==");
            dumpQuery(st,
                    "select id,nombre,giro,direccion,telefono,correo,representante,puestoRepresentante,dueno,convenio,anioConvenio,anioFinConvenio " +
                    "from empresa where estatus=1 and (" +
                    "nombre is null or trim(nombre)='' or giro is null or trim(giro)='' or direccion is null or trim(direccion)='' " +
                    "or telefono is null or trim(telefono)='' or correo is null or trim(correo)='' " +
                    "or representante is null or trim(representante)='' or puestoRepresentante is null or trim(puestoRepresentante)='' " +
                    "or dueno is null or trim(dueno)='')" );

            System.out.println("\n== ASESORES EXTERNOS WITH GAPS ==");
            dumpQuery(st,
                    "select id,nombre,apellidos,empresa,cargo,telefono,correo,fotoPath " +
                    "from asesor_externo where estatus=1 and (" +
                    "nombre is null or trim(nombre)='' or apellidos is null or trim(apellidos)='' or empresa is null or trim(empresa)='' " +
                    "or cargo is null or trim(cargo)='' or telefono is null or trim(telefono)='' or correo is null or trim(correo)='')" );

            System.out.println("\n== DOCENTES WITH GAPS ==");
            dumpQuery(st,
                    "select id,noEmpleado,nombre,apellidos,telefono,correo,fotoPath " +
                    "from docentes where estatus=1 and (" +
                    "noEmpleado is null or trim(noEmpleado)='' or nombre is null or trim(nombre)='' or apellidos is null or trim(apellidos)='' " +
                    "or telefono is null or trim(telefono)='' or correo is null or trim(correo)='')" );

            System.out.println("\n== DIRECTIVOS WITH GAPS ==");
            dumpQuery(st,
                    "select id,claveDirectivo,idDocente,tipoDirectivo,puesto,departamento,firmaPath,selloPath " +
                    "from directivos where estatus=1 and (" +
                    "claveDirectivo is null or trim(claveDirectivo)='' or puesto is null or trim(puesto)='' or departamento is null or trim(departamento)='' " +
                    "or firmaPath is null or trim(firmaPath)='' or selloPath is null or trim(selloPath)='')" );

            System.out.println("\n== ESTUDIANTES WITH GAPS ==");
            dumpQuery(st,
                    "select id,matricula,nombre,apellidos,sexo,semestre,telefono,correo,idCarrera " +
                    "from estudiante where estatus=1 and (" +
                    "matricula is null or trim(matricula)='' or nombre is null or trim(nombre)='' or apellidos is null or trim(apellidos)='' " +
                    "or sexo is null or trim(sexo)='' or semestre is null or trim(semestre)='' or telefono is null or trim(telefono)='' " +
                    "or correo is null or trim(correo)='' or idCarrera is null)" );

            System.out.println("\n== RESIDENTES WITH GAPS ==");
            dumpQuery(st,
                    "select id,idEstudiante,fotoPath from residente where estatus=1 and (idEstudiante is null or fotoPath is null or trim(fotoPath)='')" );

            System.out.println("\n== RESIDENCIAS WITH GAPS ==");
            dumpQuery(st,
                    "select id,nombreProyecto,periodo,fechaInicio,fechaFin,estatusProceso,idProyectoCarrera,estadoAutorizacion,fechaAutorizacion,origenProyecto,carreraJefeArea,observacionesAutorizacion,idEmpresa,idResidente,idAsesorInterno,idAsesorExterno " +
                    "from residencia where estatus=1 and (" +
                    "nombreProyecto is null or trim(nombreProyecto)='' or periodo is null or trim(periodo)='' or fechaInicio is null or fechaFin is null " +
                    "or estatusProceso is null or trim(estatusProceso)='' or idProyectoCarrera is null or trim(idProyectoCarrera)='' " +
                    "or estadoAutorizacion is null or trim(estadoAutorizacion)='' or origenProyecto is null or trim(origenProyecto)='' " +
                    "or idEmpresa is null or idResidente is null or idAsesorExterno is null)" );

            System.out.println("\n== BANCO PROYECTOS WITH GAPS ==");
            dumpQuery(st,
                    "select id,nombreProyecto,periodo,estado,origen,fechaPropuesta,fechaRevision,idCarrera,idEmpresa,idResidente,observaciones " +
                    "from banco_proyectos where estatus=1 and (" +
                    "nombreProyecto is null or trim(nombreProyecto)='' or periodo is null or trim(periodo)='' or estado is null or trim(estado)='' " +
                    "or origen is null or trim(origen)='' or fechaPropuesta is null or idCarrera is null or idEmpresa is null)" );
        }
    }

    private static void dumpCount(Statement st, String table) throws Exception {
        try (ResultSet rs = st.executeQuery("select count(*) from " + table)) {
            if (rs.next()) {
                System.out.println(table + ": " + rs.getInt(1));
            }
        }
    }

    private static void dumpQuery(Statement st, String sql) throws Exception {
        try (ResultSet rs = st.executeQuery(sql)) {
            int cols = rs.getMetaData().getColumnCount();
            int rows = 0;
            while (rs.next()) {
                rows++;
                StringBuilder line = new StringBuilder();
                for (int i = 1; i <= cols; i++) {
                    if (i > 1) {
                        line.append(" | ");
                    }
                    line.append(rs.getMetaData().getColumnLabel(i)).append("=").append(rs.getString(i));
                }
                System.out.println(line);
            }
            if (rows == 0) {
                System.out.println("(no gaps)");
            }
        }
    }
}
