package org.openstreetmap.atlas.geography;

import org.openstreetmap.atlas.utilities.collections.StringList;

/**
 * @author matthieun
 */
public interface WktPrintable
{
    static String toWktCollection(final Iterable<? extends WktPrintable> input)
    {
        final StringList wktList = new StringList();
        input.forEach(wktPrintable -> wktList.add(wktPrintable.toWkt()));
        final StringBuilder builder = new StringBuilder();
        builder.append("GEOMETRYCOLLECTION (");
        builder.append(wktList.join(", "));
        builder.append(")");
        return builder.toString();
    }

    String toWkt();
}
