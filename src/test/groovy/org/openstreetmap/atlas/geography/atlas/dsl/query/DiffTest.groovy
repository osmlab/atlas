package org.openstreetmap.atlas.geography.atlas.dsl.query

import org.apache.commons.lang3.tuple.Pair
import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.change.Change
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.query.difference.Difference
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.MutantResult
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasSchema
import org.openstreetmap.atlas.geography.atlas.items.ItemType

import java.util.stream.Collectors

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.*
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.*

/**
 * @author Yazad Khambata
 */
class DiffTest extends AbstractAQLTest {
    @Test
    void testDiff() {
        final AtlasSchema atlasSchema = usingAlcatraz()
        def update1 = update atlasSchema.node set node.addTag(test: 'added') where node.hasTagLike(/\*wheelchair*/) or node.hasTagLike("operator")
        final MutantResult<Node> result = exec update1
        final AtlasSchema afterUpdate = commit update1
        final Difference difference = diff atlasSchema, afterUpdate
        assert toComparableList(result.getChange()) == toComparableList(difference.getChange())
    }

    private List<Pair<ItemType, Long>> toComparableList(final Change change) {
        change.changes().map { featureChange -> Pair.of(featureChange.getItemType(), featureChange.getIdentifier()) }.sorted().collect(Collectors.toList())
    }
}
