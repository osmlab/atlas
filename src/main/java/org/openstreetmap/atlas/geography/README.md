# Geography Package

This package contains all the geography tools that make handling geographical data easy.

## Atlas package

The [Atlas package](atlas) contains the `Atlas`, an in-memory representation of the OSM data.

## Boundary package

All the classes that help with defining administrative boundaries, and associating them with three digit country codes.

## Sharding package

Tools that help eavenly divide the world into multiple shards. `DynamicTileSharding` is basically a quad tree.

## Geography Classes

### `Latitude`

A `Latitude` is an `Angle` extension which is bound between -90 degrees and 90 degrees. Like `Angle`, it can be created from degrees, radians, or a degree of magnitude 7 (dm7) long.

### `Longitude`

A `Longitude` is an `Angle` extension which is bound between -180 degrees and 179.9999999 degrees. Like `Angle`, it can be created from degrees, radians, or a degree of magnitude 7 (dm7) long. Unlike OSM which allows -180 and 180 alike for values that touch the antimeridian, `Longitude` allows only -180.

### `Location`

A `Location` is a set of two coordinates, one `Latitude` and one `Longitude`.

### `Heading`

A `Heading` is an extension of `Angle` which represents the 0 to 360 degrees cardinal direction between North and some other direction. North has a heading of 0 degrees, East 90 degrees, South 180 degrees, and West 270 degrees.

### `PolyLine`, `Polygon` and `Segment`

A `PolyLine` is a list of `Location`s. A `Polygon` is an extension of `PolyLine` where we assume that the first and last point are joined. The first and last point are not duplicated here. A `Segment` is a subset of `PolyLine` with only two points. `PolyLine`s and `Polygon`s can return the list of their `Segment`s.

### `Rectangle` and `Located`

`Rectangle` is a bounding box between two `Latitude`s and `Longitude`s. Anything that is `Located` has to return bounds as a `Rectangle`. A `Rectangle` is also a `Polygon` with 4 points.

### `MultiPolygon`

A `MultiPolygon` is a collection of outer `Polygon`s and their set of inner `Polygon`s. It is generally used to represent shapes with holes.
