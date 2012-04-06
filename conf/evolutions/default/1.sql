# Tasks schema

# --- !Ups

CREATE SEQUENCE trans_id_seq start with 1000;
CREATE TABLE trans (
    id integer NOT NULL DEFAULT nextval('trans_id_seq'),
    user_id varchar(255),
    sek int(25),
    btc int(25),
    note varchar(255),
    trans_id varchar (255),
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