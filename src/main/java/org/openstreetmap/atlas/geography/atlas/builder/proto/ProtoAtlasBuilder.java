package org.openstreetmap.atlas.geography.atlas.builder.proto;

import org.openstreetmap.atlas.proto.Test;

/**
 * Build an Atlas from a primitive proto serialized file, or write an Atlas to a proto serialized
 * file.
 *
 * @author lcram
 */
public final class ProtoAtlasBuilder
{
    public static void main(final String[] args)
    {
        final Test test = Test.newBuilder().setField1("test").setField2("asd").build();
        System.out.println(test);
    }

    private ProtoAtlasBuilder()
    {
    }
}
