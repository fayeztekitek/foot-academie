# Quick Update - Push image + Deploy to Fly.io
# Usage: .\update.ps1

$ErrorActionPreference = "Stop"
$ROOT = Split-Path -Parent $PSScriptRoot

Get-Content "$ROOT\.env" | ForEach-Object {
    if ($_ -match "^([^#=]+)=(.*)$") {
        [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), "Process")
    }
}

$GHCR_IMAGE = "ghcr.io/fayeztekitek/foot-academie/backend"
$APP_NAME = "nadi-foot-academie"
$flyctl = "$env:USERPROFILE\.fly\bin\flyctl.exe"

Write-Host "1. Building Docker image..." -ForegroundColor Cyan
Push-Location "$ROOT\backend"
& "C:\Program Files\Docker\Docker\Resources\bin\docker.exe" build --network host -t "$GHCR_IMAGE:latest" -f Dockerfile . 2>&1 | Select-String "BUILD SUCCESS|BUILD FAILURE"
Pop-Location

Write-Host "2. Pushing to ghcr.io..." -ForegroundColor Cyan
echo $env:GITHUB_TOKEN | & "C:\Program Files\Docker\Docker\Resources\bin\docker.exe" login ghcr.io -u fayeztekitek --password-stdin 2>&1 | Out-Null
& "C:\Program Files\Docker\Docker\Resources\bin\docker.exe" push "$GHCR_IMAGE:latest" 2>&1 | Select-String "Pushed|error"

Write-Host "3. Deploying to Fly.io..." -ForegroundColor Cyan
$env:FLY_API_TOKEN = $env:FLY_API_TOKEN
& $flyctl deploy --app $APP_NAME --image "$GHCR_IMAGE:latest" --remote-only 2>&1

Write-Host "`nDone! Backend updated at https://$APP_NAME.fly.dev/api" -ForegroundColor Green
