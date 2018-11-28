package org.openstreetmap.atlas.geography.atlas.change;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.change.rule.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.change.validators.ChangeValidator;

/**
 * Construct a {@link Change}. This is a gatekeeper that ensures validation.
 *
 * @author matthieun
 */
public class ChangeBuilder
{
    private final Change change;
    private boolean open;

    public ChangeBuilder()
    {
        this.change = new Change();
        this.open = true;
    }

    public void add(final FeatureChange featureChange)
    {
        if (!this.open)
        {
            throw new CoreException(
                    "Cannot append to a Change object that has already been validated");
        }
        this.change.add(featureChange);
    }

    public Change get()
    {
        new ChangeValidator(this.change).validate();
        this.open = false;
        return this.change;
    }
}
