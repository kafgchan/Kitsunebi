param([string]$Manifest = "$PSScriptRoot/texture-generation.json")
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$project = Split-Path $PSScriptRoot -Parent
$assets = Join-Path $project 'common/src/main/resources/assets/kitsunebi/textures'
$backup = Join-Path $PSScriptRoot 'texture-backup-2026-10-18'
$sources = Join-Path $PSScriptRoot 'texture-sources'
New-Item -ItemType Directory -Force -Path $backup,$sources | Out-Null
$entries = Get-Content -LiteralPath $Manifest -Raw | ConvertFrom-Json
foreach ($entry in $entries) {
    Copy-Item -LiteralPath $entry.source -Destination (Join-Path $sources ($entry.name + '.png'))
    $original = [System.Drawing.Bitmap]::new($entry.source)
    $small = [System.Drawing.Bitmap]::new(32,32,[System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    # Sample pixel centers directly: retain generated transparency without filtering.
    for ($y=0; $y -lt 32; $y++) {
        for ($x=0; $x -lt 32; $x++) {
            $sx = [Math]::Min($original.Width-1,[int][Math]::Floor(($x+0.5)*$original.Width/32))
            $sy = [Math]::Min($original.Height-1,[int][Math]::Floor(($y+0.5)*$original.Height/32))
            $small.SetPixel($x,$y,$original.GetPixel($sx,$sy))
        }
    }
    foreach ($relative in $entry.targets) {
        $target = Join-Path $assets $relative
        $saved = Join-Path $backup $relative
        New-Item -ItemType Directory -Force -Path (Split-Path $saved -Parent) | Out-Null
        if ((Test-Path -LiteralPath $target) -and !(Test-Path -LiteralPath $saved)) {
            Copy-Item -LiteralPath $target -Destination $saved
        }
        $small.Save($target,[System.Drawing.Imaging.ImageFormat]::Png)
        Write-Output "$relative 32x32 RGBA"
    }
    $original.Dispose()
    $small.Dispose()
}
