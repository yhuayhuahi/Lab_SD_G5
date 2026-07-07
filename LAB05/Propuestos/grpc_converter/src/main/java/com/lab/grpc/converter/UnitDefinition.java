package com.lab.grpc.converter;

public final class UnitDefinition {

    private final String code;
    private final String label;
    private final double factorToBase;

    public UnitDefinition(String code, String label, double factorToBase) {
        this.code = code;
        this.label = label;
        this.factorToBase = factorToBase;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public double getFactorToBase() {
        return factorToBase;
    }

    @Override
    public String toString() {
        return label;
    }
}