drop table if exists director;
create table director (
    id              bigserial       primary key,
    first_name      varchar(50)     not null,
    last_name       varchar(50)     not null,
    updated_at      timestamptz     not null
);
