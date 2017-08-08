package org.openstreetmap.atlas.utilities.jsoncompare;

import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.allJSONObjects;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.allSimpleValues;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.arrayOfJsonObjectToMap;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.findUniqueKey;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.formatUniqueKey;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.getKeys;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.isUsableAsUniqueKey;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.jsonArrayToList;
import static org.skyscreamer.jsonassert.comparator.JSONCompareUtil.qualify;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.comparator.JSONComparator;
import org.skyscreamer.jsonassert.comparator.JSONCompareUtil;

/**
 * Code adapted from the AbstractJSONComparator and DefaultJSONComparator classes in the JSONAssert
 * project found at https://github.com/skyscreamer/JSONassert
 *
 * @author cstaylor
 */
public class RegularExpressionJSONComparator implements JSONComparator
{
    /**
     * List of feature and values we should ignore when comparing values
     */
    private final List<Pattern> searchPatterns;

    /**
     * What comparison mode should we use?
     */
    private final JSONCompareMode mode;

    public RegularExpressionJSONComparator(final JSONCompareMode mode)
    {
        this.searchPatterns = new ArrayList<>();
        this.mode = mode;
    }

    /**
     * Compares JSONArray provided to the expected JSONArray, and returns the results of the
     * comparison.
     *
     * @param expected
     *            Expected JSONArray
     * @param actual
     *            JSONArray to compare
     * @throws JSONException
     *             something goes wrong when reading JSON values
     */
    @Override
    public final JSONCompareResult compareJSON(final JSONArray expected, final JSONArray actual)
            throws JSONException
    {
        final JSONCompareResult result = createResult();
        compareJSONArray("", expected, actual, result);
        return result;
    }

    /**
     * Compares JSONObject provided to the expected JSONObject, and returns the results of the
     * comparison.
     *
     * @param expected
     *            Expected JSONObject
     * @param actual
     *            JSONObject to compare
     * @throws JSONException
     *             something goes wrong when reading JSON values
     */
    @Override
    public final JSONCompareResult compareJSON(final JSONObject expected, final JSONObject actual)
            throws JSONException
    {
        final JSONCompareResult result = createResult();
        compareJSON("", expected, actual, result);
        return result;
    }

    @Override
    public void compareJSON(final String prefix, final JSONObject expected, final JSONObject actual,
            final JSONCompareResult result) throws JSONException
    {
        // Check that actual contains all the expected values
        checkJsonObjectKeysExpectedInActual(prefix, expected, actual, result);

        // If strict, check for vice-versa
        if (!this.mode.isExtensible())
        {
            checkJsonObjectKeysActualInExpected(prefix, expected, actual, result);
        }
    }

    @Override
    public void compareJSONArray(final String prefix, final JSONArray expected,
            final JSONArray actual, final JSONCompareResult result) throws JSONException
    {
        if (expected.length() > actual.length())
        {
            final int end = Math.max(expected.length(), actual.length());
            for (int loop = Math.min(expected.length(), actual.length()); loop < end; loop++)
            {
                result.missing(String.format("%s[%d]", prefix, loop), expected.get(loop));
            }
            return;
        }
        else if (expected.length() < actual.length())
        {
            final int end = Math.max(expected.length(), actual.length());
            for (int loop = Math.min(expected.length(), actual.length()); loop < end; loop++)
            {
                result.unexpected(String.format("%s[%d]", prefix, loop), actual.get(loop));
            }
            return;
        }
        else if (expected.length() == 0)
        {
            // Nothing to compare
            return;
        }

        if (this.mode.hasStrictOrder())
        {
            compareJSONArrayWithStrictOrder(prefix, expected, actual, result);
        }
        else if (allSimpleValues(expected))
        {
            compareJSONArrayOfSimpleValues(prefix, expected, actual, result);
        }
        else if (allJSONObjects(expected))
        {
            compareJSONArrayOfJsonObjects(prefix, expected, actual, result);
        }
        else
        {
            // An expensive last resort
            recursivelyCompareJSONArray(prefix, expected, actual, result);
        }
    }

    @Override
    public void compareValues(final String prefix, final Object expectedValue,
            final Object actualValue, final JSONCompareResult result) throws JSONException
    {
        if (expectedValue instanceof Number && actualValue instanceof Number)
        {
            if (((Number) expectedValue).doubleValue() != ((Number) actualValue).doubleValue())
            {
                result.fail(prefix, expectedValue, actualValue);
            }
        }
        else if (expectedValue.getClass().isAssignableFrom(actualValue.getClass()))
        {
            if (expectedValue instanceof JSONArray)
            {
                compareJSONArray(prefix, (JSONArray) expectedValue, (JSONArray) actualValue,
                        result);
            }
            else if (expectedValue instanceof JSONObject)
            {
                compareJSON(prefix, (JSONObject) expectedValue, (JSONObject) actualValue, result);
            }
            else if (!expectedValue.equals(actualValue))
            {
                result.fail(prefix, expectedValue, actualValue);
            }
        }
        else
        {
            result.fail(prefix, expectedValue, actualValue);
        }
    }

    /**
     * Automatically adds a regex section to each regex in regexes that anchors the regular
     * expression to the end of the line and allows for extra information before the beginning of
     * the regex.
     *
     * @param regexes
     *            the suffix match regexes we should add to our ignore list
     * @return Fluent API means we return this
     */
    public RegularExpressionJSONComparator endsWith(final String... regexes)
    {
        return exact(Stream.of(regexes).map(regex -> String.format("(\\W)*(\\S)*%s$", regex))
                .collect(Collectors.toList()).toArray(new String[0]));
    }

    /**
     * Adds a specific string as a regex pattern to the list of values that should be ignored from
     * processing
     *
     * @param regexes
     *            the exact match regexes we should add to our ignore list
     * @return Fluent API means we return this
     */
    public RegularExpressionJSONComparator exact(final String... regexes)
    {
        Stream.of(regexes).map(Pattern::compile).forEach(this.searchPatterns::add);
        return this;
    }

    /**
     * Automatically adds a regex section to each regex in regexes that anchors the regular
     * expression to the beginning of the line and allows for extra information after the end of the
     * regex.
     *
     * @param regexes
     *            the prefix match regexes we should add to our ignore list
     * @return Fluent API means we return this
     */
    public RegularExpressionJSONComparator startsWith(final String... regexes)
    {
        return exact(Stream.of(regexes).map(regex -> String.format("^%s(\\W)*(\\S)*", regex))
                .collect(Collectors.toList()).toArray(new String[0]));
    }

    protected void checkJsonObjectKeysActualInExpected(final String prefix,
            final JSONObject expected, final JSONObject actual, final JSONCompareResult result)
    {
        getKeys(actual).stream().filter(key -> !expected.has(key)).forEach(key ->
        {
            result.unexpected(prefix, key);
        });
    }

    protected void checkJsonObjectKeysExpectedInActual(final String prefix,
            final JSONObject expected, final JSONObject actual, final JSONCompareResult result)
            throws JSONException
    {
        for (final String key : getKeys(expected))
        {
            if (actual.has(key))
            {
                compareValues(qualify(prefix, key), expected.get(key), actual.get(key), result);
            }
            else
            {
                result.missing(prefix, key);
            }
        }
    }

    protected void compareJSONArrayOfJsonObjects(final String key, final JSONArray expected,
            final JSONArray actual, final JSONCompareResult result) throws JSONException
    {
        final String uniqueKey = findUniqueKey(expected);
        if (uniqueKey == null || !isUsableAsUniqueKey(uniqueKey, actual))
        {
            // An expensive last resort
            recursivelyCompareJSONArray(key, expected, actual, result);
            return;
        }
        final Map<Object, JSONObject> expectedValueMap = arrayOfJsonObjectToMap(expected,
                uniqueKey);
        final Map<Object, JSONObject> actualValueMap = arrayOfJsonObjectToMap(actual, uniqueKey);
        for (final Object identifier : expectedValueMap.keySet())
        {
            if (!actualValueMap.containsKey(identifier))
            {
                result.missing(formatUniqueKey(key, uniqueKey, identifier),
                        expectedValueMap.get(identifier));
                continue;
            }
            final JSONObject expectedValue = expectedValueMap.get(identifier);
            final JSONObject actualValue = actualValueMap.get(identifier);
            compareValues(formatUniqueKey(key, uniqueKey, identifier), expectedValue, actualValue,
                    result);
        }
        for (final Object identifier : actualValueMap.keySet())
        {
            if (!expectedValueMap.containsKey(identifier))
            {
                result.unexpected(formatUniqueKey(key, uniqueKey, identifier),
                        actualValueMap.get(identifier));
            }
        }
    }

    protected void compareJSONArrayOfSimpleValues(final String key, final JSONArray expected,
            final JSONArray actual, final JSONCompareResult result) throws JSONException
    {
        final Map<Object, Integer> expectedCount = JSONCompareUtil
                .getCardinalityMap(jsonArrayToList(expected));
        final Map<Object, Integer> actualCount = JSONCompareUtil
                .getCardinalityMap(jsonArrayToList(actual));
        for (final Object foundKey : expectedCount.keySet())
        {
            if (!actualCount.containsKey(foundKey))
            {
                result.missing(key + "[]", foundKey);
            }
            else if (!actualCount.get(foundKey).equals(expectedCount.get(foundKey)))
            {
                result.fail(key + "[]: Expected " + expectedCount.get(foundKey)
                        + " occurrence(s) of " + foundKey + " but got " + actualCount.get(foundKey)
                        + " occurrence(s)");
            }
        }
        for (final Object foundKey : actualCount.keySet())
        {
            if (!expectedCount.containsKey(foundKey))
            {
                result.unexpected(key + "[]", foundKey);
            }
        }
    }

    protected void compareJSONArrayWithStrictOrder(final String key, final JSONArray expected,
            final JSONArray actual, final JSONCompareResult result) throws JSONException
    {
        for (int i = 0; i < expected.length(); ++i)
        {
            final Object expectedValue = expected.get(i);
            final Object actualValue = actual.get(i);
            compareValues(key + "[" + i + "]", expectedValue, actualValue, result);
        }
    }

    protected JSONCompareResult createResult()
    {
        return new RegularExpressionJSONCompareResult(this.searchPatterns);
    }

    /**
     * This is expensive (O(n^2) -- yuck), but may be the only resort for some cases with loose
     * array ordering, and no
     *
     * @param key
     *            the named key where expected and actual came from
     * @param expected
     *            the value we expect actual to match
     * @param actual
     *            the value we're trying to compare
     * @param result
     *            where we keep our failure list
     * @throws JSONException
     *             if something goes wrong when retrieving values from the JSON objects
     */
    protected void recursivelyCompareJSONArray(final String key, final JSONArray expected,
            final JSONArray actual, final JSONCompareResult result) throws JSONException
    {
        final Set<Integer> matched = new HashSet<>();
        for (int i = 0; i < expected.length(); ++i)
        {
            final Object expectedElement = expected.get(i);
            boolean matchFound = false;
            for (int j = 0; j < actual.length(); ++j)
            {
                final Object actualElement = actual.get(j);
                if (matched.contains(j)
                        || !actualElement.getClass().equals(expectedElement.getClass()))
                {
                    continue;
                }
                if (expectedElement instanceof JSONObject)
                {
                    if (compareJSON((JSONObject) expectedElement, (JSONObject) actualElement)
                            .passed())
                    {
                        matched.add(j);
                        matchFound = true;
                        break;
                    }
                }
                else if (expectedElement instanceof JSONArray)
                {
                    if (compareJSON((JSONArray) expectedElement, (JSONArray) actualElement)
                            .passed())
                    {
                        matched.add(j);
                        matchFound = true;
                        break;
                    }
                }
                else if (expectedElement.equals(actualElement))
                {
                    matched.add(j);
                    matchFound = true;
                    break;
                }
            }
            if (!matchFound)
            {
                result.fail(
                        key + "[" + i + "] Could not find match for element " + expectedElement);
                return;
            }
        }
    }
}
