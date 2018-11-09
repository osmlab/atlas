# Creating Vector Tiles

`TippecanoeExporter.java` is a CLI that converts a directory of atlas files into
line-delimited GeoJSON, and then that gets converted into an MBTiles file full
of MapboxVectorTiles by tippecanoe.

Usage:

``` 
java -Xmx12G -cp ./atlas.jar org.openstreetmap.atlas.utilities.vectortiles.TippecanoeExporter \
-atlasDirectory=<directory of atlas files> \
-geojsonDirectory=<directory to write GeoJSON> \
-mbtiles=<where to write MBTiles file> \
-threads=8 \
-overwrite=true
```

Note: The `-mbtiles` argument should be the path to the specific file you'd like to write to--not a directory.

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


# Feature Minimum Zoom Level Configuration

The resource [minimum-zooms.json](https://github.com/hallahan/atlas/blob/tippecanoe/src/main/resources/org/openstreetmap/atlas/utilities/vectortiles/minimum-zooms.json) 
allows you to configure the minimum zoom for a given feature based on its tags.

The values you see in the minimum-zoom.json resourse file is inspired for what you
see in the standard OpenStreetMap carto style. This is very loosely based off of the minimum
zooms you see for various types of features. Note that there is definitely more work that needs
to be done to refine our min zooms.

https://github.com/gravitystorm/openstreetmap-carto

The config is a JSON array of rule objects. Each rule looks like this:

```json
{
  "key": "landuse",
  "default": 12,
  "values": {
    "basin": 7,
    "forest": 8
  }
}
```

The rule must have a key for the tag key. It must have an integer for the default minimum zoom.
values is optional, and this is an object with a given OSM tag value and a minimum zoom that will
apply to it. The way that the JSON config evaluates is that the first rules in the array take
priority. If a given key matches, we use that rule, and all other rules will not be evaluated for
finding the minimum zoom for that given atlas element's tags.

So, the order of your rule in minimum-zooms.json that applies first takes precedence.
