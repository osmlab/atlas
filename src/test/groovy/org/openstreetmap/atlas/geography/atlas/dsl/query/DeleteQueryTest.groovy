package org.openstreetmap.atlas.geography.atlas.dsl.query

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.change.Change
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.query.difference.Difference
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.Result
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB
import org.openstreetmap.atlas.geography.atlas.items.Edge
import org.openstreetmap.atlas.geography.atlas.items.ItemType

import java.util.stream.Collectors

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.*
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.*

/**
 * @author Yazad Khambata
 */
class DeleteQueryTest extends AbstractAQLTest {

    @Test
    void testDelete() {
        def atlasSchema = usingAlcatraz()
        def selectBeforeDelete = select edge.tags from atlasSchema.edge where edge.hasTag(highway: "footway") and not(edge.hasTag(foot: "yes"))
        final Result resultBeforeDelete = exec selectBeforeDelete

        def delete1 = delete atlasSchema.edge where edge.hasTag(highway: "footway") and not(edge.hasTag(foot: "yes"))

        final Result resultFromEdgeDelete = exec delete1

        assert resultFromEdgeDelete.relevantIdentifiers.sort() == resultBeforeDelete.relevantIdentifiers.sort()

        def afterDelete = commit delete1

        def selectAfterDelete = select edge._ from afterDelete.edge where edge.hasTag(highway: "footway") and not(edge.hasTag(foot: "yes"))
        final Result resultAfterDelete = exec selectAfterDelete

        assert resultAfterDelete.relevantIdentifiers.isEmpty()

        final Difference difference = diff atlasSchema, afterDelete
        final Change change = difference.getChange()

        final List<FeatureChange> featureChanges = change.getFeatureChanges()
        final Map<ItemType, List<FeatureChange>> featureChangesByItemType = featureChanges.stream().collect(Collectors.groupingBy { FeatureChange featureChange -> featureChange.getItemType() })


        assert featureChangesByItemType.size() == 2
        final int numberOfDeletedEdges = resultFromEdgeDelete.relevantIdentifiers.size()
        assert numberOfDeletedEdges > 1
        assert featureChangesByItemType[(ItemType.EDGE)].size() == numberOfDeletedEdges

        final List<Long> terminalIds = resultFromEdgeDelete.entityStream(atlasSchema.atlasMediator)
                .flatMap { Edge theEdge -> [theEdge.start().getIdentifier(), theEdge.end().getIdentifier()].stream() }
                .distinct()
                .collect(Collectors.toList())

        assert featureChangesByItemType[(ItemType.NODE)].size() == terminalIds.size()
    }

    @Test
    void docTest() {
        def atlasSchema = usingAlcatraz()
        def delete1 = delete atlasSchema.edge where edge.hasTag(highway: "footway") and not(edge.hasTag(foot: "yes"))
        def result1 = exec delete1

        def delete2 = QueryBuilderFactory
                .delete(atlasSchema.edge)
                .where(AtlasDB.edge.hasTag(highway: "footway"))
                .and(
                        QueryBuilderFactory.not(
                                AtlasDB.edge.hasTag(foot: "yes")
                        )
                )
        def result2 = exec delete2
        assert result1.relevantIdentifiers.sort() == result2.relevantIdentifiers.sort()
    }
}
