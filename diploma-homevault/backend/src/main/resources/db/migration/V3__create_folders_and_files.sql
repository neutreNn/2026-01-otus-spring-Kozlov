create table folders
(
    id uuid primary key,
    owner_id uuid not null,
    parent_id uuid,
    name varchar(255) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint fk_folders_owner foreign key (owner_id) references users (id) on delete cascade,
    constraint fk_folders_parent foreign key (parent_id) references folders (id),
    constraint chk_folders_name_not_blank check (length(trim(name)) > 0)
);

create unique index uk_folders_root_owner_name
    on folders (owner_id, lower(name))
    where parent_id is null;

create unique index uk_folders_parent_owner_name
    on folders (owner_id, parent_id, lower(name))
    where parent_id is not null;

create index idx_folders_owner_parent on folders (owner_id, parent_id);

create table files
(
    id uuid primary key,
    owner_id uuid not null,
    folder_id uuid,
    original_name varchar(255) not null,
    storage_key varchar(500) not null,
    content_type varchar(255),
    size_bytes bigint not null,
    checksum_sha256 varchar(64),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uk_files_storage_key unique (storage_key),
    constraint fk_files_owner foreign key (owner_id) references users (id) on delete cascade,
    constraint fk_files_folder foreign key (folder_id) references folders (id),
    constraint chk_files_original_name_not_blank check (length(trim(original_name)) > 0),
    constraint chk_files_size_bytes check (size_bytes >= 0)
);

create index idx_files_owner_folder on files (owner_id, folder_id);
