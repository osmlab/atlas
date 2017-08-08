package org.openstreetmap.atlas.tags.annotations.validation;

import org.junit.BeforeClass;
import org.openstreetmap.atlas.tags.Taggable;

/**
 * All tag tests are subclasses of this so we can consolidate how validators are initialized
 *
 * @author cstaylor
 */
public abstract class BaseTagTestCase
{
    private static Validators validators;

    @BeforeClass
    public static void setupValidators()
    {
        validators = new Validators(Taggable.class);
    }

    protected static Validators validators()
    {
        return BaseTagTestCase.validators;
    }
}
