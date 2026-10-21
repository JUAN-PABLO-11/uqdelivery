
### HU-05: `POST /envios/cotizar`

Requiere un JWT de un usuario con rol `CLIENTE`. La operación no crea ni modifica un
envío; únicamente calcula una propuesta con las zonas y el peso recibidos. Las dimensiones
son obligatorias y positivas, pero no intervienen en la fórmula actual.

Petición:

```json
{
    "zonaOrigenId": 1,
    "zonaDestinoId": 2,
    "pesoKg": 2.5,
    "altoCm": 20,
    "anchoCm": 30,
    "largoCm": 40
}
```

Respuesta:

```json
{
    "distanciaKm": 3.01,
    "tarifaBase": 5000.00,
    "valorDistancia": 3612.00,
    "valorPeso": 2000.00,
    "total": 10612.00
}
```

El `CotizacionService` obtiene la tarifa vigente, calcula la distancia con Haversine usando
las coordenadas de `ZONAS` y aplica `base + distanciaKm * valorKm + pesoKg * valorKg`.
Los valores monetarios se calculan con `BigDecimal` y se redondean a dos decimales con
`HALF_UP`. Si no existe una tarifa vigente, responde `500 TARIFA_NO_CONFIGURADA`.