ALTER TABLE device
    ADD COLUMN fingerprint VARCHAR(128);

ALTER TABLE device
    ADD CONSTRAINT uq_device_fingerprint UNIQUE (fingerprint);
