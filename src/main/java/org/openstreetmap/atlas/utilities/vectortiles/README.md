# Creating Vector Tiles

`TippecanoeExporter.java` is a CLI that converts a directory of atlas files into
line-delimited GeoJSON, and then that gets converted into an MBTiles file full
of MapboxVectorTiles by tippecanoe.

Useage:

``` 
java -Xmx12G -cp ./atlas.jar org.openstreetmap.atlas.utilities.vectortiles.TippecanoeExporter \
-atlasDirectory=<directory of atlas files> \
-geojsonDirectory=<directory to write GeoJSON> \
-mbtiles=<where to write MBTiles file> \
-threads=8 \
-overwrite=true
```

On a beefy server, you might do something like this:

``` 
java -Xmx240G -cp ./atlas-njh.jar org.openstreetmap.atlas.utilities.vectortiles.TippecanoeExporter \
-atlasDirectory=/opt/data/tippecanoe/atlas \
-geojsonDirectory=/opt/data/tippecanoe/geojson \
-mbtiles=/opt/data/tippecanoe/WORLD.mbtiles \
-threads=16 \
-overwrite=true
```

Once you've created your MBTiles file, you can serve and view it with mbview.

``` 
mbview <mbtiles file>
```

Then a browser should pop up at http://localhost:3000


# Dependencies

## tippecanoe

Install tippecanoe:

``` 
brew install tippecanoe
```

or compile it...

https://github.com/mapbox/tippecanoe

## mbview

Make sure you have installed [NodeJS](https://nodejs.org/).

``` 
npm install -g mbview
```

https://github.com/mapbox/mbview


# Troubleshooting

Sometimes mbview's dependency, `node-sqlite` doesn't play well wit the latest version of NodeJS. In that case, you need to pin your NodeJS to a specific version.

Install nvm (NodeJS Version Manager):

https://github.com/creationix/nvm

Then run:

``` 
nvm install 8.11.1
```

Then, reinstall mbview. You should be up and running!
