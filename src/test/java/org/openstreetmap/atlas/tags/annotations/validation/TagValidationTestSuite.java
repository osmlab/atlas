package org.openstreetmap.atlas.tags.annotations.validation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is a JUnit TestSuite for running multiple tests in one run, so when I use eclemma I can see
 * the coverage results. At the moment gradle doesn't run these, so these are primarily for code
 * coverage checks within Eclipse
 *
 * @author cstaylor
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ DisusedShopTagTestCase.class, ISOCountryTagTestCase.class,
        OpeningHoursTagTestCase.class, ValidatorsTestCase.class, BuildingTagTestCase.class,
        FromEnumTestCase.class, HighwayTagTestCase.class, ValidatorsHasValuesForTestCase.class,
        LastEditUserIdentifierTestCase.class, SpeedTagsTestCase.class })
public class TagValidationTestSuite
{

}
