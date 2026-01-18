package pl.pawel.sensecore.processor.service;

import pl.pawel.sensecore.contracts.TelemetryEvent;

public interface TelemetryProcessorService {
    public void process(TelemetryEvent event);
}
