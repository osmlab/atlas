# PyAtlas
#### A simplified Atlas API for Python

----
## Getting Started
To get setup in a new project folder, run:

    $ mkdir newproj && cd newproj
    $ virtualenv venv --python=python2.7
    $ source venv/bin/activate

NOTE: `pyatlas` will automatically install the dependencies it needs, including
the Protocol Buffers Python runtime - `protobuf-2.6.1`. 
Therefore, it is highly recommended that you develop `pyatlas` based projects in
a Python virtual environment - you may need to install `virtualenv` if you have not already. 
(If you want to create a `pyatlas` distribution that does not automatically pull
in dependencies, see the next section.)

Now that you have your virtual environment set up, you can install `pyatlas` with:

    (venv) $ pip install pyatlas

If pip is unable to find the `pyatlas` package, you may need to build it from
source yourself. Check the next section for more info.

To test that everything went smoothly, create a file `helloatlas.py` with the following code:
```python
import pyatlas
pyatlas.hello_atlas()
```
Now run:

    (venv) $ python helloatlas.py

If you see:

    Hello Atlas!

then you're good to go!

----
## Building the `pyatlas` module
To build the `pyatlas` module from source, run:

    $ git clone https://github.com/osmlab/atlas.git
    $ cd atlas
    $ ./gradlew buildPyatlas

This will generate a wheel file at `pyatlas/dist`. You can now install this with `pip` like

    $ cd /path/to/project/that/uses/pyatlas
    $ virtualenv venv --python=python2.7
    $ source venv/bin/activate
    $ pip install /path/to/atlas/pyatlas/dist/pyatlas-VERSION.whl

Again, it is recommended that you do this in the desired virtual environment.

If you want to build a `pyatlas` wheel file that does not automatically pull dependencies,
open up `setup.py` and remove the lines that say
    
    install_requires=[
    .
    .
    .
    ],

Then re-run the `./gradlew buildPyatlas` command from above and reinstall
using `pip`. Note that you will now need to manage the required dependencies manually.

### Note on the formatter
`pyatlas` uses the `yapf` formatting library to check for code format issues when building.
If you are running into issues after modifying `pyatlas`, try running

    ./gradlew applyFormatPyatlas

Now `pyatlas` should make it past the `CHECK` format step!

Note there is an issue that causes the formatter to goof if a source file does not
end with a newline (\n) character.
If the `CHECK` format step is consistently failing after repeated `APPLY` steps,
and you are seeing a message like the following:

    atlas.py: found issue, reformatting...

with no formatter diff being displayed, check to make sure that the file has an ending newline. 

----
## Documentation
`pyatlas` documentation is automatically generated using the `pydoc` tool and
stored in the `doc` folder. To build
the documentation, run the gradle build command:

    $ ./gradlew buildPyatlas

This will generate HTML files detailing the functions and classes available in each module.

----
## Some sample use cases
`pyatlas` is a highly capable subset of the API provided by the Java `Atlas`. Here are some examples
to get you started. Note that all of these examples were ran using the `test.atlas`
provided in the `resources` folder, and assume that you have an atlas variable defined like:

```python
from pyatlas.atlas import Atlas
atlas = Atlas('/path/to/atlas/pyatlas/resources/test.atlas')
```

#### Getting features and metadata
You can get filtered iterables over an `Atlas`'s features using the methods provided in the `Atlas` class.

```python
# print all Nodes
for node in atlas.nodes():
    print node

# print all Edges that have 'key1' as a tag key
for edge in atlas.edges(predicate=lambda e: 'key1' in e.get_tags().keys()):
    print edge
```

You can also get a feature with a specific identifier like:

```python
# print the Relation with Atlas ID 2
print atlas.relation(2)
```

Metadata about the `Atlas` is also available. For a quick sample, try something like:

```python
metadata = atlas.metadata()
print metadata.number_of_points
print metadata.country
```

Check out `doc/atlas.html` and `doc/atlas_metadata.html` for more information.

#### Operating on features
The `Atlas` features themselves support a set of operations defined in their respective classes.

Here is a quick example:

```python
# print the tag dict for Point with Atlas ID 3
print atlas.point(3).get_tags()

# print all Relations of which the Node with Atlas ID 1 is a member
for relation in atlas.node(1).relations():
    print relation

# print all the members of Relation with Atlas ID 1
for member in atlas.relation(1).get_members():
    # print the RelationMember object
    print member
    # print the actual AtlasEntity contained in the RelationMember
    print member.get_entity()
```

`Node`s and `Edge`s, in particular, support traversal through their connectivity API.
Here are just the basics of what you can do with the connectivity interface:

```python
# print Edges connected to Node with ID 3
for edge in atlas.node(3).in_edges():
    print edge
for edge in atlas.node(3).out_edges():
    print edge
for edge in atlas.node(3).connected_edges():
    print edge

# print the start and end Nodes of Edge 1
print atlas.edge(1).start()
print atlas.edge(1).end()
```

Many more methods are provided. See the classes in `doc/atlas_entities.html` for more information.

#### Geometry
`pyatlas` features some really simple geometry primitives for working with locations and shapes on the
surface of the Earth. Here is a simple example that uses these primitives:

```python
from pyatlas import geometry
from pyatlas.geometry import Location, PolyLine, Polygon, Rectangle

# Location constructor (lat/lon ordering) uses dm7 by default, see Location docs for info on dm7
loc1 = Location(385000000, -1160200000)
# create the same Location but with degree values instead (lat/lon ordering)
loc2 = geometry.location_with_degrees(38.5, -116.02)
print loc1.get_latitude_deg()
print loc2.get_latitude()

# create a new PolyLine with two shape points
polyline1 = PolyLine([Location(385000000, -1160200000), Location(395000000, -116300000)])
for loc in polyline1.locations():
    print loc
print polyline1.bounds()

# create a new Polygon with specified vertices
polygon1 = Polygon([geometry.location_with_degrees(0, 0),
                    geometry.location_with_degrees(10, 0),
                    geometry.location_with_degrees(5, 10)])
print polygon1
# print the vertices, will print the first again at the end to simulate closedness
for loc in polygon1.closed_loop():
    print loc
print polygon1.bounds()
# will print True, since the point lies inside the triangle
print polygon1.fully_geometrically_encloses_location(geometry.location_with_degrees(5, 5))

# create a new Rectangle with given lower left and upper right corners
rect = Rectangle(geometry.location_with_degrees(0, 0), geometry.location_with_degrees(20, 20))
print rect
# this Rectangle intersects (overlaps at any point) polygon1
print rect.intersects(polygon1)
```

See the classes in `doc/geometry.html` for more information.

#### Spatial queries
`pyatlas` supports some simple spatial queries over its feature space. The queries use the geometry
primitives provided by the `geometry` module, but convert to [Shapely](https://github.com/Toblerity/Shapely)
primitives under the hood to make queries into a native [libgeos-backed](https://github.com/OSGeo/geos) R-tree.
Below are examples for a few of the spatial queries the `Atlas` supports:

```python
from pyatlas import geometry
from pyatlas.geometry import Rectangle

# print all Points intersecting a given Polygon that also have "key1" as a tag key
lower_left = geometry.location_with_degrees(37, -118.02)
upper_right = geometry.location_with_degrees(39, -118)
for point in atlas.points_within(Rectangle(lower_left, upper_right),
                                 predicate=lambda e: 'key1' in e.get_tags().keys()):
    print point

# print all Relations with at least one member intersecting a given Polygon
lower_left = geometry.location_with_degrees(37.999, -118.001)
upper_right = geometry.location_with_degrees(38.001, -117.999)
for relation in atlas.relations_with_entities_intersecting(Rectangle(lower_left, upper_right)):
    print relation

# print all Edges that intersect a given Polygon
lower_left = geometry.location_with_degrees(38, -120)
upper_right = geometry.location_with_degrees(40, -117)
for edge in atlas.edges_intersecting(Rectangle(lower_left, upper_right)):
    print edge
```

See `doc/atlas.html` for more information on the available spatial queries.

