package org.openstreetmap.atlas.geography.atlas.changeset;

import java.util.function.Consumer;

/**
 * Serializer interface for {@link ChangeSet}.
 *
 * @author yiqing-jin
 * @author mkalender
 */
public interface ChangeSetSerializer extends Consumer<ChangeSet>, AutoCloseable
{

}
