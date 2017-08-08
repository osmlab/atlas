# Boundary

This package contains all the classes that help with defining administrative boundaries, and associating them with three digit country codes.

## Grid Index

The grid index is an optimization for efficiently identifying which country a feature belongs to. The basic concept is to first construct a Quad Tree. The quad tree is a spatial index, which allows you to query if something is contained in a 2D rectangle. In our case, we have two ways of building the Quad Tree - see `DynamicGridIndexBuilder` and `FixedGridIndexBuilder` section below. Usually, we go with the `DynamicGridIndexBuilder` approach and build the quad tree where each cell in the tree will only contain features from a single country. Because of this property, the border between countries will have many small cells, whereas the inner portion of a country will have less cells and they will typically be larger rectangles. This principle generally holds true, unless the country is really small or has many boundary enclaves. Once the Quad Tree is constructed, the individual Quad Tree cells are stored into a R-Tree. The R-Tree is a spatial index used to query two-dimensional spatial data (the rectangles making up our Quad Tree). This gives us the advantage of quickly, O(log N), looking up the exact rectangle a feature is contained/intersects and identifying which country/region it belongs to. Both the R-Tree and Quad Tree are backed by the JTS implementation.

## Implementation Details

### `CountryBoundaryMap`

Loads boundaries from an input country boundary shape file/text file/resource into a spatial index. Supports clipping, slicing and `Node` and `Way` queries.

### `CountryBoundaryMapArchiver`

Creates the country boundary text file given some shape file and/or boundary.

### `DynamicGridIndexBuilder` and `FixedGridIndexBuilder`

Builders that create a spatial index using the JTS R-tree. The fixed builder will use an equally divided grid index, whereas the dynamic one will use a 2D K-D Tree to generate variable size cells. Smaller cells will be generated at the boundaries to improve search performance and larger cells will be found near the center of a country, where there is less chance to overlap with a neighboring country.
