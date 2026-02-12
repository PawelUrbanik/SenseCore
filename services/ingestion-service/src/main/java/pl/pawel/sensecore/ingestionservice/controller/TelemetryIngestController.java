package pl.pawel.sensecore.ingestionservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.pawel.sensecore.ingestionservice.api.dto.TelemetryIngestRequest;
import pl.pawel.sensecore.ingestionservice.security.ClientIdentity;
import pl.pawel.sensecore.ingestionservice.security.ClientIdentityExtractor;
import pl.pawel.sensecore.ingestionservice.service.IngestionService;

@RestController
@RequestMapping("/api/ingest")
@Log4j2
public class TelemetryIngestController {

    private final ClientIdentityExtractor clientIdentityExtractor;
    private final IngestionService ingestionService;

    public TelemetryIngestController(ClientIdentityExtractor clientIdentityExtractor, IngestionService ingestionService) {
        this.clientIdentityExtractor = clientIdentityExtractor;
        this.ingestionService = ingestionService;
    }

    @PostMapping("/telemetry")
    public ResponseEntity<Void> ingest(
            @RequestBody TelemetryIngestRequest body,
            HttpServletRequest request) {
        log.debug("Receive request from: : " + request.getRemoteAddr() + " with body: " + body);
        ClientIdentity identity = clientIdentityExtractor.extract(request);
        ingestionService.ingest(identity, body);
        return ResponseEntity.accepted().build();
    }
}
