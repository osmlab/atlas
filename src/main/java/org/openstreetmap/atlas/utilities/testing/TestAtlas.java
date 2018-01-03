package org.openstreetmap.atlas.utilities.testing;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area.Known;

/**
 * Describes an Atlas in test code. Beneath the covers we use a {@link PackedAtlasBuilder} to create
 * the Atlas object.
 *
 * @author cstaylor
 */
@Retention(value = RUNTIME)
@Target(value = FIELD)
public @interface TestAtlas
{
    /**
     * Adds an area to an Atlas
     *
     * @author cstaylor
     */
    public @interface Area
    {
        /**
         * Known (constant) areas that we can use in testing when we aren't trying to verify
         * geometry. if USE_COORDINATES is defined, the field handler should use the coordinates
         * value to build an area
         *
         * @author cstaylor
         */
        enum Known
        {
            SILICON_VALLEY,
            USE_COORDINATES,
            BUILDING_1,
            BUILDING_2;
        }

        Loc[] coordinates() default {};

        String id() default AUTO_GENERATED;

        /**
         * We can also specify a known Area using the known enum above
         *
         * @return should we be using specified coordinates or a well-known set of coordinates?
         */
        Known known() default Known.USE_COORDINATES;

        /**
         * Tags we want on the Area
         *
         * @return array of tag annotations
         */
        String[] tags() default {};
    }

    /**
     * While this could be accomplished through areas, tags, and relations, it would make the test
     * fixture code shorter to have a specialized annotation just for buildings
     *
     * @author cstaylor
     */
    public @interface Building
    {
        String id() default AUTO_GENERATED;

        /**
         * The optional inner areas of a building
         *
         * @return an array of areas that mark the inside of buildings
         */
        Area[] inners() default {};

        /**
         * The required outer area of a building
         *
         * @return an area representing the outer structure of the building: defaults to our Silicon
         *         Valley-sized building
         */
        Area outer() default @Area(known = Known.SILICON_VALLEY);

        /**
         * Tags we want on the outline of the Building
         *
         * @return array of tag annotations specific for the outline relation
         */
        String[] outlineTags() default {};

        /**
         * The optional building parts of a building
         *
         * @return an array of areas representing parts of a larger building structure
         */
        Area[] parts() default {};

        /**
         * Tags we want on the Building
         *
         * @return array of tag annotations
         */
        String[] tags() default {};
    }

    /**
     * Adds an edge to an Atlas
     *
     * @author cstaylor
     */
    public @interface Edge
    {
        Loc[] coordinates() default {};

        String id() default AUTO_GENERATED;

        String[] tags() default {};
    }

    /**
     * Adds a line to an Atlas
     *
     * @author cstaylor
     */
    public @interface Line
    {
        Loc[] coordinates() default {};

        String id() default AUTO_GENERATED;

        String[] tags() default {};
    }

    /**
     * Represents a single Location (lat,lon) coordinate. You can also use a String instead of
     * individual coordinates. We support the following formats for value:
     * <ul>
     * <li>USE_LATLON - the values should come from the lat and lon parameters</li>
     * <li>TEST_1 - Value of Location.TEST_1</li>
     * <li>lat string,lon string - parse the latitude and longitude from the string</li>
     * </ul>
     *
     * @author cstaylor
     */
    public @interface Loc
    {
        String USE_LATLON = "<novalue>";

        String TEST_1 = "<test1>";

        double BAD_VALUE = Long.MIN_VALUE;

        double lat() default BAD_VALUE;

        double lon() default BAD_VALUE;

        String value() default TEST_1;
    }

    /**
     * Adds a node to an Atlas
     *
     * @author cstaylor
     */
    public @interface Node
    {
        Loc coordinates() default @Loc;

        String id() default AUTO_GENERATED;

        /**
         * Tags we want on the Area
         *
         * @return array of tag annotations
         */
        String[] tags() default {};
    }

    /**
     * Adds a point to an Atlas
     *
     * @author cstaylor
     */
    public @interface Point
    {
        Loc coordinates() default @Loc;

        String id() default AUTO_GENERATED;

        /**
         * Tags we want on the Area
         *
         * @return array of tag annotations
         */
        String[] tags() default {};
    }

    /**
     * Adds a Relation to an Atlas
     *
     * @author matthieun
     */
    public @interface Relation
    {
        /**
         * Adds a Member to a Relation
         *
         * @author matthieun
         */
        public @interface Member
        {
            String id();

            String role();

            String type();
        }

        String id() default AUTO_GENERATED;

        Member[] members() default {};

        /**
         * Tags we want on the Relation
         *
         * @return array of tag annotations
         */
        String[] tags() default {};
    }

    /**
     * Optional size estimate for the Atlas
     *
     * @author cstaylor
     */
    public @interface SizeEstimate
    {
        long NO_VALUE = 0;

        long areas() default NO_VALUE;

        long edges()

        default NO_VALUE;

        long lines()

        default NO_VALUE;

        long nodes()

        default NO_VALUE;

        long point()

        default NO_VALUE;

        long relations()

        default NO_VALUE;
    }

    // This is for identifiers: by default they will be auto-generated
    String AUTO_GENERATED = "<auto>";

    /**
     * Areas we want added to the atlas
     *
     * @return the array of areas we want added to the atlas
     */
    Area[] areas() default {};

    /**
     * While we could build buildings from areas and relations, this makes it easier to read in test
     * fixture code.
     *
     * @return an array of building annotations
     */
    Building[] buildings() default {};

    /**
     * Edges we want added to the atlas
     *
     * @return the array of edges we want added to the atlas
     */
    Edge[] edges() default {};

    /**
     * Lines we want added to the atlas
     *
     * @return the array of lines we want added to the atlas
     */
    Line[] lines() default {};

    /**
     * Sometimes we want to load the Atlas directly from a file. Note that this setting takes
     * precedence over directly defining values
     *
     * @return the resource path to the osm file
     */
    String loadFromJosmOsmResource() default "";

    /**
     * Sometimes we want to load the Atlas directly from a file. Note that this setting takes
     * precedence over directly defining values
     *
     * @return the resource path to the text file
     */
    String loadFromTextResource() default "";

    /**
     * Nodes we want added to the atlas
     *
     * @return the array of nodes we want added to the atlas
     */
    Node[] nodes() default {};

    /**
     * Points we want added to the atlas
     *
     * @return the array of points we want added to the atlas
     */
    Point[] points() default {};

    /**
     * Relations we want added to the atlas
     *
     * @return the array of relations we want added to the atlas
     */
    Relation[] relations() default {};

    /**
     * Size estimate for the atlas
     *
     * @return the size estimate of the atlas
     */
    SizeEstimate size() default @SizeEstimate;
}
