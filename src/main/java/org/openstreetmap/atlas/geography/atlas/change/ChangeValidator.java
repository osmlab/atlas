package org.openstreetmap.atlas.geography.atlas.change;

/**
 * Validate a {@link Change}
 *
 * @author matthieun
 */
class ChangeValidator
{
    private final Change change;

    public ChangeValidator(final Change change)
    {
        this.change = change;
    }

    public void validate()
    {
        // One among many
        System.out.println("Change Validation??");
    }
}
