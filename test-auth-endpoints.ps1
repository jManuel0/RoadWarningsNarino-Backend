# ============================================
# Script de Prueba de Endpoints de Autenticación
# RoadWarnings Nariño Backend - PowerShell Version
# ============================================

# Configuración
$BaseUrl = if ($env:API_URL) { $env:API_URL } else { "https://roadwarningsnarino-backend.onrender.com/api" }
$Username = "test_user_$(Get-Date -Format 'yyyyMMddHHmmss')"
$Email = "test_$Username@example.com"
$Password = "TestPassword123"

# Variables globales
$AccessToken = ""
$RefreshToken = ""

Write-Host "========================================" -ForegroundColor Blue
Write-Host "  Testing Auth Endpoints" -ForegroundColor Blue
Write-Host "  Base URL: $BaseUrl" -ForegroundColor Blue
Write-Host "========================================`n" -ForegroundColor Blue

# Función para hacer requests
function Invoke-ApiTest {
    param(
        [string]$Method,
        [string]$Endpoint,
        [object]$Body,
        [string]$Description
    )

    Write-Host "`nTesting: $Description" -ForegroundColor Yellow
    Write-Host "→ $Method $BaseUrl$Endpoint" -ForegroundColor Cyan

    try {
        $params = @{
            Uri = "$BaseUrl$Endpoint"
            Method = $Method
            ContentType = "application/json"
        }

        if ($Body) {
            $jsonBody = $Body | ConvertTo-Json -Depth 10
            Write-Host "→ Data: $jsonBody" -ForegroundColor Cyan
            $params.Body = $jsonBody
        }

        $response = Invoke-RestMethod @params -StatusCodeVariable statusCode

        Write-Host "← Status: $statusCode" -ForegroundColor Cyan
        Write-Host "← Response:" -ForegroundColor Cyan
        $response | ConvertTo-Json -Depth 10 | Write-Host

        return @{
            StatusCode = $statusCode
            Body = $response
            Success = $true
        }
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.Value__
        Write-Host "← Status: $statusCode" -ForegroundColor Cyan
        Write-Host "← Error: $($_.Exception.Message)" -ForegroundColor Red

        return @{
            StatusCode = $statusCode
            Body = $null
            Success = $false
            Error = $_.Exception.Message
        }
    }
}

# Función para imprimir resultados
function Print-TestResult {
    param(
        [string]$TestName,
        [int]$ActualStatus,
        [int]$ExpectedStatus
    )

    if ($ActualStatus -eq $ExpectedStatus) {
        Write-Host "✓ $TestName - Status: $ActualStatus" -ForegroundColor Green
    }
    else {
        Write-Host "✗ $TestName - Status: $ActualStatus (Expected: $ExpectedStatus)" -ForegroundColor Red
    }
}

# ============================================
# TEST 1: Health Check
# ============================================
Write-Host "`n===========================================" -ForegroundColor Blue
Write-Host "TEST 1: Health Check" -ForegroundColor Blue
Write-Host "===========================================" -ForegroundColor Blue

$result = Invoke-ApiTest -Method "GET" -Endpoint "/ping" -Description "Ping endpoint"
Print-TestResult -TestName "Health Check" -ActualStatus $result.StatusCode -ExpectedStatus 200

# ============================================
# TEST 2: Register New User
# ============================================
Write-Host "`n===========================================" -ForegroundColor Blue
Write-Host "TEST 2: Register New User" -ForegroundColor Blue
Write-Host "===========================================" -ForegroundColor Blue

$registerData = @{
    username = $Username
    email = $Email
    password = $Password
}

$result = Invoke-ApiTest -Method "POST" -Endpoint "/auth/register" -Body $registerData -Description "Register new user"
Print-TestResult -TestName "User Registration" -ActualStatus $result.StatusCode -ExpectedStatus 200

if ($result.Success -and $result.Body) {
    $AccessToken = $result.Body.token
    $RefreshToken = $result.Body.refreshToken
    Write-Host "✓ Access Token received: $($AccessToken.Substring(0, [Math]::Min(20, $AccessToken.Length)))..." -ForegroundColor Green
    Write-Host "✓ Refresh Token received: $($RefreshToken.Substring(0, [Math]::Min(20, $RefreshToken.Length)))..." -ForegroundColor Green
}

# ============================================
# TEST 3: Register Duplicate User
# ============================================
Write-Host "`n===========================================" -ForegroundColor Blue
Write-Host "TEST 3: Register Duplicate User" -ForegroundColor Blue
Write-Host "===========================================" -ForegroundColor Blue

$result = Invoke-ApiTest -Method "POST" -Endpoint "/auth/register" -Body $registerData -Description "Try to register same user again"
Print-TestResult -TestName "Duplicate Registration (Should Fail)" -ActualStatus $result.StatusCode -ExpectedStatus 400

# ============================================
# TEST 4: Login Without Email Verification
# ============================================
Write-Host "`n===========================================" -ForegroundColor Blue
Write-Host "TEST 4: Login Without Email Verification" -ForegroundColor Blue
Write-Host "===========================================" -ForegroundColor Blue

$loginData = @{
    username = $Username
    password = $Password
}

$result = Invoke-ApiTest -Method "POST" -Endpoint "/auth/login" -Body $loginData -Description "Try to login without email verification"
Print-TestResult -TestName "Login Without Verification (Should Fail)" -ActualStatus $result.StatusCode -ExpectedStatus 400

# ============================================
# TEST 5: Login with Wrong Password
# ============================================
Write-Host "`n===========================================" -ForegroundColor Blue
Write-Host "TEST 5: Login with Wrong Password" -ForegroundColor Blue
Write-Host "===========================================" -ForegroundColor Blue

$wrongLoginData = @{
    username = $Username
    password = "WrongPassword123"
}

$result = Invoke-ApiTest -Method "POST" -Endpoint "/auth/login" -Body $wrongLoginData -Description "Try to login with wrong password"
Print-TestResult -TestName "Login With Wrong Password (Should Fail)" -ActualStatus $result.StatusCode -ExpectedStatus 401

# ============================================
# TEST 6: Refresh Token
# ============================================
Write-Host "`n===========================================" -ForegroundColor Blue
Write-Host "TEST 6: Refresh Access Token" -ForegroundColor Blue
Write-Host "===========================================" -ForegroundColor Blue

if ($RefreshToken) {
    $refreshData = @{
        refreshToken = $RefreshToken
    }

    $result = Invoke-ApiTest -Method "POST" -Endpoint "/auth/refresh" -Body $refreshData -Description "Refresh access token"
    Print-TestResult -TestName "Token Refresh" -ActualStatus $result.StatusCode -ExpectedStatus 200

    if ($result.Success -and $result.Body) {
        $AccessToken = $result.Body.token
        Write-Host "✓ New Access Token received: $($AccessToken.Substring(0, [Math]::Min(20, $AccessToken.Length)))..." -ForegroundColor Green
    }
}
else {
    Write-Host "✗ Skipping - No refresh token available" -ForegroundColor Red
}

# ============================================
# TEST 7: Refresh with Invalid Token
# ============================================
Write-Host "`n===========================================" -ForegroundColor Blue
Write-Host "TEST 7: Refresh with Invalid Token" -ForegroundColor Blue
Write-Host "===========================================" -ForegroundColor Blue

$invalidRefreshData = @{
    refreshToken = "invalid-token-12345"
}

$result = Invoke-ApiTest -Method "POST" -Endpoint "/auth/refresh" -Body $invalidRefreshData -Description "Try to refresh with invalid token"
Print-TestResult -TestName "Invalid Token Refresh (Should Fail)" -ActualStatus $result.StatusCode -ExpectedStatus 400

# ============================================
# TEST 8: Logout
# ============================================
Write-Host "`n===========================================" -ForegroundColor Blue
Write-Host "TEST 8: Logout" -ForegroundColor Blue
Write-Host "===========================================" -ForegroundColor Blue

if ($RefreshToken) {
    $logoutData = @{
        refreshToken = $RefreshToken
    }

    $result = Invoke-ApiTest -Method "POST" -Endpoint "/auth/logout" -Body $logoutData -Description "Logout user"
    Print-TestResult -TestName "Logout" -ActualStatus $result.StatusCode -ExpectedStatus 200
}
else {
    Write-Host "✗ Skipping - No refresh token available" -ForegroundColor Red
}

# ============================================
# TEST 9: Use Revoked Token
# ============================================
Write-Host "`n===========================================" -ForegroundColor Blue
Write-Host "TEST 9: Use Revoked Token After Logout" -ForegroundColor Blue
Write-Host "===========================================" -ForegroundColor Blue

if ($RefreshToken) {
    $refreshData = @{
        refreshToken = $RefreshToken
    }

    $result = Invoke-ApiTest -Method "POST" -Endpoint "/auth/refresh" -Body $refreshData -Description "Try to use revoked token"
    Print-TestResult -TestName "Revoked Token Usage (Should Fail)" -ActualStatus $result.StatusCode -ExpectedStatus 400
}
else {
    Write-Host "✗ Skipping - No refresh token available" -ForegroundColor Red
}

# ============================================
# TEST 10: Invalid Email Verification
# ============================================
Write-Host "`n===========================================" -ForegroundColor Blue
Write-Host "TEST 10: Verify Email with Invalid Token" -ForegroundColor Blue
Write-Host "===========================================" -ForegroundColor Blue

$result = Invoke-ApiTest -Method "GET" -Endpoint "/auth/verify-email?token=invalid-token-123" -Description "Try to verify email with invalid token"
Print-TestResult -TestName "Invalid Email Verification (Should Fail)" -ActualStatus $result.StatusCode -ExpectedStatus 400

# ============================================
# SUMMARY
# ============================================
Write-Host "`n==========================================" -ForegroundColor Blue
Write-Host "  TEST SUMMARY" -ForegroundColor Blue
Write-Host "==========================================" -ForegroundColor Blue
Write-Host "Note: To complete the full flow:" -ForegroundColor Yellow
Write-Host "1. Check the email: $Email" -ForegroundColor Green
Write-Host "2. Click the verification link" -ForegroundColor Green
Write-Host "3. Then login with:" -ForegroundColor Green
Write-Host "   Username: $Username" -ForegroundColor Green
Write-Host "   Password: $Password" -ForegroundColor Green

if ($AccessToken) {
    Write-Host "`nAccess Token (last received): $($AccessToken.Substring(0, [Math]::Min(50, $AccessToken.Length)))..." -ForegroundColor Cyan
}
if ($RefreshToken) {
    Write-Host "Refresh Token (revoked): $($RefreshToken.Substring(0, [Math]::Min(50, $RefreshToken.Length)))..." -ForegroundColor Cyan
}

Write-Host "`nTesting completed!" -ForegroundColor Green
