package org.openstreetmap.atlas.geography.atlas.pbf.loading;

import java.util.Map;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.utilities.statistic.storeless.CounterWithStatistic;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OsmPbfCounter} is responsible for counting the number of {@link Point}s, {@link Line}s
 * and {@link org.openstreetmap.atlas.geography.atlas.items.Relation}s can be extracted from the
 * given OSM PBF file. This information will be used to populate the {@link AtlasSize} field to
 * efficiently construct a Raw {@link Atlas}.
 *
 * @author mgostintsev
 */
public class OsmPbfCounter implements Sink
{
    private static final Logger logger = LoggerFactory.getLogger(OsmPbfCounter.class);

    private final CounterWithStatistic pointCount = new CounterWithStatistic(logger);
    private final CounterWithStatistic lineCount = new CounterWithStatistic(logger);
    private final CounterWithStatistic relationCount = new CounterWithStatistic(logger);
    private final AtlasLoadingOption loadingOption;

    /**
     * Default constructor.
     *
     * @param loadingOption
     *            The {@link AtlasLoadingOption} to use
     */
    public OsmPbfCounter(final AtlasLoadingOption loadingOption)
    {
        this.loadingOption = loadingOption;
    }

    @Override
    public void complete()
    {
        // No-Op
    }

    @Override
    public void initialize(final Map<String, Object> metaData)
    {
        logger.info("Initialized OSM PBF Counter successfully");
    }

    /**
     * @return the number of {@link Line} objects found
     */
    public long lineCount()
    {
        return this.lineCount.count();
    }

    /**
     * @return the number of {@link Point} objects found
     */
    public long pointCount()
    {
        return this.pointCount.count();
    }

    @Override
    public void process(final EntityContainer entityContainer)
    {
        final Entity rawEntity = entityContainer.getEntity();

        if (OsmPbfReader.shouldProcessEntity(this.loadingOption, rawEntity))
        {
            if (this.loadingOption.isLoadOsmNode() && rawEntity instanceof Node)
            {
                this.pointCount.increment();
            }
            else if (this.loadingOption.isLoadOsmWay() && rawEntity instanceof Way)
            {
                this.lineCount.increment();
            }
            else if (this.loadingOption.isLoadOsmRelation() && rawEntity instanceof Relation)
            {
                this.relationCount.increment();
            }
            else if (rawEntity instanceof Bound)
            {
                logger.trace("Encountered PBF Bound {}, skipping over it.", rawEntity.getId());
            }
        }
    }

    /**
     * @return the number of {@link org.openstreetmap.atlas.geography.atlas.items.Relation} objects
     *         found.
     */
    public long relationCount()
    {
        return this.relationCount.count();
    }

    @Override
    public void release()
    {
        logger.info("Released OSM PBF Counter");
    }
}
