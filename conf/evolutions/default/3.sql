# --- !Ups

create sequence trade_seq start with 1000;
create table trade (
   id integer not null default nextval('trade_seq'),
   amount decimal(12,8) default 0,
   price decimal(12,8) default 0,
   seller_id integer not null,
   user_id integer not null,
   created_date timestamp not null
)

# --- !Downs

drop table if exists trade;
drop sequence if exists trade_seq;