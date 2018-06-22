package org.openstreetmap.atlas.geography.atlas.multi;

import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.AbstractAtlas;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasCloner;
import org.openstreetmap.atlas.geography.index.RTree;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.maps.LongToIntegerMap;
import org.openstreetmap.atlas.utilities.maps.LongToIntegerMultiMap;
import org.openstreetmap.atlas.utilities.maps.LongToLongMap;
import org.openstreetmap.atlas.utilities.maps.LongToLongMultiMap;
import org.openstreetmap.atlas.utilities.maps.MultiMapWithSet;
import org.openstreetmap.atlas.utilities.scalars.Ratio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

/**
 * {@link Atlas} that is backed by multiple {@link Atlas}es stitched on the fly.
 *
 * @author matthieun
 */
public class MultiAtlas extends AbstractAtlas
{
    private static final long serialVersionUID = -5917117808042670700L;
    private static final Logger logger = LoggerFactory.getLogger(MultiAtlas.class);

    private static final double ARRAY_SIZE_MULTIPLIER = 1.1;

    private final List<Atlas> atlases;

    private final AtlasMetaData metaData;

    private final long numberOfEdges;
    private final long numberOfNodes;
    private final long numberOfAreas;
    private final long numberOfLines;
    private final long numberOfPoints;
    private final long numberOfRelations;
    private final RTree<Integer> atlasSpatialIndex;

    // The identifiers of the Edges, and the Atlases we can find them into.
    private LongToIntegerMap edgeIdentifierToAtlasIndex;

    // The identifiers of the Nodes, and the Atlases we can find them into. Only shared nodes, used
    // for connectivity will be referenced in more than one Atlas (for real time stitching of the
    // navigable network).
    private final LongToIntegerMultiMap nodeIdentifierToAtlasIndices;
    private final LongToIntegerMultiMap areaIdentifierToAtlasIndices;
    private final LongToIntegerMultiMap lineIdentifierToAtlasIndices;
    private final LongToIntegerMultiMap pointIdentifierToAtlasIndices;
    private final LongToIntegerMultiMap relationIdentifierToAtlasIndices;

    // Re-build relations with the same OSM identifier
    private final LongToLongMultiMap relationOsmIdentifierToRelationIdentifiers;
    private final LongToLongMap relationIdentifierToRelationOsmIdentifier;

    private final int subArraySize;
    private final long maximumSize;

    private final int nodeMemoryBlockSize;
    private final int edgeMemoryBlockSize;
    private final int areaMemoryBlockSize;
    private final int lineMemoryBlockSize;
    private final int pointMemoryBlockSize;
    private final int relationMemoryBlockSize;

    private final int nodeHashSize;
    private final int edgeHashSize;
    private final int areaHashSize;
    private final int lineHashSize;
    private final int pointHashSize;
    private final int relationHashSize;

    // Custom fixers for crossing edges and overlapping nodes
    private final MultiAtlasBorderFixer borderFixer;
    private final MultiAtlasOverlappingNodesFixer nodesFixer;

    /**
     * Load a {@link MultiAtlas} from a serialized resource
     *
     * @param resource
     *            The {@link Resource} to read from
     * @return The deserialized {@link MultiAtlas}
     */
    public static MultiAtlas load(final Resource resource)
    {
        try (ObjectInputStream input = new ObjectInputStream(resource.read()))
        {
            final MultiAtlas result = (MultiAtlas) input.readObject();
            return result;
        }
        catch (final Exception e)
        {
            throw new CoreException("Could not load Atlas from {}", e, resource);
        }
    }

    /**
     * Load a {@link MultiAtlas} from an {@link Iterable} of {@link PackedAtlas} serialized
     * resources
     *
     * @param resources
     *            The {@link Resource}s to read from (which each contain a serialized
     *            {@link PackedAtlas}).
     * @return The deserialized {@link MultiAtlas}
     */
    public static MultiAtlas loadFromPackedAtlas(final Iterable<? extends Resource> resources)
    {
        return loadFromPackedAtlas(resources, false);
    }

    /**
     * Load a {@link MultiAtlas} from an {@link Iterable} of {@link PackedAtlas} serialized
     * resources
     *
     * @param resources
     *            The {@link Resource}s to read from (which each contain a serialized
     *            {@link PackedAtlas}).
     * @param lotsOfOverlap
     *            If this is true, then the builder will start with small arrays and re-size a lot,
     *            but won't waste memory because of the overlap of the sub-{@link Atlas}es. However,
     *            if this is false, the builder will blindly sum all the items of all the
     *            {@link Atlas}es regardless of overlap, and will hence allocate potentially more
     *            memory than necessary. In that case though, the arrays will never resize, and the
     *            load time will be faster.
     * @return The deserialized {@link MultiAtlas}
     */
    public static MultiAtlas loadFromPackedAtlas(final Iterable<? extends Resource> resources,
            final boolean lotsOfOverlap)
    {
        if (Iterables.size(resources) == 0)
        {
            throw new CoreException("Can't create an atlas from zero resources");
        }
        return new MultiAtlas(
                Iterables.translate(resources, resource -> PackedAtlas.load(resource)),
                lotsOfOverlap);
    }

    /**
     * Load a {@link MultiAtlas} from an {@link Iterable} of {@link PackedAtlas} serialized
     * resources
     *
     * @param resources
     *            The {@link Resource}s to read from (which each contain a serialized
     *            {@link PackedAtlas}).
     * @param lotsOfOverlap
     *            If this is true, then the builder will start with small arrays and re-size a lot,
     *            but won't waste memory because of the overlap of the sub-{@link Atlas}es. However,
     *            if this is false, the builder will blindly sum all the items of all the
     *            {@link Atlas}es regardless of overlap, and will hence allocate potentially more
     *            memory than necessary. In that case though, the arrays will never resize, and the
     *            load time will be faster.
     * @param filter
     *            The {@link Predicate} to use when loading from {@link PackedAtlas}
     * @return The deserialized {@link MultiAtlas}
     */
    public static MultiAtlas loadFromPackedAtlas(final Iterable<? extends Resource> resources,
            final boolean lotsOfOverlap, final Predicate<AtlasEntity> filter)
    {
        if (Iterables.size(resources) == 0)
        {
            throw new CoreException("Can't create an atlas from zero resources");
        }
        return new MultiAtlas(
                Iterables.translate(resources,
                        resource -> PackedAtlas.load(resource).subAtlas(filter).get()),
                lotsOfOverlap);
    }

    /**
     * Load a {@link MultiAtlas} from an {@link Iterable} of {@link PackedAtlas} serialized
     * resources
     *
     * @param resources
     *            The {@link Resource}s to read from (which each contain a serialized
     *            {@link PackedAtlas}).
     * @param filter
     *            The {@link Predicate} to use when loading from {@link PackedAtlas}
     * @return The deserialized {@link MultiAtlas}
     */
    public static MultiAtlas loadFromPackedAtlas(final Iterable<? extends Resource> resources,
            final Predicate<AtlasEntity> filter)
    {
        return loadFromPackedAtlas(resources, false, filter);
    }

    /**
     * Load a {@link MultiAtlas} from {@link PackedAtlas} serialized resources
     *
     * @param resources
     *            The {@link Resource}s to read from (which each contain a serialized
     *            {@link PackedAtlas}).
     * @return The deserialized {@link MultiAtlas}
     */
    public static MultiAtlas loadFromPackedAtlas(final Resource... resources)
    {
        return loadFromPackedAtlas(Iterables.iterable(resources), false);
    }

    /**
     * Create an {@link Atlas} from stitching many other {@link Atlas}
     *
     * @param atlases
     *            The {@link Atlas}es to stitch together
     */
    public MultiAtlas(final Atlas... atlases)
    {
        this(Iterables.iterable(atlases), false);
    }

    /**
     * Create an {@link Atlas} from stitching many other {@link Atlas}
     *
     * @param atlases
     *            The {@link Atlas}es to stitch together
     */
    public MultiAtlas(final Iterable<Atlas> atlases)
    {
        this(atlases, false);
    }

    /**
     * Create an {@link Atlas} from stitching many other {@link Atlas}
     *
     * @param atlases
     *            The {@link Atlas}es to stitch together
     * @param lotsOfOverlap
     *            If this is true, then the builder will start with small arrays and re-size a lot,
     *            but won't waste memory because of the overlap of the sub-{@link Atlas}es. However,
     *            if this is false, the builder will blindly sum all the items of all the
     *            {@link Atlas}es regardless of overlap, and will hence allocate potentially more
     *            memory than necessary. In that case though, the arrays will never resize, and the
     *            load time will be faster.
     */
    public MultiAtlas(final Iterable<Atlas> atlases, final boolean lotsOfOverlap)
    {
        this(Iterables.asList(atlases), lotsOfOverlap);
    }

    /**
     * Create an {@link Atlas} from stitching many other {@link Atlas}
     *
     * @param atlases
     *            The {@link Atlas}es to stitch together
     */
    public MultiAtlas(final List<Atlas> atlases)
    {
        this(atlases, false);
    }

    /**
     * Create an {@link Atlas} from stitching many other {@link Atlas}
     *
     * @param atlases
     *            The {@link Atlas}es to stitch together
     * @param lotsOfOverlap
     *            If this is true, then the builder will start with small arrays and re-size a lot,
     *            but won't waste memory because of the overlap of the sub-{@link Atlas}es. However,
     *            if this is false, the builder will blindly sum all the items of all the
     *            {@link Atlas}es regardless of overlap, and will hence allocate potentially more
     *            memory than necessary. In that case though, the arrays will never resize, and the
     *            load time will be faster.
     */
    public MultiAtlas(final List<Atlas> atlases, final boolean lotsOfOverlap)
    {
        if (atlases.isEmpty())
        {
            throw new CoreException("An Atlas is Located, and therefore cannot be empty.");
        }
        this.atlases = atlases;
        this.atlasSpatialIndex = newPackedAtlasSpatialIndex();
        long numberOfNodes;
        long numberOfEdges;
        long numberOfAreas;
        long numberOfLines;
        long numberOfPoints;
        long numberOfRelations;
        if (lotsOfOverlap)
        {
            // We do not know in advance how much overlap there will be. We choose to re-size
            // instead of wasting memory
            numberOfEdges = DEFAULT_NUMBER_OF_ITEMS;
            numberOfNodes = DEFAULT_NUMBER_OF_ITEMS;
            numberOfAreas = DEFAULT_NUMBER_OF_ITEMS;
            numberOfLines = DEFAULT_NUMBER_OF_ITEMS;
            numberOfPoints = DEFAULT_NUMBER_OF_ITEMS;
            numberOfRelations = DEFAULT_NUMBER_OF_ITEMS;
        }
        else
        {
            // Re-sizing arrays is prohibitive, so even if we do not know how much overlap there
            // will be, we choose to waste memory instead.
            numberOfNodes = Iterables.count(this.atlases, atlas -> atlas.numberOfNodes());
            numberOfEdges = Iterables.count(this.atlases, atlas -> atlas.numberOfEdges());
            numberOfAreas = Iterables.count(this.atlases, atlas -> atlas.numberOfAreas());
            numberOfLines = Iterables.count(this.atlases, atlas -> atlas.numberOfLines());
            numberOfPoints = Iterables.count(this.atlases, atlas -> atlas.numberOfPoints());
            numberOfRelations = Iterables.count(this.atlases, atlas -> atlas.numberOfRelations());
        }
        if (atlases.size() > 1)
        {
            numberOfNodes = Math.round(numberOfNodes * ARRAY_SIZE_MULTIPLIER);
            numberOfEdges = Math.round(numberOfEdges * ARRAY_SIZE_MULTIPLIER);
            numberOfAreas = Math.round(numberOfAreas * ARRAY_SIZE_MULTIPLIER);
            numberOfLines = Math.round(numberOfLines * ARRAY_SIZE_MULTIPLIER);
            numberOfPoints = Math.round(numberOfPoints * ARRAY_SIZE_MULTIPLIER);
            numberOfRelations = Math.round(numberOfRelations * ARRAY_SIZE_MULTIPLIER);
        }
        int index = 0;
        for (final Atlas atlas : this.atlases)
        {
            this.atlasSpatialIndex.add(atlas.bounds(), index);
            index++;
        }

        this.subArraySize = Integer.MAX_VALUE;
        this.maximumSize = Long.MAX_VALUE;

        this.nodeMemoryBlockSize = (int) Math.max(DEFAULT_NUMBER_OF_ITEMS,
                numberOfNodes % Integer.MAX_VALUE);
        this.edgeMemoryBlockSize = (int) Math.max(DEFAULT_NUMBER_OF_ITEMS,
                numberOfEdges % Integer.MAX_VALUE);
        this.areaMemoryBlockSize = (int) Math.max(DEFAULT_NUMBER_OF_ITEMS,
                numberOfAreas % Integer.MAX_VALUE);
        this.lineMemoryBlockSize = (int) Math.max(DEFAULT_NUMBER_OF_ITEMS,
                numberOfLines % Integer.MAX_VALUE);
        this.pointMemoryBlockSize = (int) Math.max(DEFAULT_NUMBER_OF_ITEMS,
                numberOfPoints % Integer.MAX_VALUE);
        this.relationMemoryBlockSize = (int) Math.max(DEFAULT_NUMBER_OF_ITEMS,
                numberOfRelations % Integer.MAX_VALUE);

        this.nodeHashSize = (int) Math
                .max(Math.min(numberOfNodes / HASH_MODULO_RATIO, Integer.MAX_VALUE), 1);
        this.edgeHashSize = (int) Math
                .max(Math.min(numberOfEdges / HASH_MODULO_RATIO, Integer.MAX_VALUE), 1);
        this.areaHashSize = (int) Math
                .max(Math.min(numberOfAreas / HASH_MODULO_RATIO, Integer.MAX_VALUE), 1);
        this.lineHashSize = (int) Math
                .max(Math.min(numberOfLines / HASH_MODULO_RATIO, Integer.MAX_VALUE), 1);
        this.pointHashSize = (int) Math
                .max(Math.min(numberOfPoints / HASH_MODULO_RATIO, Integer.MAX_VALUE), 1);
        this.relationHashSize = (int) Math
                .max(Math.min(numberOfRelations / HASH_MODULO_RATIO, Integer.MAX_VALUE), 1);

        this.nodeIdentifierToAtlasIndices = new LongToIntegerMultiMap(
                "MultiAtlas - nodeIdentifierToAtlasIndices", this.maximumSize, this.nodeHashSize,
                this.nodeMemoryBlockSize, this.subArraySize, this.nodeMemoryBlockSize,
                this.subArraySize);
        this.edgeIdentifierToAtlasIndex = new LongToIntegerMap(
                "MultiAtlas - edgeIdentifierToAtlasIndex", this.maximumSize, this.edgeHashSize,
                this.edgeMemoryBlockSize, this.subArraySize, this.edgeMemoryBlockSize,
                this.subArraySize);
        this.areaIdentifierToAtlasIndices = new LongToIntegerMultiMap(
                "MultiAtlas - areaIdentifierToAtlasIndex", this.maximumSize, this.areaHashSize,
                this.areaMemoryBlockSize, this.subArraySize, this.areaMemoryBlockSize,
                this.subArraySize);
        this.lineIdentifierToAtlasIndices = new LongToIntegerMultiMap(
                "MultiAtlas - lineIdentifierToAtlasIndex", this.maximumSize, this.lineHashSize,
                this.lineMemoryBlockSize, this.subArraySize, this.lineMemoryBlockSize,
                this.subArraySize);
        this.pointIdentifierToAtlasIndices = new LongToIntegerMultiMap(
                "MultiAtlas - pointIdentifierToAtlasIndex", this.maximumSize, this.pointHashSize,
                this.pointMemoryBlockSize, this.subArraySize, this.pointMemoryBlockSize,
                this.subArraySize);
        this.relationIdentifierToAtlasIndices = new LongToIntegerMultiMap(
                "MultiAtlas - relationIdentifierToAtlasIndices", this.maximumSize,
                this.relationHashSize, this.relationMemoryBlockSize, this.subArraySize,
                this.relationMemoryBlockSize, this.subArraySize);
        this.relationOsmIdentifierToRelationIdentifiers = new LongToLongMultiMap(
                "MultiAtlas - relationOsmIdentifierToRelationIdentifier", this.maximumSize,
                this.relationHashSize, this.relationMemoryBlockSize, this.subArraySize,
                this.relationMemoryBlockSize, this.subArraySize);
        this.relationIdentifierToRelationOsmIdentifier = new LongToLongMap(
                "MultiAtlas - relationIdentifierToRelationOsmIdentifier", this.maximumSize,
                this.relationHashSize, this.relationMemoryBlockSize, this.subArraySize,
                this.relationMemoryBlockSize, this.subArraySize);

        // Populate the pointers
        int atlasIndex = 0;
        for (final Atlas atlas : this.atlases)
        {
            populateReferences(atlas, atlasIndex);
            atlasIndex++;
        }

        // Fix all the sharding issues at borders related to way sectioning
        this.borderFixer = new MultiAtlasBorderFixer(this.atlases, this.edgeIdentifierToAtlasIndex);
        this.borderFixer.fixBorderIssues();

        if (this.borderFixer.hasFixes())
        {
            // Re-write the edgeIdentifier to AtlasIndex array to remove the fixed edge
            // identifiers. Those will be re-mapped to the fixAtlas index = -1 below
            final LongToIntegerMap edgeIdentifierToAtlasIndexCandidate = new LongToIntegerMap(
                    "MultiAtlas - edgeIdentifierToAtlasIndexCandidate", this.getMaximumSize(),
                    this.getEdgeHashSize(), this.getEdgeMemoryBlockSize(), this.getSubArraySize(),
                    this.getEdgeMemoryBlockSize(), this.getSubArraySize());
            this.getEdgeIdentifierToAtlasIndex().forEach(identifier ->
            {
                if (!this.borderFixer.isFixEdgeIdentifier(identifier))
                {
                    // Add only the edges that are not fixed here, as the fixed ones will be added
                    // in the populateReferences call below, coming from the fixAtlas.
                    edgeIdentifierToAtlasIndexCandidate.put(identifier,
                            this.getEdgeIdentifierToAtlasIndex().get(identifier));
                }
            });
            this.setEdgeIdentifierToAtlasIndex(edgeIdentifierToAtlasIndexCandidate);

            // Re-map the fixed edge identifiers to the fixed edge index below.
            this.populateReferences(this.borderFixer.getFixAtlas(), -1);
        }

        // Build the spatial indices
        this.getAsNewNodeSpatialIndex();
        this.getAsNewEdgeSpatialIndex();
        this.getAsNewAreaSpatialIndex();
        this.getAsNewLineSpatialIndex();
        this.getAsNewPointSpatialIndex();
        this.getAsNewRelationSpatialIndex();
        this.nodeIdentifierToAtlasIndices
                .forEach(identifier -> this.getNodeSpatialIndex().add(node(identifier)));
        this.edgeIdentifierToAtlasIndex
                .forEach(identifier -> this.getEdgeSpatialIndex().add(edge(identifier)));
        this.areaIdentifierToAtlasIndices
                .forEach(identifier -> this.getAreaSpatialIndex().add(area(identifier)));
        this.lineIdentifierToAtlasIndices
                .forEach(identifier -> this.getLineSpatialIndex().add(line(identifier)));
        this.pointIdentifierToAtlasIndices
                .forEach(identifier -> this.getPointSpatialIndex().add(point(identifier)));
        this.relationIdentifierToAtlasIndices.forEach(identifier ->
        {
            final Relation relation = relation(identifier);
            if (relation.members().size() > 0 && relation.bounds() != null)
            {
                // The relation is not empty, hence it is located
                this.getRelationSpatialIndex().add(relation);
            }
            final long osmIdentifier = relation.osmRelationIdentifier();
            this.relationOsmIdentifierToRelationIdentifiers.add(osmIdentifier, identifier);
            this.relationIdentifierToRelationOsmIdentifier.put(identifier, osmIdentifier);
        });

        // Find the overlapping nodes. Master to slave has a one to many relationship. A master
        // cannot be a slave and vice versa
        this.nodesFixer = new MultiAtlasOverlappingNodesFixer(this);
        this.nodesFixer.aggregateSameLocationNodes();

        // At this point de-duplication has been done already.
        this.numberOfEdges = this.edgeIdentifierToAtlasIndex.size();
        this.numberOfNodes = this.nodeIdentifierToAtlasIndices.size();
        this.numberOfAreas = this.areaIdentifierToAtlasIndices.size();
        this.numberOfLines = this.lineIdentifierToAtlasIndices.size();
        this.numberOfPoints = this.pointIdentifierToAtlasIndices.size();
        this.numberOfRelations = this.relationIdentifierToAtlasIndices.size();

        if (!lotsOfOverlap)
        {
            // In case we have small overlap and the arrays are larger than necessary, resize is
            // worth it if the arrays are more than twice as big as needed.
            final Ratio trimRatio = Ratio.HALF;
            this.nodeIdentifierToAtlasIndices.trimIfLessFilledThan(trimRatio);
            this.edgeIdentifierToAtlasIndex.trimIfLessFilledThan(trimRatio);
            this.areaIdentifierToAtlasIndices.trimIfLessFilledThan(trimRatio);
            this.lineIdentifierToAtlasIndices.trimIfLessFilledThan(trimRatio);
            this.pointIdentifierToAtlasIndices.trimIfLessFilledThan(trimRatio);
            this.relationIdentifierToAtlasIndices.trimIfLessFilledThan(trimRatio);
            this.relationOsmIdentifierToRelationIdentifiers.trimIfLessFilledThan(trimRatio);
            this.relationIdentifierToRelationOsmIdentifier.trimIfLessFilledThan(trimRatio);
        }

        // Update the meta data.
        this.metaData = mergeMetaData();
    }

    @Override
    public Area area(final long identifier)
    {
        if (this.areaIdentifierToAtlasIndices.containsKey(identifier))
        {
            return new MultiArea(this, identifier);
        }
        return null;
    }

    @Override
    public Iterable<Area> areas()
    {
        return Iterables.translate(this.areaIdentifierToAtlasIndices, this::area);
    }

    /**
     * Get all the Atlas intersecting some bounds
     *
     * @param bounds
     *            The bounds
     * @return All the Atlas intersecting the bounds
     */
    public Set<Atlas> atlasIntersecting(final Polygon bounds)
    {
        if (!(bounds instanceof Rectangle))
        {
            throw new UnsupportedOperationException("Non-Rectangle Polygons not supported yet.");
        }
        return Iterables.asSet(Iterables.translate(this.atlasSpatialIndex.get(bounds.bounds()),
                atlasIndex -> this.atlases.get(atlasIndex)));
    }

    @Override
    public Rectangle bounds()
    {
        return Rectangle.forLocated(this.atlases);
    }

    @Override
    public Edge edge(final long identifier)
    {
        if (this.edgeIdentifierToAtlasIndex.containsKey(identifier))
        {
            return new MultiEdge(this, identifier);
        }
        return null;
    }

    @Override
    public Iterable<Edge> edges()
    {
        return Iterables.translate(this.edgeIdentifierToAtlasIndex, identifier -> edge(identifier));
    }

    @Override
    public Line line(final long identifier)
    {
        if (this.lineIdentifierToAtlasIndices.containsKey(identifier))
        {
            return new MultiLine(this, identifier);
        }
        return null;
    }

    @Override
    public Iterable<Line> lines()
    {
        return Iterables.translate(this.lineIdentifierToAtlasIndices, this::line);
    }

    @Override
    public AtlasMetaData metaData()
    {
        return this.metaData;
    }

    @Override
    public Node node(final long identifier)
    {
        if (this.nodeIdentifierToAtlasIndices.containsKey(identifier))
        {
            return new MultiNode(this, identifier);
        }
        return null;
    }

    @Override
    public Iterable<Node> nodes()
    {
        // Use the identifier here to avoid listing duplicated nodes twice.
        return Iterables.translate(this.nodeIdentifierToAtlasIndices,
                identifier -> node(identifier));
    }

    @Override
    public long numberOfAreas()
    {
        return this.numberOfAreas;
    }

    @Override
    public long numberOfEdges()
    {
        return this.numberOfEdges;
    }

    @Override
    public long numberOfLines()
    {
        return this.numberOfLines;
    }

    @Override
    public long numberOfNodes()
    {
        return this.numberOfNodes;
    }

    @Override
    public long numberOfPoints()
    {
        return this.numberOfPoints;
    }

    @Override
    public long numberOfRelations()
    {
        return this.numberOfRelations;
    }

    public int numberOfSubAtlas()
    {
        return this.atlases.size();
    }

    @Override
    public Point point(final long identifier)
    {
        if (this.pointIdentifierToAtlasIndices.containsKey(identifier))
        {
            return new MultiPoint(this, identifier);
        }
        return null;
    }

    @Override
    public Iterable<Point> points()
    {
        return Iterables.translate(this.pointIdentifierToAtlasIndices,
                identifier -> point(identifier));
    }

    @Override
    public Relation relation(final long identifier)
    {
        if (this.relationIdentifierToAtlasIndices.containsKey(identifier))
        {
            return new MultiRelation(this, identifier);
        }
        return null;
    }

    @Override
    public Iterable<Relation> relations()
    {
        return Iterables.translate(this.relationIdentifierToAtlasIndices,
                identifier -> relation(identifier));
    }

    @Override
    public void save(final WritableResource writableResource)
    {
        throw new CoreException(
                "A MultiAtlas has to be cloned to a {} before it can be saved. Consider using {}",
                PackedAtlas.class.getName(), PackedAtlasCloner.class.getName());
    }

    /**
     * Get the List of {@link Atlas} that comprise this {@link MultiAtlas}.
     *
     * @return The list of {@link Atlas}
     */
    public List<Atlas> subAtlases()
    {
        return ImmutableList.copyOf(this.atlases);
    }

    protected List<Atlas> getAtlases()
    {
        return this.atlases;
    }

    protected int getEdgeHashSize()
    {
        return this.edgeHashSize;
    }

    protected LongToIntegerMap getEdgeIdentifierToAtlasIndex()
    {
        return this.edgeIdentifierToAtlasIndex;
    }

    protected int getEdgeMemoryBlockSize()
    {
        return this.edgeMemoryBlockSize;
    }

    @Override
    protected Logger getLogger()
    {
        return logger;
    }

    protected long getMaximumSize()
    {
        return this.maximumSize;
    }

    protected MultiMapWithSet<Long, Long> getNodeIdentifiersToRemovedInEdges()
    {
        return this.borderFixer.getNodeIdentifiersToRemovedInEdges();
    }

    protected MultiMapWithSet<Long, Long> getNodeIdentifiersToRemovedOutEdges()
    {
        return this.borderFixer.getNodeIdentifiersToRemovedOutEdges();
    }

    protected LongToIntegerMultiMap getNodeIdentifierToAtlasIndices()
    {
        return this.nodeIdentifierToAtlasIndices;
    }

    protected MultiMapWithSet<Long, Long> getRelationIdentifiersToRemovedEdgeMembers()
    {
        return this.borderFixer.getRelationIdentifiersToRemovedEdgeMembers();
    }

    protected int getSubArraySize()
    {
        return this.subArraySize;
    }

    /**
     * In case there is a master node overlapping this node, get the master node.
     *
     * @param identifier
     *            The node identifier to query
     * @return The identifier of the master node that has the exact same location
     */
    protected Optional<Long> masterNode(final Long identifier)
    {
        return this.nodesFixer.masterNode(identifier);
    }

    /**
     * Take all the relations an {@link AtlasEntity} belongs to, and replace them with the
     * corresponding relations linking back to this {@link MultiAtlas}.
     *
     * @param entity
     *            The {@link AtlasEntity}
     * @return The relations of this entity, as viewed from this multi atlas.
     */
    protected Set<Relation> multifyRelations(final AtlasEntity entity)
    {
        final Set<Relation> subRelations = entity.relations();
        final Set<Relation> result = new HashSet<>();
        for (final Relation relation : subRelations)
        {
            result.add(relation(relation.getIdentifier()));
        }
        return result;
    }

    /**
     * In case this node is a master, get all the overlapping nodes.
     *
     * @param identifier
     *            The node identifier to query
     * @return The identifiers of the overlapping nodes that has the exact same location
     */
    protected Set<Long> overlappingNodes(final Long identifier)
    {
        return this.nodesFixer.overlappingNodes(identifier);
    }

    protected void populateReferences(final Atlas atlas, final int atlasIndex)
    {
        for (final Node node : atlas.nodes())
        {
            this.nodeIdentifierToAtlasIndices.add(node.getIdentifier(), atlasIndex);
        }
        for (final Edge edge : atlas.edges())
        {
            this.edgeIdentifierToAtlasIndex.put(edge.getIdentifier(), atlasIndex);
        }
        for (final Area area : atlas.areas())
        {
            this.areaIdentifierToAtlasIndices.add(area.getIdentifier(), atlasIndex);
        }
        for (final Line line : atlas.lines())
        {
            this.lineIdentifierToAtlasIndices.add(line.getIdentifier(), atlasIndex);
        }
        for (final Point point : atlas.points())
        {
            this.pointIdentifierToAtlasIndices.add(point.getIdentifier(), atlasIndex);
        }
        for (final Relation relation : atlas.relations())
        {
            this.relationIdentifierToAtlasIndices.add(relation.getIdentifier(), atlasIndex);
        }
    }

    protected List<Relation> relationAllRelationsWithSameOsmIdentifier(final long identifier)
    {
        final List<Relation> result = new ArrayList<>();
        final long osmIdentifier = this.relationIdentifierToRelationOsmIdentifier.get(identifier);
        for (final long candidateIdentifier : this.relationOsmIdentifierToRelationIdentifiers
                .get(osmIdentifier))
        {
            result.add(relation(candidateIdentifier));
        }
        return result;
    }

    protected void setEdgeIdentifierToAtlasIndex(final LongToIntegerMap edgeIdentifierToAtlasIndex)
    {
        this.edgeIdentifierToAtlasIndex = edgeIdentifierToAtlasIndex;
    }

    /**
     * Get the {@link Area} from the {@link Atlas} that has this identifier.
     *
     * @param identifier
     *            The identifier to query
     * @return The {@link Area}s that have this identifier
     */
    protected SubAreaList subAreas(final long identifier)
    {
        final List<Area> subAreas = new ArrayList<>();
        for (final int index : this.areaIdentifierToAtlasIndices.get(identifier))
        {
            if (index != -1)
            {
                subAreas.add(this.atlases.get(index).area(identifier));
            }
        }
        return new SubAreaList(subAreas);
    }

    /**
     * Get the {@link Edge} from the {@link Atlas} that has this identifier.
     *
     * @param identifier
     *            The identifier to query
     * @return The {@link Edge} with this identifier
     */
    protected Edge subEdge(final long identifier)
    {
        if (this.borderFixer.hasFixes())
        {
            final Edge subEdge = this.borderFixer.fixEdge(identifier);
            if (subEdge != null)
            {
                return subEdge;
            }
        }
        return this.atlases.get(this.edgeIdentifierToAtlasIndex.get(identifier)).edge(identifier);
    }

    /**
     * Get the {@link Line}s from the {@link Atlas} that has this identifier.
     *
     * @param identifier
     *            The identifier to query
     * @return The {@link Line}s that have this identifier
     */
    protected SubLineList subLines(final long identifier)
    {
        final List<Line> subLines = new ArrayList<>();
        for (final int index : this.lineIdentifierToAtlasIndices.get(identifier))
        {
            if (index != -1)
            {
                subLines.add(this.atlases.get(index).line(identifier));
            }
        }
        return new SubLineList(subLines);
    }

    /**
     * Get the nodes from different Atlases that have this identifier.
     *
     * @param identifier
     *            The identifier to query
     * @return The {@link Node}s that have this identifier
     */
    protected SubNodeList subNodes(final long identifier)
    {
        final List<Node> subNodes = new ArrayList<>();
        for (final int index : this.nodeIdentifierToAtlasIndices.get(identifier))
        {
            // Add all the sub nodes that come from regular sub atlas
            if (index != -1)
            {
                subNodes.add(this.atlases.get(index).node(identifier));
            }
        }
        Node fixNode = null;
        if (this.borderFixer.hasFixes())
        {
            if (this.borderFixer.nodeIsFixed(identifier))
            {
                // The MultiNode has to detect that there is an extra Atlas here, and to query the
                // removed nodes in/out edges maps to make sure to include only the proper
                // connectivity.
                fixNode = this.borderFixer.fixNode(identifier);
            }
        }
        return new SubNodeList(subNodes, fixNode);
    }

    /**
     * Get the {@link Point}s from the {@link Atlas} that has this identifier.
     *
     * @param identifier
     *            The identifier to query
     * @return The {@link Point}s that have this identifier
     */
    protected SubPointList subPoints(final long identifier)
    {
        final List<Point> subPoints = new ArrayList<>();
        for (final int index : this.pointIdentifierToAtlasIndices.get(identifier))
        {
            if (index != -1)
            {
                subPoints.add(this.atlases.get(index).point(identifier));
            }
        }
        return new SubPointList(subPoints);
    }

    /**
     * Get the {@link Relation}s from the {@link Atlas} that have this identifier.
     *
     * @param identifier
     *            The identifier to query
     * @return The {@link Relation}s that have this identifier
     */
    protected SubRelationList subRelations(final long identifier)
    {
        final List<Relation> subRelations = new ArrayList<>();
        for (final int index : this.relationIdentifierToAtlasIndices.get(identifier))
        {
            if (index != -1)
            {
                subRelations.add(this.atlases.get(index).relation(identifier));
            }
        }
        Relation fixRelation = null;
        if (this.borderFixer.hasFixes())
        {
            if (this.borderFixer.relationIsFixed(identifier))
            {
                // The MultiRelation has to detect that there is an extra Atlas here, and to query
                // the removed members maps to make sure to include only the proper members.
                fixRelation = this.borderFixer.fixRelation(identifier);
            }
        }
        return new SubRelationList(subRelations, fixRelation);
    }

    private AtlasMetaData mergeMetaData()
    {
        final AtlasSize size = new AtlasSize(this.numberOfEdges, this.numberOfNodes,
                this.numberOfAreas, this.numberOfLines, this.numberOfPoints,
                this.numberOfRelations);
        String codeVersion = null;
        String dataVersion = null;
        String shardName = null;
        final Map<String, String> tags = Maps.hashMap();
        // countries
        final StringList countries = new StringList(this.atlases.stream().map(Atlas::metaData)
                .map(AtlasMetaData::getCountry).filter(Optional::isPresent).map(Optional::get)
                .flatMap(value -> StringList.split(value, ",").stream()).distinct()
                .collect(Collectors.toList()));
        // code version
        final List<String> codeVersions = this.atlases.stream().map(Atlas::metaData)
                .map(AtlasMetaData::getCodeVersion).filter(Optional::isPresent).map(Optional::get)
                .distinct().collect(Collectors.toList());
        if (codeVersions.size() > 1)
        {
            logger.warn("Two sub atlas files have different code versions: {}", codeVersions);
        }
        if (codeVersions.size() > 0)
        {
            codeVersion = codeVersions.get(0);
        }
        // data version
        final List<String> dataVersions = this.atlases.stream().map(Atlas::metaData)
                .map(AtlasMetaData::getDataVersion).filter(Optional::isPresent).map(Optional::get)
                .distinct().collect(Collectors.toList());
        if (dataVersions.size() > 1)
        {
            logger.warn("Two sub atlas files have different data versions: {}", dataVersions);
        }
        if (dataVersions.size() > 0)
        {
            dataVersion = dataVersions.get(0);
        }
        // shard name
        final List<String> shardNames = this.atlases.stream().map(Atlas::metaData)
                .map(AtlasMetaData::getShardName).filter(Optional::isPresent).map(Optional::get)
                .distinct().collect(Collectors.toList());
        if (shardNames.size() > 1)
        {
            logger.warn("Two sub atlas files have different shard names: {}", shardNames);
        }
        if (shardNames.size() > 0)
        {
            shardName = shardNames.get(0);
        }
        // tags
        this.atlases.stream().map(Atlas::metaData).map(AtlasMetaData::getTags)
                .flatMap(map -> map.entrySet().stream()).forEach(entry ->
                {
                    final String key = entry.getKey();
                    final String value = entry.getValue();
                    if (tags.containsKey(key))
                    {
                        final String overridenValue = tags.get(key);
                        if (overridenValue != null && !overridenValue.equals(value))
                        {
                            logger.trace(
                                    "AtlasMetaData has conflicting values for the same key:"
                                            + " key = {}, and values = [{}, {}]. 2nd one is kept.",
                                    key, overridenValue, value);
                        }
                    }
                    tags.put(key, value);
                });
        return new AtlasMetaData(size, true, codeVersion, dataVersion,
                countries.isEmpty() ? null : countries.join(","), shardName, tags);
    }

    private RTree<Integer> newPackedAtlasSpatialIndex()
    {
        return new RTree<>();
    }
}
