package com.lab.grpc.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class UnitCategory {

    private final String code;
    private final String label;
    private final String defaultFromUnit;
    private final String defaultToUnit;
    private final List<UnitDefinition> units;

    public UnitCategory(String code, String label, String defaultFromUnit, String defaultToUnit, List<UnitDefinition> units) {
        this.code = code;
        this.label = label;
        this.defaultFromUnit = defaultFromUnit;
        this.defaultToUnit = defaultToUnit;
        this.units = Collections.unmodifiableList(new ArrayList<>(units));
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getDefaultFromUnit() {
        return defaultFromUnit;
    }

    public String getDefaultToUnit() {
        return defaultToUnit;
    }

    public List<UnitDefinition> getUnits() {
        return units;
    }

    public Optional<UnitDefinition> findUnit(String unitCode) {
        if (unitCode == null) {
            return Optional.empty();
        }

        return units.stream()
                .filter(unit -> unit.getCode().equalsIgnoreCase(unitCode.trim()))
                .findFirst();
    }

    @Override
    public String toString() {
        return label;
    }
}