package pl.pawel.sensecore.contracts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import static pl.pawel.sensecore.contracts.SensorType.*;

public enum Unit {
    CELSIUS("C"),
    PERCENT("%"),
    PPM("ppm"),
    HECTOPASCAL("hPa");

    private final String symbol;

    Unit(String symbol) {
        this.symbol = symbol;
    }

    @JsonValue
    public String getSymbol() {
        return symbol;
    }

    @JsonCreator
    public static Unit fromSymbol(String symbol) {
        for (Unit unit : values()) {
            if (unit.symbol.equalsIgnoreCase(symbol)) {
                return unit;
            }
        }
        throw new IllegalArgumentException("Unknown unit: " + symbol);
    }

}
