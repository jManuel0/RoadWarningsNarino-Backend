package com.roadwarnings.narino.service;

/**
 * Resultado simplificado de reverse geocoding:
 * location: texto descriptivo de dirección (display_name).
 * municipality: ciudad/municipio/zona.
 */
public record ReverseGeocodeResult(String location, String municipality) {
}

