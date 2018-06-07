package org.openstreetmap.atlas.proto;

import org.openstreetmap.atlas.proto.adapters.ProtoAdapter;

/**
 * {@link ProtoSerializable} is a contract for types that would like to be serialized in protocol
 * buffer format. A type that implements this interface must be able to provide a valid adapter to
 * its owner.
 *
 * @author lcram
 */
public interface ProtoSerializable
{
    /**
     * @return The adapter associated with this {@link ProtoSerializable}
     */
    ProtoAdapter getProtoAdapter();
}
