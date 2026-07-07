package com.lab.grpc.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class UnitCatalog {

    private static final List<UnitCategory> CATEGORIES = buildCategories();

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
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada: " + categoryCode));

        UnitDefinition from = category.findUnit(fromUnitCode)
                .orElseThrow(() -> new IllegalArgumentException("Unidad origen no encontrada: " + fromUnitCode));

        UnitDefinition to = category.findUnit(toUnitCode)
                .orElseThrow(() -> new IllegalArgumentException("Unidad destino no encontrada: " + toUnitCode));

        if ("temperatura".equalsIgnoreCase(category.getCode())) {
            return convertTemperature(from.getCode(), to.getCode(), value);
        }

        return value * from.getFactorToBase() / to.getFactorToBase();
    }

    private static double convertTemperature(String from, String to, double value) {
        double celsius;

        switch (from.toUpperCase()) {
            case "C":
                celsius = value;
                break;
            case "F":
                celsius = (value - 32.0) * 5.0 / 9.0;
                break;
            case "K":
                celsius = value - 273.15;
                break;
            case "R":
                celsius = (value - 491.67) * 5.0 / 9.0;
                break;
            default:
                throw new IllegalArgumentException("Unidad de temperatura no valida: " + from);
        }

        switch (to.toUpperCase()) {
            case "C":
                return celsius;
            case "F":
                return celsius * 9.0 / 5.0 + 32.0;
            case "K":
                return celsius + 273.15;
            case "R":
                return (celsius + 273.15) * 9.0 / 5.0;
            default:
                throw new IllegalArgumentException("Unidad de temperatura no valida: " + to);
        }
    }

    private static List<UnitCategory> buildCategories() {
        List<UnitCategory> categories = new ArrayList<>();

        categories.add(new UnitCategory("longitud", "Longitud", "m", "km", List.of(
                new UnitDefinition("mm", "Milimetro (mm)", 0.001),
                new UnitDefinition("cm", "Centimetro (cm)", 0.01),
                new UnitDefinition("m", "Metro (m)", 1.0),
                new UnitDefinition("km", "Kilometro (km)", 1000.0),
                new UnitDefinition("in", "Pulgada (in)", 0.0254),
                new UnitDefinition("ft", "Pie (ft)", 0.3048),
                new UnitDefinition("yd", "Yarda (yd)", 0.9144),
                new UnitDefinition("mi", "Milla (mi)", 1609.344),
                new UnitDefinition("nmi", "Milla nautica (nmi)", 1852.0)
        )));

        categories.add(new UnitCategory("masa", "Masa", "kg", "g", List.of(
                new UnitDefinition("mg", "Miligramo (mg)", 0.000001),
                new UnitDefinition("g", "Gramo (g)", 0.001),
                new UnitDefinition("kg", "Kilogramo (kg)", 1.0),
                new UnitDefinition("t", "Tonelada (t)", 1000.0),
                new UnitDefinition("oz", "Onza (oz)", 0.0283495),
                new UnitDefinition("lb", "Libra (lb)", 0.453592)
        )));

        categories.add(new UnitCategory("area", "Area", "m2", "ha", List.of(
                new UnitDefinition("mm2", "Milimetro cuadrado (mm2)", 0.000001),
                new UnitDefinition("cm2", "Centimetro cuadrado (cm2)", 0.0001),
                new UnitDefinition("m2", "Metro cuadrado (m2)", 1.0),
                new UnitDefinition("km2", "Kilometro cuadrado (km2)", 1000000.0),
                new UnitDefinition("ha", "Hectarea (ha)", 10000.0),
                new UnitDefinition("in2", "Pulgada cuadrada (in2)", 0.00064516),
                new UnitDefinition("ft2", "Pie cuadrado (ft2)", 0.092903),
                new UnitDefinition("yd2", "Yarda cuadrada (yd2)", 0.836127)
        )));

        categories.add(new UnitCategory("volumen", "Volumen", "m3", "l", List.of(
                new UnitDefinition("ml", "Mililitro (ml)", 0.000001),
                new UnitDefinition("l", "Litro (l)", 0.001),
                new UnitDefinition("m3", "Metro cubico (m3)", 1.0),
                new UnitDefinition("cm3", "Centimetro cubico (cm3)", 0.000001),
                new UnitDefinition("ft3", "Pie cubico (ft3)", 0.0283168),
                new UnitDefinition("in3", "Pulgada cubica (in3)", 0.0000163871),
                new UnitDefinition("gal_us", "Galon US (gal)", 0.00378541)
        )));

        categories.add(new UnitCategory("energia", "Energia", "J", "kJ", List.of(
                new UnitDefinition("J", "Joule (J)", 1.0),
                new UnitDefinition("kJ", "Kilojoule (kJ)", 1000.0),
                new UnitDefinition("cal", "Caloria (cal)", 4.1868),
                new UnitDefinition("kcal", "Kilocaloria (kcal)", 4186.8),
                new UnitDefinition("Wh", "Watt hora (Wh)", 3600.0),
                new UnitDefinition("kWh", "Kilowatt hora (kWh)", 3600000.0),
                new UnitDefinition("BTU", "BTU", 1055.06)
        )));

        categories.add(new UnitCategory("fuerza", "Fuerza", "N", "kgf", List.of(
                new UnitDefinition("N", "Newton (N)", 1.0),
                new UnitDefinition("dyn", "Dina (dyn)", 0.00001),
                new UnitDefinition("kgf", "Kilogramo fuerza (kgf)", 9.80665),
                new UnitDefinition("lbf", "Libra fuerza (lbf)", 4.44822)
        )));

        categories.add(new UnitCategory("potencia", "Potencia", "W", "kW", List.of(
                new UnitDefinition("W", "Watt (W)", 1.0),
                new UnitDefinition("kW", "Kilowatt (kW)", 1000.0),
                new UnitDefinition("hp", "Horsepower (hp)", 745.7),
                new UnitDefinition("CV", "Caballo de vapor (CV)", 735.5)
        )));

        categories.add(new UnitCategory("presion", "Presion", "Pa", "bar", List.of(
                new UnitDefinition("Pa", "Pascal (Pa)", 1.0),
                new UnitDefinition("kPa", "Kilopascal (kPa)", 1000.0),
                new UnitDefinition("MPa", "Megapascal (MPa)", 1000000.0),
                new UnitDefinition("bar", "Bar (bar)", 100000.0),
                new UnitDefinition("atm", "Atmosfera (atm)", 101325.0),
                new UnitDefinition("psi", "Libra por pulgada cuadrada (psi)", 6894.76),
                new UnitDefinition("mmHg", "Milimetro de mercurio (mmHg)", 133.322)
        )));

        categories.add(new UnitCategory("temperatura", "Temperatura", "C", "F", List.of(
                new UnitDefinition("C", "Celsius (C)", 1.0),
                new UnitDefinition("F", "Fahrenheit (F)", 1.0),
                new UnitDefinition("K", "Kelvin (K)", 1.0),
                new UnitDefinition("R", "Rankine (R)", 1.0)
        )));

        return List.copyOf(categories);
    }
}