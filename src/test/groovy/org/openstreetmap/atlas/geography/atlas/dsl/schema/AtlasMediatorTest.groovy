package org.openstreetmap.atlas.geography.atlas.dsl.schema

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest

/**
 * @author Yazad Khambata
 */
class AtlasMediatorTest extends AbstractAQLTest {

    @Test
    void testEqualsAndHashCode() {
        final AtlasSchema schema1 = usingAlcatraz()
        final AtlasSchema schema2 = usingAlcatraz()

        assert schema1 == schema2
        assert schema1.hashCode() == schema2.hashCode()

        assert schema1.atlasMediator == schema2.atlasMediator
        assert schema1.atlasMediator.hashCode() == schema2.atlasMediator.hashCode()
    }
}
