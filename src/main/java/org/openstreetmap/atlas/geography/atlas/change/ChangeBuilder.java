package org.openstreetmap.atlas.geography.atlas.change;

import org.openstreetmap.atlas.geography.atlas.change.rule.FeatureChange;

/**
 * @author matthieun
 */
public class ChangeBuilder
{
    private final Change change;

    public ChangeBuilder()
    {
        this.change = new Change();
    }

    public void add(final FeatureChange featureChange)
    {
        this.change.add(featureChange);
    }

    public Change get()
    {
        this.change.validate();
        return this.change;
    }
}
