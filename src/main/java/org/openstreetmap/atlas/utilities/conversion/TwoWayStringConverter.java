package org.openstreetmap.atlas.utilities.conversion;

/**
 * @author matthieun
 * @param <B>
 *            The type to convert to and from String
 */
public interface TwoWayStringConverter<B> extends StringConverter<B>, TwoWayConverter<String, B>
{
}
