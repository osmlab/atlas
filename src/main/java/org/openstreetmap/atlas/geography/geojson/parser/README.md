# GeoJSON Parsing

## Why do we need a `GeoJson` parser? Won't a regular `JSON` parser do?

While the `GeoJSON` spec is well defined [RFC](https://tools.ietf.org/html/rfc7946), the specification is none the less very loose. This presents 2 challenges that are not common in `JSON` parsing where the schema is strictly defined.

1. Polymorphic nature of fields (`coordinates`),

**A Point**
```json
     {
         "type": "Point",
         "coordinates": [40, 10]
      }
```

**A Polygon**
```json
     {
        "type": "Polygon",
        "coordinates": [
          [
            [100.0, 0.0], [101.0, 0.0], [101.0, 1.0],
            [100.0, 1.0], [100.0, 0.0]
          ]
        ]
      }
```
Notice the structure in terms of fields is completely the same, but the value type is very different. For `Point` `coordinates` are an array of exactly 2 numbers of decimal type and for the `Polygon` it is an array-of-array of such point coordinates (itself represented as arrays).

This presents a unique challenge for mapping Java objects in a way that preserves type safety while still has room to accommodate different types.

2. Polymorphic nature of fields (`geometry`, `geometries` and `features`).

a. A `GeometryWithCoordinates` (all geometries except `GeometryCollection`) have coordinates. 
b. A `Feature` has one associated `geometry`, while a GeometryCollection has several assocated nested `geometries`.
c. A FeatureCollection could have several associated `features`.
d. `GeometryCollection`s can nest `GeometryCollection`s recursively.

Example,

**A Feature containing a LineString**
```json
{
  "type": "Feature",
  "geometry": {
    "type": "LineString",
    "coordinates": [
      [-122.009566, 37.33531],[-122.031007,37.390535],[-122.028932,37.332451]
    ]
  }
}
```

**A recursively nested GeometryCollection**
```json
{
    "type": "GeometryCollection",
    "geometries": [
        ...,
        {
            "type": "GeometryCollection",
            "geometries": [
                ...,
                {
                    "type": "GeometryCollection",
                    "geometries": [
                        ...,
                        {
                            "type": "GeometryCollection",
                            "geometries": [
                                ...
                            ]
                        }
                    ]
                }
            ]
        }
    ]
}

```


Since the building block of a `Geometry` is itself highly dynamic, this dynamism "leaks" into all other major structures like `Feature`s, `FeatureCollection`s and `GeometryCollection`s. A challenge that is not suited for regular JSON parsing.

3. Very liberal specification

The RFC supports the concept of [foreign fields](https://tools.ietf.org/html/rfc7946#section-6.1) and fields like `properties` are entirely foreign. This means that implementations that rely heavily in foreign fields would have to map the properties section to a map-of-map-of-map... or it's like. This makes coding against it difficult and also introduces bugs the the compiler cannot protect against due to loss of type-safety.

Example,

```json
{
  "type": "Feature",
  "bbox": [ ... ],
  "geometry": { ... },
  "properties": {
    "featureChangeType": "ADD",
    "metadata": {
      "somekey1": "some value 1",
      "somekey2": "some value 2"
    },
    "description": {
      "type": "UPDATE",
      "descriptors": [
        {
          "name": "TAG",
          "type": "ADD",
          "key": "c",
          "value": "3"
        },
        {
          "name": "TAG",
          "type": "UPDATE",
          "key": "b",
          "value": "2a",
          "originalValue": "2"
        },
        {
          "name": "TAG",
          "type": "REMOVE",
          "key": "a",
          "value": "1"
        },
        {
          "name": "GEOMETRY",
          "type": "ADD",
          "position": "5/5",
          "afterView": "LINESTRING (-122.028932 37.332451, -122.052138 37.317585, -122.0304871 37.3314171)"
        },
        {
          "name": "GEOMETRY",
          "type": "REMOVE",
          "position": "0/5",
          "beforeView": "LINESTRING (-122.052138 37.317585, -122.0304871 37.3314171, -122.028932 37.332451)"
        },
        {
          "name": "PARENT_RELATION",
          "type": "ADD",
          "afterView": "3"
        },
        {
          "name": "PARENT_RELATION",
          "type": "REMOVE",
          "beforeView": "1"
        },
        {
          "name": "START_NODE",
          "type": "UPDATE",
          "beforeView": "1",
          "afterView": "10"
        },
        {
          "name": "END_NODE",
          "type": "UPDATE",
          "beforeView": "2",
          "afterView": "20"
        }
      ]
    },
    "entityType": "EDGE",
    "completeEntityClass": "org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge",
    "identifier": 123,
    "tags": {
      "b": "2a",
      "c": "3"
    },
    " relations": [
      2,
      3
    ],
    "startNode": 10,
    "endNode": 20,
    "WKT": "LINESTRING (-122.009566 37.33531, -122.031007 37.390535, -122.028932 37.332451, -122.052138 37.317585, -122.0304871 37.3314171)",
    "bboxWKT": "POLYGON ((-122.052138 37.317585, -122.052138 37.390535, -122.009566 37.390535, -122.009566 37.317585, -122.052138 37.317585))"
  }
}

```

Notice how difficult it would be to access the originalValue inside properties#decription#descriptor[index] without type information.

In my exploration I didn't come across a framework that solves these problems to my satisfaction. In fact tools that deal with JsonSchema and JsonSchema-to-Java mapping seems to break due to the complexity of the geojson schema.

## What does this GeoJSON parser support?

1. Map GeoJSON to all standard types defined in the specification, including Feature, `FeatureCollection`, `Point`, `MultiPoint`, `LineString`, `MultiLineString`, `Polygon`, `MultiPolygon`.
2. Support complex `coordinates` structures.
3. Support for 2D and 3D `Bbox`es.
4. Full support for Foreign Fields.
5. Highly functional "auto"-`mapper` for properties, that automatically maps properties to your user-defined bean (subject to some restrictions).

## How do I use it?

If you are just interested in the standard types and do not rely heavily on `Foreign Fields` or `properties` you just need one line of code.

```java
        final GeoJsonItem geoJsonItem = GeoJsonParserGsonImpl.instance.deserialize(json);
```

If you do wish to map your properties to a deep nested `bean` or `pojo`, you will need an additional line while accessing properties (assume MyClass is your deep-nested POJO),

```java
        final MyClass myObj = geoJsonItem.getProperties().asType(MyClass.class);
```

## What is (will) not (be) supported?

Very detailed validations to ensure validation of the geometries. Example, [southwardly to northwardly](https://tools.ietf.org/html/rfc7946#section-5), [Anti-meridian](https://tools.ietf.org/html/rfc7946#section-3.1.9). These feature are available in `AtlasEntity`-ies. Only basic structural validations are added here.

## A note on Mutability

GeoJSONs i.e all `Geometry`-ies, `Feature`s and `FeatureCollection`s, and their constituents like `Coordinates`, `Bbox`, `Position`, etc are immutable. However your custom class which will be auto-mapped from the `properties` requires setter methods.  

## A note of `properties` auto-mapping

`Properties` in the `GeoJSONItem` can be automatically mapped to a user-defined POJO or bean. This doesn't require the user to explicitly call setters/getters. Under the hood the auto-mapper is a custom recursive `BeanUtilsBean#copyProperties` which is collection and nexted structure aware.

This means that for the auto-mapper to work you need to follow the `JavaBean` standards and some additional restrictions.

### `Properties` auto-mapping support and restrictions

#### Data-Types supported

* Scalar Values
    - String
    - [Java Wrapper Types](https://en.wikipedia.org/wiki/Primitive_wrapper_class)
* 1D Arrays of Scalar Values.
* A Java Bean that contains the above.
* 1D arrays of Java Beans.
* A Map of scalar values in it's `key` and `value`.

#### Restrictions

| Restriction                                    | Reason                                                                                                      | Workaround                                                                                                                         |
|------------------------------------------------|-------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------|
| Public Constructor and setters                 | Needed for reflectively constructing and setting data in the bean                                           | NA                                                                                                                                 |
| Arrays instead of `List`s or `Set`s            | This makes auto mapping of Nested structures easier, since generic info is lost at runtime. *               | Expose a getter method that converts the array to your `Collection`                                                                |
| Maps of scalars only                           | Keys in JSON cannot cantain objects. Values may contain objects but the generic info is lost in Java. *     | May be supported in the future, if the use case presents. In the mean while you can construct an array of `Pair`s as a workaround. | 

\* The loss of generic info at runtime in Java is due to [Type Erasure](https://docs.oracle.com/javase/tutorial/java/generics/erasure.html). While there are workarounds available to get that at runtime, they are awkward and would make the API complicated.

 