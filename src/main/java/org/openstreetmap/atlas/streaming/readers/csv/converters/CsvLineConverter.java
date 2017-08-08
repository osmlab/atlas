package org.openstreetmap.atlas.streaming.readers.csv.converters;

import org.openstreetmap.atlas.streaming.readers.CsvLine;
import org.openstreetmap.atlas.utilities.conversion.Converter;

/**
 * Converter from a {@link CsvLine}
 *
 * @param <T>
 *            The type to be converted
 * @author matthieun
 */
public interface CsvLineConverter<T> extends Converter<CsvLine, T>
{
}
