create table if not exists device
(
    id bigserial primary key,
    device_id varchar(100) not null unique,
    status varchar(30) not null default 'ACTIVE',
    created_at timestamptz not null default now()
);

create index if not exists idx_device_device_id
    on device (device_id);