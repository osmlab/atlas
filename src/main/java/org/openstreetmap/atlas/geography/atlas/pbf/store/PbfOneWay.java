package org.openstreetmap.atlas.geography.atlas.pbf.store;

import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.tags.OneWayTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * One way attribute of an OSM Way
 *
 * @author tony
 * @author matthieun
 */
public enum PbfOneWay
{
    YES,
    NO,
    REVERSED;

    /**
     * Determines the whether the given {@link Taggable} is a one-way, non-one-way, reversed or
     * closed Edge.
     *
     * @param taggable
     *            The {@link Taggable} to look at
     * @return the {@link PbfOneWay} for the given {@link Taggable}
     */
    public static PbfOneWay forTag(final Taggable taggable)
    {
        if (OneWayTag.isExplicitlyTwoWay(taggable))
        {
            return NO;
        }
        else if (OneWayTag.isTwoWay(taggable))
        {
            if (JunctionTag.isRoundabout(taggable)
                    || Validators.isOfType(taggable, HighwayTag.class, HighwayTag.MOTORWAY))
            {
                // Override the two-way here, as a roundabout takes precedence as a one way road in
                // OSM, when no one way tag is specified. Similarly, a motorway tag implies a
                // one way road. The same does NOT hold true for motorway_link.
                return YES;
            }
            return NO;
        }
        else if (OneWayTag.isOneWayForward(taggable))
        {
            return YES;
        }
        else if (OneWayTag.isOneWayReversed(taggable))
        {
            return REVERSED;
        }
        else
        {
            return NO;
        }
    }
}
