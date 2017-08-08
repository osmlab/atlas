#!/bin/bash

# Once the tiles sql files are generated, run this script to have the database do the counting.

: ${1:?"Make sure to pass the database user name to the script"}

for sqlscript in ./*.sql
do
	echo "processing $sqlscript"
	psql -U $1 -d osm_planet -f $sqlscript
done
