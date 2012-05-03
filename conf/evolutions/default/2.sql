# --- !Ups

create sequence user_seq start with 1000;
create table user (
   id integer not null default nextval('user_seq'),
   name varchar(256) ,
   email varchar(256) unique,
   password varchar(256),
   created_date timestamp not null
)

# --- !Downs

drop table if exists user;
drop sequence if exists user_seq;