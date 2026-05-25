import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class AuditDb {
    private static final String URL =
            "jdbc:mysql://localhost:3306/proyectoResidencia?useSSL=false&serverTimezone=America/Mexico_City&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "12345";

    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection cn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement st = cn.createStatement()) {

            print(st, "Residencias con huecos visibles",
                    """
                    select r.id,
                           coalesce(r.nombreProyecto, '') nombreProyecto,
                           coalesce(r.periodo, '') periodo,
                           coalesce(e.nombre, '') empresa,
                           coalesce(concat(ai.nombre, ' ', ai.apellidos), '') asesorInterno,
                           coalesce(concat(ae.nombre, ' ', ae.apellidos), '') asesorExterno,
                           coalesce(r.estadoAutorizacion, '') autorizacion,
                           r.fechaAutorizacion
                    from residencia r
                    left join empresa e on e.id = r.idEmpresa
                    left join (
                        select ai.id, d.nombre, d.apellidos
                        from asesor_interno ai
                        left join docentes d on d.id = ai.idDocente
                    ) ai on ai.id = r.idAsesorInterno
                    left join asesor_externo ae on ae.id = r.idAsesorExterno
                    where r.estatus = 1
                      and (
                           r.idEmpresa is null
                           or r.idAsesorInterno is null
                           or r.idAsesorExterno is null
                           or r.estadoAutorizacion is null
                           or trim(coalesce(r.estadoAutorizacion, '')) = ''
                           or (r.estadoAutorizacion <> 'PENDIENTE' and r.fechaAutorizacion is null)
                      )
                    order by r.id
                    """);

            print(st, "Empresas con huecos",
                    """
                    select id, nombre, representante, puestoRepresentante, telefono, correo, convenio,
                           anioConvenio, anioFinConvenio, vigenciaConvenio
                    from empresa
                    where estatus = 1
                      and (
                          trim(coalesce(representante, '')) = ''
                          or trim(coalesce(puestoRepresentante, '')) = ''
                          or trim(coalesce(telefono, '')) = ''
                          or trim(coalesce(correo, '')) = ''
                          or trim(coalesce(convenio, '')) = ''
                          or anioConvenio is null
                          or anioFinConvenio is null
                          or trim(coalesce(vigenciaConvenio, '')) = ''
                      )
                    order by id
                    """);

            print(st, "Esquema empresa relevante",
                    """
                    select column_name, column_type, is_nullable
                    from information_schema.columns
                    where table_schema = 'proyectoResidencia'
                      and table_name = 'empresa'
                      and column_name in ('convenio', 'anioConvenio', 'anioFinConvenio', 'vigenciaConvenio')
                    order by ordinal_position
                    """);

            print(st, "Asesores internos sin docente o con datos vacios",
                    """
                    select ai.id, ai.idDocente, d.nombre, d.apellidos, d.correo, d.telefono
                    from asesor_interno ai
                    left join docentes d on d.id = ai.idDocente
                    where ai.estatus = 1
                      and (
                          ai.idDocente is null
                          or trim(coalesce(d.nombre, '')) = ''
                          or trim(coalesce(d.apellidos, '')) = ''
                          or trim(coalesce(d.correo, '')) = ''
                          or trim(coalesce(d.telefono, '')) = ''
                      )
                    order by ai.id
                    """);

            print(st, "Asesores externos con huecos",
                    """
                    select id, nombre, apellidos, cargo, correo, empresa, telefono
                    from asesor_externo
                    where estatus = 1
                      and (
                          trim(coalesce(nombre, '')) = ''
                          or trim(coalesce(apellidos, '')) = ''
                          or trim(coalesce(cargo, '')) = ''
                          or trim(coalesce(correo, '')) = ''
                          or trim(coalesce(empresa, '')) = ''
                          or trim(coalesce(telefono, '')) = ''
                      )
                    order by id
                    """);

            print(st, "Resumen de residencias activas",
                    """
                    select count(*) total,
                           sum(case when idEmpresa is null then 1 else 0 end) sinEmpresa,
                           sum(case when idAsesorInterno is null then 1 else 0 end) sinAsesorInterno,
                           sum(case when idAsesorExterno is null then 1 else 0 end) sinAsesorExterno,
                           sum(case when estadoAutorizacion is null or trim(estadoAutorizacion) = '' then 1 else 0 end) sinAutorizacion,
                           sum(case when fechaAutorizacion is null then 1 else 0 end) sinFechaAutorizacion
                    from residencia
                    where estatus = 1
                    """);

            print(st, "Listado visible de residencias activas",
                    """
                    select r.id,
                           est.matricula noControl,
                           concat(est.nombre, ' ', est.apellidos) estudiante,
                           est.sexo,
                           r.nombreProyecto,
                           e.nombre empresa,
                           concat(di.nombre, ' ', di.apellidos) asesorInterno,
                           concat(ae.nombre, ' ', ae.apellidos) asesorExterno,
                           r.estadoAutorizacion,
                           r.fechaAutorizacion
                    from residencia r
                    left join residente rr on rr.id = r.idResidente
                    left join estudiante est on est.id = rr.idEstudiante
                    left join empresa e on e.id = r.idEmpresa
                    left join asesor_interno ai on ai.id = r.idAsesorInterno
                    left join docentes di on di.id = ai.idDocente
                    left join asesor_externo ae on ae.id = r.idAsesorExterno
                    where r.estatus = 1
                    order by r.id
                    """);
        }
    }

    private static void print(Statement st, String title, String sql) throws Exception {
        System.out.println("=== " + title + " ===");
        try (ResultSet rs = st.executeQuery(sql)) {
            int cols = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                StringBuilder row = new StringBuilder();
                for (int i = 1; i <= cols; i++) {
                    if (i > 1) {
                        row.append(" | ");
                    }
                    row.append(rs.getMetaData().getColumnLabel(i)).append("=").append(rs.getString(i));
                }
                System.out.println(row);
            }
        }
        System.out.println();
    }
}
