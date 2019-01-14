package org.openstreetmap.atlas.geography.atlas.validators;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.complete.EmptyAtlas;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author matthieun
 */
public class AtlasLineItemValidatorTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testMissingPolyLineValidation()
    {
        final Atlas atlas = new EmptyAtlas()
        {
            private static final long serialVersionUID = 3421885788588156857L;

            @Override
            public Iterable<LineItem> lineItems()
            {
                return Iterables.from(new CompleteLine(123L, null, null, null));
            }
        };

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("is missing its PolyLine.");

        new AtlasLineItemValidator(atlas).validate();
    }
}
