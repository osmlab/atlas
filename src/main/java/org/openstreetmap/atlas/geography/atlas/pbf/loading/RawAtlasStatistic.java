package org.openstreetmap.atlas.geography.atlas.pbf.loading;

import java.util.function.Consumer;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.utilities.statistic.storeless.CounterWithStatistic;
import org.slf4j.Logger;

/**
 * Keeps track of created and filtered (by configuration) {@link Point}s, {@link Line}s and
 * {@link Relation}s during raw {@link Atlas} generation - see {@link OsmPbfReader}. Also, tracks
 * any {@link Relation}s that were dropped.
 *
 * @author mgostintsev
 */
public class RawAtlasStatistic
{
    private static final long LOG_FREQUENCY = 10_000;
    private final Logger logger;

    // Added entities
    private final CounterWithStatistic points;
    private final CounterWithStatistic lines;
    private final CounterWithStatistic relations;

    // Filtered entities
    private final CounterWithStatistic filteredNodes;
    private final CounterWithStatistic filteredWays;
    private final CounterWithStatistic filteredRelations;

    // Dropped relations
    private final CounterWithStatistic droppedRelations;

    public RawAtlasStatistic(final Logger logger)
    {
        this.logger = logger;
        final Consumer<String> log = logger::info;

        this.points = new CounterWithStatistic(this.logger, LOG_FREQUENCY, "Added Point");
        this.points.logUsingLevel(log);
        this.lines = new CounterWithStatistic(this.logger, LOG_FREQUENCY, "Added Line");
        this.lines.logUsingLevel(log);
        this.relations = new CounterWithStatistic(this.logger, LOG_FREQUENCY, "Added Relation");
        this.relations.logUsingLevel(log);

        this.filteredNodes = new CounterWithStatistic(this.logger, LOG_FREQUENCY, "Filtered Node");
        this.filteredNodes.logUsingLevel(log);
        this.filteredWays = new CounterWithStatistic(this.logger, LOG_FREQUENCY, "Filtered Way");
        this.filteredWays.logUsingLevel(log);
        this.filteredRelations = new CounterWithStatistic(this.logger, LOG_FREQUENCY,
                "Filtered Relation");
        this.filteredRelations.logUsingLevel(log);

        this.droppedRelations = new CounterWithStatistic(this.logger, LOG_FREQUENCY,
                "Dropped Relation");
        this.droppedRelations.logUsingLevel(log);
    }

    public void recordCreatedLine()
    {
        this.lines.increment();
    }

    public void recordCreatedPoint()
    {
        this.points.increment();
    }

    public void recordCreatedRelation()
    {
        this.relations.increment();
    }

    public void recordDroppedRelation()
    {
        this.droppedRelations.increment();
    }

    public void recordFilteredNode()
    {
        this.filteredNodes.increment();
    }

    public void recordFilteredRelation()
    {
        this.filteredRelations.increment();
    }

    public void recordFilteredWay()
    {
        this.filteredWays.increment();
    }

    public void summary()
    {
        this.logger.trace("PBF to Raw Atlas Summary");
        this.points.summaryWithoutTimer();
        this.lines.summaryWithoutTimer();
        this.relations.summaryWithoutTimer();
        this.filteredNodes.summaryWithoutTimer();
        this.filteredWays.summaryWithoutTimer();
        this.filteredRelations.summaryWithoutTimer();
        this.droppedRelations.summaryWithoutTimer();
    }
}
