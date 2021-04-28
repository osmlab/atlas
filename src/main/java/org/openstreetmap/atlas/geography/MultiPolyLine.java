package org.openstreetmap.atlas.geography;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.converters.WkbMultiPolyLineConverter;
import org.openstreetmap.atlas.geography.converters.WktMultiPolyLineConverter;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonGeometry;
import org.openstreetmap.atlas.geography.geojson.GeoJsonType;
import org.openstreetmap.atlas.geography.geojson.GeoJsonUtils;
import org.openstreetmap.atlas.streaming.readers.json.converters.PolyLineCoordinateConverter;
import org.openstreetmap.atlas.utilities.collections.Iterables;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * A MultiPolyLine is a set of distinct {@link PolyLine}s in a specific order
 *
 * @author yalimu
 */
public class MultiPolyLine
        implements Iterable<PolyLine>, Located, Serializable, GeometryPrintable, GeoJsonGeometry
{
    private static final long serialVersionUID = 5907807607388840698L;
    private final List<PolyLine> polyLineList;

    /**
     * Create a {@link MultiPolyLine} from Well Known Text
     *
     * @param wkt
     *            The Well Known Text
     * @return The {@link MultiPolyLine}
     */
    public static MultiPolyLine wkt(final String wkt)
    {
        return new WktMultiPolyLineConverter().backwardConvert(wkt);
    }

    public MultiPolyLine(final Iterable<? extends PolyLine> polyLines)
    {
        this(Iterables.asList(polyLines));
    }

    public MultiPolyLine(final List<? extends PolyLine> polyLines)
    {
        if (polyLines.isEmpty())
        {
            throw new CoreException("Cannot have an empty list of PolyLine or Polygon.");
        }
        this.polyLineList = polyLines.stream().distinct().collect(Collectors.toList());
    }

    public MultiPolyLine(final PolyLine... polyLines)
    {
        this(Iterables.iterable(polyLines));
    }

    @Override
    public JsonObject asGeoJsonGeometry()
    {
        final PolyLineCoordinateConverter converter = new PolyLineCoordinateConverter();
        final JsonArray coordinateArray = new JsonArray();
        Iterables.stream(this).map(converter::convert).forEach(coordinateArray::add);
        return GeoJsonUtils.geometry(this.getGeoJsonType(), coordinateArray);
    }

    public Iterable<GeoJsonBuilder.LocationIterableProperties> asLocationIterableProperties()
    {
        return this.polyLineList.stream()
                .map(polyLine -> new GeoJsonBuilder.LocationIterableProperties(polyLine,
                        new HashMap<>()))
                .collect(Collectors.toList());
    }

    @Override
    public Rectangle bounds()
    {
        final List<Location> locations = Lists.newArrayList();
        this.polyLineList.stream().map(PolyLine::getPoints).forEach(locations::addAll);
        return Rectangle.forLocations(locations);
    }

    @Override
    public boolean equals(final Object other)
    {
        if (!(other instanceof MultiPolyLine))
        {
            return false;
        }
        final MultiPolyLine otherItem = (MultiPolyLine) other;
        return new HashSet<>(this.polyLineList).equals(new HashSet<>(otherItem.getPolyLineList()));
    }

    @Override
    public GeoJsonType getGeoJsonType()
    {
        return GeoJsonType.MULTI_LINESTRING;
    }

    public List<PolyLine> getPolyLineList()
    {
        return this.polyLineList;
    }

    @Override
    public int hashCode()
    {
        final StringBuilder stringBuilder = new StringBuilder();
        for (final PolyLine polyLine : this.polyLineList)
        {
            stringBuilder.append(polyLine.hashCode());
        }
        return stringBuilder.toString().hashCode();
    }

    @Override
    public Iterator<PolyLine> iterator()
    {
        return this.polyLineList.iterator();
    }

    @Override
    public byte[] toWkb()
    {
        return new WkbMultiPolyLineConverter().convert(this);
    }

    @Override
    public String toWkt()
    {
        return new WktMultiPolyLineConverter().convert(this);
    }
}
