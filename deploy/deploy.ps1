# Nadi Foot Academie - Deployment Script
# Usage: .\deploy.ps1 [-Backend] [-Frontend] [-All]

param(
    [switch]$Backend,
    [switch]$Frontend,
    [switch]$All,
    [switch]$Setup
)

$ErrorActionPreference = "Stop"
$ROOT = Split-Path -Parent $PSScriptRoot

# Load .env
Get-Content "$ROOT\.env" | ForEach-Object {
    if ($_ -match "^([^#=]+)=(.*)$") {
        [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), "Process")
    }
}

$FLY_TOKEN = $env:FLY_API_TOKEN
$VERCEL_TOKEN = $env:VERCEL_TOKEN
$GHCR_USER = "fayeztekitek"
$GHCR_IMAGE = "ghcr.io/$GHCR_USER/foot-academie/backend"
$APP_NAME = "nadi-foot-academie"
$DB_NAME = "nadi-foot-academie-db"

function Write-Step($msg) { Write-Host "`n=== $msg ===" -ForegroundColor Cyan }
function Write-Ok($msg) { Write-Host "  OK: $msg" -ForegroundColor Green }
function Write-Fail($msg) { Write-Host "  FAIL: $msg" -ForegroundColor Red }

# ========== SETUP ==========
if ($Setup -or $All) {
    Write-Step "Setting up Fly.io app"

    $env:FLY_API_TOKEN = $FLY_TOKEN
    $flyctl = "$env:USERPROFILE\.fly\bin\flyctl.exe"

    # Create backend app
    Write-Host "  Creating app: $APP_NAME"
    & $flyctl apps create --name $APP_NAME --org personal 2>&1
    if ($LASTEXITCODE -eq 0) { Write-Ok "App $APP_NAME created" }
    else { Write-Host "  App may already exist, continuing..." }

    # Create Postgres app
    Write-Host "  Creating Postgres: $DB_NAME"
    & $flyctl pg create --name $DB_NAME --region fra --password $env:POSTGRES_PASSWORD --initial-cluster-size 1 --detach 2>&1
    if ($LASTEXITCODE -eq 0) { Write-Ok "Postgres $DB_NAME created" }
    else { Write-Fail "Postgres creation failed - check fly.io billing/limits" }

    # Get Postgres connection string
    Write-Host "`n  Getting Postgres connection details..."
    $pgInfo = & $flyctl postgres connect --app $DB_NAME --echo-credentials 2>&1
    Write-Host $pgInfo

    Write-Host "`n  IMPORTANT: Update your .env with the Postgres connection details above" -ForegroundColor Yellow
    Write-Host "  Then re-run: .\deploy.ps1 -All" -ForegroundColor Yellow
}

# ========== BACKEND ==========
if ($Backend -or $All) {
    Write-Step "Building & Pushing Backend Docker Image"

    # Build
    Write-Host "  Building Docker image..."
    Push-Location "$ROOT\backend"
    & "C:\Program Files\Docker\Docker\Resources\bin\docker.exe" build --network host -t "$GHCR_IMAGE:latest" -f Dockerfile . 2>&1 | Select-String "BUILD SUCCESS|BUILD FAILURE"
    if ($LASTEXITCODE -ne 0) { Write-Fail "Docker build failed"; Pop-Location; exit 1 }
    Write-Ok "Docker image built"

    # Tag
    & "C:\Program Files\Docker\Docker\Resources\bin\docker.exe" tag "$GHCR_IMAGE:latest" "$GHCR_IMAGE:latest" 2>&1

    # Push to ghcr.io
    Write-Host "  Pushing to ghcr.io..."
    echo $env:GITHUB_TOKEN | & "C:\Program Files\Docker\Docker\Resources\bin\docker.exe" login ghcr.io -u $GHCR_USER --password-stdin 2>&1 | Select-String "Login Succeeded|error"
    & "C:\Program Files\Docker\Docker\Resources\bin\docker.exe" push "$GHCR_IMAGE:latest" 2>&1 | Select-String "Pushed|error"
    Write-Ok "Image pushed to ghcr.io"
    Pop-Location

    Write-Step "Deploying Backend to Fly.io"
    $env:FLY_API_TOKEN = $FLY_TOKEN
    $flyctl = "$env:USERPROFILE\.fly\bin\flyctl.exe"

    # Create/update machines
    & $flyctl deploy --app $APP_NAME --image "$GHCR_IMAGE:latest" --remote-only 2>&1
    Write-Ok "Backend deployed to Fly.io"

    # Set env vars on Fly
    Write-Host "  Setting environment variables..."
    $envVars = @(
        "SPRING_PROFILES_ACTIVE=prod"
        "DB_HOST=$DB_NAME.flycast"
        "DB_PORT=5432"
        "DB_NAME=nadi"
        "DB_USERNAME=postgres"
        "DB_PASSWORD=$env:POSTGRES_PASSWORD"
        "JWT_SECRET=$env:JWT_SECRET"
        "AI_PROVIDER=openai"
        "AI_API_KEY=$env:AI_API_KEY"
        "AI_MODEL=gemini-3.6-flash"
        "AI_BASE_URL=https://generativelanguage.googleapis.com/v1beta/openai/"
        "AI_MAX_TOKENS=2048"
        "AI_TEMPERATURE=0.7"
        "AI_RATE_LIMIT=20"
    )
    foreach ($var in $envVars) {
        & $flyctl secrets set --app $APP_NAME $var 2>&1 | Out-Null
    }
    Write-Ok "Backend secrets configured"
}

# ========== FRONTEND ==========
if ($Frontend -or $All) {
    Write-Step "Deploying Frontend to Vercel"

    # Build frontend
    Write-Host "  Building frontend..."
    Push-Location "$ROOT\frontend"
    npx vite build 2>&1 | Select-String "built in"
    Write-Ok "Frontend built"

    # Deploy to Vercel
    Write-Host "  Deploying to Vercel..."
    $backendUrl = "https://$APP_NAME.fly.dev"
    npx vercel --prod --yes --token $VERCEL_TOKEN --env "VITE_API_URL=$backendUrl/api" 2>&1
    Write-Ok "Frontend deployed to Vercel"
    Pop-Location
}

Write-Host "`n=== Deployment Complete ===" -ForegroundColor Green
Write-Host "Backend:  https://$APP_NAME.fly.dev/api"
Write-Host "Frontend: Check Vercel dashboard"
