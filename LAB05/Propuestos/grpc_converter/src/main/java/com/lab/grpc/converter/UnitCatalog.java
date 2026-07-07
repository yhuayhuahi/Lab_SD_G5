package com.lab.grpc.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class UnitCatalog {

    private static final List<UnitCategory> CATEGORIES = createCategories();

    private UnitCatalog() {
    }

    public static List<UnitCategory> getCategories() {
        return CATEGORIES;
    }

    public static Optional<UnitCategory> findCategory(String categoryCode) {
        if (categoryCode == null) {
            return Optional.empty();
        }

        return CATEGORIES.stream()
                .filter(category -> category.getCode().equalsIgnoreCase(categoryCode.trim()))
                .findFirst();
    }

    public static double convert(String categoryCode, String fromUnitCode, String toUnitCode, double value) {
        UnitCategory category = findCategory(categoryCode)
                .orElseThrow(() -> new IllegalArgumentException("Categoria no reconocida: " + categoryCode));

        UnitDefinition fromUnit = category.findUnit(fromUnitCode)
                .orElseThrow(() -> new IllegalArgumentException("Unidad origen no reconocida: " + fromUnitCode));

        UnitDefinition toUnit = category.findUnit(toUnitCode)
                .orElseThrow(() -> new IllegalArgumentException("Unidad destino no reconocida: " + toUnitCode));

        ConversionValidator.validateGeneralValue(category.getCode(), fromUnit.getCode(), value);

        if ("temperatura".equalsIgnoreCase(category.getCode())) {
            return convertTemperature(fromUnit.getCode(), toUnit.getCode(), value);
        }

        return value * fromUnit.getFactorToBase() / toUnit.getFactorToBase();
    }

    private static List<UnitCategory> createCategories() {
        List<UnitCategory> categories = new ArrayList<>();

        categories.add(new UnitCategory("longitud", "Longitud", "m", "km", List.of(
                new UnitDefinition("mm", "Milimetro", 0.001),
                new UnitDefinition("cm", "Centimetro", 0.01),
                new UnitDefinition("m", "Metro", 1.0),
                new UnitDefinition("km", "Kilometro", 1000.0),
                new UnitDefinition("in", "Pulgada", 0.0254),
                new UnitDefinition("ft", "Pie", 0.3048),
                new UnitDefinition("yd", "Yarda", 0.9144),
                new UnitDefinition("mi", "Milla", 1609.344)
        )));

        categories.add(new UnitCategory("masa", "Masa", "kg", "g", List.of(
                new UnitDefinition("mg", "Miligramo", 0.000001),
                new UnitDefinition("g", "Gramo", 0.001),
                new UnitDefinition("kg", "Kilogramo", 1.0),
                new UnitDefinition("t", "Tonelada", 1000.0),
                new UnitDefinition("lb", "Libra", 0.45359237),
                new UnitDefinition("oz", "Onza", 0.028349523125)
        )));

        categories.add(new UnitCategory("area", "Area", "ha", "m2", List.of(
                new UnitDefinition("m2", "Metro cuadrado", 1.0),
                new UnitDefinition("km2", "Kilometro cuadrado", 1_000_000.0),
                new UnitDefinition("ha", "Hectarea", 10_000.0),
                new UnitDefinition("ac", "Acre", 4046.8564224)
        )));

        categories.add(new UnitCategory("volumen", "Volumen", "l", "m3", List.of(
                new UnitDefinition("ml", "Mililitro", 0.000001),
                new UnitDefinition("l", "Litro", 0.001),
                new UnitDefinition("m3", "Metro cubico", 1.0),
                new UnitDefinition("gal_us", "Galon US", 0.003785411784)
        )));

        categories.add(new UnitCategory("temperatura", "Temperatura", "C", "F", List.of(
                new UnitDefinition("C", "Grados Celsius", 1.0),
                new UnitDefinition("F", "Grados Fahrenheit", 1.0),
                new UnitDefinition("K", "Kelvin", 1.0),
                new UnitDefinition("R", "Rankine", 1.0)
        )));

        categories.add(new UnitCategory("moneda", "Moneda", "PEN", "USD", List.of(
                new UnitDefinition("PEN", "Soles", 1.0),
                new UnitDefinition("USD", "Dolares", 3.70),
                new UnitDefinition("EUR", "Euros", 4.00)
        )));

        categories.add(new UnitCategory("tiempo", "Tiempo", "h", "min", List.of(
                new UnitDefinition("ms", "Milisegundo", 0.001),
                new UnitDefinition("s", "Segundo", 1.0),
                new UnitDefinition("min", "Minuto", 60.0),
                new UnitDefinition("h", "Hora", 3600.0),
                new UnitDefinition("dia", "Dia", 86400.0)
        )));

        categories.add(new UnitCategory("velocidad", "Velocidad", "km_h", "m_s", List.of(
                new UnitDefinition("m_s", "Metro por segundo", 1.0),
                new UnitDefinition("km_h", "Kilometro por hora", 0.2777777778),
                new UnitDefinition("mph", "Milla por hora", 0.44704),
                new UnitDefinition("kn", "Nudo", 0.514444)
        )));

        categories.add(new UnitCategory("energia", "Energia", "kJ", "J", List.of(
                new UnitDefinition("J", "Joule", 1.0),
                new UnitDefinition("kJ", "Kilojoule", 1000.0),
                new UnitDefinition("cal", "Caloria", 4.184),
                new UnitDefinition("kcal", "Kilocaloria", 4184.0)
        )));

        categories.add(new UnitCategory("presion", "Presion", "bar", "Pa", List.of(
                new UnitDefinition("Pa", "Pascal", 1.0),
                new UnitDefinition("kPa", "Kilopascal", 1000.0),
                new UnitDefinition("bar", "Bar", 100000.0),
                new UnitDefinition("atm", "Atmosfera", 101325.0),
                new UnitDefinition("psi", "PSI", 6894.757)
        )));

        categories.add(new UnitCategory("fuerza", "Fuerza", "N", "kgf", List.of(
                new UnitDefinition("N", "Newton", 1.0),
                new UnitDefinition("kN", "Kilonewton", 1000.0),
                new UnitDefinition("kgf", "Kilogramo fuerza", 9.80665),
                new UnitDefinition("lbf", "Libra fuerza", 4.4482216153)
        )));

        categories.add(new UnitCategory("potencia", "Potencia", "kW", "W", List.of(
                new UnitDefinition("W", "Watt", 1.0),
                new UnitDefinition("kW", "Kilowatt", 1000.0),
                new UnitDefinition("hp", "Horsepower", 745.699872)
        )));

        categories.add(new UnitCategory("densidad", "Densidad", "kg_m3", "g_l", List.of(
                new UnitDefinition("kg_m3", "Kilogramo por metro cubico", 1.0),
                new UnitDefinition("g_l", "Gramo por litro", 1.0),
                new UnitDefinition("g_cm3", "Gramo por centimetro cubico", 1000.0),
                new UnitDefinition("lb_ft3", "Libra por pie cubico", 16.018463)
        )));

        categories.add(new UnitCategory("caudal", "Caudal", "l_s", "m3_h", List.of(
                new UnitDefinition("m3_s", "Metro cubico por segundo", 1.0),
                new UnitDefinition("m3_h", "Metro cubico por hora", 1.0 / 3600.0),
                new UnitDefinition("l_s", "Litro por segundo", 0.001),
                new UnitDefinition("l_min", "Litro por minuto", 0.001 / 60.0),
                new UnitDefinition("gpm_us", "Galon US por minuto", 0.003785411784 / 60.0)
        )));

        return List.copyOf(categories);
    }

    private static double convertTemperature(String fromUnitCode, String toUnitCode, double value) {
        double celsius = switch (fromUnitCode) {
            case "C" -> value;
            case "F" -> (value - 32.0) * 5.0 / 9.0;
            case "K" -> value - 273.15;
            case "R" -> (value - 491.67) * 5.0 / 9.0;
            default -> throw new IllegalArgumentException("Unidad de temperatura no reconocida: " + fromUnitCode);
        };

        return switch (toUnitCode) {
            case "C" -> celsius;
            case "F" -> celsius * 9.0 / 5.0 + 32.0;
            case "K" -> celsius + 273.15;
            case "R" -> (celsius + 273.15) * 9.0 / 5.0;
            default -> throw new IllegalArgumentException("Unidad de temperatura no reconocida: " + toUnitCode);
        };
    }
}