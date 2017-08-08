package org.openstreetmap.atlas.geography.atlas.packed;

import java.util.Map;
import java.util.PrimitiveIterator.OfLong;
import java.util.Random;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.utilities.random.RandomTagsSupplier;
import org.openstreetmap.atlas.utilities.random.RandomTextGenerator;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class RandomPackedAtlasBuilder
{
    /**
     * @author matthieun
     */
    public static class AtlasStartIdentifiers
    {
        private final long edgeStartIdentifier;
        private final long nodeStartIdentifier;
        private final long areaStartIdentifier;
        private final long lineStartIdentifier;
        private final long pointStartIdentifier;
        private final long relationStartIdentifier;

        public AtlasStartIdentifiers(final long edgeStartIdentifier, final long nodeStartIdentifier,
                final long areaStartIdentifier, final long lineStartIdentifier,
                final long pointStartIdentifier, final long relationStartIdentifier)
        {
            this.edgeStartIdentifier = edgeStartIdentifier;
            this.nodeStartIdentifier = nodeStartIdentifier;
            this.areaStartIdentifier = areaStartIdentifier;
            this.lineStartIdentifier = lineStartIdentifier;
            this.pointStartIdentifier = pointStartIdentifier;
            this.relationStartIdentifier = relationStartIdentifier;
        }

        public long getAreaStartIdentifier()
        {
            return this.areaStartIdentifier;
        }

        public long getEdgeStartIdentifier()
        {
            return this.edgeStartIdentifier;
        }

        public long getLineStartIdentifier()
        {
            return this.lineStartIdentifier;
        }

        public long getNodeStartIdentifier()
        {
            return this.nodeStartIdentifier;
        }

        public long getPointStartIdentifier()
        {
            return this.pointStartIdentifier;
        }

        public long getRelationStartIdentifier()
        {
            return this.relationStartIdentifier;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(RandomPackedAtlasBuilder.class);

    private static final String[] edgeHighway = new String[] { "motorway", "trunk", "primary",
            "secondary", "tertiary", "residential" };
    private final Random random;

    private final int printFrequency = 100_000;

    public static PackedAtlas generate(final long size, final long startIdentifier)
    {
        final Rectangle bounds = Location.TEST_5.boxAround(Distance.miles(100));
        final AtlasSize estimates = new AtlasSize(size, size, size, size, size, size);
        final AtlasStartIdentifiers startIdentifiers = new AtlasStartIdentifiers(startIdentifier,
                startIdentifier, startIdentifier + size, startIdentifier + 2 * size,
                startIdentifier + size, startIdentifier);
        return new RandomPackedAtlasBuilder().generate(estimates, startIdentifiers, bounds);
    }

    private static Map<String, String> randomTags(final int count)
    {
        final Map<String, String> tags1 = RandomTagsSupplier.randomTags(count - 1);
        final Map<String, String> tags2 = RandomTagsSupplier.randomTags(1, HighwayTag.KEY,
                edgeHighway);
        tags1.putAll(tags2);
        return tags1;
    }

    /**
     * Construct
     */
    public RandomPackedAtlasBuilder()
    {
        this.random = new Random();
    }

    /**
     * Build a random {@link Atlas}
     *
     * @param estimates
     *            The size of the generated atlas
     * @param startIdentifiers
     *            The start identifier for each category
     * @param bounds
     *            The bounds of the Atlas
     * @return A random Atlas with {@link Node}s, {@link Edge}s, {@link Area}s, {@link Line}s and
     *         {@link Point}s.
     */
    public PackedAtlas generate(final AtlasSize estimates,
            final AtlasStartIdentifiers startIdentifiers, final Rectangle bounds)
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        builder.setSizeEstimates(estimates);

        Time start = Time.now();
        for (long i = 0; i < estimates.getNodeNumber(); i++)
        {
            final Location geometry = Location.random(bounds);
            builder.addNode(startIdentifiers.getNodeStartIdentifier() + i, geometry,
                    RandomTagsSupplier.randomTags(5));
            if ((i + 1) % this.printFrequency == 0)
            {
                logger.info("Loaded {} Nodes.", i + 1);
            }
        }
        logger.info("Finished Loading {} Nodes in {}", estimates.getNodeNumber(),
                start.elapsedSince());
        start = Time.now();
        for (long i = 0; i < estimates.getEdgeNumber(); i++)
        {
            // Edges
            final Location[] tips = getTwoNodes(builder.peek(), estimates.getEdgeNumber());
            final PolyLine geometry = new PolyLine(tips[0], Location.random(bounds),
                    Location.random(bounds), Location.random(bounds), tips[1]);
            builder.addEdge(startIdentifiers.getEdgeStartIdentifier() + i, geometry,
                    RandomTagsSupplier.randomTags(5));
            if ((i + 1) % this.printFrequency == 0)
            {
                logger.info("Loaded {} Edges.", i + 1);
            }
        }
        logger.info("Finished Loading {} Edges in {}", estimates.getEdgeNumber(),
                start.elapsedSince());
        start = Time.now();
        for (long i = 0; i < estimates.getAreaNumber(); i++)
        {
            // Areas
            final Polygon geometry = Polygon.random(5, bounds);
            builder.addArea(startIdentifiers.getAreaStartIdentifier() + i, geometry,
                    RandomPackedAtlasBuilder.randomTags(5));
            if ((i + 1) % this.printFrequency == 0)
            {
                logger.info("Loaded {} Areas.", i + 1);
            }
        }
        logger.info("Finished Loading {} Areas in {}", estimates.getAreaNumber(),
                start.elapsedSince());
        start = Time.now();
        for (long i = 0; i < estimates.getLineNumber(); i++)
        {
            // Lines
            final PolyLine geometry = PolyLine.random(5, bounds);
            builder.addLine(startIdentifiers.getLineStartIdentifier() + i, geometry,
                    RandomTagsSupplier.randomTags(5));
            if ((i + 1) % this.printFrequency == 0)
            {
                logger.info("Loaded {} Lines.", i + 1);
            }
        }
        logger.info("Finished Loading {} Lines in {}", estimates.getLineNumber(),
                start.elapsedSince());
        start = Time.now();
        for (long i = 0; i < estimates.getPointNumber(); i++)
        {
            // Points
            final Location geometry = Location.random(bounds);
            builder.addPoint(startIdentifiers.getPointStartIdentifier() + i, geometry,
                    RandomTagsSupplier.randomTags(5));
            if ((i + 1) % this.printFrequency == 0)
            {
                logger.info("Loaded {} Points.", i + 1);
            }
        }
        logger.info("Finished Loading {} Points in {}", estimates.getPointNumber(),
                start.elapsedSince());
        start = Time.now();
        for (long i = 0; i < estimates.getRelationNumber(); i++)
        {
            // Relations
            final Random random = new Random();
            final RelationBean structure = new RelationBean();
            for (int j = 0; j < random.nextInt(5) + 5; j++)
            {
                boolean found = false;
                long featureIdentifier = -1;
                ItemType type = null;
                while (!found)
                {
                    final int typeIndex = random.nextInt(ItemType.values().length);
                    type = ItemType.values()[typeIndex];
                    switch (type)
                    {
                        case NODE:
                            featureIdentifier = builder.peek().nodeIdentifier(
                                    random.longs(0, estimates.getNodeNumber()).iterator().next());
                            found = true;
                            break;
                        case EDGE:
                            featureIdentifier = builder.peek().edgeIdentifier(
                                    random.longs(0, estimates.getEdgeNumber()).iterator().next());
                            found = true;
                            break;
                        case AREA:
                            featureIdentifier = builder.peek().areaIdentifier(
                                    random.longs(0, estimates.getAreaNumber()).iterator().next());
                            found = true;
                            break;
                        case LINE:
                            featureIdentifier = builder.peek().lineIdentifier(
                                    random.longs(0, estimates.getLineNumber()).iterator().next());
                            found = true;
                            break;
                        case POINT:
                            featureIdentifier = builder.peek().pointIdentifier(
                                    random.longs(0, estimates.getPointNumber()).iterator().next());
                            found = true;
                            break;
                        case RELATION:
                            if (i > 1)
                            {
                                // Make sure that this relation is not trying to depend on itself if
                                // it is the first one...
                                featureIdentifier = builder.peek().relationIdentifier(
                                        random.longs(0, i - 1).iterator().next());
                                found = true;
                            }
                            break;
                        default:
                            throw new CoreException("Unknown type {}", type);
                    }
                }
                structure.addItem(featureIdentifier, new RandomTextGenerator().newWord(), type);
            }

            final long identifier = startIdentifiers.getRelationStartIdentifier() + i;
            builder.addRelation(identifier, identifier, structure,
                    RandomTagsSupplier.randomTags(5));
            if ((i + 1) % this.printFrequency == 0)
            {
                logger.info("Loaded {} Relations.", i + 1);
            }
        }
        logger.info("Finished Loading {} Points in {}", estimates.getPointNumber(),
                start.elapsedSince());
        return (PackedAtlas) builder.get();
    }

    private Location[] getTwoNodes(final PackedAtlas packedAtlas, final long size)
    {
        final OfLong iterator = this.random.longs(0, size).iterator();
        final long index1 = iterator.next();
        final long index2 = iterator.next();
        final Location[] result = new Location[2];
        result[0] = packedAtlas.nodeLocation(index1);
        result[1] = packedAtlas.nodeLocation(index2);
        return result;
    }
}
