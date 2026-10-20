$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$project = Split-Path $PSScriptRoot -Parent
$assets = Join-Path $project 'common-1.21.1/src/main/resources/assets/kitsunebi'
$names = @('phantom_stairs_high','phantom_stairs_mid','phantom_stairs_low','phantom_slope_high','phantom_slope_mid','phantom_slope_low','phantom_fence','phantom_wall','phantom_door','phantom_button','phantom_lever','phantom_pressure_plate')
$sheet = [System.Drawing.Bitmap]::new(960,360)
$g = [System.Drawing.Graphics]::FromImage($sheet)
$g.Clear([System.Drawing.Color]::FromArgb(255,42,48,60))
$pen = [System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb(255,62,102,124),1)
$font = [System.Drawing.Font]::new('Arial',9)
function ProjectPoint([double]$x,[double]$y,[double]$z,[double]$ox,[double]$oy) {
    return [System.Drawing.PointF]::new([single]($ox+($x-$z)*3.3),[single]($oy+($x+$z)*1.65-$y*3.3))
}
for($i=0;$i -lt $names.Count;$i++) {
    $item = Get-Content (Join-Path $assets ('models/item/'+$names[$i]+'.json')) -Raw | ConvertFrom-Json
    $model = if ($item.elements) { $item } else { Get-Content (Join-Path $assets ('models/'+$item.parent.Substring(10)+'.json')) -Raw | ConvertFrom-Json }
    $ox = ($i % 6)*160+80
    $oy = [Math]::Floor($i/6)*180+80
    foreach($e in ($model.elements | Sort-Object { $_.from[0]+$_.to[0]+$_.from[2]+$_.to[2]+0.01*$_.from[1] })) {
        $a=$e.from; $b=$e.to
        $polys = @(
            @{Color=([System.Drawing.Color]::FromArgb(255,65,103,127)); P=@(@($b[0],$a[1],$a[2]),@($b[0],$a[1],$b[2]),@($b[0],$b[1],$b[2]),@($b[0],$b[1],$a[2]))},
            @{Color=([System.Drawing.Color]::FromArgb(255,85,128,152)); P=@(@($a[0],$a[1],$b[2]),@($b[0],$a[1],$b[2]),@($b[0],$b[1],$b[2]),@($a[0],$b[1],$b[2]))},
            @{Color=([System.Drawing.Color]::FromArgb(255,147,193,214)); P=@(@($a[0],$b[1],$a[2]),@($b[0],$b[1],$a[2]),@($b[0],$b[1],$b[2]),@($a[0],$b[1],$b[2]))}
        )
        foreach($poly in $polys) {
            [System.Drawing.PointF[]]$points = @($poly.P | ForEach-Object { ProjectPoint $_[0] $_[1] $_[2] $ox $oy })
            $brush=[System.Drawing.SolidBrush]::new($poly.Color)
            $g.FillPolygon($brush,$points); $g.DrawPolygon($pen,$points); $brush.Dispose()
        }
    }
    $g.DrawString($names[$i].Replace('phantom_',''),$font,[System.Drawing.Brushes]::White,[single](($i%6)*160+8),[single]([Math]::Floor($i/6)*180+148))
}
$g.Dispose(); $pen.Dispose(); $font.Dispose()
$sheet.Save((Join-Path $PSScriptRoot 'building-model-preview.png'),[System.Drawing.Imaging.ImageFormat]::Png)
$sheet.Dispose()
