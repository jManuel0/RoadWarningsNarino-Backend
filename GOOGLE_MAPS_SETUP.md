# 🗺️ Configuración de Google Maps API

## Obtener API Keys

1. Ve a: https://console.cloud.google.com/
2. Crea un nuevo proyecto o selecciona uno existente
3. Ve a **APIs & Services** → **Credentials**
4. Haz clic en **Create Credentials** → **API Key**
5. Copia la API key generada

## Habilitar APIs Necesarias

En Google Cloud Console, ve a **APIs & Services** → **Library** y habilita:

1. **Places API** (para búsqueda de lugares)
2. **Directions API** (para cálculo de rutas)
3. **Maps JavaScript API** (para el frontend)
4. **Geocoding API** (opcional, para geocodificación)

## Configurar Variables de Entorno

### En Railway (Producción):

1. Ve a tu proyecto en Railway
2. Selecciona tu servicio backend
3. Ve a **Variables**
4. Agrega:
   ```
   GOOGLE_MAPS_API_KEY=tu_api_key_aqui
   ```

### En Local (.env):

```bash
GOOGLE_MAPS_API_KEY=tu_api_key_aqui
```

## Endpoints Implementados

### 1. CRUD de Lugares Guardados

**Requiere autenticación (JWT)**

```bash
# Obtener lugares guardados
GET /api/users/:userId/saved-places

# Crear lugar guardado
POST /api/users/:userId/saved-places
Body: {
  "name": "Mi Casa",
  "address": "Calle 18 #25-04, Pasto",
  "latitude": 1.2136,
  "longitude": -77.2811,
  "type": "HOME"
}

# Actualizar lugar guardado
PUT /api/users/:userId/saved-places/:placeId
Body: { ... }

# Eliminar lugar guardado
DELETE /api/users/:userId/saved-places/:placeId
```

### 2. Búsqueda de Lugares

**Público (no requiere autenticación)**

```bash
GET /api/places/search?query=hospital&lat=1.2136&lng=-77.2811&radius=5000
```

### 3. Detalles de Lugar

**Público**

```bash
GET /api/places/:placeId
```

### 4. Cálculo de Rutas

**Público**

```bash
POST /api/routes/calculate
Body: {
  "origin": { "lat": 1.2136, "lng": -77.2811 },
  "destination": { "lat": 1.2150, "lng": -77.2800 },
  "alternatives": true,
  "avoidAlerts": false
}
```

## Límites y Costos

Google Maps API tiene un plan gratuito con límites:

- **Places API:** $200 de crédito gratis/mes
- **Directions API:** $200 de crédito gratis/mes
- **Después del crédito:** Se cobra por uso

### Recomendaciones:

1. **Implementar caché** para búsquedas frecuentes
2. **Rate limiting** en los endpoints
3. **Monitorear uso** en Google Cloud Console

## Validaciones Implementadas

✅ Límite de 50 lugares guardados por usuario
✅ Solo un lugar de tipo HOME y uno de tipo WORK por usuario
✅ Validación de coordenadas (lat: -90 a 90, lng: -180 a 180)
✅ Validación de permisos (solo el dueño puede modificar sus lugares)

## Testing

Puedes probar los endpoints con:

```bash
# Swagger UI
https://tu-backend.railway.app/api/swagger-ui/index.html

# cURL
curl -X GET "https://tu-backend.railway.app/api/places/search?query=hospital&lat=1.2136&lng=-77.2811"
```

## Próximos Pasos

1. Obtener Google Maps API key
2. Agregar la variable en Railway
3. Redeploy del backend
4. Probar los endpoints desde el frontend
