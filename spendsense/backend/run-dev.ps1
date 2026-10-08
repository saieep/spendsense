# Load spendsense/.env and start Spring Boot (Windows PowerShell)
$envFile = Join-Path $PSScriptRoot "..\.env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#") -and $line -match "^([^=]+)=(.*)$") {
            Set-Item -Path "Env:$($matches[1])" -Value $matches[2]
        }
    }
    Write-Host "Loaded environment from .env"
} else {
    Write-Warning ".env not found at $envFile - copy .env.example to .env"
}

Set-Location $PSScriptRoot
$env:SPRING_PROFILES_ACTIVE = "dev"
Write-Host "Starting backend with profile: dev"
.\mvnw.cmd spring-boot:run
