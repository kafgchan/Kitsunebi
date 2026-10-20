[CmdletBinding()]
param(
    [ValidateSet('build','client','server')][string]$Action = 'build',
    [ValidateSet('forge-1.20.1','fabric-1.21.1','neoforge-1.21.1','fabric-26.1.2','neoforge-26.1.2','fabric-26.2','neoforge-26.2','fabric-26.3','neoforge-26.3','all')]
    [string]$Target = 'forge-1.20.1',
    [ValidateSet('none','jei','emi','rei')][string]$RecipeViewer = 'none',
    [switch]$Curios,
    [switch]$DryRun
)
$ErrorActionPreference = 'Stop'
$repository = Split-Path $PSScriptRoot -Parent
$targets = if ($Target -eq 'all') {
    if ($Action -ne 'build') { throw 'Target all is supported only for builds.' }
    @('forge-1.20.1','fabric-1.21.1','neoforge-1.21.1')
} else { @($Target) }
foreach ($module in $targets) {
    $loader, $minecraft = $module -split '-', 2
    $suffix = @{ build='Build'; client='RunClient'; server='RunServer' }[$Action]
    $task = if ($minecraft -eq '1.21.1') {
        ':' + $module + ':' + @{ build='build'; client='runClient'; server='runServer' }[$Action]
    } else { $loader + ($minecraft -replace '\.','') + $suffix }
    $arguments = @($task, '--console=plain', "-PrecipeViewer=$RecipeViewer", "-PcuriosEnabled=$($Curios.IsPresent.ToString().ToLowerInvariant())")
    Push-Location $repository
    try {
        Write-Host "Kitsunebi: $module / $Action"
        if ($DryRun) { Write-Host ('gradlew.bat ' + ($arguments -join ' ')); continue }
        & (Join-Path $repository 'gradlew.bat') @arguments
        if ($LASTEXITCODE -ne 0) { throw "Gradle failed for $module (exit $LASTEXITCODE)." }
    } finally { Pop-Location }
}


