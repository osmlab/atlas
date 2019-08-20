package org.openstreetmap.atlas.geography.sharding;

import java.io.BufferedWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder.LocationIterableProperties;
import org.openstreetmap.atlas.geography.geojson.GeoJsonObject;
import org.openstreetmap.atlas.geography.sharding.preparation.TilePrinter;
import org.openstreetmap.atlas.streaming.Streams;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.streaming.writers.JsonWriter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quad Tree sharding. The name does not reflect QuadTree to not confuse with the quad tree spatial
 * index.
 * <p>
 * The {@link Command} portion of this class is to read a csv file containing feature counts
 * (usually ways) for each of the maxZoom - 1 tiles. This is generated in the OSM database snapshot,
 * using the {@link TilePrinter} class to create the list of tiles needed. The csv file that is read
 * helps generate a tree, which is then serialized.
 *
 * @author matthieun
 * @author mgostintsev
 */
public class DynamicTileSharding extends Command implements Sharding
{
    /**
     * Node of the quad tree. Implementation is recursive.
     *
     * @author matthieun
     */
    private static class Node implements Located, Serializable
    {
        private static final int MAXIMUM_CHILDREN = 4;
        private static final long serialVersionUID = -7789058745501080439L;
        private final List<Node> children;
        private final SlippyTile tile;

        protected static Node read(final Resource resource)
        {
            return read(resource.lines().iterator());
        }

        private static Node read(final Iterator<String> lineIterator)
        {
            final String line = lineIterator.next();
            final List<Node> children = new ArrayList<>();
            String tileName = line;
            if (line.endsWith("+"))
            {
                for (int i = 0; i < MAXIMUM_CHILDREN; i++)
                {
                    children.add(read(lineIterator));
                }
                tileName = line.substring(0, line.length() - 1);
            }
            return new Node(SlippyTile.forName(tileName), children);
        }

        protected Node()
        {
            this(SlippyTile.ROOT);
        }

        private Node(final SlippyTile tile)
        {
            this.tile = tile;
            this.children = new ArrayList<>();
        }

        private Node(final SlippyTile tile, final List<Node> children)
        {
            this.tile = tile;
            this.children = children;
        }

        @Override
        public Rectangle bounds()
        {
            return this.tile.bounds();
        }

        @Override
        public boolean equals(final Object other)
        {
            if (other instanceof Node)
            {
                return ((Node) other).getTile().equals(this.tile);
            }
            return false;
        }

        public SlippyTile getTile()
        {
            return this.tile;
        }

        @Override
        public int hashCode()
        {
            return this.tile.hashCode();
        }

        public Set<Node> leafNodes(final GeometricSurface surface)
        {
            final Set<Node> result = new HashSet<>();
            final Rectangle polygonBounds = surface.bounds();
            if (polygonBounds.overlaps(bounds()))
            {
                if (isFinal() && surface.overlaps(bounds()))
                {
                    result.add(this);
                }
                else
                {
                    for (final Node child : this.children)
                    {
                        result.addAll(child.leafNodes(surface));
                    }
                }
            }
            return result;
        }

        public Set<Node> leafNodesCovering(final Location location)
        {
            final Set<Node> result = new HashSet<>();
            if (bounds().fullyGeometricallyEncloses(location))
            {
                if (isFinal())
                {
                    result.add(this);
                }
                else
                {
                    for (final Node child : this.children)
                    {
                        result.addAll(child.leafNodesCovering(location));
                    }
                }
            }
            return result;
        }

        public Set<Node> leafNodesIntersecting(final PolyLine polyLine)
        {
            final Set<Node> result = new HashSet<>();
            final Rectangle polyLineBounds = polyLine.bounds();
            if (polyLineBounds.overlaps(bounds()))
            {
                if (isFinal() && (polyLine.intersects(bounds())
                        || bounds().fullyGeometricallyEncloses(polyLine)))
                {
                    result.add(this);
                }
                else
                {
                    for (final Node child : this.children)
                    {
                        result.addAll(child.leafNodesIntersecting(polyLine));
                    }
                }
            }
            return result;
        }

        public LocationIterableProperties toGeoJsonBuildingBlock()
        {
            final Map<String, String> tags = new HashMap<>();
            tags.put("tile", this.name());
            return new GeoJsonBuilder.LocationIterableProperties(bounds(), tags);
        }

        protected void build(final Predicate<SlippyTile> shouldSplit)
        {
            if (this.zoom() < SlippyTile.MAX_ZOOM && shouldSplit.test(this.tile))
            {
                this.split();
                for (final Node child : this.children)
                {
                    child.build(shouldSplit);
                }
            }
        }

        protected boolean isFinal()
        {
            return this.children.isEmpty();
        }

        protected String name()
        {
            return this.tile.getName();
        }

        protected Set<Node> neighbors(final SlippyTile targetTile)
        {
            final Set<Node> neighboringNodes = new HashSet<>();

            for (final Node leafNode : this.leafNodes(targetTile.bounds()))
            {
                final Rectangle expandedBoundary = leafNode.bounds()
                        .expand(SlippyTile.calculateExpansionDistance(leafNode.bounds()));
                if (targetTile.bounds().overlaps(expandedBoundary)
                        && !leafNode.bounds().equals(targetTile.bounds()))
                {
                    neighboringNodes.add(leafNode);
                }
            }

            return neighboringNodes;
        }

        protected void save(final WritableResource resource)
        {
            final BufferedWriter writer = resource.writer();
            try
            {
                this.save(writer);
            }
            catch (final Exception e)
            {
                Streams.close(writer);
                throw e;
            }
            Streams.close(writer);
        }

        protected void split()
        {
            this.children.addAll(this.tile.split(this.zoom() + 1).stream().map(Node::new)
                    .collect(Collectors.toList()));
        }

        protected int zoom()
        {
            return this.tile.getZoom();
        }

        /**
         * Does a deep equals with the other node
         *
         * @param other
         *            other Node
         * @return true if entire structure is equal, false if not
         */
        private boolean deepEquals(final Node other)
        {
            final Comparator<Node> nodeCompare = Comparator.comparing(Node::getTile);
            // BFS through both trees to get equality
            final Queue<Node> queue = new LinkedList<>();
            queue.offer(this);
            queue.offer(other);
            while (!queue.isEmpty())
            {
                // We always offer two at a time, so we can poll two at a time.
                final Node node1 = queue.poll();
                final Node node2 = queue.poll();
                if (node1.equals(node2) && node1.getChildren().size() == node2.getChildren().size())
                {
                    final List<Node> children1 = node1.getChildren();
                    final List<Node> children2 = node2.getChildren();
                    children1.sort(nodeCompare);
                    children2.sort(nodeCompare);
                    for (int index = 0; index < children1.size(); index++)
                    {
                        queue.offer(children1.get(index));
                        queue.offer(children2.get(index));
                    }
                }
                else
                {
                    return false;
                }
            }
            return true;
        }

        private List<Node> getChildren()
        {
            return this.children;
        }

        private void save(final BufferedWriter writer)
        {
            try
            {
                writer.write(this.tile.getName());
                if (!isFinal())
                {
                    writer.write("+");
                }
                writer.write("\n");
            }
            catch (final Exception e)
            {
                throw new CoreException("Unable to write slippy tile {}", this.tile, e);
            }
            for (final Node child : this.children)
            {
                child.save(writer);
            }
        }
    }

    public static final Switch<Resource> DEFINITION = new Switch<>("definition",
            "Resource containing the maxZoom - 1 tile to feature count mapping.", File::new,
            Optionality.REQUIRED);
    public static final Switch<WritableResource> GEOJSON = new Switch<>("geoJson",
            "The resource where to save the geojson tree for debugging", File::new,
            Optionality.OPTIONAL);
    public static final Switch<Integer> MAXIMUM_COUNT = new Switch<>("maxCount",
            "The maximum feature count. Any cell with a larger feature count will be split, up to maxZoom",
            Integer::valueOf, Optionality.OPTIONAL, "200000");
    public static final Switch<Integer> MAXIMUM_ZOOM = new Switch<>("maxZoom", "The maximum zoom",
            Integer::valueOf, Optionality.OPTIONAL, "10");
    public static final Switch<Integer> MINIMUM_ZOOM = new Switch<>("minZoom", "The minimum zoom",
            Integer::valueOf, Optionality.OPTIONAL, "5");
    public static final Switch<WritableResource> OUTPUT = new Switch<>("output",
            "The resource where to save the serialized tree.", File::new, Optionality.REQUIRED);
    private static final int MINIMUM_TO_SPLIT = 1_000;
    private static final int READER_REPORT_FREQUENCY = 10_000_000;
    private static final Logger logger = LoggerFactory.getLogger(DynamicTileSharding.class);
    private static final long serialVersionUID = 229952569300405488L;
    // The root of the tree for this dynamic sharding
    private final Node root;

    public static void main(final String[] args)
    {
        new DynamicTileSharding().run(args);
    }

    /**
     * Construct.
     *
     * @param resource
     *            The resource containing the serialized tree definition.
     */
    public DynamicTileSharding(final Resource resource)
    {
        this.root = Node.read(resource);
    }

    /**
     * Construct, with a root that covers the whole world.
     */
    private DynamicTileSharding()
    {
        this.root = new Node();
    }

    @Override
    public boolean equals(final Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (other == null || getClass() != other.getClass())
        {
            return false;
        }

        final DynamicTileSharding that = (DynamicTileSharding) other;
        return this.root.deepEquals(that.root);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.root);
    }

    @Override
    public Iterable<Shard> neighbors(final Shard shard)
    {
        return this.root.neighbors(SlippyTile.forName(shard.getName())).stream().map(Node::getTile)
                .collect(Collectors.toList());
    }

    /**
     * Save the tree to a {@link WritableResource}
     *
     * @param resource
     *            The {@link WritableResource} to serialize the tree definition to.
     */
    public void save(final WritableResource resource)
    {
        this.root.save(resource);
    }

    public void saveAsGeoJson(final WritableResource resource)
    {
        final JsonWriter writer = new JsonWriter(resource);
        final GeoJsonObject geoJson = new GeoJsonBuilder().create(Iterables
                .translate(this.root.leafNodes(Rectangle.MAXIMUM), Node::toGeoJsonBuildingBlock));
        writer.write(geoJson.jsonObject());
        writer.close();
    }

    @Override
    public Iterable<Shard> shards(final GeometricSurface surface)
    {
        return Iterables.stream(this.root.leafNodes(surface)).map(Node::getTile);
    }

    @Override
    public Iterable<Shard> shardsCovering(final Location location)
    {
        return Iterables.stream(this.root.leafNodesCovering(location)).map(Node::getTile);
    }

    @Override
    public Iterable<Shard> shardsIntersecting(final PolyLine polyLine)
    {
        return Iterables.stream(this.root.leafNodesIntersecting(polyLine)).map(Node::getTile);
    }

    /**
     * Calculates and saves the counts for each zoom layer lower than firstZoomLayerToGenerate.
     *
     * @param firstZoomLayerToGenerate
     *            the first zoom layer for which to generate counts
     * @param counts
     *            Map containing counts for all {@link SlippyTile}s in firstZoomLayerToGenerate+1
     * @return a Map containing counts for all (@link SlippyTile}s in zoomLayerToGenerate and below
     */
    protected Map<SlippyTile, Long> calculateTileCountsForAllZoom(
            final int firstZoomLayerToGenerate, final Map<SlippyTile, Long> counts)
    {
        for (int currentZoom = firstZoomLayerToGenerate; currentZoom >= 0; currentZoom--)
        {
            long count = 0;
            long tilesCalculated = 0;
            for (int x = 0; x < Math.pow(2, currentZoom + 1.0); x += 2)
            {
                for (int y = 0; y < Math.pow(2, currentZoom + 1.0); y += 2)
                {
                    count = 0;
                    // top left
                    count += counts.getOrDefault(new SlippyTile(x, y, currentZoom + 1), (long) 0);
                    // top right
                    count += counts.getOrDefault(new SlippyTile(x + 1, y, currentZoom + 1),
                            (long) 0);
                    // bottom left
                    count += counts.getOrDefault(new SlippyTile(x, y + 1, currentZoom + 1),
                            (long) 0);
                    // bottom right
                    count += counts.getOrDefault(new SlippyTile(x + 1, y + 1, currentZoom + 1),
                            (long) 0);
                    if (count != 0)
                    {
                        counts.put(new SlippyTile(x / 2, y / 2, currentZoom), count);
                    }
                    if (++tilesCalculated % READER_REPORT_FREQUENCY == 0)
                    {
                        logger.info("Calculated {} zoom level {} tiles.", tilesCalculated,
                                currentZoom);
                    }
                }
            }
        }
        return counts;
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final Resource definition = (Resource) command.get(DEFINITION);
        final int numberLines = (int) Iterables.size(definition.lines());
        logger.info("There are {} tiles.", numberLines);
        final Map<SlippyTile, Long> counts = new HashMap<>(numberLines);
        final WritableResource output = (WritableResource) command.get(OUTPUT);
        final int maximum = (int) command.get(MAXIMUM_COUNT);
        final int minimumZoom = (int) command.get(MINIMUM_ZOOM);
        final int maximumZoom = (int) command.get(MAXIMUM_ZOOM);
        final WritableResource geoJson = (WritableResource) command.get(GEOJSON);
        int zoom = 0;
        int counter = 0;
        for (final String line : definition.lines())
        {
            final StringList split = StringList.split(line, ",");
            final SlippyTile tile = SlippyTile.forName(split.get(0));
            counts.put(tile, Long.valueOf(split.get(1)));
            zoom = tile.getZoom();
            if (++counter % READER_REPORT_FREQUENCY == 0)
            {
                logger.info("Read counts for {} zoom level {} tiles.", counter, zoom);
            }
        }
        // maximumZoom is decremented by 2 because it represents the highest zoom that the sharding
        // tree will split to. The input CSV (definition) has count information at the
        // (maximumZoom-1) level, which is already put into the HashMap "counts" by the code above.
        // Therefore we want to start calculating counts one level below, which is (maximumZoom-2)
        final Map<SlippyTile, Long> allCounts = calculateTileCountsForAllZoom(maximumZoom - 2,
                counts);
        if (zoom == 0)
        {
            throw new CoreException("No tiles in definition");
        }
        final int finalZoom = zoom;
        if (maximumZoom > finalZoom + 1)
        {
            throw new CoreException(
                    "Cannot go over the resolution of the counts definition. "
                            + "MaxZoom = {} has to be at most equal to definition zoom + 1 = {}",
                    maximumZoom, finalZoom);
        }
        this.root.build(tile ->
        {
            final long count = allCounts.getOrDefault(tile, (long) 0);
            if (count <= MINIMUM_TO_SPLIT)
            {
                return false;
            }
            if (tile.getZoom() < minimumZoom)
            {
                return true;
            }
            if (tile.getZoom() >= maximumZoom)
            {
                return false;
            }
            return count > maximum;
        });
        this.save(output);
        final String outputLocation = lastRawCommand(OUTPUT);
        logger.info("Printed tree to {}. Loading for verification...", outputLocation);
        new DynamicTileSharding(new File(outputLocation));
        logger.info("Successfully loaded tree from {}", outputLocation);
        if (geoJson != null)
        {
            if (logger.isInfoEnabled())
            {
                logger.info("Saving geojson to {}...", lastRawCommand(GEOJSON));
            }
            this.saveAsGeoJson(geoJson);
        }
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(DEFINITION, OUTPUT, MINIMUM_ZOOM, MAXIMUM_ZOOM, MAXIMUM_COUNT,
                GEOJSON);
    }
}
