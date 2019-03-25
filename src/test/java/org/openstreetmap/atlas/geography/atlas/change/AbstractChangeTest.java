package org.openstreetmap.atlas.geography.atlas.change;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yazad Khambata
 */
public abstract class AbstractChangeTest
{
    protected static final Logger log = LoggerFactory.getLogger(AtlasResourceLoader.class);

    public static final long TEST_IDENTIFIER = 123L;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    protected Change newChangeWith2Areas()
    {
        final String key1 = "key";
        final String value1 = "value";

        return newChangeWith2Areas(TEST_IDENTIFIER, TEST_IDENTIFIER, key1, value1);
    }

    protected Change newChangeWith2Areas(final long identifier1, final long identifier2,
            final String key1, final String value1)
    {
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD, new CompleteArea(
                identifier1, Polygon.TEST_BUILDING, Maps.hashMap(key1, value1), null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(identifier2, Polygon.TEST_BUILDING, null, null));
        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(featureChange1);
        builder.add(featureChange2);
        return builder.get();
    }

    protected Change newChangeWithAreaAndLine()
    {
        final long identifier1 = TEST_IDENTIFIER;
        final long identifier2 = TEST_IDENTIFIER;
        return newChangeWithAreaAndLine(identifier1, identifier2);
    }

    protected Change newChangeWithAreaAndLine(final long identifier1, final long identifier2)
    {
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(identifier1, null, Maps.hashMap(), null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.REMOVE,
                new CompleteLine(identifier2, null, null, null));
        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(featureChange1);
        builder.add(featureChange2);
        return builder.get();
    }
}
