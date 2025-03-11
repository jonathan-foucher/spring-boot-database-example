drop index if exists movie_idx01;

drop table if exists movie;
create table movie (
    id              bigserial       primary key,
    director_id     bigserial       not null,
    title           varchar(100)    not null,
    release_date    date            not null,
    updated_at      timestamptz     not null,
    constraint movie_fk01 foreign key (director_id) references director(id)
);

create index movie_idx01 on movie(director_id);
