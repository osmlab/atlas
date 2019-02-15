package org.openstreetmap.atlas.geography.atlas.change;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.change.validators.ChangeValidator;

/**
 * Construct a {@link Change}. This is a gatekeeper that ensures validation.
 *
 * @author matthieun
 * @author Yazad Khambata
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

    /**
     * A factory-method to construct a new {@link ChangeBuilder}. Constructs a {@code new}
     * {@link ChangeBuilder} with default values.
     *
     * @return - a new ChangeBuilder.
     */
    public static ChangeBuilder newInstance()
    {
        return new ChangeBuilder();
    }

    /**
     * @param featureChange
     *            - teh {@link FeatureChange} to add to the builder.
     * @return ChangeBuilder - returns itself to allow fluency in calls.
     */
    public synchronized ChangeBuilder add(final FeatureChange featureChange)
    {
        if (!this.open)
        {
            throw new CoreException(
                    "Cannot append to a Change object that has already been validated");
        }
        this.change.add(featureChange);

        return this;
    }

    public synchronized Change get()
    {
        new ChangeValidator(this.change).validate();
        this.open = false;
        return this.change;
    }

    public synchronized int peekNumberOfChanges()
    {
        return this.change.changeCount();
    }
}
