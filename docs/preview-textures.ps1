$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$project = Split-Path $PSScriptRoot -Parent
$assets = Join-Path $project 'common-1.21.1/src/main/resources/assets/kitsunebi'
$names = @('block/kitsunebi_white.png','block/kitsunebi_blue.png','block/kitsunebi_cyan.png','block/kitsunebi_green.png','block/kitsunebi_purple.png','block/kitsunebi_orange.png','block/kitsunebi_red.png','item/kitsunebi_gohei.png','item/kitsunebi_goggles.png','block/phantom_block.png')
$sheet = [System.Drawing.Bitmap]::new(800,320)
$g = [System.Drawing.Graphics]::FromImage($sheet)
$g.Clear([System.Drawing.Color]::FromArgb(255,42,48,60))
$g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
$g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
$i = 0
foreach ($name in $names) {
    $bmp = [System.Drawing.Bitmap]::new((Join-Path $assets ('textures/' + $name)))
    if ($bmp.Width -ne 32 -or $bmp.Height -ne 32) { throw "Wrong size: $name" }
    $g.DrawImage($bmp,(($i % 5)*160),([Math]::Floor($i/5)*160),160,160)
    $bmp.Dispose()
    $i++
}
$g.Dispose()
$sheet.Save((Join-Path $PSScriptRoot 'texture-preview.png'),[System.Drawing.Imaging.ImageFormat]::Png)
$sheet.Dispose()
# Validate every explicit local texture reference in the existing model files.
$missing = @()
Get-ChildItem (Join-Path $assets 'models') -Filter *.json -Recurse | ForEach-Object {
    $model = Get-Content -LiteralPath $_.FullName -Raw | ConvertFrom-Json
    if ($model.textures) {
        foreach ($property in $model.textures.PSObject.Properties) {
            $reference = [string]$property.Value
            if ($reference.StartsWith('kitsunebi:')) {
                $path = Join-Path $assets ('textures/' + $reference.Substring(10) + '.png')
                if (!(Test-Path -LiteralPath $path)) { $missing += $path }
            }
        }
    }
}
if ($missing.Count) { throw "Missing texture references: $missing" }
Write-Output '10 designs: dimensions and explicit local model texture references verified.'
