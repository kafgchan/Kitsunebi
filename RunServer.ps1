[CmdletBinding()]
param(
    [string]$Target = 'forge-1.20.1',
    [ValidateSet('none','jei','emi','rei')][string]$RecipeViewer = 'none',
    [switch]$Curios,
    [switch]$DryRun
)
& "$PSScriptRoot/scripts/Invoke-Kitsunebi.ps1" -Action 'server' -Target $Target -RecipeViewer $RecipeViewer -Curios:$Curios -DryRun:$DryRun


