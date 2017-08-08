package org.openstreetmap.atlas.tags;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.openstreetmap.atlas.tags.annotations.validation.TagValidationTestSuite;

/**
 * This is a JUnit TestSuite for running multiple tests in one run, so when I use eclemma I can see
 * the coverage results. At the moment gradle doesn't run these, so these are primarily for code
 * coverage checks within Eclipse
 *
 * @author cstaylor
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ LocalizedTagNameWithOptionalDateTestCase.class,
        LocalizedTaggableTestCase.class, TagValidationTestSuite.class, NameFinderTestCase.class,
        BulkNameFinderTestCase.class, ISOCountryTagTestCase.class })
public class TagTestSuite
{

}
