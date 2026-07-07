package com.lab.grpc.converter;

public final class ConversionValidator {

    private ConversionValidator() {
    }

    public static void validateGeneralValue(String categoryCode, String fromUnitCode, double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("El valor ingresado no es finito.");
        }

        if (isTemperature(categoryCode)) {
            validateTemperature(fromUnitCode, value);
            return;
        }

        if (value < 0) {
            throw new IllegalArgumentException("No se permiten valores negativos para esta categoria.");
        }
    }

    private static boolean isTemperature(String categoryCode) {
        return "temperatura".equalsIgnoreCase(categoryCode);
    }

    private static void validateTemperature(String fromUnitCode, double value) {
        if ("K".equalsIgnoreCase(fromUnitCode) && value < 0) {
            throw new IllegalArgumentException("Kelvin no puede ser menor que cero.");
        }

        if ("R".equalsIgnoreCase(fromUnitCode) && value < 0) {
            throw new IllegalArgumentException("Rankine no puede ser menor que cero.");
        }
    }
}