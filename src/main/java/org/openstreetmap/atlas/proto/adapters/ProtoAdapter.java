package org.openstreetmap.atlas.proto.adapters;

import org.openstreetmap.atlas.proto.ProtoSerializable;

/**
 * Any protobuf adapter class must conform to this interface.
 *
 * @author lcram
 */
public interface ProtoAdapter
{
    /**
     * @param byteArray
     *            The raw byte representation of the {@link ProtoSerializable} in protocol buffer
     *            format
     * @return The object represented by the byte stream.
     */
    ProtoSerializable deserialize(byte[] byteArray);

    /**
     * @param serializable
     *            The object to serialize
     * @return The raw byte representation of the {@link ProtoSerializable} in protocol buffer
     *         format.
     */
    byte[] serialize(ProtoSerializable serializable);
}
