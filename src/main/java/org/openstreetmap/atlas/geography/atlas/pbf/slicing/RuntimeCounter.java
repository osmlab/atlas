package org.openstreetmap.atlas.geography.atlas.pbf.slicing;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

/**
 * Tracks notable events occurring during country slicing.
 *
 * @author Yiqing Jin
 */
public final class RuntimeCounter
{
    private static Logger logger = LoggerFactory.getLogger(RuntimeCounter.class);
    private static final int OUTPUT_THRESHOLD = 10000;

    private static long totalProcessed = 0;
    private static long wayProcessed = 0;
    private static long relationProcessed = 0;
    private static long waySliced = 0;
    private static long relationSliced = 0;
    private static long geometryChecked = 0;
    private static long geometryWithin = 0;
    private static long geometryNoIntersect = 0;
    private static long geometryIntersect = 0;

    private static Set<Long> skippedWays = new HashSet<>();

    public static void geometryChecked()
    {
        RuntimeCounter.geometryChecked++;
    }

    public static void geometryCheckedIntersect()
    {
        RuntimeCounter.geometryIntersect++;
    }

    public static void geometryCheckedNoIntersect()
    {
        RuntimeCounter.geometryNoIntersect++;
    }

    public static void geometryCheckedWithin()
    {
        RuntimeCounter.geometryWithin++;
    }

    public static long getGeometryChecked()
    {
        return geometryChecked;
    }

    public static long getGeometryCheckedIntersect()
    {
        return geometryIntersect;
    }

    public static long getGeometryCheckedNoIntersect()
    {
        return geometryNoIntersect;
    }

    public static long getGeometryCheckedWithin()
    {
        return geometryWithin;
    }

    public static long getRelationProcessed()
    {
        return relationProcessed;
    }

    public static long getRelationSlicedProcessed()
    {
        return relationSliced;
    }

    public static Set<Long> getSkippedWays()
    {
        return skippedWays;
    }

    public static long getWayProcessed()
    {
        return wayProcessed;
    }

    public static long getWaySlicedProcessed()
    {
        return waySliced;
    }

    public static String print()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("Way processed: ").append(wayProcessed).append(" ,Way Sliced: ")
                .append(waySliced).append(" ,Relation processed: ").append(relationProcessed)
                .append(" ,RelationSliced: ").append(relationSliced).append(" ,Geometry checked: ")
                .append(geometryChecked).append(" ,Checked within:").append(geometryWithin)
                .append(" ,Checked No Intersect: ").append(geometryNoIntersect)
                .append(" ,Checked Intersect: ").append(geometryIntersect)
                .append(" , Skipped Ways: ").append(Joiner.on(",").join(skippedWays));

        return builder.toString();
    }

    public static void relationProcessed()
    {
        totalProcessed();
        RuntimeCounter.relationProcessed++;
    }

    public static void relationSliced()
    {
        RuntimeCounter.relationSliced++;
    }

    public static void wayProcessed()
    {
        totalProcessed();
        RuntimeCounter.wayProcessed++;
    }

    public static void waySkipped(final long skipped)
    {
        skippedWays.add(skipped);
    }

    public static void waySliced()
    {
        RuntimeCounter.waySliced++;
    }

    private static void totalProcessed()
    {
        totalProcessed++;
        if (totalProcessed % OUTPUT_THRESHOLD == 0)
        {
            logger.info(print());
        }
    }

    private RuntimeCounter()
    {
    }

}
