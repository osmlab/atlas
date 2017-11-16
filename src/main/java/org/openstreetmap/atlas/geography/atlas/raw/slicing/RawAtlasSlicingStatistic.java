package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.function.Consumer;

import org.openstreetmap.atlas.utilities.statistic.storeless.CounterWithStatistic;
import org.slf4j.Logger;

/**
 * Tracks all notable events that happened during country slicing.
 *
 * @author mgostintsev
 */
public class RawAtlasSlicingStatistic
{
    private static final long LOG_FREQUENCY = 10_000;
    private final Logger logger;

    // Processed entities
    private final CounterWithStatistic lines;
    private final CounterWithStatistic points;
    private final CounterWithStatistic relations;

    // Sliced entities
    private final CounterWithStatistic slicedLines;
    private final CounterWithStatistic slicedRelations;

    // Skipped entities
    private final CounterWithStatistic skippedLines;
    private final CounterWithStatistic skipedRelations;

    // TOOD statistics on intersections? Rethink what to track!

    public RawAtlasSlicingStatistic(final Logger logger)
    {
        this.logger = logger;
        final Consumer<String> log = logger::info;

        this.points = new CounterWithStatistic(this.logger, LOG_FREQUENCY, "Processed Point");
        this.points.logUsingLevel(log);
        this.lines = new CounterWithStatistic(this.logger, LOG_FREQUENCY, "Processed Line");
        this.lines.logUsingLevel(log);
        this.relations = new CounterWithStatistic(this.logger, LOG_FREQUENCY, "Processed Relation");
        this.relations.logUsingLevel(log);

        this.slicedLines = new CounterWithStatistic(this.logger, LOG_FREQUENCY, "Sliced Line");
        this.slicedLines.logUsingLevel(log);
        this.slicedRelations = new CounterWithStatistic(this.logger, LOG_FREQUENCY,
                "Sliced Relation");
        this.slicedRelations.logUsingLevel(log);

        this.skippedLines = new CounterWithStatistic(this.logger, LOG_FREQUENCY, "Skipped Line");
        this.skippedLines.logUsingLevel(log);
        this.skipedRelations = new CounterWithStatistic(this.logger, LOG_FREQUENCY,
                "Skipped Relation");
        this.skipedRelations.logUsingLevel(log);
    }

    public void recordProcessedLine()
    {
        this.lines.increment();
    }

    public void recordProcessedPoint()
    {
        this.points.increment();
    }

    public void recordProcessedRelation()
    {
        this.relations.increment();
    }

    public void recordSkippedLine()
    {
        this.skippedLines.increment();
    }

    public void recordSkippedRelation()
    {
        this.skipedRelations.increment();
    }

    public void recordSlicedLine()
    {
        this.slicedLines.increment();
    }

    public void recordSlicedRelation()
    {
        this.slicedRelations.increment();
    }

    public void summary()
    {
        this.logger.trace("Raw Atlas Country Slicing Summary");
        this.points.summaryWithoutTimer();
        this.lines.summaryWithoutTimer();
        this.relations.summaryWithoutTimer();
        this.slicedLines.summaryWithoutTimer();
        this.slicedRelations.summaryWithoutTimer();
        this.skippedLines.summaryWithoutTimer();
        this.skipedRelations.summaryWithoutTimer();
    }
}
