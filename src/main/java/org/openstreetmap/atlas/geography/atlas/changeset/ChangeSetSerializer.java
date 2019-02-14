package org.openstreetmap.atlas.geography.atlas.changeset;

import java.util.function.Consumer;

/**
 * Serializer interface for {@link ChangeSet}.
 *
 * @author yiqing-jin
 * @author mkalender
 * @deprecated - see new API under org.openstreetmap.atlas.geography.atlas.change package.
 */
@Deprecated
public interface ChangeSetSerializer extends Consumer<ChangeSet>, AutoCloseable // NOSONAR
{

}
