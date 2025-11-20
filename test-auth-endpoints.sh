#!/bin/bash

# ============================================
# Script de Prueba de Endpoints de Autenticación
# RoadWarnings Nariño Backend
# ============================================

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuración
BASE_URL="${API_URL:-https://roadwarningsnarino-backend.onrender.com/api}"
CONTENT_TYPE="Content-Type: application/json"

# Variables globales para almacenar tokens
ACCESS_TOKEN=""
REFRESH_TOKEN=""
USERNAME="test_user_$(date +%s)"
EMAIL="test_${USERNAME}@example.com"
PASSWORD="TestPassword123"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Testing Auth Endpoints${NC}"
echo -e "${BLUE}  Base URL: $BASE_URL${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Función para imprimir resultados
print_result() {
    local test_name=$1
    local status_code=$2
    local expected=$3

    if [ "$status_code" = "$expected" ]; then
        echo -e "${GREEN}✓${NC} $test_name - Status: $status_code"
    else
        echo -e "${RED}✗${NC} $test_name - Status: $status_code (Expected: $expected)"
    fi
}

# Función para hacer requests y mostrar response
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4

    echo -e "\n${YELLOW}Testing:${NC} $description"
    echo -e "${BLUE}→${NC} $method $BASE_URL$endpoint"

    if [ -n "$data" ]; then
        echo -e "${BLUE}→${NC} Data: $data"
    fi

    response=$(curl -s -w "\n%{http_code}" -X "$method" "$BASE_URL$endpoint" \
        -H "$CONTENT_TYPE" \
        ${data:+-d "$data"})

    status_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    echo -e "${BLUE}←${NC} Status: $status_code"

    if [ -n "$body" ]; then
        echo -e "${BLUE}←${NC} Response:"
        echo "$body" | jq '.' 2>/dev/null || echo "$body"
    fi

    echo "$status_code|$body"
}

# ============================================
# TEST 1: Health Check
# ============================================
echo -e "\n${BLUE}===========================================${NC}"
echo -e "${BLUE}TEST 1: Health Check${NC}"
echo -e "${BLUE}===========================================${NC}"

result=$(make_request "GET" "/ping" "" "Ping endpoint")
status=$(echo "$result" | cut -d'|' -f1)
print_result "Health Check" "$status" "200"

# ============================================
# TEST 2: Register New User
# ============================================
echo -e "\n${BLUE}===========================================${NC}"
echo -e "${BLUE}TEST 2: Register New User${NC}"
echo -e "${BLUE}===========================================${NC}"

register_data=$(cat <<EOF
{
  "username": "$USERNAME",
  "email": "$EMAIL",
  "password": "$PASSWORD"
}
EOF
)

result=$(make_request "POST" "/auth/register" "$register_data" "Register new user")
status=$(echo "$result" | cut -d'|' -f1)
body=$(echo "$result" | cut -d'|' -f2-)
print_result "User Registration" "$status" "200"

# Extraer tokens de la respuesta de registro
if [ "$status" = "200" ]; then
    ACCESS_TOKEN=$(echo "$body" | jq -r '.token' 2>/dev/null)
    REFRESH_TOKEN=$(echo "$body" | jq -r '.refreshToken' 2>/dev/null)
    echo -e "${GREEN}✓${NC} Access Token received: ${ACCESS_TOKEN:0:20}..."
    echo -e "${GREEN}✓${NC} Refresh Token received: ${REFRESH_TOKEN:0:20}..."
fi

# ============================================
# TEST 3: Register Duplicate User (Should Fail)
# ============================================
echo -e "\n${BLUE}===========================================${NC}"
echo -e "${BLUE}TEST 3: Register Duplicate User${NC}"
echo -e "${BLUE}===========================================${NC}"

result=$(make_request "POST" "/auth/register" "$register_data" "Try to register same user again")
status=$(echo "$result" | cut -d'|' -f1)
print_result "Duplicate Registration (Should Fail)" "$status" "400"

# ============================================
# TEST 4: Login Without Email Verification (Should Fail)
# ============================================
echo -e "\n${BLUE}===========================================${NC}"
echo -e "${BLUE}TEST 4: Login Without Email Verification${NC}"
echo -e "${BLUE}===========================================${NC}"

login_data=$(cat <<EOF
{
  "username": "$USERNAME",
  "password": "$PASSWORD"
}
EOF
)

result=$(make_request "POST" "/auth/login" "$login_data" "Try to login without email verification")
status=$(echo "$result" | cut -d'|' -f1)
print_result "Login Without Verification (Should Fail)" "$status" "400"

# ============================================
# TEST 5: Login with Wrong Password (Should Fail)
# ============================================
echo -e "\n${BLUE}===========================================${NC}"
echo -e "${BLUE}TEST 5: Login with Wrong Password${NC}"
echo -e "${BLUE}===========================================${NC}"

wrong_login_data=$(cat <<EOF
{
  "username": "$USERNAME",
  "password": "WrongPassword123"
}
EOF
)

result=$(make_request "POST" "/auth/login" "$wrong_login_data" "Try to login with wrong password")
status=$(echo "$result" | cut -d'|' -f1)
print_result "Login With Wrong Password (Should Fail)" "$status" "401"

# ============================================
# TEST 6: Refresh Token
# ============================================
echo -e "\n${BLUE}===========================================${NC}"
echo -e "${BLUE}TEST 6: Refresh Access Token${NC}"
echo -e "${BLUE}===========================================${NC}"

if [ -n "$REFRESH_TOKEN" ]; then
    refresh_data=$(cat <<EOF
{
  "refreshToken": "$REFRESH_TOKEN"
}
EOF
)

    result=$(make_request "POST" "/auth/refresh" "$refresh_data" "Refresh access token")
    status=$(echo "$result" | cut -d'|' -f1)
    body=$(echo "$result" | cut -d'|' -f2-)
    print_result "Token Refresh" "$status" "200"

    if [ "$status" = "200" ]; then
        NEW_ACCESS_TOKEN=$(echo "$body" | jq -r '.token' 2>/dev/null)
        echo -e "${GREEN}✓${NC} New Access Token received: ${NEW_ACCESS_TOKEN:0:20}..."
        ACCESS_TOKEN=$NEW_ACCESS_TOKEN
    fi
else
    echo -e "${RED}✗${NC} Skipping - No refresh token available"
fi

# ============================================
# TEST 7: Refresh Token with Invalid Token (Should Fail)
# ============================================
echo -e "\n${BLUE}===========================================${NC}"
echo -e "${BLUE}TEST 7: Refresh with Invalid Token${NC}"
echo -e "${BLUE}===========================================${NC}"

invalid_refresh_data=$(cat <<EOF
{
  "refreshToken": "invalid-token-12345"
}
EOF
)

result=$(make_request "POST" "/auth/refresh" "$invalid_refresh_data" "Try to refresh with invalid token")
status=$(echo "$result" | cut -d'|' -f1)
print_result "Invalid Token Refresh (Should Fail)" "$status" "400"

# ============================================
# TEST 8: Logout
# ============================================
echo -e "\n${BLUE}===========================================${NC}"
echo -e "${BLUE}TEST 8: Logout${NC}"
echo -e "${BLUE}===========================================${NC}"

if [ -n "$REFRESH_TOKEN" ]; then
    logout_data=$(cat <<EOF
{
  "refreshToken": "$REFRESH_TOKEN"
}
EOF
)

    result=$(make_request "POST" "/auth/logout" "$logout_data" "Logout user")
    status=$(echo "$result" | cut -d'|' -f1)
    print_result "Logout" "$status" "200"
else
    echo -e "${RED}✗${NC} Skipping - No refresh token available"
fi

# ============================================
# TEST 9: Use Revoked Refresh Token (Should Fail)
# ============================================
echo -e "\n${BLUE}===========================================${NC}"
echo -e "${BLUE}TEST 9: Use Revoked Token After Logout${NC}"
echo -e "${BLUE}===========================================${NC}"

if [ -n "$REFRESH_TOKEN" ]; then
    result=$(make_request "POST" "/auth/refresh" "$refresh_data" "Try to use revoked token")
    status=$(echo "$result" | cut -d'|' -f1)
    print_result "Revoked Token Usage (Should Fail)" "$status" "400"
else
    echo -e "${RED}✗${NC} Skipping - No refresh token available"
fi

# ============================================
# TEST 10: Invalid Email Verification Token
# ============================================
echo -e "\n${BLUE}===========================================${NC}"
echo -e "${BLUE}TEST 10: Verify Email with Invalid Token${NC}"
echo -e "${BLUE}===========================================${NC}"

result=$(make_request "GET" "/auth/verify-email?token=invalid-token-123" "" "Try to verify email with invalid token")
status=$(echo "$result" | cut -d'|' -f1)
print_result "Invalid Email Verification (Should Fail)" "$status" "400"

# ============================================
# SUMMARY
# ============================================
echo -e "\n${BLUE}==========================================${NC}"
echo -e "${BLUE}  TEST SUMMARY${NC}"
echo -e "${BLUE}==========================================${NC}"
echo -e "${YELLOW}Note:${NC} To complete the full flow:"
echo -e "1. Check the email: ${GREEN}$EMAIL${NC}"
echo -e "2. Click the verification link"
echo -e "3. Then login with:"
echo -e "   Username: ${GREEN}$USERNAME${NC}"
echo -e "   Password: ${GREEN}$PASSWORD${NC}"
echo -e "\n${BLUE}Access Token (last received):${NC} ${ACCESS_TOKEN:0:50}..."
echo -e "${BLUE}Refresh Token (revoked):${NC} ${REFRESH_TOKEN:0:50}..."
echo -e "\n${GREEN}Testing completed!${NC}\n"
