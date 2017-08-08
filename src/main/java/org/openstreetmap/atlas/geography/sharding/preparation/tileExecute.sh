#!/bin/bash

# Run the function that will count the tiles members in the database.

: ${1:?"Make sure to pass the database user name to the script"}

psql -U $1 -d osm_planet -c "SELECT * FROM countForTiles()"
