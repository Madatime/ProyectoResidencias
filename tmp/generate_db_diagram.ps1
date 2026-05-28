$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.Drawing

$outputPath = 'C:\Users\Daniel\Desktop\ProyectoResidencias - copia\tmp\ER_MODEL_COMPLETO.png'
$width = 3000
$height = 1900

$bmp = New-Object System.Drawing.Bitmap $width, $height
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
$g.Clear([System.Drawing.Color]::White)

$titleFont = New-Object System.Drawing.Font('Arial', 20, [System.Drawing.FontStyle]::Bold)
$headerFont = New-Object System.Drawing.Font('Arial', 11, [System.Drawing.FontStyle]::Bold)
$textFont = New-Object System.Drawing.Font('Arial', 8.4, [System.Drawing.FontStyle]::Regular)
$smallFont = New-Object System.Drawing.Font('Arial', 8, [System.Drawing.FontStyle]::Bold)

$borderPen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(70, 90, 110)), 2
$linePen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(45, 45, 45)), 2
$dashPen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(145, 145, 145)), 2
$dashPen.DashStyle = [System.Drawing.Drawing2D.DashStyle]::Dash
$fillBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::White)
$textBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(20, 20, 20))
$mutedBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(70, 70, 70))
$shadowBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(22, 0, 0, 0))

function New-HeaderBrush {
    param([int]$R, [int]$G, [int]$B)
    return New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb($R, $G, $B))
}

$headerBrushes = @{
    Purple = (New-HeaderBrush 232 220 255)
    Blue = (New-HeaderBrush 205 227 255)
    Teal = (New-HeaderBrush 201 241 245)
    Green = (New-HeaderBrush 219 244 207)
    Yellow = (New-HeaderBrush 255 236 170)
    Orange = (New-HeaderBrush 255 221 196)
    Red = (New-HeaderBrush 255 214 214)
}

function Draw-Box {
    param(
        [string]$Title,
        [string[]]$Lines,
        [int]$X,
        [int]$Y,
        [int]$W,
        [string]$ColorKey = 'Blue'
    )

    $lineHeight = 19
    $headerHeight = 30
    $padding = 10
    $H = $headerHeight + ($Lines.Count * $lineHeight) + ($padding * 2)

    $g.FillRectangle($shadowBrush, $X + 6, $Y + 6, $W, $H)
    $g.FillRectangle($fillBrush, $X, $Y, $W, $H)
    $g.DrawRectangle($borderPen, $X, $Y, $W, $H)
    $g.FillRectangle($headerBrushes[$ColorKey], $X, $Y, $W, $headerHeight)
    $g.DrawRectangle($borderPen, $X, $Y, $W, $headerHeight)

    $sf = New-Object System.Drawing.StringFormat
    $sf.Alignment = [System.Drawing.StringAlignment]::Center
    $sf.LineAlignment = [System.Drawing.StringAlignment]::Center
    $g.DrawString($Title, $headerFont, $textBrush, (New-Object System.Drawing.RectangleF($X, $Y, $W, $headerHeight)), $sf)

    $currentY = $Y + $headerHeight + $padding
    foreach ($line in $Lines) {
        $g.DrawString($line, $textFont, $textBrush, $X + 10, $currentY)
        $currentY += $lineHeight
    }

    return @{
        X = $X; Y = $Y; W = $W; H = $H;
        CX = [int]($X + ($W / 2));
        CY = [int]($Y + ($H / 2));
        LeftX = $X;
        RightX = $X + $W;
        TopY = $Y;
        BottomY = $Y + $H
    }
}

function Draw-Line {
    param(
        [int]$X1, [int]$Y1, [int]$X2, [int]$Y2,
        [bool]$Dashed = $false
    )
    if ($Dashed) {
        $g.DrawLine($dashPen, $X1, $Y1, $X2, $Y2)
    } else {
        $g.DrawLine($linePen, $X1, $Y1, $X2, $Y2)
    }
}

function Draw-Label {
    param([string]$Text, [int]$X, [int]$Y)
    $g.DrawString($Text, $smallFont, $mutedBrush, $X, $Y)
}

$g.DrawString('Modelo E-R completo de la base de datos', $titleFont, $textBrush, 980, 24)

$perfil = Draw-Box 'PERFIL' @(
    'PK  id                  INT',
    '    nombre              VARCHAR(100)',
    '    estatus             INT'
) 40 90 300 'Purple'

$usuarioPerfil = Draw-Box 'USUARIO_PERFIL' @(
    'PK  id                  INT',
    'FK  idUsuario           INT',
    'FK  idPerfil            INT',
    '    estatus             INT'
) 400 90 300 'Blue'

$usuario = Draw-Box 'USUARIO' @(
    'PK  id                  INT',
    '    username            VARCHAR(100)',
    '    password            VARCHAR(255)',
    '    nombreCompleto      VARCHAR(150)',
    '    nombreMostrar       VARCHAR(150)',
    '    email               VARCHAR(150)',
    '    rol                 VARCHAR(50)',
    '    estatus             INT',
    'FK  idDocente           INT',
    'FK  idResidente         INT',
    'FK  idAsesorExterno     INT'
) 800 80 380 'Teal'

$docentes = Draw-Box 'DOCENTES' @(
    'PK  id                  INT',
    '    noEmpleado          VARCHAR(20)',
    '    nombre              VARCHAR(80)',
    '    apellidos           VARCHAR(120)',
    '    correo              VARCHAR(120)',
    '    telefono            VARCHAR(20)',
    '    fotoPath            VARCHAR(255)',
    '    estatus             INT'
) 2300 60 360 'Blue'

$carreras = Draw-Box 'CARRERAS' @(
    'PK  id                  INT',
    '    nombre              VARCHAR(150)',
    '    estatus             INT'
) 900 470 350 'Green'

$docenteCarrera = Draw-Box 'DOCENTE_CARRERA' @(
    'PK,FK idDocente         INT',
    'PK,FK idCarrera         INT'
) 420 520 340 'Yellow'

$directivos = Draw-Box 'DIRECTIVOS' @(
    'PK  id                  INT',
    '    claveDirectivo      VARCHAR(25)',
    'FK  idDocente           INT',
    '    tipoDirectivo       VARCHAR(50)',
    '    puesto              VARCHAR(120)',
    '    departamento        VARCHAR(120)',
    '    firmaPath           VARCHAR(255)',
    '    selloPath           VARCHAR(255)',
    '    estatus             INT'
) 60 920 360 'Purple'

$asesorInterno = Draw-Box 'ASESOR_INTERNO' @(
    'PK  id                  INT',
    '    claveAsesor         VARCHAR(25)',
    'FK  idDocente           INT',
    '    estatus             INT'
) 520 950 340 'Yellow'

$estudiante = Draw-Box 'ESTUDIANTE' @(
    'PK  id                  INT',
    '    matricula           VARCHAR(50)',
    '    nombre              VARCHAR(100)',
    '    apellidos           VARCHAR(100)',
    '    sexo                VARCHAR(10)',
    '    semestre            VARCHAR(20)',
    '    telefono            VARCHAR(20)',
    '    correo              VARCHAR(100)',
    '    estatus             INT',
    'FK  idCarrera           INT'
) 880 860 360 'Red'

$residente = Draw-Box 'RESIDENTE' @(
    'PK  id                  INT',
    'FK  idEstudiante        INT',
    '    fotoPath            VARCHAR(255)',
    '    estatus             INT'
) 970 1280 330 'Green'

$empresa = Draw-Box 'EMPRESA' @(
    'PK  id                  INT',
    '    nombre              VARCHAR(150)',
    '    giro                VARCHAR(150)',
    '    direccion           VARCHAR(255)',
    '    telefono            VARCHAR(20)',
    '    correo              VARCHAR(100)',
    '    representante       VARCHAR(150)',
    '    puestoRepresentante VARCHAR(100)',
    '    dueno               VARCHAR(150)',
    '    convenio            VARCHAR(255)',
    '    anioConvenio        INT',
    '    vigenciaConvenio    INT',
    '    anioFinConvenio     INT',
    '    estatus             INT'
) 1320 820 420 'Teal'

$asesorExterno = Draw-Box 'ASESOR_EXTERNO' @(
    'PK  id                  INT',
    '    nombre              VARCHAR(100)',
    '    apellidos           VARCHAR(100)',
    '    empresa             VARCHAR(150)',
    '    cargo               VARCHAR(100)',
    '    telefono            VARCHAR(20)',
    '    correo              VARCHAR(100)',
    '    fotoPath            VARCHAR(255)',
    '    estatus             INT'
) 2020 820 370 'Orange'

$banco = Draw-Box 'BANCO_PROYECTOS' @(
    'PK  id                  INT',
    '    nombreProyecto      VARCHAR(200)',
    '    descripcion         TEXT',
    '    objetivo            TEXT',
    '    periodo             VARCHAR(100)',
    '    estado              VARCHAR(50)',
    '    origen              VARCHAR(50)',
    '    observaciones       TEXT',
    '    fechaPropuesta      DATE',
    '    fechaRevision       DATE',
    '    estatus             INT',
    'FK  idEmpresa           INT',
    'FK  idCarrera           INT',
    'FK  idResidente         INT'
) 1760 1150 420 'Red'

$residencia = Draw-Box 'RESIDENCIA' @(
    'PK  id                  INT',
    '    nombreProyecto      VARCHAR(200)',
    '    descripcion         TEXT',
    '    objetivo            TEXT',
    '    totalRechazos       INT',
    'FK  idEmpresa           INT',
    '    periodo             VARCHAR(100)',
    '    fechaInicio         DATE',
    '    fechaFin            DATE',
    '    estatus             INT',
    '    estatusProceso      VARCHAR(50)',
    '    fechaCierre         DATE',
    '    idProyectoCarrera   VARCHAR(100)',
    '    estadoAutorizacion  VARCHAR(50)',
    '    fechaAutorizacion   DATE',
    '    origenProyecto      VARCHAR(50)',
    '    carreraJefeArea     VARCHAR(150)',
    '    observacionesAut... TEXT',
    'FK  idResidente         INT',
    'FK  idAsesorInterno     INT',
    'FK  idAsesorExterno     INT'
) 1260 1320 430 'Blue'

$documentoResidencia = Draw-Box 'DOCUMENTO_RESIDENCIA' @(
    'PK  id                  INT',
    '    tipoDocumento       VARCHAR(100)',
    '    nombreArchivo       VARCHAR(255)',
    '    rutaArchivo         VARCHAR(255)',
    '    estatus             VARCHAR(50)',
    '    observaciones       TEXT',
    '    fechaCarga          DATETIME',
    '    fechaRevision       DATETIME',
    '    estatusRegistro     INT',
    'FK  idResidencia        INT'
) 2190 1260 410 'Yellow'

$evaluacionResidencia = Draw-Box 'EVALUACION_RESIDENCIA' @(
    'PK  id                  INT',
    '    tipoEvaluacion      VARCHAR(100)',
    '    calificacion        DECIMAL(5,2)',
    '    observaciones       TEXT',
    '    fechaEvaluacion     DATE',
    '    evaluadorNombre     VARCHAR(150)',
    '    evaluadorRol        VARCHAR(50)',
    '    estatus             INT',
    '    criterio1           DECIMAL(5,2)',
    '    criterio2           DECIMAL(5,2)',
    '    criterio3           DECIMAL(5,2)',
    '    criterio4           DECIMAL(5,2)',
    '    criterio5           DECIMAL(5,2)',
    '    criterio6           DECIMAL(5,2)',
    '    criterio7           DECIMAL(5,2)',
    '    criterio8           DECIMAL(5,2)',
    '    criterio9           DECIMAL(5,2)',
    '    criterio10          DECIMAL(5,2)',
    '    criterio11          DECIMAL(5,2)',
    '    criterio12          DECIMAL(5,2)',
    '    criterio13          DECIMAL(5,2)',
    'FK  idResidencia        INT'
) 2610 1200 350 'Purple'

$departamento = Draw-Box 'DEPARTAMENTO' @('PK  id                  INT') 70 1560 260 'Blue'
$detalleEvaluacion = Draw-Box 'DETALLE_EVALUACION' @('PK  id                  INT') 370 1560 300 'Orange'
$documento = Draw-Box 'DOCUMENTO' @('PK  id                  INT') 710 1560 260 'Teal'
$evaluacion = Draw-Box 'EVALUACION' @('PK  id                  INT') 1010 1560 260 'Purple'
$jefeDepartamento = Draw-Box 'JEFE_DEPARTAMENTO' @('PK  id                  INT') 1310 1660 320 'Red'
$proyecto = Draw-Box 'PROYECTO' @('PK  id                  INT') 1670 1660 250 'Green'
$proyectoResidencia = Draw-Box 'PROYECTO_RESIDENCIA' @('PK  id                  INT') 1960 1660 340 'Blue'
$rol = Draw-Box 'ROL' @('PK  id                  INT') 2340 1660 220 'Yellow'
$seguimiento = Draw-Box 'SEGUIMIENTO' @('PK  id                  INT') 2600 1660 290 'Teal'

# Confirmed relationships
Draw-Line $perfil.RightX $perfil.CY $usuarioPerfil.X $usuarioPerfil.CY
Draw-Label '1' ($perfil.RightX + 8) ($perfil.CY - 18)
Draw-Label 'N' ($usuarioPerfil.X - 18) ($usuarioPerfil.CY - 18)

Draw-Line $usuarioPerfil.RightX $usuarioPerfil.CY $usuario.X ($usuario.CY - 40)
Draw-Label 'N' ($usuarioPerfil.RightX + 8) ($usuarioPerfil.CY - 18)
Draw-Label '1' ($usuario.X - 18) ($usuario.CY - 58)

Draw-Line $usuario.RightX ($usuario.Y + 140) $docentes.X ($docentes.CY - 10) $true
Draw-Label '0..1' ($usuario.RightX + 6) ($usuario.Y + 122)
Draw-Label '1' ($docentes.X - 14) ($docentes.CY - 28)

Draw-Line $usuario.RightX ($usuario.Y + 178) $residente.X ($residente.CY - 18) $true
Draw-Label '0..1' ($usuario.RightX + 6) ($usuario.Y + 160)
Draw-Label '1' ($residente.X - 14) ($residente.CY - 36)

Draw-Line $usuario.RightX ($usuario.Y + 216) $asesorExterno.X ($asesorExterno.CY - 44) $true
Draw-Label '0..1' ($usuario.RightX + 6) ($usuario.Y + 198)
Draw-Label '1' ($asesorExterno.X - 14) ($asesorExterno.CY - 62)

Draw-Line $docentes.LeftX ($docentes.Y + 170) $docenteCarrera.RightX $docenteCarrera.CY
Draw-Label '1' ($docentes.LeftX - 18) ($docentes.Y + 152)
Draw-Label 'N' ($docenteCarrera.RightX + 4) ($docenteCarrera.CY - 18)

Draw-Line $docenteCarrera.LeftX $docenteCarrera.CY $carreras.X ($carreras.CY - 10)
Draw-Label 'N' ($docenteCarrera.LeftX - 18) ($docenteCarrera.CY - 18)
Draw-Label '1' ($carreras.X + 4) ($carreras.CY - 28)

Draw-Line $docentes.LeftX ($docentes.CY + 20) $directivos.RightX ($directivos.CY - 25)
Draw-Label '1' ($docentes.LeftX - 18) ($docentes.CY + 2)
Draw-Label 'N' ($directivos.RightX + 4) ($directivos.CY - 43)

Draw-Line $docentes.LeftX ($docentes.CY + 50) $asesorInterno.RightX ($asesorInterno.CY - 16)
Draw-Label '1' ($docentes.LeftX - 18) ($docentes.CY + 32)
Draw-Label 'N' ($asesorInterno.RightX + 4) ($asesorInterno.CY - 34)

Draw-Line $carreras.CX $carreras.BottomY $estudiante.CX $estudiante.Y
Draw-Label '1' ($carreras.CX + 8) ($carreras.BottomY - 16)
Draw-Label 'N' ($estudiante.CX + 8) ($estudiante.Y - 18)

Draw-Line $estudiante.CX $estudiante.BottomY $residente.CX $residente.Y
Draw-Label '1' ($estudiante.CX + 8) ($estudiante.BottomY - 16)
Draw-Label 'N' ($residente.CX + 8) ($residente.Y - 18)

Draw-Line $empresa.RightX ($empresa.CY - 60) $banco.X ($banco.CY - 50)
Draw-Label '1' ($empresa.RightX + 8) ($empresa.CY - 78)
Draw-Label 'N' ($banco.X - 18) ($banco.CY - 68)

Draw-Line $carreras.RightX ($carreras.CY + 15) $banco.X ($banco.CY - 10)
Draw-Label '1' ($carreras.RightX + 8) ($carreras.CY - 3)
Draw-Label 'N' ($banco.X - 18) ($banco.CY - 28)

Draw-Line $residente.RightX ($residente.CY - 18) $banco.X ($banco.CY + 28)
Draw-Label '1' ($residente.RightX + 8) ($residente.CY - 36)
Draw-Label 'N' ($banco.X - 18) ($banco.CY + 10)

Draw-Line $empresa.CX $empresa.BottomY $residencia.CX ($residencia.Y - 10)
Draw-Label '1' ($empresa.CX + 8) ($empresa.BottomY - 16)
Draw-Label 'N' ($residencia.CX + 8) ($residencia.Y - 28)

Draw-Line $residente.RightX ($residente.CY + 18) $residencia.X ($residencia.CY + 50)
Draw-Label '1' ($residente.RightX + 8) ($residente.CY)
Draw-Label 'N' ($residencia.X - 18) ($residencia.CY + 32)

Draw-Line $asesorInterno.RightX ($asesorInterno.CY + 10) $residencia.X ($residencia.CY - 10)
Draw-Label '1' ($asesorInterno.RightX + 8) ($asesorInterno.CY - 8)
Draw-Label 'N' ($residencia.X - 18) ($residencia.CY - 28)

Draw-Line $asesorExterno.LeftX ($asesorExterno.CY + 10) $residencia.RightX ($residencia.CY - 35)
Draw-Label '1' ($asesorExterno.LeftX - 18) ($asesorExterno.CY - 8)
Draw-Label 'N' ($residencia.RightX + 6) ($residencia.CY - 53)

Draw-Line $residencia.RightX ($residencia.CY + 30) $documentoResidencia.X ($documentoResidencia.CY - 20)
Draw-Label '1' ($residencia.RightX + 8) ($residencia.CY + 12)
Draw-Label 'N' ($documentoResidencia.X - 18) ($documentoResidencia.CY - 38)

Draw-Line $residencia.RightX ($residencia.CY + 70) $evaluacionResidencia.X ($evaluacionResidencia.CY - 10)
Draw-Label '1' ($residencia.RightX + 8) ($residencia.CY + 52)
Draw-Label 'N' ($evaluacionResidencia.X - 18) ($evaluacionResidencia.CY - 28)

# Legacy group dashed relationships for layout only
Draw-Line $departamento.RightX $departamento.CY $detalleEvaluacion.X $detalleEvaluacion.CY $true
Draw-Line $detalleEvaluacion.RightX $detalleEvaluacion.CY $documento.X $documento.CY $true
Draw-Line $documento.RightX $documento.CY $evaluacion.X $evaluacion.CY $true
Draw-Line $evaluacion.RightX $evaluacion.CY $jefeDepartamento.X $jefeDepartamento.CY $true
Draw-Line $jefeDepartamento.RightX $jefeDepartamento.CY $proyecto.X $proyecto.CY $true
Draw-Line $proyecto.RightX $proyecto.CY $proyectoResidencia.X $proyectoResidencia.CY $true
Draw-Line $proyectoResidencia.RightX $proyectoResidencia.CY $rol.X $rol.CY $true
Draw-Line $rol.RightX $rol.CY $seguimiento.X $seguimiento.CY $true

$leyenda = Draw-Box 'LEYENDA' @(
    'PK   Clave primaria',
    'FK   Clave foranea',
    '1    Uno',
    'N    Muchos',
    '---  Relacion no confirmada'
) 40 1260 300 'Blue'

$bmp.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)

$g.Dispose()
$bmp.Dispose()
$titleFont.Dispose()
$headerFont.Dispose()
$textFont.Dispose()
$smallFont.Dispose()
$borderPen.Dispose()
$linePen.Dispose()
$dashPen.Dispose()
$fillBrush.Dispose()
$textBrush.Dispose()
$mutedBrush.Dispose()
$shadowBrush.Dispose()
$headerBrushes.Values | ForEach-Object { $_.Dispose() }

Write-Output $outputPath
