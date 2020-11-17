package org.openstreetmap.atlas.tags.filters.matcher;

import java.io.Serializable;
import java.util.function.Predicate;

import org.openstreetmap.atlas.tags.Taggable;

/**
 * @author lcram
 */
public class TaggableMatcher implements Predicate<Taggable>, Serializable
{
    private static final long serialVersionUID = -3505184622005535575L;

    /**
     * Print this {@link TaggableMatcher} in syntax tree form.
     * 
     * @return this {@link TaggableMatcher} as a tree
     */
    public String prettyPrint()
    {
        return "TODO";
    }

    @Override
    public boolean test(final Taggable taggable)
    {
        return false;
    }

    @Override
    public String toString()
    {
        return "TODO";
    }
}
