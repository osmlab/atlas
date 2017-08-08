package org.openstreetmap.atlas.geography.atlas.pbf;

import java.util.function.Consumer;

import org.openstreetmap.atlas.utilities.statistic.storeless.CounterWithStatistic;
import org.slf4j.Logger;

/**
 * Statistic for OsmPbfProcessor
 *
 * @author tony
 */
public class OsmPbfStatistic
{
    private final Logger logger;
    private final CounterWithStatistic osmEntityCounter;
    private final CounterWithStatistic osmNodeCounter;
    private final CounterWithStatistic osmWayCounter;
    private final CounterWithStatistic osmRelationCounter;
    private final CounterWithStatistic atlasNodeCounter;
    private final CounterWithStatistic atlasPointCounter;
    private final CounterWithStatistic atlasEdgeCounter;
    private final CounterWithStatistic atlasLineCounter;
    private final CounterWithStatistic atlasAreaCounter;
    private final CounterWithStatistic atlasRelationCounter;

    public OsmPbfStatistic(final Logger logger)
    {
        this.logger = logger;
        final Consumer<String> log = logger::trace;
        final long osmEntityLogFrequency = 100_000;
        final long atlasEntityLogFrequency = 10_000;
        this.osmEntityCounter = new CounterWithStatistic(this.logger, osmEntityLogFrequency,
                "PBF Entities");
        this.osmEntityCounter.logUsingLevel(log);
        this.osmNodeCounter = new CounterWithStatistic(this.logger, osmEntityLogFrequency,
                "PBF Node");
        this.osmNodeCounter.logUsingLevel(log);
        this.osmWayCounter = new CounterWithStatistic(this.logger, osmEntityLogFrequency,
                "PBF Way");
        this.osmWayCounter.logUsingLevel(log);
        this.osmRelationCounter = new CounterWithStatistic(this.logger, atlasEntityLogFrequency,
                "PBF Relation");
        this.osmRelationCounter.logUsingLevel(log);
        this.atlasNodeCounter = new CounterWithStatistic(this.logger, atlasEntityLogFrequency,
                "Atlas Node");
        this.atlasNodeCounter.logUsingLevel(log);
        this.atlasPointCounter = new CounterWithStatistic(this.logger, atlasEntityLogFrequency,
                "Atlas Point");
        this.atlasPointCounter.logUsingLevel(log);
        this.atlasEdgeCounter = new CounterWithStatistic(this.logger, atlasEntityLogFrequency,
                "Atlas Edge");
        this.atlasEdgeCounter.logUsingLevel(log);
        this.atlasLineCounter = new CounterWithStatistic(this.logger, atlasEntityLogFrequency,
                "Atlas Line");
        this.atlasLineCounter.logUsingLevel(log);
        this.atlasAreaCounter = new CounterWithStatistic(this.logger, atlasEntityLogFrequency,
                "Atlas Area");
        this.atlasAreaCounter.logUsingLevel(log);
        this.atlasRelationCounter = new CounterWithStatistic(this.logger, atlasEntityLogFrequency,
                "Atlas Relation");
        this.atlasRelationCounter.logUsingLevel(log);
    }

    public long atlasAreaNumber()
    {
        return this.atlasAreaCounter.count();
    }

    public long atlasEdgeNumber()
    {
        return this.atlasEdgeCounter.count();
    }

    public long atlasLineNumber()
    {
        return this.atlasLineCounter.count();
    }

    public void clear()
    {
        this.osmEntityCounter.clear();
        this.osmNodeCounter.clear();
        this.osmWayCounter.clear();
        this.osmRelationCounter.clear();
        this.atlasNodeCounter.clear();
        this.atlasPointCounter.clear();
        this.atlasEdgeCounter.clear();
        this.atlasLineCounter.clear();
        this.atlasAreaCounter.clear();
        this.atlasRelationCounter.clear();
    }

    public void incrementAtlasArea()
    {
        this.atlasAreaCounter.increment();
    }

    public void incrementAtlasEdge()
    {
        this.atlasEdgeCounter.increment();
    }

    public void incrementAtlasEdge(final int count)
    {
        this.atlasEdgeCounter.incrementCount(count);
    }

    public void incrementAtlasLine()
    {
        this.atlasLineCounter.increment();
    }

    public void incrementAtlasNode()
    {
        this.atlasNodeCounter.increment();
    }

    public void incrementAtlasPoint()
    {
        this.atlasPointCounter.increment();
    }

    public void incrementAtlasRelation()
    {
        this.atlasRelationCounter.increment();
    }

    public void incrementOsmEntity()
    {
        this.osmEntityCounter.increment();
    }

    public void incrementOsmNode()
    {
        this.osmNodeCounter.increment();
    }

    public void incrementOsmRelation()
    {
        this.osmRelationCounter.increment();
    }

    public void incrementOsmWay()
    {
        this.osmWayCounter.increment();
    }

    public void pauseOsmNodeCounter()
    {
        this.osmNodeCounter.pause();
    }

    public void pauseOsmRelationCounter()
    {
        this.osmRelationCounter.pause();
    }

    public void pauseOsmWayCounter()
    {
        this.osmWayCounter.pause();
    }

    public void summary()
    {
        this.logger.trace("PBF Loading Summary");
        this.osmEntityCounter.summary();
        this.osmNodeCounter.summary();
        this.osmWayCounter.summary();
        this.osmRelationCounter.summary();
        this.atlasNodeCounter.summaryWithoutTimer();
        this.atlasPointCounter.summaryWithoutTimer();
        this.atlasEdgeCounter.summaryWithoutTimer();
        this.atlasLineCounter.summaryWithoutTimer();
        this.atlasAreaCounter.summaryWithoutTimer();
        this.atlasRelationCounter.summaryWithoutTimer();
    }

}
