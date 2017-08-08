package org.openstreetmap.atlas.utilities.jsoncompare;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.skyscreamer.jsonassert.JSONCompareResult;

/**
 * Uses the searchPatterns to selectively ignore certain failures based on the feature and values in
 * question.
 *
 * @author cstaylor
 */
class RegularExpressionJSONCompareResult extends JSONCompareResult
{
    private final List<Pattern> searchPatterns;

    RegularExpressionJSONCompareResult(final List<Pattern> searchPatterns)
    {
        this.searchPatterns = searchPatterns;
    }

    @Override
    public JSONCompareResult fail(final String field, final Object expected, final Object actual)
    {
        if (!matches(field))
        {
            super.fail(field, expected, actual);
        }
        return this;
    }

    @Override
    public JSONCompareResult missing(final String field, final Object expected)
    {
        if (!matches(String.format("%s.%s", field, expected)))
        {
            super.missing(field, expected);
        }
        return this;
    }

    @Override
    public JSONCompareResult unexpected(final String field, final Object value)
    {
        if (!matches(String.format("%s.%s", field, value)))
        {
            super.unexpected(field, value);
        }
        return this;
    }

    private boolean matches(final String compareMe)
    {
        return this.searchPatterns.stream().map(pattern -> pattern.matcher(compareMe))
                .anyMatch(Matcher::matches);
    }
}
