# Script de prueba simple para autenticación
# Asegúrate de que el servidor esté corriendo en http://localhost:8080

$baseUrl = "http://localhost:8080/api"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   PRUEBA DE AUTENTICACIÓN" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# 1. Verificar que el servidor esté funcionando
Write-Host "[1] Verificando servidor..." -ForegroundColor Yellow
try {
    $healthCheck = Invoke-RestMethod -Uri "$baseUrl/ping" -Method GET -ErrorAction Stop
    Write-Host "    ✓ Servidor funcionando" -ForegroundColor Green
} catch {
    Write-Host "    ✗ Error: No se puede conectar al servidor" -ForegroundColor Red
    Write-Host "    Asegúrate de que el servidor esté corriendo en http://localhost:8080" -ForegroundColor Yellow
    exit
}

# 2. Intentar registro
Write-Host "`n[2] Probando registro..." -ForegroundColor Yellow
$timestamp = Get-Date -Format "yyyyMMddHHmmss"
$registerData = @{
    username = "testuser$timestamp"
    email = "testuser$timestamp@test.com"
    password = "Test123456"
} | ConvertTo-Json

Write-Host "    Datos de registro:" -ForegroundColor Gray
Write-Host "    Username: testuser$timestamp" -ForegroundColor Gray
Write-Host "    Email: testuser$timestamp@test.com" -ForegroundColor Gray
Write-Host "    Password: Test123456" -ForegroundColor Gray

try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method POST `
        -Body $registerData -ContentType "application/json" -ErrorAction Stop

    Write-Host "    ✓ Registro exitoso" -ForegroundColor Green
    Write-Host "    Token recibido: $($registerResponse.token.Substring(0, 20))..." -ForegroundColor Gray
    Write-Host "    Username: $($registerResponse.username)" -ForegroundColor Gray

    $token = $registerResponse.token
    $username = $registerResponse.username

} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    $errorBody = $_.ErrorDetails.Message

    Write-Host "    ✗ Error en registro (HTTP $statusCode)" -ForegroundColor Red
    Write-Host "    Detalles: $errorBody" -ForegroundColor Red

    # Si falla el registro, intentar con usuario existente para login
    $username = "testuser"
    $password = "Test123456"
}

# 3. Intentar login
Write-Host "`n[3] Probando login..." -ForegroundColor Yellow
$loginData = @{
    username = $username
    password = "Test123456"
} | ConvertTo-Json

Write-Host "    Intentando login con:" -ForegroundColor Gray
Write-Host "    Username: $username" -ForegroundColor Gray

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST `
        -Body $loginData -ContentType "application/json" -ErrorAction Stop

    Write-Host "    ✓ Login exitoso" -ForegroundColor Green
    Write-Host "    Token recibido: $($loginResponse.token.Substring(0, 20))..." -ForegroundColor Gray
    Write-Host "    Username: $($loginResponse.username)" -ForegroundColor Gray

} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    $errorBody = $_.ErrorDetails.Message

    Write-Host "    ✗ Error en login (HTTP $statusCode)" -ForegroundColor Red
    Write-Host "    Detalles: $errorBody" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   FIN DE LA PRUEBA" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan
