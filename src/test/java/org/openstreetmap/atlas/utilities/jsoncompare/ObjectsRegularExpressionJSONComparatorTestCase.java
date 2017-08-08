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

/**
 * JSONObject-specific Test cases for the {@link RegularExpressionJSONComparator}
 *
 * @author cstaylor
 */
public class ObjectsRegularExpressionJSONComparatorTestCase
{
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void extensibleObjectTest() throws JSONException
    {
        final JSONObject obj1 = (JSONObject) JSONParser.parseJSON("{ foo:\"abc\" }");
        final JSONObject obj2 = (JSONObject) JSONParser.parseJSON("{ foo:\"abc\", bar: \"def\" }");

        final JSONCompareResult results = JSONCompare.compareJSON(obj1, obj2,
                new RegularExpressionJSONComparator(JSONCompareMode.LENIENT));
        Assert.assertFalse(results.failed());
    }

    @Test
    public void nonExtensibleObjectTest() throws JSONException
    {
        final JSONObject obj1 = (JSONObject) JSONParser.parseJSON("{ foo:\"abc\" }");
        final JSONObject obj2 = (JSONObject) JSONParser.parseJSON("{ foo:\"abc\", bar: \"def\" }");

        final JSONCompareResult results = JSONCompare.compareJSON(obj1, obj2,
                new RegularExpressionJSONComparator(JSONCompareMode.STRICT));
        Assert.assertTrue(results.failed());
    }

    @Test
    public void objectsDifferentTypes() throws JSONException
    {
        final JSONObject obj1 = (JSONObject) JSONParser.parseJSON("{ foo:\"123\" }");
        final JSONObject obj2 = (JSONObject) JSONParser.parseJSON("{ foo:123 }");

        final JSONCompareResult results = JSONCompare.compareJSON(obj1, obj2,
                new RegularExpressionJSONComparator(JSONCompareMode.LENIENT));
        Assert.assertTrue(results.failed());
    }

    @Test
    public void reverseObjectsDifferentTypes() throws JSONException
    {
        final JSONObject obj1 = (JSONObject) JSONParser.parseJSON("{ foo:\"123\" }");
        final JSONObject obj2 = (JSONObject) JSONParser.parseJSON("{ foo:123 }");

        final JSONCompareResult results = JSONCompare.compareJSON(obj2, obj1,
                new RegularExpressionJSONComparator(JSONCompareMode.LENIENT));
        Assert.assertTrue(results.failed());
    }
}
