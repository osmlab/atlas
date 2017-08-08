package org.openstreetmap.atlas.utilities.jsoncompare;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.comparator.JSONComparator;

/**
 * Edge-case Test cases for the {@link RegularExpressionJSONComparator}
 *
 * @author cstaylor
 */
public class DegenerateRegularExpressionJSONComparatorTestCase
{
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void allArraysNull() throws JSONException
    {
        this.thrown.expect(NullPointerException.class);
        JSONCompare.compareJSON((JSONArray) null, (JSONArray) null, (JSONComparator) null);
    }

    @Test
    public void allObjectsNull() throws JSONException
    {
        this.thrown.expect(NullPointerException.class);
        JSONCompare.compareJSON((JSONObject) null, (JSONObject) null, (JSONComparator) null);
    }

    @Test
    public void allStringsNull() throws JSONException
    {
        this.thrown.expect(NullPointerException.class);
        JSONCompare.compareJSON((String) null, (String) null, (JSONComparator) null);
    }

    @Test
    public void degenerateStringsTest() throws JSONException
    {
        this.thrown.expect(JSONException.class);
        JSONCompare.compareJSON("", "", (JSONComparator) null);
    }

    @Test
    public void emptyArrays() throws JSONException
    {
        final JSONCompareResult results = JSONCompare.compareJSON("[]", "[]",
                new RegularExpressionJSONComparator(JSONCompareMode.STRICT));
        Assert.assertFalse(results.failed());
    }

    @Test
    public void equalFieldsEmptyComparator() throws JSONException
    {
        final JSONCompareResult results = JSONCompare.compareJSON("{}", "{}",
                new RegularExpressionJSONComparator(JSONCompareMode.STRICT));
        Assert.assertFalse(results.failed());
    }

    @Test
    public void equalFieldsNullComparator() throws JSONException
    {
        this.thrown.expect(NullPointerException.class);
        JSONCompare.compareJSON("{}", "{}", (JSONComparator) null);
    }
}
