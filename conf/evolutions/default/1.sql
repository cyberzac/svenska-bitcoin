# Tasks schema

# --- !Ups

CREATE SEQUENCE trans_id_seq start with 1000;
CREATE TABLE trans (
    id integer NOT NULL DEFAULT nextval('trans_id_seq'),
    credit_amount decimal(12,8),
    credit_account int(8),
    credit_user_id varchar(255),
    debit_amount decimal(12,8),
    debit_account int(8),
    debit_user_id varchar(255),
    note varchar(255),
    external_id varchar (255),
    created_date timestamp
);

-- CREATE SEQUENCE user_id_seq start with 1000;
-- CREATE TABLE user (
--     id integer NOT NULL DEFAULT nextval('user_id_seq'),
--     userId varchar(255)
--    name varchar (255)
--    email varchar (255)user
--    password varchar (255)
-- );

# --- !Downs

DROP TABLE trans;
DROP SEQUENCE trans_id_seq;