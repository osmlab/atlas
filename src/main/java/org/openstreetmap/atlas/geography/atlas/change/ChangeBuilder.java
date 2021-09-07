package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Arrays;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    public ChangeBuilder()
    {
        this.change = new Change();
        this.open = true;
    }

    /**
     * @param featureChange
     *            - the {@link FeatureChange} to add to the builder.
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

    /**
     * @see #addAll(Stream)
     * @param featureChanges
     *            - The featureChanges to add.
     * @return ChangeBuilder - returns itself to allow fluency in calls.
     */
    public synchronized ChangeBuilder addAll(final FeatureChange... featureChanges)
    {
        return addAll(Arrays.stream(featureChanges));
    }

    /**
     * @see #addAll(Stream)
     * @param featureChanges
     *            - The featureChanges to add.
     * @return ChangeBuilder - returns itself to allow fluency in calls.
     */
    public synchronized ChangeBuilder addAll(final Iterable<FeatureChange> featureChanges)
    {
        return addAll(StreamSupport.stream(featureChanges.spliterator(), false));
    }

    /**
     * Iteratively {@link #add(FeatureChange)} all the FeatureChanges.
     *
     * @param featureChanges
     *            - The featureChanges to add.
     * @return ChangeBuilder - returns itself to allow fluency in calls.
     */
    public synchronized ChangeBuilder addAll(final Stream<FeatureChange> featureChanges)
    {
        featureChanges.forEach(this::add);
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

    /**
     * Assign a name to the change being constructed.
     *
     * @param name
     *            - a name for the change.
     * @return ChangeBuilder - returns itself to allow fluency in calls.
     */
    public ChangeBuilder withName(final String name)
    {
        this.change.withName(name);
        return this;
    }
}
