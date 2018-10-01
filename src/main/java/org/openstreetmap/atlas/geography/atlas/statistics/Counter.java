package org.openstreetmap.atlas.geography.atlas.statistics;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.Crawler;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.Coverage;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.area.LakeAreaCoverage;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.area.RiverAreaCoverage;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.BusRouteLinearCoverage;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge.AllHighwayTagEdgeCoverage;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge.BridgeEdgeCoverage;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge.FerryEdgeCoverage;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge.FreshnessEdgeCoverage;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge.LanesEdgeCoverage;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge.NameEdgeCoverage;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge.NoNameEdgeCoverage;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge.OneWayEdgeCoverage;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge.PrivateAccessEdgeCoverage;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge.ReferenceEdgeCoverage;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge.SpeedLimitEdgeCoverage;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge.SurfaceEdgeCoverage;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge.TollEdgeCoverage;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.edge.TunnelEdgeCoverage;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.line.RailLineCoverage;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.line.RiverLineCoverage;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.linear.line.TransitRailLineCoverage;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.poi.EdgesCountCoverage;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.poi.LastUserNameCountCoverage;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.poi.OneWayEdgesCountCoverage;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.poi.SimpleCoverage;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

/**
 * @author matthieun
 */
public class Counter extends Crawler
{
    private static final Logger logger = LoggerFactory.getLogger(Counter.class);
    private static final long LAST_USER_EDITS_CUTOFF = 1_000L;

    public static final Switch<Resource> POI_COUNTS_DEFINITION = new Switch<>("poiCounts",
            "file containing all the poi counts definition", value ->
            {
                final Resource defaultResource = new InputStreamResource(
                        () -> SimpleCoverage.class.getResourceAsStream("counts.txt"));
                if ("".equals(value))
                {
                    return defaultResource;
                }
                else
                {
                    try
                    {
                        return new File(value);
                    }
                    catch (final Exception e)
                    {
                        return defaultResource;
                    }
                }
            }, Optionality.OPTIONAL, "");

    private Resource countsDefinition = POI_COUNTS_DEFINITION.getDefault();

    private Sharding sharding;

    public static void main(final String[] args)
    {
        new Counter().run(args);
    }

    public Counter()
    {
        super(logger);
    }

    public List<Coverage<? extends AtlasEntity>> generateCoverages(final Atlas atlas)
    {
        final List<Coverage<? extends AtlasEntity>> coverages = new ArrayList<>();

        // Areas
        coverages.add(new LakeAreaCoverage(atlas));
        coverages.add(new RiverAreaCoverage(atlas));

        // Edges
        coverages.add(new SpeedLimitEdgeCoverage(atlas, HighwayTag::isCarNavigableHighway));
        coverages.add(new LanesEdgeCoverage(atlas, HighwayTag::isCarNavigableHighway));
        coverages.add(new SurfaceEdgeCoverage(atlas, HighwayTag::isMetricHighway));
        coverages.add(new NameEdgeCoverage(atlas, HighwayTag::isMetricHighway, "length_named"));
        coverages.add(new NameEdgeCoverage(atlas, HighwayTag::isCarNavigableHighway,
                "length_roads_named"));
        coverages.add(new NoNameEdgeCoverage(atlas, HighwayTag::isCarNavigableHighway));
        coverages.add(new OneWayEdgeCoverage(atlas, HighwayTag::isCarNavigableHighway));
        coverages.add(
                new AllHighwayTagEdgeCoverage(atlas, HighwayTag::isMetricHighway, "length_total"));
        coverages.add(new AllHighwayTagEdgeCoverage(atlas, HighwayTag::isCarNavigableHighway,
                "length_roads_total"));
        coverages.add(new BridgeEdgeCoverage(atlas));
        coverages.add(new TunnelEdgeCoverage(atlas));
        coverages.add(new FerryEdgeCoverage(atlas));
        coverages.add(new AllHighwayTagEdgeCoverage(atlas, HighwayTag::isPedestrianNavigableHighway,
                "length_roads_pedestrian"));
        coverages.add(new ReferenceEdgeCoverage(atlas, HighwayTag::isCarNavigableHighway));
        coverages.add(new TollEdgeCoverage(atlas, HighwayTag::isCarNavigableHighway));
        coverages.add(new PrivateAccessEdgeCoverage(atlas, HighwayTag::isCarNavigableHighway));

        // LineItems
        coverages.add(new BusRouteLinearCoverage(atlas));

        // Lines
        coverages.add(new RiverLineCoverage(atlas));
        coverages.add(new RailLineCoverage(atlas));
        coverages.add(new TransitRailLineCoverage(atlas));

        // POIs
        SimpleCoverage.parseSimpleCoverages(atlas, this.countsDefinition.lines())
                .forEach(coverages::add);
        coverages.add(new EdgesCountCoverage(atlas, edge -> HighwayTag.isMetricHighway(edge)
                && edge.length().isGreaterThan(Distance.ZERO) && edge.asPolyLine().size() > 1));
        coverages.add(new OneWayEdgesCountCoverage(atlas, edge -> HighwayTag.isMetricHighway(edge)
                && edge.length().isGreaterThan(Distance.ZERO) && edge.asPolyLine().size() > 1));
        coverages.add(new LastUserNameCountCoverage(atlas, LAST_USER_EDITS_CUTOFF));

        // Freshness
        coverages.add(new FreshnessEdgeCoverage(atlas));

        // Sharding related adjustments
        if (this.sharding != null)
        {
            coverages.forEach(coverage -> coverage.setShardDivisor(entity ->
            {
                if (entity instanceof AtlasItem && !(entity instanceof LocationItem))
                {
                    final PolyLine geometry;
                    if (entity instanceof LineItem)
                    {
                        geometry = ((LineItem) entity).asPolyLine();
                    }
                    else if (entity instanceof Area)
                    {
                        geometry = ((Area) entity).asPolygon();
                    }
                    else
                    {
                        throw new CoreException("Unknown entity type: {}",
                                entity.getClass().getCanonicalName());
                    }
                    return Iterables.size(this.sharding.shardsIntersecting(geometry));
                }
                else
                {
                    // Skip relations, points and nodes
                }
                return 1;
            }));
        }
        return coverages;
    }

    public AtlasStatistics processAtlas(final Atlas atlas)
    {
        final AtlasStatistics result = new AtlasStatistics();
        generateCoverages(atlas).forEach(coverage ->
        {
            coverage.run();
            result.append(coverage.getStatistic());
        });
        return result;
    }

    public void setCountsDefinition(final Resource countsDefinition)
    {
        this.countsDefinition = countsDefinition;
    }

    public Counter withSharding(final Sharding sharding)
    {
        this.sharding = sharding;
        return this;
    }

    @Override
    protected void initialize(final CommandMap command)
    {
        this.countsDefinition = (Resource) command.get(POI_COUNTS_DEFINITION);
        logger.info("Using {} for POI counts", this.countsDefinition);
    }

    @Override
    protected void processAtlas(final String atlasName, final Atlas atlas, final String folder)
    {
        final File file = new File(folder).child(atlasName + "-statistics.csv");
        final AtlasStatistics statistics = processAtlas(atlas);
        file.writeAndClose(statistics.toString());
    }

    @Override
    protected SwitchList switches()
    {
        return super.switches().with(POI_COUNTS_DEFINITION);
    }
}
