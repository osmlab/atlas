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

    private boolean usePolygons;
    private boolean prettyPrint;
    private Set<String> countryWhitelist;
    private Set<String> countryBlacklist;

    public CountryBoundaryMapGeoJsonConverter()
    {
        this.usePolygons = false;
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
                if (this.usePolygons)
                {
                    final org.openstreetmap.atlas.geography.Polygon atlasPolygon = new JtsPolygonConverter()
                            .backwardConvert(polygon);
                    featureObject.add("geometry", atlasPolygon.asGeoJsonGeometry());
                }
                else
                {
                    final org.openstreetmap.atlas.geography.Polygon atlasPolygon = new JtsPolygonConverter()
                            .backwardConvert(polygon);
                    featureObject.add("geometry",
                            new PolyLine(atlasPolygon.closedLoop()).asGeoJsonGeometry());
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

    /**
     * Convert a {@link CountryBoundaryMap} directly to a GeoJSON string. This method will respect
     * the 'prettyPrint' parameter.
     * 
     * @param map
     *            the {@link CountryBoundaryMap}
     * @return the GeoJSON string form of the {@link CountryBoundaryMap}
     */
    public String convertToString(final CountryBoundaryMap map)
    {
        if (this.prettyPrint)
        {
            return new GsonBuilder().setPrettyPrinting().create().toJson(this.convert(map));
        }
        return this.convert(map).toString();
    }

    /**
     * Specify if the GeoJSON should be pretty printed. Otherwise, it will all be on a single line.
     * This parameter only affects the
     * {@link CountryBoundaryMapGeoJsonConverter#convertToString(CountryBoundaryMap)} method.
     * 
     * @param prettyPrint
     *            pretty print the GeoJSON
     * @return a modified instance of {@link CountryBoundaryMapGeoJsonConverter}
     */
    public CountryBoundaryMapGeoJsonConverter prettyPrint(final boolean prettyPrint)
    {
        this.prettyPrint = prettyPrint;
        return this;
    }

    /**
     * Use polygons instead of linestrings in the GeoJSON representation of the
     * {@link CountryBoundaryMap}. This may be useful if the GeoJSON is being used in visualization
     * software.
     * 
     * @param usePolygons
     *            use polygons
     * @return a modified instance of {@link CountryBoundaryMapGeoJsonConverter}
     */
    public CountryBoundaryMapGeoJsonConverter usePolygons(final boolean usePolygons)
    {
        this.usePolygons = usePolygons;
        return this;
    }

    /**
     * Specify a blacklist for countries to exclude. If this set is empty, then no countries will be
     * excluded.
     *
     * @param countryBlacklist
     *            the blacklist
     * @return a modified instance of {@link CountryBoundaryMapGeoJsonConverter}
     */
    public CountryBoundaryMapGeoJsonConverter withCountryBlacklist(
            final Set<String> countryBlacklist)
    {
        this.countryBlacklist = countryBlacklist;
        return this;
    }

    /**
     * Specify a whitelist for countries to include. If this set is empty, then no countries will be
     * included.
     * 
     * @param countryWhitelist
     *            the whitelist
     * @return a modified instance of {@link CountryBoundaryMapGeoJsonConverter}
     */
    public CountryBoundaryMapGeoJsonConverter withCountryWhitelist(
            final Set<String> countryWhitelist)
    {
        this.countryWhitelist = countryWhitelist;
        return this;
    }
}
