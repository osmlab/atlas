package org.openstreetmap.atlas.geography.atlas.changeset;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Deerializer interface for {@link ChangeSet}.
 *
 * @author yiqing-jin
 * @author mkalender
 */
public interface ChangeSetDeserializer extends Supplier<Optional<ChangeSet>>, AutoCloseable
{

}
