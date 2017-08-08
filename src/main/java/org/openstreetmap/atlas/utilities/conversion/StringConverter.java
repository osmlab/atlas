package org.openstreetmap.atlas.utilities.conversion;

/**
 * Converter from a String to a specified type
 *
 * @author matthieun
 * @param <B>
 *            The target conversion type
 */
public interface StringConverter<B> extends Converter<String, B>
{
    StringConverter<String> IDENTITY = string -> string;
}
