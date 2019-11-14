package org.openstreetmap.atlas.geography.atlas.dsl.query

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.query.explain.ExplainerImpl
import org.openstreetmap.atlas.geography.atlas.dsl.query.explain.Explanation
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.MutantResult
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.ScanType

import java.util.stream.Collectors

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.*
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.*

/**
 * @author Yazad Khambata
 */
class UpdateQueryTest extends AbstractAQLTest {
    @Test
    void addTag() {
        final String key = "accessible"
        final String value = "true"

        verifyAddTag(key, value)
    }

    /**
     * Update use-case.
     */
    @Test
    void replace() {
        final String key = "access"
        final String value = "danger"

        verifyAddTag(key, value)
    }

    @Test
    void delete() {
        final String key = "access"
        verifyDeleteTag(key)
    }

    private void verifyAddTag(String key, String value) {
        def atlasSchema = usingAlcatraz()

        def update1 = update atlasSchema.node set node.addTag((key): value) where node.hasIds(307459622000000, 307446836000000)

        final Explanation explanation = ExplainerImpl.instance.explain update1
        assert explanation.scanStrategy.indexUsageInfo.indexSetting.scanType == ScanType.ID_UNIQUE_INDEX

        final MutantResult result = exec update1
        assert result.relevantIdentifiers.size() == 2

        final List<FeatureChange> featureChanges = result.change.changes().collect(Collectors.toList())

        assert featureChanges.size() == 2

        featureChanges.stream().forEach { featureChange ->
            final Atlas theAtlas = atlasSchema.atlasMediator.atlas
            assert !featureChange.beforeView.getTag(key).isPresent() || featureChange.beforeView.getTag(key) != value

            assert !theAtlas.node(featureChange.getIdentifier()).getTag(key).isPresent() || theAtlas.node(featureChange.getIdentifier()).getTag(key).get() != value
            assert featureChange.getAfterView().getTag(key).isPresent()
            assert featureChange.getAfterView().getTag(key).get() == value
        }
    }

    private void verifyDeleteTag(String key) {
        def atlasSchema = usingAlcatraz()

        def update1 = update atlasSchema.node set node.deleteTag(key) where node.hasId(307459622000000) or node.hasId(307446836000000)

        final Explanation explanation = ExplainerImpl.instance.explain update1
        assert !explanation.scanStrategy.indexUsageInfo.isIndexUsed()

        final MutantResult result = execute update1
        assert result.relevantIdentifiers.size() == 2

        final List<FeatureChange> featureChanges = result.change.changes().collect(Collectors.toList())
        assert featureChanges.size() == 2

        featureChanges.stream().forEach { featureChange ->
            final Atlas theAtlas = atlasSchema.atlasMediator.atlas

            assert featureChange.beforeView.getTag(key).isPresent()

            assert theAtlas.node(featureChange.getIdentifier()).getTag(key).isPresent()
            assert !featureChange.getAfterView().getTag(key).isPresent()
        }
    }
}
