CREATE TABLE metadata (key text NOT NULL, value);
CREATE TABLE dictionary (key integer PRIMARY KEY, value);
CREATE TABLE node (id integer PRIMARY KEY, checksum text, shard text, geom blob NOT NULL);
CREATE TABLE node_tags (id integer NOT NULL, key integer NOT NULL, value integer NOT NULL);
CREATE TABLE edge (id integer PRIMARY KEY, checksum text, shard text, geom blob NOT NULL);
CREATE TABLE edge_tags (id integer NOT NULL, key integer NOT NULL, value integer NOT NULL);
CREATE TABLE point (id integer PRIMARY KEY, checksum text, shard text, geom blob NOT NULL);
CREATE TABLE point_tags (id integer NOT NULL, key integer NOT NULL, value integer NOT NULL);
CREATE TABLE line (id integer PRIMARY KEY, checksum text, shard text, geom blob NOT NULL);
CREATE TABLE line_tags (id integer NOT NULL, key integer NOT NULL, value integer NOT NULL);
CREATE TABLE area (id integer PRIMARY KEY, checksum text, shard text, geom blob NOT NULL);
CREATE TABLE area_tags (id integer NOT NULL, key integer NOT NULL, value integer NOT NULL);

