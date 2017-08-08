package org.openstreetmap.atlas.tags.annotations.validation;

/**
 * TagValidators verify a Tag's value. Some may be complex checks like data conversions, while
 * others may compare against a simple set of values
 *
 * @author cstaylor
 */
public interface TagValidator
{
    /**
     * Checks if value is valid for this kind of tag
     *
     * @param value
     *            the textual representation of this tag's value
     * @return true if the value is valid, false otherwise
     */
    boolean isValid(String value);
}
