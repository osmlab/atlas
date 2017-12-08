package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryEntity;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryLine;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryPoint;

/**
 * {@link TemporaryEntity} equals test.
 *
 * @author mgostintsev
 */
public class TemporaryEntityTest
{
    @Test
    public void testEquals()
    {
        final TemporaryPoint pointOne = new TemporaryPoint(1L, Location.EIFFEL_TOWER,
                new HashMap<>());
        final TemporaryPoint pointOneCopy = new TemporaryPoint(1L, Location.EIFFEL_TOWER,
                new HashMap<>());
        final TemporaryPoint pointTwo = new TemporaryPoint(2L, Location.EIFFEL_TOWER,
                new HashMap<>());

        final List<Long> lineOnePoints = new ArrayList<>();
        lineOnePoints.add(1L);
        lineOnePoints.add(2L);

        final TemporaryLine lineOne = new TemporaryLine(1L, lineOnePoints, new HashMap<>());
        final TemporaryLine lineOneCopy = new TemporaryLine(1L, lineOnePoints, new HashMap<>());
        final TemporaryLine lineTwo = new TemporaryLine(2L, lineOnePoints, new HashMap<>());

        Assert.assertFalse(pointOne.equals(pointTwo));
        Assert.assertFalse(pointOne.equals(lineOne));
        Assert.assertFalse(lineOne.equals(lineTwo));

        Assert.assertFalse(lineOne.equals(lineTwo));
        Assert.assertTrue(pointOne.equals(pointOne));
        Assert.assertTrue(pointOne.equals(pointOneCopy));
        Assert.assertTrue(lineOne.equals(lineOne));
        Assert.assertTrue(lineOne.equals(lineOneCopy));
    }
}
