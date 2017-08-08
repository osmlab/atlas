package org.openstreetmap.atlas.streaming.resource;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test Cases for new File Detection in the File Suffix Enum
 *
 * @author Jack
 */
public class FileSuffixTestCase
{
    @Test
    public void testDoesntExistNoMatch()
    {
        Assert.assertFalse(FileSuffix.CSV.matches(new FileSuffixTestCaseResource("test.fizzbuzz")));
    }

    @Test
    public void testExistsButNoMatch()
    {
        Assert.assertFalse(FileSuffix.CSV.matches(new FileSuffixTestCaseResource("test.csv.ext")));
    }

    @Test
    public void testMatches()
    {
        Assert.assertTrue(FileSuffix.CSV.matches(new FileSuffixTestCaseResource("test.csv")));
    }
}
