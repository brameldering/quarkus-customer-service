CREATE TABLE IF NOT EXISTS quarkus_user (
      id INT,
      username VARCHAR(255),
      password VARCHAR(255),
      role VARCHAR(255)
);
INSERT INTO quarkus_user (id, username, password, role)
VALUES (1, 'admin', 'test', 'admin');
INSERT INTO quarkus_user (id, username, password, role)
VALUES (2, 'test','test', 'user');

INSERT INTO customer (id, firstName, lastName) VALUES ( nextval('customerId_seq'), 'John','Doe');
INSERT INTO customer (id, firstName, lastName) VALUES ( nextval('customerId_seq'), 'Fred','Smith');

