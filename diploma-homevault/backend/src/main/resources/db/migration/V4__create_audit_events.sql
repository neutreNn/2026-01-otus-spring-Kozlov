create table audit_events
(
    id uuid primary key,
    actor_user_id uuid,
    action varchar(80) not null,
    entity_type varchar(80),
    entity_id uuid,
    ip_address varchar(80),
    user_agent varchar(500),
    details jsonb,
    created_at timestamptz not null,
    constraint fk_audit_events_actor foreign key (actor_user_id) references users (id) on delete set null
);

create index idx_audit_events_actor_created_at on audit_events (actor_user_id, created_at);
create index idx_audit_events_action_created_at on audit_events (action, created_at);
