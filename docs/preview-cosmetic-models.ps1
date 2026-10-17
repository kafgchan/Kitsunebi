$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.Drawing
$assets=Join-Path (Split-Path $PSScriptRoot -Parent) 'common/src/main/resources/assets/kitsunebi'
$palette=[System.Drawing.Bitmap]::new((Join-Path $assets 'textures/block/model_palette.png'))
$sheet=[System.Drawing.Bitmap]::new(600,720)
$g=[System.Drawing.Graphics]::FromImage($sheet)
$g.Clear([System.Drawing.Color]::FromArgb(255,42,48,60))
$font=[System.Drawing.Font]::new('Arial',10)
$models=@('item/kitsunebi_goggles','block/kitsunebi_white','block/kitsunebi_blue','item/phantom_door')
for($row=0;$row -lt 4;$row++) {
    $model=Get-Content (Join-Path $assets ('models/'+$models[$row]+'.json')) -Raw | ConvertFrom-Json
    for($view=0;$view -lt 3;$view++) {
        $face=@('north','west','up')[$view]
        $sorted=switch($view) {0 {$model.elements | Sort-Object {-$_.from[2]}} 1 {$model.elements | Sort-Object {-$_.from[0]}} 2 {$model.elements | Sort-Object {$_.to[1]}}}
        foreach($e in $sorted) {
            $f=$e.faces.$face
            if(!$f){continue}
            $a=$e.from;$b=$e.to
            $color=if($f.texture -eq '#palette'){$palette.GetPixel([int][Math]::Floor($f.uv[0]*4),8)}else{[System.Drawing.Color]::FromArgb(255,101,154,177)}
            switch($view) {
                0 {$x=$a[0];$y=16-$b[1];$w=$b[0]-$a[0];$h=$b[1]-$a[1]}
                1 {$x=$a[2];$y=16-$b[1];$w=$b[2]-$a[2];$h=$b[1]-$a[1]}
                2 {$x=$a[0];$y=$a[2];$w=$b[0]-$a[0];$h=$b[2]-$a[2]}
            }
            $brush=[System.Drawing.SolidBrush]::new($color)
            $g.FillRectangle($brush,[single]($view*200+36+$x*8),[single]($row*180+12+$y*8),[single]($w*8),[single]($h*8))
            $brush.Dispose()
        }
        $g.DrawString(($models[$row].Split('/')[-1]+' '+@('front','side','top')[$view]),$font,[System.Drawing.Brushes]::White,[single]($view*200+8),[single]($row*180+150))
    }
}
$g.Dispose();$palette.Dispose();$font.Dispose()
$sheet.Save((Join-Path $PSScriptRoot 'cosmetic-model-preview.png'))
$sheet.Dispose()
