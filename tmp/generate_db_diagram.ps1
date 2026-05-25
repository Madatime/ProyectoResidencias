$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.Drawing

$outputPath = 'C:\Users\Daniel\Downloads\DIAGRAMA_BD_ACTUAL.png'
$width = 2200
$height = 1500

$bmp = New-Object System.Drawing.Bitmap $width, $height
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.Clear([System.Drawing.Color]::FromArgb(248,250,252))

$titleFont = New-Object System.Drawing.Font('Arial', 20, [System.Drawing.FontStyle]::Bold)
$headerFont = New-Object System.Drawing.Font('Arial', 11, [System.Drawing.FontStyle]::Bold)
$textFont = New-Object System.Drawing.Font('Arial', 8.5, [System.Drawing.FontStyle]::Regular)
$labelFont = New-Object System.Drawing.Font('Arial', 8, [System.Drawing.FontStyle]::Bold)

$pen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(71,85,105)), 2
$linePen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(100,116,139)), 2
$fill = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::White)
$headerFill = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(219,234,254))
$textBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(15,23,42))
$labelBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(51,65,85))

function Draw-Box {
    param(
        [string]$Title,
        [string[]]$Lines,
        [int]$X,
        [int]$Y,
        [int]$W,
        [int]$H
    )

    $g.FillRectangle($fill, $X, $Y, $W, $H)
    $g.DrawRectangle($pen, $X, $Y, $W, $H)
    $g.FillRectangle($headerFill, $X, $Y, $W, 28)
    $g.DrawRectangle($pen, $X, $Y, $W, 28)

    $sf = New-Object System.Drawing.StringFormat
    $sf.Alignment = [System.Drawing.StringAlignment]::Center
    $sf.LineAlignment = [System.Drawing.StringAlignment]::Center
    $g.DrawString($Title, $headerFont, $textBrush, (New-Object System.Drawing.RectangleF($X, $Y, $W, 28)), $sf)

    $lineY = $Y + 36
    foreach ($line in $Lines) {
        $g.DrawString($line, $textFont, $textBrush, $X + 10, $lineY)
        $lineY += 18
    }
}

function Draw-Rel {
    param(
        [int]$X1, [int]$Y1, [int]$X2, [int]$Y2,
        [string]$LeftLabel = '',
        [string]$RightLabel = ''
    )
    $g.DrawLine($linePen, $X1, $Y1, $X2, $Y2)
    if ($LeftLabel) { $g.DrawString($LeftLabel, $labelFont, $labelBrush, $X1 + 4, $Y1 - 16) }
    if ($RightLabel) { $g.DrawString($RightLabel, $labelFont, $labelBrush, $X2 + 4, $Y2 - 16) }
}

$g.DrawString('Diagrama actual de la base de datos', $titleFont, $textBrush, 780, 20)

Draw-Box 'PERFIL' @('id : int PK','nombre : varchar','estatus : int') 40 80 220 100
Draw-Box 'USUARIO' @('id : int PK','idDocente : int FK','idResidente : int FK','idAsesorExterno : int FK','username : varchar','password : varchar','estatus : int') 320 60 280 180
Draw-Box 'USUARIO_PERFIL' @('id : int PK','idUsuario : int FK','idPerfil : int FK','estatus : int') 680 80 240 120
Draw-Box 'CARRERAS' @('id : int PK','nombre : varchar','estatus : int') 40 300 220 100
Draw-Box 'DOCENTES' @('id : int PK','noEmpleado : varchar','nombre : varchar','apellidos : varchar','correo : varchar','telefono : varchar','fotoPath : varchar') 320 280 300 190
Draw-Box 'DOCENTE_CARRERA' @('idDocente : int FK','idCarrera : int FK') 700 320 220 80
Draw-Box 'DIRECTIVOS' @('id : int PK','claveDirectivo : varchar','idDocente : int FK','tipoDirectivo : enum','puesto : varchar','departamento : varchar') 980 280 280 170
Draw-Box 'ASESOR_INTERNO' @('id : int PK','claveAsesor : varchar','idDocente : int FK','estatus : int') 980 500 280 130
Draw-Box 'ESTUDIANTE' @('id : int PK','matricula : varchar','nombre : varchar','apellidos : varchar','sexo : varchar','semestre : varchar','idCarrera : int FK') 40 520 280 180
Draw-Box 'RESIDENTE' @('id : int PK','idEstudiante : int FK','fotoPath : varchar') 380 560 240 100
Draw-Box 'EMPRESA' @('id : int PK','nombre : varchar','giro : varchar','direccion : varchar','telefono : varchar','correo : varchar','representante : varchar','convenio : varchar','anioFinConvenio : int') 1360 80 280 220
Draw-Box 'ASESOR_EXTERNO' @('id : int PK','nombre : varchar','apellidos : varchar','empresa : varchar','cargo : varchar','correo : varchar') 1760 80 280 170
Draw-Box 'BANCO_PROYECTOS' @('id : int PK','nombreProyecto : varchar','descripcion : text','objetivo : text','periodo : varchar','estado : enum/texto','idEmpresa : int FK','idCarrera : int FK','idResidente : int FK') 1360 380 320 210
Draw-Box 'RESIDENCIA' @('id : int PK','nombreProyecto : varchar','descripcion : text','objetivo : text','idEmpresa : int FK','idResidente : int FK','idAsesorInterno : int FK','idAsesorExterno : int FK','periodo : varchar','fechaInicio / fechaFin : date','estatusProceso : varchar','estadoAutorizacion : varchar') 780 760 420 290
Draw-Box 'DOCUMENTO_RESIDENCIA' @('id : int PK','idResidencia : int FK','tipoDocumento : enum','nombreArchivo : varchar','rutaArchivo : varchar','estatus : enum') 520 1180 320 170
Draw-Box 'EVALUACION_RESIDENCIA' @('id : int PK','idResidencia : int FK','tipoEvaluacion : enum','calificacion : double','observaciones : text','fechaEvaluacion : date','evaluadorNombre : varchar','criterio1..criterio13 : double') 920 1140 420 210

Draw-Rel 260 130 320 130 '1' 'N'
Draw-Rel 600 145 680 145 '1' 'N'
Draw-Rel 150 400 150 520 '1' 'N'
Draw-Rel 620 355 700 355 '1' 'N'
Draw-Rel 620 370 980 320 '1' 'N'
Draw-Rel 620 405 980 520 '1' 'N'
Draw-Rel 320 610 380 610 '1' 'N'
Draw-Rel 1520 300 1520 380 '1' 'N'
Draw-Rel 260 350 1360 350 '1 carrera' ''
Draw-Rel 1360 350 1360 560 '' 'N'
Draw-Rel 620 610 780 910 '1' 'N'
Draw-Rel 1640 215 1200 960 '1' 'N'
Draw-Rel 1260 570 1200 900 '1' 'N'
Draw-Rel 1120 630 1120 760 '1' 'N'
Draw-Rel 1500 590 1180 760 '1' 'N'
Draw-Rel 990 1050 990 1180 '1' 'N'
Draw-Rel 1100 1050 1100 1140 '1' 'N'
Draw-Rel 460 240 500 560 '1' '1'

$bmp.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose()
$bmp.Dispose()
$titleFont.Dispose()
$headerFont.Dispose()
$textFont.Dispose()
$labelFont.Dispose()
$pen.Dispose()
$linePen.Dispose()
$fill.Dispose()
$headerFill.Dispose()
$textBrush.Dispose()
$labelBrush.Dispose()

Write-Output $outputPath
