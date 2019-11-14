package org.openstreetmap.atlas.geography.atlas.dsl

import org.junit.BeforeClass
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasMediator
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasSchema
import org.openstreetmap.atlas.geography.atlas.dsl.schema.uri.impl.ClasspathScheme

/**
 * @author Yazad Khambata
 */
abstract class AbstractAQLTest {
    private static Map<String, AtlasSchema> cache = [:]

    private static final String BUTTERFLY_PARK = "BUTTERFLY_PARK"
    private static final String ALCATRAZ = "ALCATRAZ"

    @BeforeClass
    static void setup() {
        cache[BUTTERFLY_PARK] = loadOsmXmlFromClasspath(TestConstants.BUTTERFLY_PARK_CLASSPATH)
        cache[ALCATRAZ] = loadOsmXmlFromClasspath(TestConstants.ALCATRAZ_CLASSPATH)
    }

    private static AtlasSchema loadOsmXmlFromClasspath(final String classpathExcludingScheme) {
        new AtlasSchema(new AtlasMediator(ClasspathScheme.instance.loadOsmXml(classpathExcludingScheme)))
    }

    protected AtlasSchema usingAlcatraz() {
        cache[ALCATRAZ]
    }

    protected AtlasSchema usingButterflyPark() {
        cache[BUTTERFLY_PARK]
    }
}
