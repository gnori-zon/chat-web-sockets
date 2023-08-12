-- liquibase formatted sql

-- changeset gnori-zon:V1691609166_create_table_users_role

create table users_role(
    user_id bigint not null,
    role varchar(255)
);

alter table users_role add constraint FK_users_role_user_id foreign key (user_id) references users(id) on delete cascade;
alter table users_role add constraint users_role_role_check
            check ((role)::text = ANY ((ARRAY ['ADMIN'::character varying, 'USER'::character varying])::text[]));
alter table users_role add constraint uk_users_role_user_id_role unique (user_id, role);