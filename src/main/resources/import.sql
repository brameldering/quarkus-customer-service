CREATE TABLE quarkus_user (
      id INT,
      username VARCHAR(255),
      password VARCHAR(255),
      role VARCHAR(255)
);
INSERT INTO quarkus_user (id, username, password, role)
VALUES (1, 'admin', 'admin', 'admin');
INSERT INTO quarkus_user (id, username, password, role)
VALUES (2, 'bram','bram', 'user');

INSERT INTO customer (id, firstName, lastName) VALUES ( nextval('customerId_seq'), 'John','Doe');
INSERT INTO customer (id, firstName, lastName) VALUES ( nextval('customerId_seq'), 'Fred','Smith');

