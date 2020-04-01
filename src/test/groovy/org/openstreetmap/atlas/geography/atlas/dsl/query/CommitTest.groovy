package org.openstreetmap.atlas.geography.atlas.dsl.query

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.Result
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasSchema
import org.openstreetmap.atlas.geography.atlas.items.Edge
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.stream.Collectors
import java.util.stream.StreamSupport

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.*
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.edge

/**
 * @author Yazad Khambata
 */
class CommitTest extends AbstractAQLTest {

    private static final Logger log = LoggerFactory.getLogger(CommitTest.class);

    @Test
    void testOneQueryCommit() {
        def atlas = usingAlcatraz()

        def update1 = update atlas.edge set edge.addTag(website: "https://www.nps.gov/alca") where edge.hasTagLike(name: /Pier/) or edge.hasTagLike(name: /Island/) or edge.hasTagLike(name: /Road/)
        def update2 = update atlas.edge set edge.addTag(wikipedia: "https://en.wikipedia.org/wiki/Alcatraz_Island") where edge.hasTagLike(/foot/)
        
        def level1ChangesAtlas = commit update1, update2

        verifyLevel1Updates(level1ChangesAtlas)

        def update3 = update level1ChangesAtlas.edge set edge.addTag(accessibility_website: "https://www.nps.gov/goga/planyourvisit/accessibility.htm") where edge.hasTagLike(/web*/) or edge.hasTagLike(/wiki*/)

        def level2ChangesAtlas = commit update3

        final int edgesWithAccessibilityWebsite = verifyLevel2Updates(level2ChangesAtlas)

        def wikiSelect = select edge.id, edge.tags from level2ChangesAtlas.edge where edge.hasTag("accessibility_website")

        final Result result = exec wikiSelect
        assert result.relevantIdentifiers.size() == edgesWithAccessibilityWebsite
    }

    private int verifyLevel2Updates(AtlasSchema level2ChangesAtlas) {
        final Atlas atlasAfterLevel2Commit = level2ChangesAtlas.atlasMediator.atlas
        final List<Edge> level2UpdatedEdges = StreamSupport.stream(atlasAfterLevel2Commit.edges { edge ->
            return !edge.getTags { tagKey -> tagKey.contains("website") || tagKey.contains("wikipedia") }.isEmpty()
        }.spliterator(), false).collect(Collectors.toList())

        assert level2UpdatedEdges.size() > 1

        final List<Edge> edgesWithWebOrWiki = level2UpdatedEdges.stream().collect(Collectors.toList())
        edgesWithWebOrWiki.forEach { relation ->
            assert relation.getTag("accessibility_website").get() == "https://www.nps.gov/goga/planyourvisit/accessibility.htm"
        }

        return edgesWithWebOrWiki.size()
    }

    private void verifyLevel1Updates(AtlasSchema level1ChangesAtlas) {
        final Atlas atlasAfterLevel1Commit = level1ChangesAtlas.atlasMediator.atlas

        final List<Edge> level1UpdatedEdges = StreamSupport.stream(atlasAfterLevel1Commit.edges { edge ->
            final Optional<String> tagValue = edge.getTag("name")

            if (!tagValue.isPresent()) {
                return false
            }

            def value = tagValue.get()

            log.info("name: ${value}")

            return value.contains("Pier") || value.startsWith("Island") || value.contains("Road")
        }.spliterator(), false).collect(Collectors.toList())

        assert level1UpdatedEdges.size() >= 3

        level1UpdatedEdges.stream().forEach { edge ->
            assert edge.getTag("website").isPresent() || edge.getTag("wikipedia").isPresent()
        }
    }
}
