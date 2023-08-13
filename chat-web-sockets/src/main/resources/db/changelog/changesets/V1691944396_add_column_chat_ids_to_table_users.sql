-- liquibase formatted sql

-- changeset gnori-zon:V1691944396_add_column_chat_ids_to_table_users

alter table users add column chat_ids jsonb;