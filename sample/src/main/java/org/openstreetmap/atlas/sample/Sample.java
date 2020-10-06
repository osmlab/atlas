package org.openstreetmap.atlas.sample;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class Sample extends Command
{
    private static final Logger logger = LoggerFactory.getLogger(Sample.class);
    private static final int VALENCE_CUTOFF = 4;
    private static final int PRINT_CUTOFF = 10;

    private static final Switch<Resource> ATLAS = new Switch<>("atlas",
            "Path to the Atlas file to load", value -> new File(Path.of(value)),
            Optionality.REQUIRED);
    private static final Switch<Sharding> SHARDING = new Switch<>("sharding",
            "Sharding tree to load", Sharding::forString, Optionality.OPTIONAL, "geohash@8");

    public static void main(final String[] args)
    {
        new Sample().runWithoutQuitting(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        // Get resource from command
        final Resource atlasResource = (Resource) command.get(ATLAS);
        logger.info("");
        logger.info("Loading {}", atlasResource);
        // Load Atlas object in memory
        final Time start = Time.now();
        final Atlas atlas = new AtlasResourceLoader().load(atlasResource);
        // Print Atlas summary
        logger.info("");
        logger.info("Loaded Atlas in {}: {}", start.elapsedSince(), atlas);

        // Count the number of dual carriageway Edges:
        final long numberOfReverseEdges = Iterables.size(atlas.edges(Edge::hasReverseEdge));
        logger.info("");
        logger.info("The Atlas has {} dual carriageway edges.", numberOfReverseEdges);

        // Count the number of Nodes with valence >= VALENCE_CUTOFF (At least VALENCE_CUTOFF edges
        // connected), and print the most southern ones
        final Iterable<Node> valencePlusNodes = atlas
                .nodes(node -> node.valence() >= VALENCE_CUTOFF);
        final long numberOfValencePlusNodes = Iterables.size(valencePlusNodes);
        logger.info("");
        logger.info("The Atlas has {} nodes with valence greater than or equal to {}.",
                numberOfValencePlusNodes, VALENCE_CUTOFF);
        final Comparator<Node> latitudeComparator = (node1, node2) ->
        {
            final Latitude latitude1 = node1.getLocation().getLatitude();
            final Latitude latitude2 = node2.getLocation().getLatitude();
            return Double.compare(latitude1.asDegrees(), latitude2.asDegrees());
        };
        final SortedSet<Node> sortedValencePlusNodes = new TreeSet<>(latitudeComparator);
        valencePlusNodes.forEach(sortedValencePlusNodes::add);
        logger.info("");
        logger.info("{} southernmost nodes with valence greater than or equal to {}:", PRINT_CUTOFF,
                VALENCE_CUTOFF);
        sortedValencePlusNodes.stream().limit(PRINT_CUTOFF)
                .forEach(node -> logger.info("{}", node));

        // Take a random edge and see what sharding box it intersects
        final Sharding sharding = (Sharding) command.get(SHARDING);
        final Edge randomEdge = Iterables.stream(atlas.edges()).collectToList()
                .get(new Random().nextInt((int) atlas.numberOfEdges()));
        final Iterable<Shard> shards = sharding.shardsIntersecting(randomEdge.asPolyLine());
        logger.info("");
        logger.info("Shards intersecting Edge {}:", randomEdge.getIdentifier());
        shards.forEach(shard -> logger.info("{} with shape \"{}\"", shard, shard.toWkt()));

        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(ATLAS, SHARDING);
    }
}
