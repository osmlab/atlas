package org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.regex

import org.junit.Test

/**
 * @author Yazad Khambata
 */
class RegexSupportTest {

    @Test
    void testMatchesOneWord() {
        assert RegexSupport.instance.matches("facebook", /face.*/)
        assert RegexSupport.instance.matches("facebook", /.*book/)
        assert RegexSupport.instance.matches("facebook", /.*ace.*/)
    }

    @Test
    void testMatchesSentences() {
        assert RegexSupport.instance.matches("The Course at Cuisinart Golf Resort & Spa", /Resort/)
    }
}
