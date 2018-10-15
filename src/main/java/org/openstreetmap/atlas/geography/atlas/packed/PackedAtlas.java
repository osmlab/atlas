package org.openstreetmap.atlas.geography.atlas.packed;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.AbstractAtlas;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.exception.AtlasIntegrityException;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.arrays.ByteArrayOfArrays;
import org.openstreetmap.atlas.utilities.arrays.IntegerArrayOfArrays;
import org.openstreetmap.atlas.utilities.arrays.LongArray;
import org.openstreetmap.atlas.utilities.arrays.LongArrayOfArrays;
import org.openstreetmap.atlas.utilities.arrays.PolyLineArray;
import org.openstreetmap.atlas.utilities.arrays.PolygonArray;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.compression.IntegerDictionary;
import org.openstreetmap.atlas.utilities.maps.LongToLongMap;
import org.openstreetmap.atlas.utilities.maps.LongToLongMultiMap;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Atlas that packs data in large arrays
 *
 * @author matthieun
 */
public final class PackedAtlas extends AbstractAtlas
{
    /**
     * Serialization format settings for an {@link Atlas}. While the serialization interface for
     * saving is well-defined by the {@link Atlas}, the actual serialization mechanics - as well as
     * the interface for loading - are left up to the discretion of the implementing {@link Atlas}
     * subclass.
     *
     * @author lcram
     */
    public enum AtlasSerializationFormat
    {
        JAVA,
        PROTOBUF,
    }

    private static final long serialVersionUID = -7582554057580336684L;
    private static final Logger logger = LoggerFactory.getLogger(PackedAtlas.class);

    // Keep track of the field names for reflection code in the Serializer.
    protected static final String FIELD_PREFIX = "FIELD_";
    protected static final String FIELD_BOUNDS = "bounds";
    protected static final String FIELD_LOGGER = "logger";
    protected static final String FIELD_SERIAL_VERSION_UID = "serialVersionUID";
    protected static final String FIELD_SERIALIZER = "serializer";
    protected static final String FIELD_SAVE_SERIALIZATION_FORMAT = "saveSerializationFormat";
    protected static final String FIELD_LOAD_SERIALIZATION_FORMAT = "loadSerializationFormat";
    protected static final String FIELD_META_DATA = "metaData";
    protected static final String FIELD_DICTIONARY = "dictionary";
    protected static final String FIELD_EDGE_IDENTIFIERS = "edgeIdentifiers";
    protected static final String FIELD_NODE_IDENTIFIERS = "nodeIdentifiers";
    protected static final String FIELD_AREA_IDENTIFIERS = "areaIdentifiers";
    protected static final String FIELD_LINE_IDENTIFIERS = "lineIdentifiers";
    protected static final String FIELD_POINT_IDENTIFIERS = "pointIdentifiers";
    protected static final String FIELD_RELATION_IDENTIFIERS = "relationIdentifiers";
    protected static final String FIELD_EDGE_IDENTIFIER_TO_EDGE_ARRAY_INDEX = "edgeIdentifierToEdgeArrayIndex";
    protected static final String FIELD_NODE_IDENTIFIER_TO_NODE_ARRAY_INDEX = "nodeIdentifierToNodeArrayIndex";
    protected static final String FIELD_AREA_IDENTIFIER_TO_AREA_ARRAY_INDEX = "areaIdentifierToAreaArrayIndex";
    protected static final String FIELD_LINE_IDENTIFIER_TO_LINE_ARRAY_INDEX = "lineIdentifierToLineArrayIndex";
    protected static final String FIELD_POINT_IDENTIFIER_TO_POINT_ARRAY_INDEX = "pointIdentifierToPointArrayIndex";
    protected static final String FIELD_RELATION_IDENTIFIER_TO_RELATION_ARRAY_INDEX = "relationIdentifierToRelationArrayIndex";
    protected static final String FIELD_NODE_LOCATIONS = "nodeLocations";
    protected static final String FIELD_NODE_IN_EDGES_INDICES = "nodeInEdgesIndices";
    protected static final String FIELD_NODE_OUT_EDGES_INDICES = "nodeOutEdgesIndices";
    protected static final String FIELD_NODE_TAGS = "nodeTags";
    protected static final String FIELD_NODE_INDEX_TO_RELATION_INDICES = "nodeIndexToRelationIndices";
    protected static final String FIELD_EDGE_START_NODE_INDEX = "edgeStartNodeIndex";
    protected static final String FIELD_EDGE_END_NODE_INDEX = "edgeEndNodeIndex";
    protected static final String FIELD_EDGE_POLY_LINES = "edgePolyLines";
    protected static final String FIELD_EDGE_TAGS = "edgeTags";
    protected static final String FIELD_EDGE_INDEX_TO_RELATION_INDICES = "edgeIndexToRelationIndices";
    protected static final String FIELD_AREA_POLYGONS = "areaPolygons";
    protected static final String FIELD_AREA_TAGS = "areaTags";
    protected static final String FIELD_AREA_INDEX_TO_RELATION_INDICES = "areaIndexToRelationIndices";
    protected static final String FIELD_LINE_POLYLINES = "linePolyLines";
    protected static final String FIELD_LINE_TAGS = "lineTags";
    protected static final String FIELD_LINE_INDEX_TO_RELATION_INDICES = "lineIndexToRelationIndices";
    protected static final String FIELD_POINT_LOCATIONS = "pointLocations";
    protected static final String FIELD_POINT_TAGS = "pointTags";
    protected static final String FIELD_POINT_INDEX_TO_RELATION_INDICES = "pointIndexToRelationIndices";
    protected static final String FIELD_RELATION_MEMBERS_INDICES = "relationMemberIndices";
    protected static final String FIELD_RELATION_MEMBER_TYPES = "relationMemberTypes";
    protected static final String FIELD_RELATION_MEMBER_ROLES = "relationMemberRoles";
    protected static final String FIELD_RELATION_TAGS = "relationTags";
    protected static final String FIELD_RELATION_INDEX_TO_RELATION_INDICES = "relationIndexToRelationIndices";
    protected static final String FIELD_RELATION_OSM_IDENTIFIER_TO_RELATION_IDENTIFIERS = "relationOsmIdentifierToRelationIdentifiers";
    protected static final String FIELD_RELATION_OSM_IDENTIFIERS = "relationOsmIdentifiers";

    // Serializer.
    private transient PackedAtlasSerializer serializer;

    // Serialization formats for saving/loading this PackedAtlas
    private AtlasSerializationFormat saveSerializationFormat = AtlasSerializationFormat.JAVA;
    private AtlasSerializationFormat loadSerializationFormat = AtlasSerializationFormat.JAVA;

    // Meta-Data
    private AtlasMetaData metaData = new AtlasMetaData();

    // Dictionary
    private final IntegerDictionary<String> dictionary;

    // The OSM (and way-sectioned) edge and node indices
    private final LongArray edgeIdentifiers;
    private final LongArray nodeIdentifiers;
    private final LongArray areaIdentifiers;
    private final LongArray lineIdentifiers;
    private final LongArray pointIdentifiers;
    private final LongArray relationIdentifiers;

    // The maps from edge index to index in the arrays above, and in the attributes
    private final LongToLongMap edgeIdentifierToEdgeArrayIndex;
    private final LongToLongMap nodeIdentifierToNodeArrayIndex;
    private final LongToLongMap areaIdentifierToAreaArrayIndex;
    private final LongToLongMap lineIdentifierToLineArrayIndex;
    private final LongToLongMap pointIdentifierToPointArrayIndex;
    private final LongToLongMap relationIdentifierToRelationArrayIndex;

    // Node attributes
    private final LongArray nodeLocations;
    private final LongArrayOfArrays nodeInEdgesIndices;
    private final LongArrayOfArrays nodeOutEdgesIndices;
    private final PackedTagStore nodeTags;
    private final LongToLongMultiMap nodeIndexToRelationIndices;

    // Edge attributes
    private final LongArray edgeStartNodeIndex;
    private final LongArray edgeEndNodeIndex;
    private final PolyLineArray edgePolyLines;
    private final PackedTagStore edgeTags;
    private final LongToLongMultiMap edgeIndexToRelationIndices;

    // Areas attributes
    private final PolygonArray areaPolygons;
    private final PackedTagStore areaTags;
    private final LongToLongMultiMap areaIndexToRelationIndices;

    // Line attributes
    private final PolyLineArray linePolyLines;
    private final PackedTagStore lineTags;
    private final LongToLongMultiMap lineIndexToRelationIndices;

    // Point attributes
    private final LongArray pointLocations;
    private final PackedTagStore pointTags;
    private final LongToLongMultiMap pointIndexToRelationIndices;

    // Relation attributes
    private final LongArrayOfArrays relationMemberIndices;
    private final ByteArrayOfArrays relationMemberTypes;
    private final IntegerArrayOfArrays relationMemberRoles;
    private final PackedTagStore relationTags;
    private final LongToLongMultiMap relationIndexToRelationIndices;
    private final LongToLongMultiMap relationOsmIdentifierToRelationIdentifiers;
    private final LongArray relationOsmIdentifiers;

    // Bounds of the Atlas
    private Rectangle bounds;

    /**
     * Clone an {@link Atlas} into a {@link PackedAtlas}
     *
     * @param other
     *            The {@link Atlas} to clone
     * @return The cloned {@link PackedAtlas}
     */
    public static PackedAtlas cloneFrom(final Atlas other)
    {
        return new PackedAtlasCloner().cloneFrom(other);
    }

    /**
     * Load a {@link PackedAtlas} from a zip entry resource
     *
     * @param resource
     *            The {@link Resource} to read from
     * @return The deserialized {@link PackedAtlas}
     */
    public static PackedAtlas load(final Resource resource)
    {
        final PackedAtlas result = PackedAtlasSerializer.load(resource);
        result.setName(resource.getName());
        return result;
    }

    /**
     * This constructor is used only by the serializer.
     */
    protected PackedAtlas()
    {
        this.metaData = null;
        this.dictionary = null;
        this.edgeIdentifiers = null;
        this.nodeIdentifiers = null;
        this.areaIdentifiers = null;
        this.lineIdentifiers = null;
        this.pointIdentifiers = null;
        this.relationIdentifiers = null;
        this.edgeIdentifierToEdgeArrayIndex = null;
        this.nodeIdentifierToNodeArrayIndex = null;
        this.areaIdentifierToAreaArrayIndex = null;
        this.lineIdentifierToLineArrayIndex = null;
        this.pointIdentifierToPointArrayIndex = null;
        this.relationIdentifierToRelationArrayIndex = null;
        this.nodeLocations = null;
        this.nodeInEdgesIndices = null;
        this.nodeOutEdgesIndices = null;
        this.nodeTags = null;
        this.nodeIndexToRelationIndices = null;
        this.edgeStartNodeIndex = null;
        this.edgeEndNodeIndex = null;
        this.edgePolyLines = null;
        this.edgeTags = null;
        this.edgeIndexToRelationIndices = null;
        this.areaPolygons = null;
        this.areaTags = null;
        this.areaIndexToRelationIndices = null;
        this.linePolyLines = null;
        this.lineTags = null;
        this.lineIndexToRelationIndices = null;
        this.pointLocations = null;
        this.pointTags = null;
        this.pointIndexToRelationIndices = null;
        this.relationMemberIndices = null;
        this.relationMemberTypes = null;
        this.relationMemberRoles = null;
        this.relationTags = null;
        this.relationIndexToRelationIndices = null;
        this.relationOsmIdentifierToRelationIdentifiers = null;
        this.relationOsmIdentifiers = null;
    }

    /**
     * Construct an Atlas
     *
     * @param estimates
     *            The size estimates
     */
    protected PackedAtlas(final AtlasSize estimates)
    {
        final long edgeNumberEstimate = estimates.getEdgeNumber();
        final long nodeNumberEstimate = estimates.getNodeNumber();
        final long areaNumberEstimate = estimates.getAreaNumber();
        final long lineNumberEstimate = estimates.getLineNumber();
        final long pointNumberEstimate = estimates.getPointNumber();
        final long relationNumberEstimate = estimates.getRelationNumber();

        final int subArraySize = Integer.MAX_VALUE;
        final long maximumSize = Long.MAX_VALUE;

        final int edgeMemoryBlockSize = (int) Math.max(DEFAULT_NUMBER_OF_ITEMS,
                edgeNumberEstimate % Integer.MAX_VALUE);
        final int nodeMemoryBlockSize = (int) Math.max(DEFAULT_NUMBER_OF_ITEMS,
                nodeNumberEstimate % Integer.MAX_VALUE);
        final int areaMemoryBlockSize = (int) Math.max(DEFAULT_NUMBER_OF_ITEMS,
                areaNumberEstimate % Integer.MAX_VALUE);
        final int lineMemoryBlockSize = (int) Math.max(DEFAULT_NUMBER_OF_ITEMS,
                lineNumberEstimate % Integer.MAX_VALUE);
        final int pointMemoryBlockSize = (int) Math.max(DEFAULT_NUMBER_OF_ITEMS,
                pointNumberEstimate % Integer.MAX_VALUE);
        final int relationMemoryBlockSize = (int) Math.max(DEFAULT_NUMBER_OF_ITEMS,
                relationNumberEstimate % Integer.MAX_VALUE);

        final int edgeHashSize = (int) Math
                .max(Math.min(edgeNumberEstimate / HASH_MODULO_RATIO, Integer.MAX_VALUE), 1);
        final int nodeHashSize = (int) Math
                .max(Math.min(nodeNumberEstimate / HASH_MODULO_RATIO, Integer.MAX_VALUE), 1);
        final int areaHashSize = (int) Math
                .max(Math.min(areaNumberEstimate / HASH_MODULO_RATIO, Integer.MAX_VALUE), 1);
        final int lineHashSize = (int) Math
                .max(Math.min(lineNumberEstimate / HASH_MODULO_RATIO, Integer.MAX_VALUE), 1);
        final int pointHashSize = (int) Math
                .max(Math.min(pointNumberEstimate / HASH_MODULO_RATIO, Integer.MAX_VALUE), 1);
        final int relationHashSize = (int) Math
                .max(Math.min(relationNumberEstimate / HASH_MODULO_RATIO, Integer.MAX_VALUE), 1);

        this.dictionary = new IntegerDictionary<>();

        this.edgeIdentifiers = new LongArray(maximumSize, edgeMemoryBlockSize, subArraySize);
        this.nodeIdentifiers = new LongArray(maximumSize, nodeMemoryBlockSize, subArraySize);
        this.areaIdentifiers = new LongArray(maximumSize, areaMemoryBlockSize, subArraySize);
        this.lineIdentifiers = new LongArray(maximumSize, lineMemoryBlockSize, subArraySize);
        this.pointIdentifiers = new LongArray(maximumSize, pointMemoryBlockSize, subArraySize);
        this.relationIdentifiers = new LongArray(maximumSize, relationMemoryBlockSize,
                subArraySize);

        this.edgeIdentifierToEdgeArrayIndex = new LongToLongMap(
                "PackedAtlas - edgeIdentifierToEdgeArrayIndex", maximumSize, edgeHashSize,
                edgeMemoryBlockSize, subArraySize, edgeMemoryBlockSize, subArraySize);
        this.nodeIdentifierToNodeArrayIndex = new LongToLongMap(
                "PackedAtlas - nodeIdentifierToNodeArrayIndex", maximumSize, nodeHashSize,
                nodeMemoryBlockSize, subArraySize, nodeMemoryBlockSize, subArraySize);
        this.areaIdentifierToAreaArrayIndex = new LongToLongMap(
                "PackedAtlas - areaIdentifierToAreaArrayIndex", maximumSize, areaHashSize,
                areaMemoryBlockSize, subArraySize, areaMemoryBlockSize, subArraySize);
        this.lineIdentifierToLineArrayIndex = new LongToLongMap(
                "PackedAtlas - lineIdentifierToLineArrayIndex", maximumSize, lineHashSize,
                lineMemoryBlockSize, subArraySize, lineMemoryBlockSize, subArraySize);
        this.pointIdentifierToPointArrayIndex = new LongToLongMap(
                "PackedAtlas - pointIdentifierToPointArrayIndex", maximumSize, pointHashSize,
                pointMemoryBlockSize, subArraySize, pointMemoryBlockSize, subArraySize);
        this.relationIdentifierToRelationArrayIndex = new LongToLongMap(
                "PackedAtlas - relationIdentifierToRelationArrayIndex", maximumSize,
                relationHashSize, relationMemoryBlockSize, subArraySize, relationMemoryBlockSize,
                subArraySize);

        this.nodeInEdgesIndices = new LongArrayOfArrays(subArraySize, nodeMemoryBlockSize,
                subArraySize);
        this.nodeOutEdgesIndices = new LongArrayOfArrays(subArraySize, nodeMemoryBlockSize,
                subArraySize);
        this.nodeLocations = new LongArray(maximumSize, nodeMemoryBlockSize, subArraySize);
        this.nodeTags = new PackedTagStore(maximumSize, nodeMemoryBlockSize, subArraySize,
                dictionary());
        this.nodeIndexToRelationIndices = new LongToLongMultiMap(
                "PackedAtlas - nodeIndexToRelationIndices", maximumSize, nodeHashSize,
                nodeMemoryBlockSize, subArraySize, nodeMemoryBlockSize, nodeHashSize);

        this.edgeStartNodeIndex = new LongArray(maximumSize, edgeMemoryBlockSize, subArraySize);
        this.edgeEndNodeIndex = new LongArray(maximumSize, edgeMemoryBlockSize, subArraySize);
        this.edgePolyLines = new PolyLineArray(maximumSize, edgeMemoryBlockSize, subArraySize);
        this.edgeTags = new PackedTagStore(maximumSize, edgeMemoryBlockSize, subArraySize,
                dictionary());
        this.edgeIndexToRelationIndices = new LongToLongMultiMap(
                "PackedAtlas - edgeIndexToRelationIndices", maximumSize, edgeHashSize,
                edgeMemoryBlockSize, subArraySize, edgeMemoryBlockSize, edgeHashSize);

        this.areaPolygons = new PolygonArray(maximumSize, areaMemoryBlockSize, subArraySize);
        this.areaTags = new PackedTagStore(maximumSize, areaMemoryBlockSize, subArraySize,
                dictionary());
        this.areaIndexToRelationIndices = new LongToLongMultiMap(
                "PackedAtlas - areaIndexToRelationIndices", maximumSize, areaHashSize,
                areaMemoryBlockSize, subArraySize, areaMemoryBlockSize, areaHashSize);

        this.linePolyLines = new PolyLineArray(maximumSize, lineMemoryBlockSize, subArraySize);
        this.lineTags = new PackedTagStore(maximumSize, lineMemoryBlockSize, subArraySize,
                dictionary());
        this.lineIndexToRelationIndices = new LongToLongMultiMap(
                "PackedAtlas - lineIndexToRelationIndices", maximumSize, lineHashSize,
                lineMemoryBlockSize, subArraySize, lineMemoryBlockSize, lineHashSize);

        this.pointLocations = new LongArray(maximumSize, pointMemoryBlockSize, subArraySize);
        this.pointTags = new PackedTagStore(maximumSize, pointMemoryBlockSize, subArraySize,
                dictionary());
        this.pointIndexToRelationIndices = new LongToLongMultiMap(
                "PackedAtlas - pointIndexToRelationIndices", maximumSize, pointHashSize,
                pointMemoryBlockSize, subArraySize, pointMemoryBlockSize, pointHashSize);

        this.relationMemberIndices = new LongArrayOfArrays(maximumSize, relationMemoryBlockSize,
                subArraySize);
        this.relationMemberTypes = new ByteArrayOfArrays(maximumSize, relationMemoryBlockSize,
                subArraySize);
        this.relationMemberRoles = new IntegerArrayOfArrays(maximumSize, relationMemoryBlockSize,
                subArraySize);
        this.relationTags = new PackedTagStore(maximumSize, relationMemoryBlockSize, subArraySize,
                dictionary());
        this.relationIndexToRelationIndices = new LongToLongMultiMap(
                "PackedAtlas - relationIndexToRelationIndices", maximumSize, relationHashSize,
                relationMemoryBlockSize, subArraySize, relationMemoryBlockSize, relationHashSize);
        this.relationOsmIdentifierToRelationIdentifiers = new LongToLongMultiMap(
                "PackedAtlas - relationOsmIdentifierToRelationIdentifier", maximumSize,
                relationHashSize, relationMemoryBlockSize, subArraySize, relationMemoryBlockSize,
                subArraySize);
        this.relationOsmIdentifiers = new LongArray(maximumSize, relationMemoryBlockSize,
                subArraySize);

        this.edgeIdentifiers.setName("PackedAtlas - edgeIdentifiers");
        this.edgeStartNodeIndex.setName("PackedAtlas - edgeStartNodeIndex");
        this.edgeEndNodeIndex.setName("PackedAtlas - edgeEndNodeIndex");
        this.edgePolyLines.setName("PackedAtlas - edgePolyLines");

        this.nodeIdentifiers.setName("PackedAtlas - nodeIdentifiers");
        this.nodeInEdgesIndices.setName("PackedAtlas - nodeInEdgesIndices");
        this.nodeOutEdgesIndices.setName("PackedAtlas - nodeOutEdgesIndices");
        this.nodeLocations.setName("PackedAtlas - nodeLocations");

        this.areaIdentifiers.setName("PackedAtlas - areaIdentifiers");
        this.areaPolygons.setName("PackedAtlas - areaPolygons");

        this.lineIdentifiers.setName("PackedAtlas - lineIdentifiers");
        this.linePolyLines.setName("PackedAtlas - linePolyLines");

        this.pointIdentifiers.setName("PackedAtlas - pointIdentifiers");
        this.pointLocations.setName("PackedAtlas - pointLocations");

        this.relationIdentifiers.setName("PackedAtlas - relationIdentifiers");
        this.relationMemberIndices.setName("PackedAtlas - relationMemberIndices");
        this.relationMemberTypes.setName("PackedAtlas - relationMemberTypes");
        this.relationMemberRoles.setName("PackedAtlas - relationMemberRoles");
        this.relationOsmIdentifiers.setName("PackedAtlas - relationOsmIdentifiers");
    }

    @Override
    public Area area(final long identifier)
    {
        if (this.areaIdentifierToAreaArrayIndex().containsKey(identifier))
        {
            return new PackedArea(this, this.areaIdentifierToAreaArrayIndex().get(identifier));
        }
        return null;
    }

    @Override
    public Iterable<Area> areas()
    {
        return () -> new Iterator<Area>()
        {
            private long index = 0L;

            @Override
            public boolean hasNext()
            {
                return this.index < PackedAtlas.this.areaIdentifiers().size();
            }

            @Override
            public Area next()
            {
                return new PackedArea(PackedAtlas.this, this.index++);
            }
        };
    }

    @Override
    public Rectangle bounds()
    {
        if (this.bounds == null)
        {
            final Iterable<AtlasEntity> boundedEntities = Iterables.filter(this,
                    entity -> entity.bounds() != null);
            this.bounds = Rectangle.forLocated(boundedEntities);
        }
        return this.bounds;
    }

    @Override
    public Edge edge(final long identifier)
    {
        if (this.edgeIdentifierToEdgeArrayIndex().containsKey(identifier))
        {
            return new PackedEdge(this, this.edgeIdentifierToEdgeArrayIndex().get(identifier));
        }
        return null;
    }

    @Override
    public Iterable<Edge> edges()
    {
        return () -> new Iterator<Edge>()
        {
            private long index = 0L;

            @Override
            public boolean hasNext()
            {
                return this.index < PackedAtlas.this.edgeIdentifiers().size();
            }

            @Override
            public Edge next()
            {
                return new PackedEdge(PackedAtlas.this, this.index++);
            }
        };
    }

    /**
     * Get the serialization format used for saving this {@link PackedAtlas}. By default use Java
     * serialization.
     *
     * @return The serialization format setting
     */
    public AtlasSerializationFormat getSaveSerializationFormat()
    {
        return this.saveSerializationFormat;
    }

    @Override
    public Line line(final long identifier)
    {
        if (this.lineIdentifierToLineArrayIndex().containsKey(identifier))
        {
            return new PackedLine(this, this.lineIdentifierToLineArrayIndex().get(identifier));
        }
        return null;
    }

    @Override
    public Iterable<Line> lines()
    {
        return () -> new Iterator<Line>()
        {
            private long index = 0L;

            @Override
            public boolean hasNext()
            {
                return this.index < PackedAtlas.this.lineIdentifiers().size();
            }

            @Override
            public Line next()
            {
                return new PackedLine(PackedAtlas.this, this.index++);
            }
        };
    }

    @Override
    public AtlasMetaData metaData()
    {
        if (this.metaData == null)
        {
            this.serializer.deserializeIfNeeded(FIELD_META_DATA);
        }
        return this.metaData;
    }

    @Override
    public Node node(final long identifier)
    {
        if (this.nodeIdentifierToNodeArrayIndex().containsKey(identifier))
        {
            return new PackedNode(this, this.nodeIdentifierToNodeArrayIndex().get(identifier));
        }
        return null;
    }

    @Override
    public Iterable<Node> nodes()
    {
        return () -> new Iterator<Node>()
        {
            private long index = 0L;

            @Override
            public boolean hasNext()
            {
                return this.index < PackedAtlas.this.nodeIdentifiers().size();
            }

            @Override
            public Node next()
            {
                return new PackedNode(PackedAtlas.this, this.index++);
            }
        };
    }

    @Override
    public long numberOfAreas()
    {
        return this.areaIdentifiers().size();
    }

    @Override
    public long numberOfEdges()
    {
        return this.edgeIdentifiers().size();
    }

    @Override
    public long numberOfLines()
    {
        return this.lineIdentifiers().size();
    }

    @Override
    public long numberOfNodes()
    {
        return this.nodeIdentifiers().size();
    }

    @Override
    public long numberOfPoints()
    {
        return this.pointIdentifiers().size();
    }

    @Override
    public long numberOfRelations()
    {
        return this.relationIdentifiers().size();
    }

    @Override
    public Point point(final long identifier)
    {
        if (this.pointIdentifierToPointArrayIndex().containsKey(identifier))
        {
            return new PackedPoint(this, this.pointIdentifierToPointArrayIndex().get(identifier));
        }
        return null;
    }

    @Override
    public Iterable<Point> points()
    {
        return () -> new Iterator<Point>()
        {
            private long index = 0L;

            @Override
            public boolean hasNext()
            {
                return this.index < PackedAtlas.this.pointIdentifiers().size();
            }

            @Override
            public Point next()
            {
                return new PackedPoint(PackedAtlas.this, this.index++);
            }
        };
    }

    @Override
    public Relation relation(final long identifier)
    {
        if (this.relationIdentifierToRelationArrayIndex().containsKey(identifier))
        {
            return new PackedRelation(this,
                    this.relationIdentifierToRelationArrayIndex().get(identifier));
        }
        return null;
    }

    @Override
    public Iterable<Relation> relations()
    {
        return () -> new Iterator<Relation>()
        {
            private long index = 0L;

            @Override
            public boolean hasNext()
            {
                return this.index < PackedAtlas.this.relationIdentifiers().size();
            }

            @Override
            public Relation next()
            {
                return new PackedRelation(PackedAtlas.this, this.index++);
            }
        };
    }

    @Override
    public void save(final WritableResource writableResource)
    {
        new PackedAtlasSerializer(this, writableResource).save();
    }

    /**
     * Set the serialization format for saving this {@link PackedAtlas}.
     *
     * @param format
     *            The format to use
     */
    public void setSaveSerializationFormat(final AtlasSerializationFormat format)
    {
        this.saveSerializationFormat = format;
    }

    /**
     * Trim this Atlas' arrays with the proper size. WARNING! This could potentially temporarily
     * double the amount of memory used by each array.
     */
    public void trim()
    {
        logger.info("Trimming Atlas {} to save on space.", this.getName());
        final Time start = Time.now();

        this.edgeIdentifiers.trim();
        this.nodeIdentifiers.trim();
        this.areaIdentifiers.trim();
        this.lineIdentifiers.trim();
        this.pointIdentifiers.trim();
        this.relationIdentifiers.trim();

        this.edgeIdentifierToEdgeArrayIndex.trim();
        this.nodeIdentifierToNodeArrayIndex.trim();
        this.areaIdentifierToAreaArrayIndex.trim();
        this.lineIdentifierToLineArrayIndex.trim();
        this.pointIdentifierToPointArrayIndex.trim();
        this.relationIdentifierToRelationArrayIndex.trim();

        this.nodeLocations.trim();
        this.nodeInEdgesIndices.trim();
        this.nodeOutEdgesIndices.trim();
        this.nodeTags.trim();
        this.nodeIndexToRelationIndices.trim();

        this.edgeStartNodeIndex.trim();
        this.edgeEndNodeIndex.trim();
        this.edgePolyLines.trim();
        this.edgeTags.trim();
        this.edgeIndexToRelationIndices.trim();

        this.areaPolygons.trim();
        this.areaTags.trim();
        this.areaIndexToRelationIndices.trim();

        this.linePolyLines.trim();
        this.lineTags.trim();
        this.lineIndexToRelationIndices.trim();

        this.pointLocations.trim();
        this.pointTags.trim();
        this.pointIndexToRelationIndices.trim();

        this.relationMemberIndices.trim();
        this.relationMemberTypes.trim();
        this.relationMemberRoles.trim();
        this.relationTags.trim();
        this.relationIndexToRelationIndices.trim();
        this.relationOsmIdentifierToRelationIdentifiers.trim();
        this.relationOsmIdentifiers.trim();

        logger.info("Trimmed Atlas {} in {}.", this.getName(), start.elapsedSince());
    }

    protected void addArea(final long areaIdentifier, final Polygon polygon,
            final Map<String, String> tags)
    {
        synchronized (this.areaIdentifiers)
        {
            if (this.areaIdentifierToAreaArrayIndex.containsKey(areaIdentifier))
            {
                throw new AtlasIntegrityException(
                        "Area with identifier " + areaIdentifier + " already exists.");
            }
            final long index = this.areaIdentifiers.size();
            this.areaIdentifiers.add(areaIdentifier);
            this.areaIdentifierToAreaArrayIndex.put(areaIdentifier, index);

            this.areaPolygons.add(polygon);

            this.getAsNewAreaSpatialIndex().add(new PackedArea(this, index));

            // Tags
            for (final String key : tags.keySet())
            {
                final String value = tags.get(key);
                this.areaTags.add(index, key, value);
            }

            if (tags.keySet().isEmpty())
            {
                this.areaTags.add(index, null, null);
            }
        }
    }

    protected void addEdge(final long edgeIdentifier, final long startNodeIdentifier,
            final long endNodeIdentifier, final PolyLine polyline, final Map<String, String> tags)
    {
        synchronized (this.edgeIdentifiers)
        {
            if (this.edgeIdentifierToEdgeArrayIndex.containsKey(edgeIdentifier))
            {
                throw new AtlasIntegrityException(
                        "Edge with identifier " + edgeIdentifier + " already exists.");
            }
            final long index = this.edgeIdentifiers.size();
            this.edgeIdentifiers.add(edgeIdentifier);
            this.edgeIdentifierToEdgeArrayIndex.put(edgeIdentifier, index);

            this.edgePolyLines.add(polyline);

            // Start Node
            final long startNodeIndex = this.nodeIdentifierToNodeArrayIndex
                    .get(startNodeIdentifier);
            this.edgeStartNodeIndex.add(startNodeIndex);

            // End Node
            final long endNodeIndex = this.nodeIdentifierToNodeArrayIndex.get(endNodeIdentifier);
            this.edgeEndNodeIndex.add(endNodeIndex);

            // Node In edges
            updateNodeEdgesReference(endNodeIndex, this.nodeInEdgesIndices, index);

            // Node Out edges
            updateNodeEdgesReference(startNodeIndex, this.nodeOutEdgesIndices, index);

            // Spatial Index
            this.getAsNewEdgeSpatialIndex().add(new PackedEdge(this, index));

            // Tags
            for (final String key : tags.keySet())
            {
                final String value = tags.get(key);
                this.edgeTags.add(index, key, value);
            }

            if (tags.keySet().isEmpty())
            {
                this.edgeTags.add(index, null, null);
            }
        }
    }

    protected void addLine(final long lineIdentifier, final PolyLine polyline,
            final Map<String, String> tags)
    {
        synchronized (this.lineIdentifiers)
        {
            if (this.lineIdentifierToLineArrayIndex.containsKey(lineIdentifier))
            {
                throw new AtlasIntegrityException(
                        "Line with identifier " + lineIdentifier + " already exists.");
            }
            final long index = this.lineIdentifiers.size();
            this.lineIdentifiers.add(lineIdentifier);
            this.lineIdentifierToLineArrayIndex.put(lineIdentifier, index);

            this.linePolyLines.add(polyline);

            this.getAsNewLineSpatialIndex().add(new PackedLine(this, index));

            // Tags
            for (final String key : tags.keySet())
            {
                final String value = tags.get(key);
                this.lineTags.add(index, key, value);
            }

            if (tags.keySet().isEmpty())
            {
                this.lineTags.add(index, null, null);
            }
        }
    }

    protected void addNode(final long nodeIdentifier, final Location location,
            final Map<String, String> tags)
    {
        synchronized (this.nodeIdentifiers)
        {
            if (this.nodeIdentifierToNodeArrayIndex.containsKey(nodeIdentifier))
            {
                throw new AtlasIntegrityException(
                        "Node with identifier " + nodeIdentifier + " already exists.");
            }
            final long index = this.nodeIdentifiers.size();
            this.nodeIdentifiers.add(nodeIdentifier);
            this.nodeIdentifierToNodeArrayIndex.put(nodeIdentifier, index);

            this.nodeLocations.add(location.asConcatenation());

            // Fill the in/out edges arrays for later
            this.nodeInEdgesIndices.add(new long[0]);
            this.nodeOutEdgesIndices.add(new long[0]);

            this.getAsNewNodeSpatialIndex().add(new PackedNode(this, index));

            // Tags
            for (final String key : tags.keySet())
            {
                final String value = tags.get(key);
                this.nodeTags.add(index, key, value);
            }

            if (tags.keySet().isEmpty())
            {
                this.nodeTags.add(index, null, null);
            }
        }
    }

    protected void addPoint(final long pointIdentifier, final Location location,
            final Map<String, String> tags)
    {
        synchronized (this.pointIdentifiers)
        {
            if (this.pointIdentifierToPointArrayIndex.containsKey(pointIdentifier))
            {
                throw new AtlasIntegrityException(
                        "Point with identifier " + pointIdentifier + " already exists.");
            }
            final long index = this.pointIdentifiers.size();
            this.pointIdentifiers.add(pointIdentifier);
            this.pointIdentifierToPointArrayIndex.put(pointIdentifier, index);

            this.pointLocations.add(location.asConcatenation());

            this.getAsNewPointSpatialIndex().add(new PackedPoint(this, index));

            // Tags
            for (final String key : tags.keySet())
            {
                final String value = tags.get(key);
                this.pointTags.add(index, key, value);
            }

            if (tags.keySet().isEmpty())
            {
                this.pointTags.add(index, null, null);
            }
        }
    }

    /**
     * Add a relation to the {@link PackedAtlas}. WARNING: This method will throw
     * {@link AtlasIntegrityException}s in the following cases:
     * <ul>
     * <li>The identifiers list, types list and roles list do not have all the same length
     * <li>The identifiers list, types list and roles list are empty
     * <li>Some member identifiers are null
     * </ul>
     *
     * @param relationIdentifier
     *            The identifier of the relation
     * @param relationOsmIdentifier
     *            The original OSM identifier of the relation. Can be the same as the identifier.
     * @param identifiers
     *            The member identifiers.
     * @param types
     *            The member types in the same order as the member identifiers
     * @param roles
     *            The member roles in the same order as the member identifiers
     * @param tags
     *            The relation's tags
     */
    protected void addRelation(final long relationIdentifier, final long relationOsmIdentifier,
            final List<Long> identifiers, final List<ItemType> types, final List<String> roles,
            final Map<String, String> tags)
    {
        if (identifiers.size() != types.size() || types.size() != roles.size())
        {
            throw new AtlasIntegrityException(
                    "Different sizes for relation identifiers and types and roles.");
        }
        if (identifiers.isEmpty())
        {
            throw new AtlasIntegrityException("Cannot add the relation {} with no members",
                    relationIdentifier);
        }
        // Do not allow relations with some null members.
        if (identifiers.stream().anyMatch(Objects::isNull))
        {
            throw new AtlasIntegrityException("Cannot have a relation with null members.");
        }
        synchronized (this.relationIdentifiers)
        {
            if (this.relationIdentifierToRelationArrayIndex.containsKey(relationIdentifier))
            {
                throw new AtlasIntegrityException(
                        "Relation with identifier " + relationIdentifier + " already exists.");
            }

            final long index = this.relationIdentifiers.size();
            this.relationIdentifiers.add(relationIdentifier);
            this.relationIdentifierToRelationArrayIndex.put(relationIdentifier, index);
            this.relationOsmIdentifierToRelationIdentifiers.add(relationOsmIdentifier,
                    relationIdentifier);
            this.relationOsmIdentifiers.add(relationOsmIdentifier);

            final long[] memberIndices = new long[identifiers.size()];
            final byte[] typeValues = new byte[types.size()];
            final int[] roleValues = new int[roles.size()];
            for (int i = 0; i < identifiers.size(); i++)
            {
                final ItemType type = types.get(i);
                typeValues[i] = (byte) type.getValue();
                roleValues[i] = this.dictionary.add(roles.get(i));
                final Long memberIdentifier = identifiers.get(i);
                switch (type)
                {
                    case NODE:
                        addRelationMember("Node", index, memberIdentifier, i, memberIndices,
                                this.nodeIdentifierToNodeArrayIndex,
                                this.nodeIndexToRelationIndices);
                        break;
                    case EDGE:
                        addRelationMember("Edge", index, memberIdentifier, i, memberIndices,
                                this.edgeIdentifierToEdgeArrayIndex,
                                this.edgeIndexToRelationIndices);
                        break;
                    case AREA:
                        addRelationMember("Area", index, memberIdentifier, i, memberIndices,
                                this.areaIdentifierToAreaArrayIndex,
                                this.areaIndexToRelationIndices);
                        break;
                    case LINE:
                        addRelationMember("Line", index, memberIdentifier, i, memberIndices,
                                this.lineIdentifierToLineArrayIndex,
                                this.lineIndexToRelationIndices);
                        break;
                    case POINT:
                        addRelationMember("Point", index, memberIdentifier, i, memberIndices,
                                this.pointIdentifierToPointArrayIndex,
                                this.pointIndexToRelationIndices);
                        break;
                    case RELATION:
                        addRelationMember("Relation", index, memberIdentifier, i, memberIndices,
                                this.relationIdentifierToRelationArrayIndex,
                                this.relationIndexToRelationIndices);
                        break;

                    default:
                        throw new CoreException("Cannot recognize ItemType {}", type);
                }
            }
            this.relationMemberTypes.add(typeValues);
            this.relationMemberIndices.add(memberIndices);
            this.relationMemberRoles.add(roleValues);

            // Tags
            for (final String key : tags.keySet())
            {
                final String value = tags.get(key);
                this.relationTags.add(index, key, value);
            }

            if (tags.keySet().isEmpty())
            {
                this.relationTags.add(index, null, null);
            }
        }
    }

    protected long areaIdentifier(final long index)
    {
        return this.areaIdentifiers().get(index);
    }

    protected Polygon areaPolygon(final long index)
    {
        return this.areaPolygons().get(index);
    }

    protected Set<Relation> areaRelations(final long index)
    {
        return itemRelations(this.areaIndexToRelationIndices().get(index));
    }

    protected Map<String, String> areaTags(final long index)
    {
        return this.areaTags().keyValuePairs(index);
    }

    protected Node edgeEndNode(final long index)
    {
        return new PackedNode(this, this.edgeEndNodeIndex().get(index));
    }

    protected long edgeIdentifier(final long index)
    {
        return this.edgeIdentifiers().get(index);
    }

    protected PolyLine edgePolyLine(final long index)
    {
        return this.edgePolyLines().get(index);
    }

    protected Set<Relation> edgeRelations(final long index)
    {
        return itemRelations(this.edgeIndexToRelationIndices().get(index));
    }

    protected Node edgeStartNode(final long index)
    {
        return new PackedNode(this, this.edgeStartNodeIndex().get(index));
    }

    protected Map<String, String> edgeTags(final long index)
    {
        return this.edgeTags().keyValuePairs(index);
    }

    /**
     * Get the serialization format used for loading this {@link PackedAtlas}.
     *
     * @return The load serialization format setting
     */
    protected AtlasSerializationFormat getLoadSerializationFormat()
    {
        return this.loadSerializationFormat;
    }

    @Override
    protected Logger getLogger()
    {
        return logger;
    }

    protected Optional<PackedAtlasSerializer> getSerializer()
    {
        return Optional.ofNullable(this.serializer);
    }

    protected boolean isEmpty()
    {
        return this.nodeIdentifiers().isEmpty() && this.edgeIdentifiers().isEmpty()
                && this.areaIdentifiers().isEmpty() && this.lineIdentifiers().isEmpty()
                && this.pointIdentifiers().isEmpty() && this.relationIdentifiers().isEmpty();
    }

    protected long lineIdentifier(final long index)
    {
        return this.lineIdentifiers().get(index);
    }

    protected PolyLine linePolyLine(final long index)
    {
        return this.linePolyLines().get(index);
    }

    protected Set<Relation> lineRelations(final long index)
    {
        return itemRelations(this.lineIndexToRelationIndices().get(index));
    }

    protected Map<String, String> lineTags(final long index)
    {
        return this.lineTags().keyValuePairs(index);
    }

    protected long nodeIdentifier(final long index)
    {
        return this.nodeIdentifiers().get(index);
    }

    /**
     * In very rare cases, Way Slicing will return slightly non-deterministic cut locations in
     * different shards. This tolerance allows the PackedAtlasBuilder to identify very closeby nodes
     * and use them instead.
     *
     * @param location
     *            The location to target
     * @param searchDistance
     *            The distance to search for around the location
     * @param toleranceDistance
     *            The maximum distance at which a node is accepted
     * @return The resulting node identifier
     */
    protected Long nodeIdentifierForEnlargedLocation(final Location location,
            final Distance searchDistance, final Distance toleranceDistance)
    {
        final Rectangle bounds = location.bounds().expand(searchDistance);
        buildNodeSpatialIndexIfNecessary();
        final SortedSet<Node> nodes = new TreeSet<>((node1, node2) ->
        {
            final Distance distance1 = location.distanceTo(node1.getLocation());
            final Distance distance2 = location.distanceTo(node2.getLocation());
            final double difference = distance2.asMillimeters() - distance1.asMillimeters();
            if (difference > 0.0)
            {
                return 1;
            }
            else if (difference < 0.0)
            {
                return -1;
            }
            else
            {
                return 0;
            }
        });
        this.getNodeSpatialIndex().get(bounds).forEach(nodes::add);
        for (final Node candidate : nodes)
        {
            final Distance distance = location.distanceTo(candidate.getLocation());
            if (distance.isLessThanOrEqualTo(toleranceDistance))
            {
                return candidate.getIdentifier();
            }
        }
        return null;
    }

    protected Long nodeIdentifierForLocation(final Location location)
    {
        buildNodeSpatialIndexIfNecessary();

        final Iterator<Node> nodes;
        final Rectangle bounds = location.bounds();

        // Handle the anti-meridian case. +180 and -180 are identical in the Atlas, if this happens
        // to be the Longitude of the passed in Location, make sure to also check the equivalent
        // Location across the anti-meridian.
        if (location.getLongitude().equals(Longitude.ANTIMERIDIAN_EAST)
                || location.getLongitude().equals(Longitude.ANTIMERIDIAN_WEST))
        {
            final Location locationAcrossAntiMeridian = new Location(location.getLatitude(),
                    Longitude.dm7(-location.getLongitude().asDm7()));
            final Rectangle boundsAcrossAntiMeridian = locationAcrossAntiMeridian.bounds();

            nodes = new MultiIterable<>(this.getNodeSpatialIndex().get(bounds),
                    this.getNodeSpatialIndex().get(boundsAcrossAntiMeridian)).iterator();
        }
        else
        {
            nodes = this.getNodeSpatialIndex().get(bounds).iterator();
        }

        if (nodes.hasNext())
        {
            return nodes.next().getIdentifier();
        }

        return null;
    }

    protected SortedSet<Edge> nodeInEdges(final long index)
    {
        final SortedSet<Edge> result = new TreeSet<>();
        for (final long edgeIndex : this.nodeInEdgesIndices().get(index))
        {
            result.add(new PackedEdge(this, edgeIndex));
        }
        return result;
    }

    protected Location nodeLocation(final long index)
    {
        return new Location(this.nodeLocations().get(index));
    }

    protected SortedSet<Edge> nodeOutEdges(final long index)
    {
        final SortedSet<Edge> result = new TreeSet<>();
        for (final long edgeIndex : this.nodeOutEdgesIndices().get(index))
        {
            result.add(new PackedEdge(this, edgeIndex));
        }
        return result;
    }

    protected Set<Relation> nodeRelations(final long index)
    {
        return itemRelations(this.nodeIndexToRelationIndices().get(index));
    }

    protected Map<String, String> nodeTags(final long index)
    {
        return this.nodeTags().keyValuePairs(index);
    }

    protected long pointIdentifier(final long index)
    {
        return this.pointIdentifiers().get(index);
    }

    protected Location pointLocation(final long index)
    {
        return new Location(this.pointLocations().get(index));
    }

    protected Set<Relation> pointRelations(final long index)
    {
        return itemRelations(this.pointIndexToRelationIndices().get(index));
    }

    protected Map<String, String> pointTags(final long index)
    {
        return this.pointTags().keyValuePairs(index);
    }

    protected RelationMemberList relationAllKnownOsmMembers(final long index)
    {
        final List<RelationMember> result = new ArrayList<>();
        for (final long candidateIdentifier : this.relationOsmIdentifierToRelationIdentifiers()
                .get(relationOsmIdentifier(index)))
        {
            final long candidateIndex = this.relationIdentifierToRelationArrayIndex()
                    .get(candidateIdentifier);
            relationMembers(candidateIndex).forEach(relationMember -> result.add(relationMember));
        }
        return new RelationMemberList(result);
    }

    protected List<Relation> relationAllRelationsWithSameOsmIdentifier(final long index)
    {
        final List<Relation> result = new ArrayList<>();
        for (final long candidateIdentifier : this.relationOsmIdentifierToRelationIdentifiers()
                .get(relationOsmIdentifier(index)))
        {
            final long candidateIndex = this.relationIdentifierToRelationArrayIndex()
                    .get(candidateIdentifier);
            result.add(new PackedRelation(this, candidateIndex));
        }
        return result;
    }

    protected long relationIdentifier(final long index)
    {
        return this.relationIdentifiers().get(index);
    }

    protected RelationMemberList relationMembers(final long index)
    {
        final Set<RelationMember> result = new TreeSet<>();
        int arrayIndex = 0;
        for (final byte typeValue : this.relationMemberTypes().get(index))
        {
            final ItemType type = ItemType.forValue(typeValue);
            final long memberIndex = this.relationMemberIndices().get(index)[arrayIndex];
            final String role = this.dictionary()
                    .word(this.relationMemberRoles().get(index)[arrayIndex]);
            final AtlasEntity entity;
            switch (type)
            {
                case NODE:
                    entity = new PackedNode(this, memberIndex);
                    break;
                case EDGE:
                    entity = new PackedEdge(this, memberIndex);
                    break;
                case AREA:
                    entity = new PackedArea(this, memberIndex);
                    break;
                case LINE:
                    entity = new PackedLine(this, memberIndex);
                    break;
                case POINT:
                    entity = new PackedPoint(this, memberIndex);
                    break;
                case RELATION:
                    entity = new PackedRelation(this, memberIndex);
                    break;
                default:
                    throw new CoreException("Invalid member type {}", type);
            }
            result.add(new RelationMember(role, entity, relationIdentifier(index)));
            arrayIndex++;
        }
        return new RelationMemberList(result);
    }

    protected long relationOsmIdentifier(final long index)
    {
        return this.relationOsmIdentifiers().get(index);
    }

    protected Set<Relation> relationRelations(final long index)
    {
        return itemRelations(this.relationIndexToRelationIndices().get(index));
    }

    protected Map<String, String> relationTags(final long index)
    {
        return this.relationTags().keyValuePairs(index);
    }

    /**
     * Set the serialization format for loading this {@link PackedAtlas}.
     *
     * @param loadFormat
     *            The format to use
     */
    protected void setLoadSerializationFormat(final AtlasSerializationFormat loadFormat)
    {
        this.loadSerializationFormat = loadFormat;
    }

    /**
     * This method is to be used by the {@link PackedAtlasBuilder} only
     *
     * @param metaData
     *            The new MetaData
     */
    protected void setMetaData(final AtlasMetaData metaData)
    {
        this.metaData = metaData;
    }

    @Override
    protected void setName(final String name)
    {
        super.setName(name);
    }

    /**
     * Add a {@link RelationMember}
     *
     * @param relationIndex
     *            The index of the {@link Relation} in the {@link Relation} arrays.
     * @param memberIdentifier
     *            The identifier of the member to add
     * @param relationMemberListIndex
     *            The index of the member in the {@link Relation}'s member arrays.
     * @param relationMemberIndexArray
     *            The array of the indices of the relation members.
     * @param memberIdentifierToArrayIndex
     *            The member type's identifier to array index map
     * @param memberIndicesToRelationIndices
     *            The member type's index to relation indices map.
     */
    private void addRelationMember(final String type, final long relationIndex,
            final Long memberIdentifier, final int relationMemberListIndex,
            final long[] relationMemberIndexArray, final LongToLongMap memberIdentifierToArrayIndex,
            final LongToLongMultiMap memberIndicesToRelationIndices)
    {
        if (memberIdentifierToArrayIndex.containsKey(memberIdentifier))
        {
            relationMemberIndexArray[relationMemberListIndex] = memberIdentifierToArrayIndex
                    .get(memberIdentifier);
            memberIndicesToRelationIndices.add(relationMemberIndexArray[relationMemberListIndex],
                    relationIndex);
        }
        else
        {
            throw new AtlasIntegrityException("The {} {} does not exist for relation {}.", type,
                    memberIdentifier, this.relationIdentifiers.get(relationIndex));
        }
    }

    private LongArray areaIdentifiers()
    {
        if (this.areaIdentifiers == null)
        {
            synchronized (FIELD_AREA_IDENTIFIERS)
            {
                if (this.areaIdentifiers == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_AREA_IDENTIFIERS);
                }
            }
        }
        return this.areaIdentifiers;
    }

    private LongToLongMap areaIdentifierToAreaArrayIndex()
    {
        if (this.areaIdentifierToAreaArrayIndex == null)
        {
            synchronized (FIELD_AREA_IDENTIFIER_TO_AREA_ARRAY_INDEX)
            {
                if (this.areaIdentifierToAreaArrayIndex == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_AREA_IDENTIFIER_TO_AREA_ARRAY_INDEX);
                }
            }
        }
        return this.areaIdentifierToAreaArrayIndex;
    }

    private LongToLongMultiMap areaIndexToRelationIndices()
    {
        if (this.areaIndexToRelationIndices == null)
        {
            synchronized (FIELD_AREA_INDEX_TO_RELATION_INDICES)
            {
                if (this.areaIndexToRelationIndices == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_AREA_INDEX_TO_RELATION_INDICES);
                }
            }
        }
        return this.areaIndexToRelationIndices;
    }

    private PolygonArray areaPolygons()
    {
        if (this.areaPolygons == null)
        {
            synchronized (FIELD_AREA_POLYGONS)
            {
                if (this.areaPolygons == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_AREA_POLYGONS);
                }
            }
        }
        return this.areaPolygons;
    }

    private PackedTagStore areaTags()
    {
        if (this.areaTags == null)
        {
            synchronized (FIELD_AREA_TAGS)
            {
                if (this.areaTags == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_AREA_TAGS);
                }
            }
        }
        this.areaTags.setDictionary(dictionary());
        return this.areaTags;
    }

    private IntegerDictionary<String> dictionary()
    {
        if (this.dictionary == null)
        {
            synchronized (FIELD_DICTIONARY)
            {
                if (this.dictionary == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_DICTIONARY);
                }
            }
        }
        return this.dictionary;
    }

    private LongArray edgeEndNodeIndex()
    {
        if (this.edgeEndNodeIndex == null)
        {
            synchronized (FIELD_EDGE_END_NODE_INDEX)
            {
                if (this.edgeEndNodeIndex == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_EDGE_END_NODE_INDEX);
                }
            }
        }
        return this.edgeEndNodeIndex;
    }

    private LongArray edgeIdentifiers()
    {
        if (this.edgeIdentifiers == null)
        {
            synchronized (FIELD_EDGE_IDENTIFIERS)
            {
                if (this.edgeIdentifiers == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_EDGE_IDENTIFIERS);
                }
            }
        }
        return this.edgeIdentifiers;
    }

    private LongToLongMap edgeIdentifierToEdgeArrayIndex()
    {
        if (this.edgeIdentifierToEdgeArrayIndex == null)
        {
            synchronized (FIELD_EDGE_IDENTIFIER_TO_EDGE_ARRAY_INDEX)
            {
                if (this.edgeIdentifierToEdgeArrayIndex == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_EDGE_IDENTIFIER_TO_EDGE_ARRAY_INDEX);
                }
            }
        }
        return this.edgeIdentifierToEdgeArrayIndex;
    }

    private LongToLongMultiMap edgeIndexToRelationIndices()
    {
        if (this.edgeIndexToRelationIndices == null)
        {
            synchronized (FIELD_EDGE_INDEX_TO_RELATION_INDICES)
            {
                if (this.edgeIndexToRelationIndices == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_EDGE_INDEX_TO_RELATION_INDICES);
                }
            }
        }
        return this.edgeIndexToRelationIndices;
    }

    private PolyLineArray edgePolyLines()
    {
        if (this.edgePolyLines == null)
        {
            synchronized (FIELD_EDGE_POLY_LINES)
            {
                if (this.edgePolyLines == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_EDGE_POLY_LINES);
                }
            }
        }
        return this.edgePolyLines;
    }

    private LongArray edgeStartNodeIndex()
    {
        if (this.edgeStartNodeIndex == null)
        {
            synchronized (FIELD_EDGE_START_NODE_INDEX)
            {
                if (this.edgeStartNodeIndex == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_EDGE_START_NODE_INDEX);
                }
            }
        }
        return this.edgeStartNodeIndex;
    }

    private PackedTagStore edgeTags()
    {
        if (this.edgeTags == null)
        {
            synchronized (FIELD_EDGE_TAGS)
            {
                if (this.edgeTags == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_EDGE_TAGS);
                }
            }
        }
        this.edgeTags.setDictionary(dictionary());
        return this.edgeTags;
    }

    private Set<Relation> itemRelations(final long[] relationIndices)
    {
        final Set<Relation> result = new HashSet<>();
        if (relationIndices == null)
        {
            return result;
        }
        for (final long relationIndex : relationIndices)
        {
            result.add(new PackedRelation(this, relationIndex));
        }
        return result;
    }

    private LongArray lineIdentifiers()
    {
        if (this.lineIdentifiers == null)
        {
            synchronized (FIELD_LINE_IDENTIFIERS)
            {
                if (this.lineIdentifiers == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_LINE_IDENTIFIERS);
                }
            }
        }
        return this.lineIdentifiers;
    }

    private LongToLongMap lineIdentifierToLineArrayIndex()
    {
        if (this.lineIdentifierToLineArrayIndex == null)
        {
            synchronized (FIELD_LINE_IDENTIFIER_TO_LINE_ARRAY_INDEX)
            {
                if (this.lineIdentifierToLineArrayIndex == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_LINE_IDENTIFIER_TO_LINE_ARRAY_INDEX);
                }
            }
        }
        return this.lineIdentifierToLineArrayIndex;
    }

    private LongToLongMultiMap lineIndexToRelationIndices()
    {
        if (this.lineIndexToRelationIndices == null)
        {
            synchronized (FIELD_LINE_INDEX_TO_RELATION_INDICES)
            {
                if (this.lineIndexToRelationIndices == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_LINE_INDEX_TO_RELATION_INDICES);
                }
            }
        }
        return this.lineIndexToRelationIndices;
    }

    private PolyLineArray linePolyLines()
    {
        if (this.linePolyLines == null)
        {
            synchronized (FIELD_LINE_POLYLINES)
            {
                if (this.linePolyLines == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_LINE_POLYLINES);
                }
            }
        }
        return this.linePolyLines;
    }

    private PackedTagStore lineTags()
    {
        if (this.lineTags == null)
        {
            synchronized (FIELD_LINE_TAGS)
            {
                if (this.lineTags == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_LINE_TAGS);
                }
            }
        }
        this.lineTags.setDictionary(dictionary());
        return this.lineTags;
    }

    // Keep this method around so legacy Atlas files can still be deserialized.
    @SuppressWarnings("unused")
    private PackedTagStore newPackedTagStore(final long maximumSize, final int memoryBlockSize,
            final int subArraySize)
    {
        return new PackedTagStore(maximumSize, memoryBlockSize, subArraySize, dictionary())
        {
            private static final long serialVersionUID = 5959934069025112665L;

            @Override
            public IntegerDictionary<String> keysDictionary()
            {
                return super.keysDictionary();
            }

            @Override
            public IntegerDictionary<String> valuesDictionary()
            {
                return super.valuesDictionary();
            }
        };
    }

    private LongArray nodeIdentifiers()
    {
        if (this.nodeIdentifiers == null)
        {
            synchronized (FIELD_NODE_IDENTIFIERS)
            {
                if (this.nodeIdentifiers == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_NODE_IDENTIFIERS);
                }
            }
        }
        return this.nodeIdentifiers;
    }

    private LongToLongMap nodeIdentifierToNodeArrayIndex()
    {
        if (this.nodeIdentifierToNodeArrayIndex == null)
        {
            synchronized (FIELD_NODE_IDENTIFIER_TO_NODE_ARRAY_INDEX)
            {
                if (this.nodeIdentifierToNodeArrayIndex == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_NODE_IDENTIFIER_TO_NODE_ARRAY_INDEX);
                }
            }
        }
        return this.nodeIdentifierToNodeArrayIndex;
    }

    private LongToLongMultiMap nodeIndexToRelationIndices()
    {
        if (this.nodeIndexToRelationIndices == null)
        {
            synchronized (FIELD_NODE_INDEX_TO_RELATION_INDICES)
            {
                if (this.nodeIndexToRelationIndices == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_NODE_INDEX_TO_RELATION_INDICES);
                }
            }
        }
        return this.nodeIndexToRelationIndices;
    }

    private LongArrayOfArrays nodeInEdgesIndices()
    {
        if (this.nodeInEdgesIndices == null)
        {
            synchronized (FIELD_NODE_IN_EDGES_INDICES)
            {
                if (this.nodeInEdgesIndices == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_NODE_IN_EDGES_INDICES);
                }
            }
        }
        return this.nodeInEdgesIndices;
    }

    private LongArray nodeLocations()
    {
        if (this.nodeLocations == null)
        {
            synchronized (FIELD_NODE_LOCATIONS)
            {
                if (this.nodeLocations == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_NODE_LOCATIONS);
                }
            }
        }
        return this.nodeLocations;
    }

    private LongArrayOfArrays nodeOutEdgesIndices()
    {
        if (this.nodeOutEdgesIndices == null)
        {
            synchronized (FIELD_NODE_OUT_EDGES_INDICES)
            {
                if (this.nodeOutEdgesIndices == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_NODE_OUT_EDGES_INDICES);
                }
            }
        }
        return this.nodeOutEdgesIndices;
    }

    private PackedTagStore nodeTags()
    {
        if (this.nodeTags == null)
        {
            synchronized (FIELD_NODE_TAGS)
            {
                if (this.nodeTags == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_NODE_TAGS);
                }
            }
        }
        this.nodeTags.setDictionary(dictionary());
        return this.nodeTags;
    }

    private LongArray pointIdentifiers()
    {
        if (this.pointIdentifiers == null)
        {
            synchronized (FIELD_POINT_IDENTIFIERS)
            {
                if (this.pointIdentifiers == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_POINT_IDENTIFIERS);
                }
            }
        }
        return this.pointIdentifiers;
    }

    private LongToLongMap pointIdentifierToPointArrayIndex()
    {
        if (this.pointIdentifierToPointArrayIndex == null)
        {
            synchronized (FIELD_POINT_IDENTIFIER_TO_POINT_ARRAY_INDEX)
            {
                if (this.pointIdentifierToPointArrayIndex == null)
                {
                    this.serializer
                            .deserializeIfNeeded(FIELD_POINT_IDENTIFIER_TO_POINT_ARRAY_INDEX);
                }
            }
        }
        return this.pointIdentifierToPointArrayIndex;
    }

    private LongToLongMultiMap pointIndexToRelationIndices()
    {
        if (this.pointIndexToRelationIndices == null)
        {
            synchronized (FIELD_POINT_INDEX_TO_RELATION_INDICES)
            {
                if (this.pointIndexToRelationIndices == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_POINT_INDEX_TO_RELATION_INDICES);
                }
            }
        }
        return this.pointIndexToRelationIndices;
    }

    private LongArray pointLocations()
    {
        if (this.pointLocations == null)
        {
            synchronized (FIELD_POINT_LOCATIONS)
            {
                if (this.pointLocations == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_POINT_LOCATIONS);
                }
            }
        }
        return this.pointLocations;
    }

    private PackedTagStore pointTags()
    {
        if (this.pointTags == null)
        {
            synchronized (FIELD_POINT_TAGS)
            {
                if (this.pointTags == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_POINT_TAGS);
                }
            }
        }
        this.pointTags.setDictionary(dictionary());
        return this.pointTags;
    }

    private LongArray relationIdentifiers()
    {
        if (this.relationIdentifiers == null)
        {
            synchronized (FIELD_RELATION_IDENTIFIERS)
            {
                if (this.relationIdentifiers == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_RELATION_IDENTIFIERS);
                }
            }
        }
        return this.relationIdentifiers;
    }

    private LongToLongMap relationIdentifierToRelationArrayIndex()
    {
        if (this.relationIdentifierToRelationArrayIndex == null)
        {
            synchronized (FIELD_RELATION_IDENTIFIER_TO_RELATION_ARRAY_INDEX)
            {
                if (this.relationIdentifierToRelationArrayIndex == null)
                {
                    this.serializer
                            .deserializeIfNeeded(FIELD_RELATION_IDENTIFIER_TO_RELATION_ARRAY_INDEX);
                }
            }
        }
        return this.relationIdentifierToRelationArrayIndex;
    }

    private LongToLongMultiMap relationIndexToRelationIndices()
    {
        if (this.relationIndexToRelationIndices == null)
        {
            synchronized (FIELD_RELATION_INDEX_TO_RELATION_INDICES)
            {
                if (this.relationIndexToRelationIndices == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_RELATION_INDEX_TO_RELATION_INDICES);
                }
            }
        }
        return this.relationIndexToRelationIndices;
    }

    private LongArrayOfArrays relationMemberIndices()
    {
        if (this.relationMemberIndices == null)
        {
            synchronized (FIELD_RELATION_MEMBERS_INDICES)
            {
                if (this.relationMemberIndices == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_RELATION_MEMBERS_INDICES);
                }
            }
        }
        return this.relationMemberIndices;
    }

    private IntegerArrayOfArrays relationMemberRoles()
    {
        if (this.relationMemberRoles == null)
        {
            synchronized (FIELD_RELATION_MEMBER_ROLES)
            {
                if (this.relationMemberRoles == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_RELATION_MEMBER_ROLES);
                }
            }
        }
        return this.relationMemberRoles;
    }

    private ByteArrayOfArrays relationMemberTypes()
    {
        if (this.relationMemberTypes == null)
        {
            synchronized (FIELD_RELATION_MEMBER_TYPES)
            {
                if (this.relationMemberTypes == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_RELATION_MEMBER_TYPES);
                }
            }
        }
        return this.relationMemberTypes;
    }

    private LongArray relationOsmIdentifiers()
    {
        if (this.relationOsmIdentifiers == null)
        {
            synchronized (FIELD_RELATION_OSM_IDENTIFIERS)
            {
                if (this.relationOsmIdentifiers == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_RELATION_OSM_IDENTIFIERS);
                }
            }
        }
        return this.relationOsmIdentifiers;
    }

    private LongToLongMultiMap relationOsmIdentifierToRelationIdentifiers()
    {
        if (this.relationOsmIdentifierToRelationIdentifiers == null)
        {
            synchronized (FIELD_RELATION_OSM_IDENTIFIER_TO_RELATION_IDENTIFIERS)
            {
                if (this.relationOsmIdentifierToRelationIdentifiers == null)
                {
                    this.serializer.deserializeIfNeeded(
                            FIELD_RELATION_OSM_IDENTIFIER_TO_RELATION_IDENTIFIERS);
                }
            }
        }
        return this.relationOsmIdentifierToRelationIdentifiers;
    }

    private PackedTagStore relationTags()
    {
        if (this.relationTags == null)
        {
            synchronized (FIELD_RELATION_TAGS)
            {
                if (this.relationTags == null)
                {
                    this.serializer.deserializeIfNeeded(FIELD_RELATION_TAGS);
                }
            }
        }
        this.relationTags.setDictionary(dictionary());
        return this.relationTags;
    }

    /**
     * Update references for Node in/out edges
     *
     * @param nodeEdgesIndices
     *            Either the nodeInEdges or the nodeOutEdges
     */
    private void updateNodeEdgesReference(final long nodeIndex,
            final LongArrayOfArrays nodeEdgesIndices, final long edgeIndex)
    {
        final long[] nodeEdges = nodeEdgesIndices.get(nodeIndex);
        final long[] newNodeEdges = new long[nodeEdges.length + 1];
        for (int i = 0; i < nodeEdges.length; i++)
        {
            newNodeEdges[i] = nodeEdges[i];
        }
        newNodeEdges[newNodeEdges.length - 1] = edgeIndex;
        nodeEdgesIndices.set(nodeIndex, newNodeEdges);
    }
}
