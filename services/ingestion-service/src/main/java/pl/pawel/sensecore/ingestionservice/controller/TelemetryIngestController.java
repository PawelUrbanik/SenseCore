package pl.pawel.sensecore.ingestionservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.pawel.sensecore.ingestionservice.api.dto.TelemetryIngestRequest;
import pl.pawel.sensecore.ingestionservice.security.ClientIdentity;
import pl.pawel.sensecore.ingestionservice.security.ClientIdentityExtractor;
import pl.pawel.sensecore.ingestionservice.service.IngestionService;

import java.util.UUID;

@RestController
@RequestMapping("/api/ingest")
@Slf4j
public class TelemetryIngestController {

    private static final String HEADER_REQUEST_ID = "X-Request-Id";
    private static final String HEADER_TRACE_ID = "X-Trace-Id";
    private static final String MDC_REQUEST_ID = "requestId";
    private static final String MDC_TRACE_ID = "traceId";

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
        String requestId = resolveOrGenerate(request.getHeader(HEADER_REQUEST_ID));
        String traceId = resolveOrDefault(request.getHeader(HEADER_TRACE_ID), requestId);
        MDC.put(MDC_REQUEST_ID, requestId);
        MDC.put(MDC_TRACE_ID, traceId);
        try {
            log.debug(
                    "Received ingest request: remoteIp={}, unit={}, timestampPresent={}",
                    request.getRemoteAddr(),
                    body.unit(),
                    body.timestamp() != null
            );
            ClientIdentity identity = clientIdentityExtractor.extract(request);
            ingestionService.ingest(identity, body);
            return ResponseEntity.accepted().build();
        } finally {
            MDC.remove(MDC_REQUEST_ID);
            MDC.remove(MDC_TRACE_ID);
        }
    }

    private String resolveOrGenerate(String value) {
        if (value == null || value.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return value.trim();
    }

    private String resolveOrDefault(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
