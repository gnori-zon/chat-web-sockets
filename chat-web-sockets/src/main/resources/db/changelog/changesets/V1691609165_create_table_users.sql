-- liquibase formatted sql

-- changeset gnori-zon:V1691609165_create_table_users

create table users(
    id bigserial primary key,
    created_at timestamp(6),
    updated_at timestamp(6),
    email varchar(255) not null unique,
    name varchar(128) not null,
    password varchar(255) not null,
    username varchar(255) not null unique
);

alter table users
    owner to chat;

