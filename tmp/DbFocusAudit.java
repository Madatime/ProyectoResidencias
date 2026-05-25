import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbFocusAudit {
    private static final String URL =
            "jdbc:mysql://localhost:3306/proyectoResidencia?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "12345";

    public static void main(String[] args) throws Exception {
        try (Connection cn = DriverManager.getConnection(URL, USER, PASS);
             Statement st = cn.createStatement()) {

            dump("Residencias con placeholder o texto vacio",
                    st,
                    "select id,nombreProyecto,descripcion,objetivo,periodo,estadoAutorizacion,observacionesAutorizacion " +
                    "from residencia where estatus=1 and (" +
                    "lower(trim(coalesce(nombreProyecto,'')))='xxx' or " +
                    "lower(trim(coalesce(descripcion,'')))='xxx' or " +
                    "lower(trim(coalesce(objetivo,'')))='xxx' or " +
                    "trim(coalesce(descripcion,''))='' or trim(coalesce(objetivo,''))='')");

            dump("Residentes huerfanos o sin foto",
                    st,
                    "select r.id,r.idEstudiante,r.fotoPath,e.matricula,e.nombre,e.apellidos " +
                    "from residente r left join estudiante e on e.id=r.idEstudiante " +
                    "where r.estatus=1 and (r.idEstudiante is null or r.fotoPath is null or trim(coalesce(r.fotoPath,''))='')");

            dump("Residencias activas por residente",
                    st,
                    "select r.id as idResidencia,r.idResidente,res.nombre,est.matricula " +
                    "from residencia r left join residente res on res.id=r.idResidente " +
                    "left join estudiante est on est.id=res.idEstudiante where r.estatus=1 order by r.id");
        }
    }

    private static void dump(String title, Statement st, String sql) throws Exception {
        System.out.println("\n== " + title + " ==");
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
                System.out.println("(none)");
            }
        }
    }
}
