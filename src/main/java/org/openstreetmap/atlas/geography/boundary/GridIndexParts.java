package org.openstreetmap.atlas.geography.boundary;

import org.openstreetmap.atlas.utilities.maps.MultiMap;

import com.vividsolutions.jts.geom.Envelope;

/**
 * POJO to hold all data required to rebuild a Grid Index.
 *
 * @author mgostintsev
 */
public class GridIndexParts
{
    private final MultiMap<String, Envelope> spatialIndexCells;
    private final Envelope envelope;

    public GridIndexParts(final MultiMap<String, Envelope> spatialIndexCells,
            final Envelope envelope)
    {
        this.spatialIndexCells = spatialIndexCells;
        this.envelope = envelope;
    }

    public boolean areComplete()
    {
        return this.envelope != null || !this.spatialIndexCells.isEmpty();
    }

    public Envelope getEnvelope()
    {
        return this.envelope;
    }

    public MultiMap<String, Envelope> getSpatialIndexCells()
    {
        return this.spatialIndexCells;
    }

}
