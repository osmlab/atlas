package org.openstreetmap.atlas.geography.boundary.converters;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.locationtech.jts.geom.Polygon;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.utilities.conversion.Converter;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author lcram
 */
public class CountryBoundaryMapGeoJsonConverter implements Converter<CountryBoundaryMap, JsonObject>
{
    private static final Logger logger = LoggerFactory
            .getLogger(CountryBoundaryMapGeoJsonConverter.class);

    private boolean useLinestrings;
    private boolean prettyPrint;
    private Set<String> countryWhitelist;
    private Set<String> countryBlacklist;

    public CountryBoundaryMapGeoJsonConverter()
    {
        this.useLinestrings = false;
        this.prettyPrint = false;
        this.countryWhitelist = null;
        this.countryBlacklist = null;
    }

    @Override
    public JsonObject convert(final CountryBoundaryMap map)
    {
        final MultiMap<String, Polygon> countryNameToBoundaryMap = map
                .getCountryNameToBoundaryMap();
        final JsonObject featureCollectionObject = new JsonObject();
        featureCollectionObject.addProperty("type", "FeatureCollection");
        final JsonArray features = new JsonArray();
        for (final Map.Entry<String, List<Polygon>> entry : countryNameToBoundaryMap.entrySet())
        {
            final String countryCode = entry.getKey();
            final List<Polygon> polygons = entry.getValue();
            if ((this.countryBlacklist != null && this.countryBlacklist.contains(countryCode))
                    || (this.countryWhitelist != null
                            && !this.countryWhitelist.contains(countryCode)))
            {
                continue;
            }
            polygons.forEach(polygon ->
            {
                final JsonObject featureObject = new JsonObject();
                featureObject.addProperty("type", "Feature");
                if (this.useLinestrings)
                {
                    final org.openstreetmap.atlas.geography.Polygon atlasPolygon = new JtsPolygonConverter()
                            .backwardConvert(polygon);
                    featureObject.add("geometry",
                            new PolyLine(atlasPolygon.closedLoop()).asGeoJsonGeometry());
                }
                else
                {
                    final org.openstreetmap.atlas.geography.Polygon atlasPolygon = new JtsPolygonConverter()
                            .backwardConvert(polygon);
                    featureObject.add("geometry", atlasPolygon.asGeoJsonGeometry());
                }
                final JsonObject propertiesObject = new JsonObject();
                propertiesObject.addProperty("iso_country_code", countryCode);
                featureObject.add("properties", propertiesObject);
                features.add(featureObject);
            });
            logger.trace("Finished processing polygons for {}", countryCode);
        }
        featureCollectionObject.add("features", features);

        return featureCollectionObject;
    }

    public String convertToString(final CountryBoundaryMap map)
    {
        if (this.prettyPrint)
        {
            return new GsonBuilder().setPrettyPrinting().create().toJson(this.convert(map));
        }
        return this.convert(map).toString();
    }

    public CountryBoundaryMapGeoJsonConverter prettyPrint(final boolean prettyPrint)
    {
        this.prettyPrint = prettyPrint;
        return this;
    }

    public CountryBoundaryMapGeoJsonConverter useLinestrings(final boolean useLinestrings)
    {
        this.useLinestrings = useLinestrings;
        return this;
    }

    public CountryBoundaryMapGeoJsonConverter withCountryBlacklist(
            final Set<String> countryBlacklist)
    {
        this.countryBlacklist = countryBlacklist;
        return this;
    }

    public CountryBoundaryMapGeoJsonConverter withCountryWhitelist(
            final Set<String> countryWhitelist)
    {
        this.countryWhitelist = countryWhitelist;
        return this;
    }
}
