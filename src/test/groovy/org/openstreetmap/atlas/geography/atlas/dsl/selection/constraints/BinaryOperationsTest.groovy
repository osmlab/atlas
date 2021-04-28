package org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints

import org.junit.Test
import org.openstreetmap.atlas.geography.Latitude
import org.openstreetmap.atlas.geography.Location
import org.openstreetmap.atlas.geography.Longitude
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode
import org.openstreetmap.atlas.geography.atlas.items.Node

/**
 * @author Yazad Khambata
 */
class BinaryOperationsTest {

    @Test
    void testEq() {
        final def l1 = 100L

        assert BinaryOperations.eq.perform(l1, l1, Node) == true
        assert BinaryOperations.eq.perform(l1, l1 - 1, Node) == false
    }

    @Test
    void testNe() {
        final def l1 = 100L

        assert BinaryOperations.ne.perform(l1, l1 - 1, Node) == true
        assert BinaryOperations.ne.perform(l1, l1, Node) == false
    }

    @Test
    void testLt() {
        final def l1 = 99L

        final Long l2 = 100L

        assert BinaryOperations.lt.perform(l1, l2, Node) == true
        assert BinaryOperations.lt.perform(l2, l1, Node) == false
        assert BinaryOperations.lt.perform(l1, l1, Node) == false
    }

    @Test
    void testLe() {
        final def l1 = 99L

        final Long l2 = 100L

        assert BinaryOperations.le.perform(l1, l2, Node) == true
        assert BinaryOperations.le.perform(l2, l1, Node) == false
        assert BinaryOperations.le.perform(l1, l1, Node) == true
    }

    @Test
    void testGt() {
        final def l1 = 100L

        final Long l2 = 99L

        assert BinaryOperations.gt.perform(l1, l2, Node) == true
        assert BinaryOperations.gt.perform(l2, l1, Node) == false
        assert BinaryOperations.gt.perform(l1, l1, Node) == false
    }

    @Test
    void testGe() {
        final def l1 = 100L

        final Long l2 = 99L

        assert BinaryOperations.ge.perform(l1, l2, Node) == true
        assert BinaryOperations.ge.perform(l2, l1, Node) == false
        assert BinaryOperations.ge.perform(l1, l1, Node) == true
    }

    @Test
    void testIn() {
        final Long l = 10L
        final def lst1 = [20L, 10L, 30L]
        final def lst2 = [20L, 10L, 30L] - l

        assert BinaryOperations.inside.perform(l, lst1, Node) == true
        assert BinaryOperations.inside.perform(l, lst2, Node) == false
    }

    @Test
    void testWithinForLocationItem() {
        final List<List<List<BigDecimal>>> geometricSurfaceLocations = [
                [
                        [103.7817055, 1.249780],
                        [103.8952434, 1.249780],
                        [103.8952434, 1.404801],
                        [103.7817055, 1.404801],
                        [103.7817055, 1.249780]
                ]
        ]

        final BigDecimal longitude = 1.3272885
        final BigDecimal latitude = 103.8384742
        final Location goodDot = toDot(longitude, latitude)

        final Location badDot = toDot(longitude - 0.2, latitude - 0.2)

        assert BinaryOperations.within.perform(toNode(goodDot), geometricSurfaceLocations, Node)
        assert BinaryOperations.within.perform(toNode(badDot), geometricSurfaceLocations, Node) == false
    }

    private CompleteNode toNode(Location goodDot) {
        new CompleteNode(1, goodDot, new HashMap<>(), null, null, null)
    }

    private Location toDot(BigDecimal longitude, BigDecimal latitude) {
        new Location(Latitude.degrees(longitude), Longitude.degrees(latitude))
    }

    @Test
    void testTag() {
        final Map<String, String> actualTags = [name: "San Jose", state: "California", country: "United States of America"]
        final Map<String, String> actualTagsStateWithQuotes = [name: "San Jose", "state": "California", country: "United States of America"]

        final String keyToCheck1 = "state"
        final String keyToCheck2 = "continent"

        assert BinaryOperations.tag.perform(actualTags, keyToCheck1, Node)
        assert BinaryOperations.tag.perform(actualTags, keyToCheck2, Node) == false

        assert BinaryOperations.tag.perform(actualTagsStateWithQuotes, keyToCheck1, Node)

        def keyValuePairToCheck1 = [state: "California"]
        def keyValuePairToCheck2 = [state: "Washington"]

        assert BinaryOperations.tag.perform(actualTags, keyValuePairToCheck1, Node)
        assert BinaryOperations.tag.perform(actualTags, keyValuePairToCheck2, Node) == false
    }

    @Test
    void testTagValuesTest() {
        final Map<String, String> actualTags1 = [name: "San Jose", state: "California", country: "United States of America"]
        final Map<String, String> actualTags2 = [name: "Seattle", state: "Washington", country: "United States of America"]

        def keyValuePairToCheck1 = [state: "California"]
        def keyValuePairToCheck2 = [state: ["California", "Washington"]]

        assert BinaryOperations.tag.perform(actualTags1, keyValuePairToCheck1, Node)
        assert BinaryOperations.tag.perform(actualTags2, keyValuePairToCheck1, Node) == false

        assert BinaryOperations.tag.perform(actualTags1, keyValuePairToCheck2, Node)
        assert BinaryOperations.tag.perform(actualTags2, keyValuePairToCheck2, Node)
    }

    @Test
    void testTagLike() {
        final Map<String, String> actualTags = [name: "San Jose", state: "California", country: "United States of America", "name:ko": "산호세"]

        assert BinaryOperations.tag_like.perform(actualTags, [name: /San.*/], Node)
        assert BinaryOperations.tag_like.perform(actualTags, [name: /.*BlahBlahBlah.*/], Node) == false
        assert BinaryOperations.tag_like.perform(actualTags, /.*tat.*/, Node)
        assert BinaryOperations.tag_like.perform(actualTags, /.*BlahBlahBlah.*/, Node) == false

        assert BinaryOperations.tag_like.perform(actualTags, /name:.{2,3}/, Node)
    }

    @Test
    void testLike() {
        String name = "San Jose"

        assert BinaryOperations.like.perform(name, /San.*/, Node)
        assert BinaryOperations.like.perform(name, /.*Jose/, Node)
        assert BinaryOperations.like.perform(name, /.*a.*/, Node)
        assert BinaryOperations.like.perform(name, /.*blah.*/, Node) == false
    }

    @Test
    void test() {
        def list = [0, 1, 2]
        assert 1 in list
        boolean all = [2, 0, 1].stream().map { it in list }.reduce({ x, y -> x && y }).orElse(false)
        boolean any = [1].stream().map { it in list }.reduce({ x, y -> x || y }).orElse(false)

        assert all
        assert any
    }
}
