create table users
(
    id uuid primary key,
    email varchar(320) not null,
    password_hash varchar(255) not null,
    display_name varchar(120) not null,
    status varchar(30) not null,
    storage_limit_bytes bigint,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uk_users_email unique (email),
    constraint chk_users_status check (status in ('ACTIVE', 'BLOCKED')),
    constraint chk_users_storage_limit check (storage_limit_bytes is null or storage_limit_bytes >= 0)
);

create table user_roles
(
    user_id uuid not null,
    role varchar(30) not null,
    constraint pk_user_roles primary key (user_id, role),
    constraint fk_user_roles_user foreign key (user_id) references users (id) on delete cascade,
    constraint chk_user_roles_role check (role in ('USER', 'ADMIN'))
);

create table refresh_tokens
(
    id uuid primary key,
    user_id uuid not null,
    token_hash varchar(255) not null,
    expires_at timestamptz not null,
    revoked_at timestamptz,
    created_at timestamptz not null,
    constraint uk_refresh_tokens_token_hash unique (token_hash),
    constraint fk_refresh_tokens_user foreign key (user_id) references users (id) on delete cascade
);

create index idx_refresh_tokens_user_id on refresh_tokens (user_id);
create index idx_refresh_tokens_expires_at on refresh_tokens (expires_at);

