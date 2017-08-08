package org.openstreetmap.atlas.utilities.jsoncompare;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test Suite for all {@link RegularExpressionJSONComparator} Test Cases: makes coverage checks
 * easier to do
 *
 * @author cstaylor
 */
@RunWith(Suite.class)
@SuiteClasses({ ArraysRegularExpressionJSONComparatorTestCase.class,
        DegenerateRegularExpressionJSONComparatorTestCase.class,
        MatchingRegularExpressionJSONComparatorTestCase.class,
        ObjectsRegularExpressionJSONComparatorTestCase.class, })
public class RegularExpressionJSONComparatorTestSuite
{

}
