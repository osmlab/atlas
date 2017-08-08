package org.openstreetmap.atlas.tags.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Java annotation for Atlas tags including how we should validate their values at runtime
 *
 * @author cstaylor
 */
@Target(value = ElementType.TYPE_USE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Tag
{
    /**
     * This is an optional range of values that can be used in numeric validators
     *
     * @author cstaylor
     */
    public @interface Range
    {
        // These are values that shouldn't be allowed, even if they fall inside the range
        long[] exclude() default {};

        // Values aren't mixed up: these are sentinel values that disable the range
        long max() default Long.MIN_VALUE;

        long min() default Long.MAX_VALUE;
    }

    /**
     * Validation rules for the values of a tag:
     * <ul>
     * <li>MATCH - an exact match against a set of values</li>
     * <li>ORDINAL - a positive integer. Also implies MATCH for any defined TagValues in the Tag
     * </li>
     * <li>LONG - a long of any value constrained by the optional range. Also implies MATCH for any
     * defined TagValues in the Tag</li>
     * <li>DOUBLE - parse the value as a double. Also implies MATCH for any defined TagValues in the
     * Tag</li>
     * <li>TIMESTAMP - parse the value as a long and create a Date from that value. Also implies
     * MATCH for any defined TagValues in the Tag</li>
     * <li>NON_EMPTY_STRING - valid if the value has at least one non-whitespace character. Does NOT
     * imply MATCH</li>
     * <li>ISO_COUNTRY - valid if the value matches an entry in the ISO country code list. Does NOT
     * imply MATCH</li>
     * <li>NONE - no validation performed</li>
     * <li>URI - check if the value if a well-formed URI</li>
     * <li>SPEED - check if the value is a well-formed speed</li>
     * <li>LENGTH - check if the value is a well-formed length</li>
     * </ul>
     *
     * @author cstaylor
     */
    enum Validation
    {
        MATCH,
        ORDINAL,
        LONG,
        DOUBLE,
        TIMESTAMP,
        NON_EMPTY_STRING,
        ISO3_COUNTRY,
        ISO2_COUNTRY,
        NONE,
        URI,
        SPEED,
        LENGTH;
    }

    /**
     * Optional URL to OSM wiki page for this tag
     *
     * @return the URL or an empty string if no tag is defined
     */
    String osm() default "";

    Range range() default @Range();

    /**
     * If true, this tag is an artifact of processing and does not exist in OSM itself.
     *
     * @return true if this tag has been marked as synthetic, false otherwise
     */
    boolean synthetic() default false;

    /**
     * Optional URL to taginfo site for this tag
     *
     * @return the URL or an empty string if no tag is defined
     */
    String taginfo() default "";

    /**
     * The validation rule for this particular Atlas Tag
     *
     * @return the defined Validation rule: defaults to MATCH
     */
    Validation value() default Validation.MATCH;

    /**
     * This lets tags use shared enum values. For example shop and disused:shop
     *
     * @return the optional array of enums to copy their names for exact matches
     */
    Class<? extends Enum<?>>[] with() default {};
}
