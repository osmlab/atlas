package org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.regex
/**
 * @author Yazad Khambata
 */
@Singleton
class RegexSupport {

    boolean matches(final String input, final String regex) {
        input =~ regex
    }
}
