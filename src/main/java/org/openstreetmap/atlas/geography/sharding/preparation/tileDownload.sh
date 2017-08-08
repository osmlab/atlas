#!/bin/bash

# Once the tiles have been counted in the database, use this to download them.

: ${1:?"Make sure to pass the database user name to the script"}

psql -U $1 -d osm_planet -c "\copy sharding.counts TO ./counts.csv WITH DELIMITER ',' CSV"
