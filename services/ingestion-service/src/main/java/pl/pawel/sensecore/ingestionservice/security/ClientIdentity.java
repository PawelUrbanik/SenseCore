package pl.pawel.sensecore.ingestionservice.security;

public record ClientIdentity(String fingerprint, String ip) {
}
