package org.openstreetmap.atlas.utilities.jsoncompare;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.JSONParser;

/**
 * JSONArray-specific Test cases for the {@link RegularExpressionJSONComparator}
 *
 * @author cstaylor
 */
public class ArraysRegularExpressionJSONComparatorTestCase
{
    @Test
    public void arrayCompare() throws JSONException
    {
        final JSONCompareResult results = JSONCompare.compareJSON("[\"abc\"]", "[\"abc\"]",
                new RegularExpressionJSONComparator(JSONCompareMode.STRICT));
        Assert.assertFalse(results.failed());
    }

    @Test
    public void extensibleNotReallyIfAnArrayTest() throws JSONException
    {
        final JSONCompareResult results = JSONCompare.compareJSON("[\"abc\"]", "[\"abc\", \"def\"]",
                new RegularExpressionJSONComparator(JSONCompareMode.STRICT_ORDER));
        Assert.assertTrue(results.failed());
    }

    @Test
    public void lenientArrayOfJSONObjectsWithSameKeysButDifferentValues() throws JSONException
    {
        final JSONArray array1 = (JSONArray) JSONParser
                .parseJSON("[ { foo:\"123\" }, { bar:\"456\" } ]");
        final JSONArray array2 = (JSONArray) JSONParser
                .parseJSON("[ { bar:\"456\" }, { foo:\"123\" } ]");

        final JSONCompareResult results = JSONCompare.compareJSON(array1, array2,
                new RegularExpressionJSONComparator(JSONCompareMode.LENIENT));
        Assert.assertFalse(results.failed());
    }

    @Test
    public void lenientArraysContainingDifferentKeys() throws JSONException
    {
        final JSONArray array1 = (JSONArray) JSONParser
                .parseJSON("[ { foo:\"123\" }, { foo:\"456\" }, { foo:\"BBB\" } ]");
        final JSONArray array2 = (JSONArray) JSONParser
                .parseJSON("[ { foo:\"456\" }, { foo:\"123\" }, { foo:\"AAA\" } ]");

        final JSONCompareResult results = JSONCompare.compareJSON(array1, array2,
                new RegularExpressionJSONComparator(JSONCompareMode.LENIENT));
        Assert.assertTrue(results.failed());
    }

    @Test
    public void lenientOutOfOrderArrays() throws JSONException
    {
        final JSONArray array1 = (JSONArray) JSONParser.parseJSON("[\"123\", \"456\"]");
        final JSONArray array2 = (JSONArray) JSONParser.parseJSON("[\"456\", \"123\"]");

        final JSONCompareResult results = JSONCompare.compareJSON(array1, array2,
                new RegularExpressionJSONComparator(JSONCompareMode.LENIENT));
        Assert.assertFalse(results.failed());
    }

    @Test
    public void lenientOutOfOrderArraysContainingArrays() throws JSONException
    {
        final JSONArray array1 = (JSONArray) JSONParser.parseJSON("[ [\"123\" ], [\"456\" ] ]");
        final JSONArray array2 = (JSONArray) JSONParser.parseJSON("[ [\"456\" ], [\"123\" ] ]");

        final JSONCompareResult results = JSONCompare.compareJSON(array1, array2,
                new RegularExpressionJSONComparator(JSONCompareMode.LENIENT));
        Assert.assertFalse(results.failed());
    }

    @Test
    public void lenientOutOfOrderArraysContainingDifferentObjects() throws JSONException
    {
        final JSONArray array1 = (JSONArray) JSONParser
                .parseJSON("[ { foo:\"123\" }, { foo:\"456\" }, { foo:\"BBB\" } ]");
        final JSONArray array2 = (JSONArray) JSONParser
                .parseJSON("[ { bar:\"456\" }, { bar:\"123\" }, { bar:\"AAA\" } ]");

        final JSONCompareResult results = JSONCompare.compareJSON(array1, array2,
                new RegularExpressionJSONComparator(JSONCompareMode.LENIENT));
        Assert.assertTrue(results.failed());
    }

    @Test
    public void lenientOutOfOrderArraysContainingMixedItems() throws JSONException
    {
        final JSONArray array1 = (JSONArray) JSONParser.parseJSON("[ { foo:\"123\" }, 456 ]");
        final JSONArray array2 = (JSONArray) JSONParser.parseJSON("[ 456, { foo:\"123\" } ]");

        final JSONCompareResult results = JSONCompare.compareJSON(array1, array2,
                new RegularExpressionJSONComparator(JSONCompareMode.LENIENT));
        Assert.assertFalse(results.failed());
    }

    @Test
    public void lenientOutOfOrderArraysContainingObjects() throws JSONException
    {
        final JSONArray array1 = (JSONArray) JSONParser
                .parseJSON("[ { foo:\"123\" }, { foo:\"456\" } ]");
        final JSONArray array2 = (JSONArray) JSONParser
                .parseJSON("[ { foo:\"456\" }, { foo:\"123\" } ]");

        final JSONCompareResult results = JSONCompare.compareJSON(array1, array2,
                new RegularExpressionJSONComparator(JSONCompareMode.LENIENT));
        Assert.assertFalse(results.failed());
    }

    @Test
    public void lenientOutOfOrderArraysInsufficientItems() throws JSONException
    {
        final JSONArray array1 = (JSONArray) JSONParser.parseJSON("[\"123\", \"123\"]");
        final JSONArray array2 = (JSONArray) JSONParser.parseJSON("[\"789\", \"123\"]");

        final JSONCompareResult results = JSONCompare.compareJSON(array1, array2,
                new RegularExpressionJSONComparator(JSONCompareMode.LENIENT));
        Assert.assertTrue(results.failed());
    }

    @Test
    public void lenientOutOfOrderArraysMissingItems() throws JSONException
    {
        final JSONArray array1 = (JSONArray) JSONParser.parseJSON("[\"123\", \"456\"]");
        final JSONArray array2 = (JSONArray) JSONParser.parseJSON("[\"789\", \"123\"]");

        final JSONCompareResult results = JSONCompare.compareJSON(array1, array2,
                new RegularExpressionJSONComparator(JSONCompareMode.LENIENT));
        Assert.assertTrue(results.failed());
    }

    @Test
    public void lenientOutOfOrderArraysMixedAndMissingItems() throws JSONException
    {
        final JSONArray array1 = (JSONArray) JSONParser.parseJSON("[ { foo:\"123\" }, 789 ]");
        final JSONArray array2 = (JSONArray) JSONParser.parseJSON("[ 456, { foo:\"123\" } ]");

        final JSONCompareResult results = JSONCompare.compareJSON(array1, array2,
                new RegularExpressionJSONComparator(JSONCompareMode.LENIENT));
        Assert.assertTrue(results.failed());
    }

    @Test
    public void strictOutOfOrderArrays() throws JSONException
    {
        final JSONArray array1 = (JSONArray) JSONParser.parseJSON("[\"123\", \"456\"]");
        final JSONArray array2 = (JSONArray) JSONParser.parseJSON("[\"456\", \"123\"]");

        final JSONCompareResult results = JSONCompare.compareJSON(array1, array2,
                new RegularExpressionJSONComparator(JSONCompareMode.STRICT_ORDER));
        Assert.assertTrue(results.failed());
    }

    @Test
    public void strictOutOfOrderArraysContainingArrays() throws JSONException
    {
        final JSONArray array1 = (JSONArray) JSONParser.parseJSON("[ [\"123\" ], [\"456\" ] ]");
        final JSONArray array2 = (JSONArray) JSONParser.parseJSON("[ [\"456\" ], [\"123\" ] ]");

        final JSONCompareResult results = JSONCompare.compareJSON(array1, array2,
                new RegularExpressionJSONComparator(JSONCompareMode.STRICT_ORDER));
        Assert.assertTrue(results.failed());
    }

    @Test
    public void strictOutOfOrderArraysContainingObjects() throws JSONException
    {
        final JSONArray array1 = (JSONArray) JSONParser
                .parseJSON("[ { foo:\"123\" }, { bar:\"456\" } ]");
        final JSONArray array2 = (JSONArray) JSONParser
                .parseJSON("[ { bar:\"456\" }, { foo:\"123\" } ]");

        final JSONCompareResult results = JSONCompare.compareJSON(array1, array2,
                new RegularExpressionJSONComparator(JSONCompareMode.STRICT_ORDER));
        Assert.assertTrue(results.failed());
    }

    @Test
    public void structMissingItemsInArray() throws JSONException
    {
        final JSONObject object1 = (JSONObject) JSONParser.parseJSON("{ \"a\": [ 1, 2 ] }");
        final JSONObject object2 = (JSONObject) JSONParser.parseJSON("{ \"a\": [ 1 ] }");

        final JSONCompareResult results = JSONCompare.compareJSON(object1, object2,
                new RegularExpressionJSONComparator(JSONCompareMode.STRICT));
        Assert.assertTrue(results.failed());
        Assert.assertEquals(1, results.getFieldMissing().size());
        Assert.assertEquals(0, results.getFieldUnexpected().size());
    }

    @Test
    public void structUnexpectedItemsInArray() throws JSONException
    {
        final JSONObject object1 = (JSONObject) JSONParser.parseJSON("{ \"a\": [ 1 ] }");
        final JSONObject object2 = (JSONObject) JSONParser.parseJSON("{ \"a\": [ 1,2 ] }");

        final JSONCompareResult results = JSONCompare.compareJSON(object1, object2,
                new RegularExpressionJSONComparator(JSONCompareMode.STRICT));
        Assert.assertTrue(results.failed());
        Assert.assertEquals(0, results.getFieldMissing().size());
        Assert.assertEquals(1, results.getFieldUnexpected().size());
    }

}
