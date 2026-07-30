create table share_links
(
    id uuid primary key,
    token varchar(120) not null,
    owner_id uuid not null,
    resource_type varchar(30) not null,
    resource_id uuid not null,
    expires_at timestamptz not null,
    revoked_at timestamptz,
    access_count bigint not null default 0,
    created_at timestamptz not null,
    constraint uk_share_links_token unique (token),
    constraint fk_share_links_owner foreign key (owner_id) references users (id) on delete cascade,
    constraint chk_share_links_resource_type check (resource_type in ('FILE', 'NOTE')),
    constraint chk_share_links_access_count check (access_count >= 0)
);

create index idx_share_links_owner_id on share_links (owner_id);
create index idx_share_links_token on share_links (token);
create index idx_share_links_resource on share_links (resource_type, resource_id);
