package org.openstreetmap.atlas.tags.annotations.validation;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.ParkingTag;
import org.openstreetmap.atlas.utilities.testing.TestTaggable;

/**
 * Test case for verifying if TagAsValue works when converting tag values to enums
 *
 * @author cstaylor
 */
public class TagValueAsTestCase extends BaseTagTestCase
{
    @Test
    public void simpleEnum()
    {
        Assert.assertEquals(Optional.of(HighwayTag.MOTORWAY),
                Validators.from(HighwayTag.class, new TestTaggable(HighwayTag.KEY, "motorway")));
    }

    @Test
    public void stringToEnum()
    {
        Assert.assertEquals(Optional.of(ParkingTag.MULTI_STOREY), Validators.from(ParkingTag.class,
                new TestTaggable(ParkingTag.KEY, "multi-storey")));
    }
}
