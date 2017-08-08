package org.openstreetmap.atlas.utilities.jsoncompare;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.JSONParser;
import org.skyscreamer.jsonassert.comparator.JSONComparator;

/**
 * Regular Expression tests for the {@link RegularExpressionJSONComparator}
 *
 * @author cstaylor
 */
public class MatchingRegularExpressionJSONComparatorTestCase
{
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void startsWith() throws JSONException
    {
        final JSONObject object1 = (JSONObject) JSONParser
                .parseJSON("{ mixedAtBirth:{ child:[123] }, dnaChecksOut: { child:[555] } }");
        final JSONObject object2 = (JSONObject) JSONParser
                .parseJSON("{ mixedAtBirth:{ child:[456] }, dnaChecksOut: { child:[555] } }");
        final RegularExpressionJSONComparator comparator = new RegularExpressionJSONComparator(
                JSONCompareMode.STRICT);
        comparator.startsWith("mixedAtBirth.child");
        final JSONCompareResult results = JSONCompare.compareJSON(object1, object2, comparator);
        Assert.assertFalse(results.failed());
    }

    @Test
    public void testRoadNodes() throws JSONException
    {
        final JSONCompareResult results = JSONCompare.compareJSON(
                "{\"FeatureProto\":{\"bounding_box\":{\"max\":{\"latitude\":184761037,\"longitude\":-699138300},\"min\":{\"latitude\":184761037,\"longitude\":-699138300}},\"vendor_info\":{\"vendor_version\":\"-20160509-235548\",\"vendor_feature_id\":\"OSM_W_410348025_000000\",\"vendor_id\":6},\"protocol_version\":\"0.28.1\",\"is_active\":true,\"road_node\":{\"road_segment\":[{\"feature\":\"148693917074718963\",\"end\":\"FROM\"},{\"feature\":\"144190317701110516\",\"end\":\"TO\"}]},\"geo_type_id\":18,\"point\":[{\"latitude\":184761037,\"longitude\":-699138300}],\"feature_id\":\"148693917074718965\",\"representative_point\":{\"latitude\":184761037,\"longitude\":-699138300},\"candidate\":true,\"geo_ontology_id\":11966,\"feature_type\":[\"ROAD_NODE\"],\"iso_country_code\":\"DOM\",\"history\":[{\"source_id\":1745,\"edit_type\":\"CREATION\",\"source_key\":\"OSM_W_410348025_000000\",\"source\":\"1745/6/OSM_W_410348025_000000\",\"vendor_id\":6,\"timestamp\":1462887123}],\"time_zone_reference\":[{\"tz_database_name\":[\"Atlantic Standard Time\"],\"time_zone\":{\"feature\":\"144190317670694970\"}}],\"version\":\"a364fc02-16b3-11e6-800c-56847afe9799\"}}",
                "{\"FeatureProto\":{\"bounding_box\":{\"max\":{\"latitude\":184761037,\"longitude\":-699138300},\"min\":{\"latitude\":184761037,\"longitude\":-699138300}},\"is_active\":true,\"vendor_info\":{\"vendor_version\":\"-20160512-184524\",\"vendor_id\":6,\"vendor_feature_id\":\"OSM_W_410348025_000000\"},\"protocol_version\":\"0.28.1\",\"road_node\":{\"road_segment\":[{\"feature\":\"148694183946749497\",\"end\":\"FROM\"},{\"feature\":\"144190584576286789\",\"end\":\"TO\"}]},\"geo_type_id\":18,\"feature_id\":\"148694183946749499\",\"point\":[{\"latitude\":184761037,\"longitude\":-699138300}],\"representative_point\":{\"latitude\":184761037,\"longitude\":-699138300},\"candidate\":true,\"geo_ontology_id\":11966,\"feature_type\":[\"ROAD_NODE\"],\"iso_country_code\":\"DOM\",\"history\":[{\"source_id\":1745,\"edit_type\":\"CREATION\",\"source_key\":\"OSM_W_410348025_000000\",\"source\":\"1745/6/OSM_W_410348025_000000\",\"vendor_id\":6,\"timestamp\":1463141634}],\"time_zone_reference\":[{\"tz_database_name\":[\"Atlantic Standard Time\"],\"time_zone\":{\"feature\":\"144190584546918458\"}}],\"version\":\"36902a8b-1904-11e6-854c-56847afe9799\"}}",
                createRoadsComparator());
        Assert.assertFalse(results.failed());
    }

    private JSONComparator createRoadsComparator()
    {
        final RegularExpressionJSONComparator returnValue = new RegularExpressionJSONComparator(
                JSONCompareMode.STRICT);
        returnValue.endsWith(".timestamp");
        returnValue.endsWith(".feature");
        returnValue.endsWith(".feature_id");
        returnValue.exact("FeatureProto.vendor_info.vendor_version");
        returnValue.exact("FeatureProto.version");
        return returnValue;
    }
}
