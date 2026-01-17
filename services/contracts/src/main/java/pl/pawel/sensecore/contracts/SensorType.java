package pl.pawel.sensecore.contracts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SensorType {

    TEMPERATURE("temperature"),
    HUMIDITY("humidity"),
    PRESSURE("pressure"),
    CO2("co2");

    private final String value;

    SensorType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static SensorType fromValue(String value) {
        for (SensorType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown sensorType: " + value);
    }

    public boolean isUnitCompatible(Unit unit) {
        return switch (this) {
            case TEMPERATURE -> unit == Unit.CELSIUS;
            case HUMIDITY -> unit == Unit.PERCENT;
            case CO2 -> unit == Unit.PPM;
            case PRESSURE -> unit == Unit.HECTOPASCAL;
        };
    }
}
