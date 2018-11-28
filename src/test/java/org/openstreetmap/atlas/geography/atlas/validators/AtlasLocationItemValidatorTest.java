package org.openstreetmap.atlas.geography.atlas.validators;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedAtlas;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedPoint;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author matthieun
 */
public class AtlasLocationItemValidatorTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testLocationPresent()
    {
        final Atlas atlas = new BloatedAtlas()
        {
            private static final long serialVersionUID = -242183195939062159L;

            @Override
            public Iterable<LocationItem> locationItems()
            {
                return Iterables.from(new BloatedPoint(123L, null, null, null));
            }
        };

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("is missing a Location.");

        new AtlasLocationItemValidator(atlas).validateLocationPresent();
    }
}
