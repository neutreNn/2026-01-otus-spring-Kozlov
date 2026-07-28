create table notes
(
    id uuid primary key,
    owner_id uuid not null,
    title varchar(255) not null,
    content text not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint fk_notes_owner foreign key (owner_id) references users (id) on delete cascade,
    constraint chk_notes_title_not_blank check (length(trim(title)) > 0),
    constraint chk_notes_content_not_blank check (length(trim(content)) > 0)
);

create table note_tags
(
    note_id uuid not null,
    tag varchar(80) not null,
    constraint pk_note_tags primary key (note_id, tag),
    constraint fk_note_tags_note foreign key (note_id) references notes (id) on delete cascade,
    constraint chk_note_tags_tag_not_blank check (length(trim(tag)) > 0)
);

create index idx_notes_owner_updated_at on notes (owner_id, updated_at desc);
create index idx_note_tags_tag on note_tags (tag);
