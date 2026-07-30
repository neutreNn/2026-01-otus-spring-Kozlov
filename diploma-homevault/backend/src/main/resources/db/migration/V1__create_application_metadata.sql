create table app_metadata
(
    metadata_key varchar(100) primary key,
    metadata_value varchar(500) not null,
    created_at timestamptz not null default now()
);

insert into app_metadata (metadata_key, metadata_value)
values ('schema.version', '1')
on conflict (metadata_key) do nothing;

