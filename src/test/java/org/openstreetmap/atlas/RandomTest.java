package org.openstreetmap.atlas;

import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.identifiers.EntityIdentifierGenerator;

/**
 * @author lcram
 */
public class RandomTest
{
    @Test
    public void test()
    {
        final File atlas1File = new File("/Users/lucascram/Desktop/ARG_7-41-77.atlas");
        final File atlas2File = new File("/Users/lucascram/Desktop/ARG_7-41-78.atlas");
        final File atlas3File = new File("/Users/lucascram/Desktop/ARG_7-39-82.atlas");

        final EntityIdentifierGenerator generator = new EntityIdentifierGenerator();
        final Atlas atlas1 = new AtlasResourceLoader().load(atlas1File);
        final Atlas atlas2 = new AtlasResourceLoader().load(atlas2File);
        final Atlas atlas3 = new AtlasResourceLoader().load(atlas3File);

        System.err.println(atlas1.metaData().getTags());
        System.err.println(atlas2.metaData().getTags());
        System.err.println(atlas3.metaData().getTags());

        final CompleteRelation relation1 = CompleteRelation
                .from(atlas1.relation(5464611279205775725L));
        final CompleteRelation relation2 = CompleteRelation
                .from(atlas2.relation(5464611279205775725L));
        final CompleteRelation relation3 = CompleteRelation
                .from(atlas3.relation(5464611279205775725L));

        System.err.println(generator.generateIdentifier(relation1));
        System.err.println(generator.generateIdentifier(relation2));
        System.err.println(generator.generateIdentifier(relation3));
    }
}
