# --- !Ups

create sequence trans_seq start with 1000;
create table trans (
    id integer not null default nextval('trans_seq'),
    user_id integer not null,
    credit_amount decimal(12,8) default 0,
    credit_account int(8) not null,
    debit_amount decimal(12,8) default 0,
    debit_account int(8) not null,
    note varchar(255),
    external_id varchar (255),
    created_date timestamp not null
);


# --- !Downs

drop table if exists trans;
drop sequence if exists trans_seq;

