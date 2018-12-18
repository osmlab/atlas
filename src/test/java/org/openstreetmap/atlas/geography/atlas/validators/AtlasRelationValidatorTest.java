package org.openstreetmap.atlas.geography.atlas.validators;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedAtlas;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedRelation;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean.RelationBeanItem;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author matthieun
 */
public class AtlasRelationValidatorTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testAtlasRelationValidation()
    {
        // Make a fake BloatedAtlas which will surface up the only problem the Relation validator
        // looks for.
        final BloatedAtlas atlas = new BloatedAtlas()
        {
            private static final long serialVersionUID = -1162255036446588163L;

            @Override
            public AtlasEntity entity(final long identifier, final ItemType type)
            {
                // BloatedRelation uses this method when populating the member entities. Here it
                // will trigger the BloatedPoint 456 to be null.
                return null;
            }

            @Override
            public Iterable<Relation> relations()
            {
                final RelationBean members = new RelationBean();
                members.addItem(new RelationBeanItem(456L, "myRole", ItemType.POINT));
                return Iterables.from(
                        new BloatedRelation(123L, null, null, members, null, null, null, null));
            }
        };

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("specifies a member with role");

        new AtlasRelationValidator(atlas).validate();
    }
}
