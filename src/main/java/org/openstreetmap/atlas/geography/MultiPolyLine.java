package org.openstreetmap.atlas.geography;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.converters.WktMultiPolyLineConverter;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.utilities.collections.Iterables;

import com.google.common.collect.Lists;

/**
 * A MultiPolyLine is a set of {@link PolyLine}s in a specific order
 *
 * @author yalimu
 */
public class MultiPolyLine implements Iterable<PolyLine>, Located, Serializable
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
        this.polyLineList = new ArrayList<>(polyLines);
    }

    public MultiPolyLine(final PolyLine... polyLines)
    {
        this(Iterables.iterable(polyLines));
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
    public Iterator<PolyLine> iterator()
    {
        return this.polyLineList.iterator();
    }
}
