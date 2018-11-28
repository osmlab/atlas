package org.openstreetmap.atlas.geography.atlas.validators;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedAtlas;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedPoint;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author matthieun
 */
public class AtlasValidatorTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testParentRelations()
    {
        final BloatedAtlas atlas = new BloatedAtlas()
        {
            private static final long serialVersionUID = -1162255036446588163L;

            @Override
            public Iterable<AtlasEntity> entities()
            {
                return Iterables.from(new BloatedPoint(456L, null, null, null)
                {
                    private static final long serialVersionUID = 3282284682633937718L;

                    @Override
                    public Set<Relation> relations()
                    {
                        final Set<Relation> result = new HashSet<>();
                        result.add(null);
                        return result;
                    }
                });
            }
        };

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("lists some parent relation that is not present");

        new AtlasValidator(atlas).validateRelationsPresent();
    }

    @Test
    public void testTagsPresent()
    {
        final BloatedAtlas atlas = new BloatedAtlas()
        {
            private static final long serialVersionUID = -2478701330988023398L;

            @Override
            public Iterable<AtlasEntity> entities()
            {
                return Iterables.from(new BloatedPoint(456L, null, null, null)
                {
                    private static final long serialVersionUID = -3850622600530001863L;

                    @Override
                    public Map<String, String> getTags()
                    {
                        return null;
                    }
                });
            }
        };

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("is missing tags.");

        new AtlasValidator(atlas).validateTagsPresent();
    }
}
