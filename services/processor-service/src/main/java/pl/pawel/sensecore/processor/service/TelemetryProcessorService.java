package pl.pawel.sensecore.processor.service;

import pl.pawel.sensecore.contracts.TelemetryEvent;

public interface TelemetryProcessorService {
    void process(TelemetryEvent event);
}
