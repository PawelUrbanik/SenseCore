create table if not exists telemetry_reading
(
    id bigserial primary key,
    device_id varchar(100) not null,
    sensor_type varchar(50) not null,
    value_numeric numeric(18, 6) not null,
    unit varchar(20) not null,
    ts timestamptz not null,
    received_at timestamptz not null default now(),

    constraint fk_telemetry_device
    foreign key (device_id)
    references device(device_id)
);

create index if not exists idx_telemetry_device_ts
    on telemetry_reading (device_id, ts);

create index if not exists idx_telemetry_sensor_ts
    on telemetry_reading (sensor_type, ts);