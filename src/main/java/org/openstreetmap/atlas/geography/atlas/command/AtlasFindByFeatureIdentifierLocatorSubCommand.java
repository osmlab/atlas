package org.openstreetmap.atlas.geography.atlas.command;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.utilities.runtime.Command.Optionality;
import org.openstreetmap.atlas.utilities.runtime.Command.Switch;
import org.openstreetmap.atlas.utilities.runtime.Command.SwitchList;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * Parses an extended identifier including the way-sectioned id and feature type and searches for
 * the matching atlas objects in a collection of atlases.
 *
 * @author cstaylor
 */
public class AtlasFindByFeatureIdentifierLocatorSubCommand extends AbstractAtlasSubCommand
{
    private static final Switch<Set<String>> FEATURE_ID_PARAMETER = new Switch<>("id",
            "list of comma-delimited Atlas extended feature identifier",
            possibleMultipleOSMIdentifier -> Stream.of(possibleMultipleOSMIdentifier.split(","))
                    .collect(Collectors.toSet()),
            Optionality.REQUIRED);

    private static final int TYPE_INDEX = 1;

    private static final int ID_INDEX = 2;

    private static final int EXPECTED_IDENTIFIER_LENGTH = 4;

    private final Predicate<Node> nodeCheck = item -> this.nodeIds
            .contains(item.getOsmIdentifier());

    private final Predicate<Point> pointCheck = item -> this.pointIds
            .contains(item.getOsmIdentifier());

    private final Predicate<Area> areaCheck = item -> this.areaIds
            .contains(item.getOsmIdentifier());

    private final Predicate<Edge> edgeCheck = item -> item.getOsmIdentifier() > 0L
            && this.edgeIds.contains(item.getOsmIdentifier());

    private final Predicate<Line> lineCheck = item -> this.lineIds
            .contains(item.getOsmIdentifier());

    private final Predicate<Relation> relationCheck = item -> this.relationIds
            .contains(item.getOsmIdentifier());

    private final Set<Long> nodeIds = new HashSet<>();

    private final Set<Long> pointIds = new HashSet<>();

    private final Set<Long> areaIds = new HashSet<>();

    private final Set<Long> edgeIds = new HashSet<>();

    private final Set<Long> lineIds = new HashSet<>();

    private final Set<Long> relationIds = new HashSet<>();

    public AtlasFindByFeatureIdentifierLocatorSubCommand()
    {
        super("find",
                "find which atlas files contain particular OSM features using a given set of extended identifiers");
    }

    @Override
    public SwitchList switches()
    {
        return super.switches().with(FEATURE_ID_PARAMETER);
    }

    @Override
    public void usage(final PrintStream writer)
    {
        writer.printf(AtlasCommandConstants.INPUT_PARAMETER_DESCRIPTION);
        writer.printf("-id=comma-separated atlas extended feature identifiers to search for\n");
    }

    protected String formatAtlasObject(final String type, final Atlas atlas,
            final AtlasEntity entity)
    {
        final String shardName = atlas.metaData().getShardName().orElse("UNKNOWN");
        return String.format("[%10s] [%d] [%d] --> [%s:%s] Tags: [%s]\n", type,
                entity.getOsmIdentifier(), entity.getIdentifier(), shardName, atlas.getName(),
                entity.getTags());
    }

    @Override
    protected void handle(final Atlas atlas, final CommandMap command)
    {
        final int[] found = { 0 };
        atlas.nodes(this.nodeCheck).forEach(item ->
        {
            System.out.printf(formatAtlasObject("NODE", atlas, item));
            found[0]++;
        });

        atlas.points(this.pointCheck).forEach(item ->
        {
            System.out.printf(formatAtlasObject("POINT", atlas, item));
            found[0]++;
        });

        atlas.edges(this.edgeCheck).forEach(item ->
        {
            System.out.printf(formatAtlasObject("EDGE", atlas, item));
            found[0]++;
        });

        atlas.lines(this.lineCheck).forEach(item ->
        {
            System.out.printf(formatAtlasObject("LINE", atlas, item));
            found[0]++;
        });

        atlas.areas(this.areaCheck).forEach(item ->
        {
            System.out.printf(formatAtlasObject("AREA", atlas, item));
            found[0]++;
        });

        atlas.relations(this.relationCheck).forEach(item ->
        {
            System.out.printf(formatAtlasObject("RELATION", atlas, item));
            found[0]++;
        });
    }

    @Override
    protected void start(final CommandMap command)
    {
        @SuppressWarnings("unchecked")
        final Set<String> unparsedFeatureIds = (Set<String>) command.get(FEATURE_ID_PARAMETER);

        for (final String unparsedFeatureId : unparsedFeatureIds)
        {
            final String[] parsedFeatureIds = unparsedFeatureId.split("_");
            // By convention the third piece is the OSM ID, and the second piece is the type of item
            // we're searching for.
            if (parsedFeatureIds.length != EXPECTED_IDENTIFIER_LENGTH)
            {
                throw new CoreException(
                        "There should be four pieces in an extended feature identifier: {}",
                        unparsedFeatureId);
            }

            final long osmId = Long.parseLong(parsedFeatureIds[ID_INDEX]);

            if (parsedFeatureIds[TYPE_INDEX].equals("N"))
            {
                this.nodeIds.add(osmId);
                this.pointIds.add(osmId);
            }
            else if (parsedFeatureIds[TYPE_INDEX].equals("W"))
            {
                this.areaIds.add(osmId);
                this.lineIds.add(osmId);
                this.edgeIds.add(osmId);
            }
            else if (parsedFeatureIds[TYPE_INDEX].equals("R"))
            {
                this.relationIds.add(osmId);
            }
            else
            {
                throw new CoreException("Unknown type: {}", parsedFeatureIds[TYPE_INDEX]);
            }
        }
    }
}
