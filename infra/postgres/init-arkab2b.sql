CREATE ROLE identity_access LOGIN PASSWORD 'identity_access';
CREATE DATABASE identity_access OWNER identity_access;

CREATE ROLE directory LOGIN PASSWORD 'directory';
CREATE DATABASE directory OWNER directory;

CREATE ROLE catalog LOGIN PASSWORD 'catalog';
CREATE DATABASE arkab2b_catalog OWNER catalog;

CREATE ROLE inventory LOGIN PASSWORD 'inventory';
CREATE DATABASE inventory OWNER inventory;

CREATE ROLE "order" LOGIN PASSWORD 'order';
CREATE DATABASE "order" OWNER "order";

CREATE ROLE notification LOGIN PASSWORD 'notification';
CREATE DATABASE arkab2b_notification OWNER notification;

CREATE ROLE reporting LOGIN PASSWORD 'reporting';
CREATE DATABASE arkab2b_reporting OWNER reporting;
