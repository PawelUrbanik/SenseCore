package pl.pawel.sensecore.ingestionservice.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class ClientIdentityExtractor {

    private static final String HEADER_VERIFY= "X-SSL-Client-Verify";
    private static final String HEADER_FINGERPRINT = "X-SSL-Client-Fingerprint";
    private static final String HEADER_CLIENT_CERT = "X-SSL-Client-Cert";
//    private static final String HEADER_DN = "X-SSL-Client-DN";
    private static final String HEADER_XFF = "X-Forwarded-For";

    public ClientIdentity extract(HttpServletRequest request) {
        String verify = header(request, HEADER_VERIFY);

        if (verify == null || !"SUCCESS".equalsIgnoreCase(verify.trim())){
            log.error("mTLS not verified");
            throw new UnauthorizedException("mTLS not verified (missing/invalid X-SSL-Client-Verify)");
        }

        String cert = header(request, HEADER_CLIENT_CERT);
        if (cert == null || cert.isBlank()) {
            log.error("Cert is empty/null");
            throw new UnauthorizedException("Missing X-SSL-Client-Cert");
        }
        String fingerprint = CertUtils.sha256FromClientCertHeader(cert);

        if (fingerprint.isBlank()) {
            log.error("Fingerprint is empty");
            throw new UnauthorizedException("Missing fingerprint");
        }

        String ip = extractIp(request);
        return new ClientIdentity(fingerprint.trim(), ip);
    }

    private String extractIp(HttpServletRequest request) {
        String xff = header(request, HEADER_XFF);
        if (xff != null && !xff.isBlank()) {
            // Get first ip address
            log.debug("First ip address: " + xff.split(",")[0].trim());
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String header(HttpServletRequest request, String name) {
        log.debug("Reading header: "+  name + " with value: " + request.getHeader(name));
        return request.getHeader(name);
    }


}
