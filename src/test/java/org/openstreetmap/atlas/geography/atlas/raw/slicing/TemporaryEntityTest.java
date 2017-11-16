package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;

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
        final TemporaryPoint pointOne = new TemporaryPoint(1l, Location.EIFFEL_TOWER,
                new HashMap<>());
        final TemporaryPoint pointOneCopy = new TemporaryPoint(1l, Location.EIFFEL_TOWER,
                new HashMap<>());
        final TemporaryPoint pointTwo = new TemporaryPoint(2l, Location.EIFFEL_TOWER,
                new HashMap<>());

        final List<Long> lineOnePoints = new ArrayList<>();
        lineOnePoints.add(1l);
        lineOnePoints.add(2l);

        final TemporaryLine lineOne = new TemporaryLine(1l, lineOnePoints, new HashMap<>());
        final TemporaryLine lineOneCopy = new TemporaryLine(1l, lineOnePoints, new HashMap<>());
        final TemporaryLine lineTwo = new TemporaryLine(2l, lineOnePoints, new HashMap<>());

        Assert.assertFalse(pointOne.equals(pointTwo));
        Assert.assertFalse(pointOne.equals(lineOne));
        Assert.assertFalse(lineOne.equals(lineTwo));

        Assert.assertTrue(pointOne.equals(pointOne));
        Assert.assertTrue(pointOne.equals(pointOneCopy));
        Assert.assertTrue(lineOne.equals(lineOne));
        Assert.assertTrue(lineOne.equals(lineOneCopy));
    }
}
