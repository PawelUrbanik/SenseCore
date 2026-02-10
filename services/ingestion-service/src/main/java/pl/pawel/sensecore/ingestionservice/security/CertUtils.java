package pl.pawel.sensecore.ingestionservice.security;

import java.security.MessageDigest;
import java.util.Base64;

public final class CertUtils {

    public static String sha256FromClientCertHeader(String certValue) {
        try {
            String b64 = certValue
                    .replace("-----BEGIN CERTIFICATE-----", "")
                    .replace("-----END CERTIFICATE-----", "")
                    .replaceAll("\\s+", "");

            byte[] der = Base64.getDecoder().decode(b64);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(der);

            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();

        } catch (Exception e) {
            throw new IllegalStateException("Cannot compute SHA-256 fingerprint from client cert header", e);
        }
    }

}
