package org.openstreetmap.atlas.proto.adapters;

import java.util.Map;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.proto.ProtoAtlasMetaData;
import org.openstreetmap.atlas.proto.ProtoSerializable;
import org.openstreetmap.atlas.proto.converters.ProtoTagListConverter;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Implements the {@link ProtoAdapter} interface to connect {@link AtlasMetaData} and
 * {@link ProtoAtlasMetaData}.
 *
 * @author lcram
 */
public class ProtoAtlasMetaDataAdapter implements ProtoAdapter
{
    private static final ProtoTagListConverter PROTOTAG_LIST_CONVERTER = new ProtoTagListConverter();

    @Override
    public ProtoSerializable deserialize(final byte[] byteArray)
    {
        ProtoAtlasMetaData protoAtlasMetaData = null;
        try
        {
            protoAtlasMetaData = ProtoAtlasMetaData.parseFrom(byteArray);
        }
        catch (final InvalidProtocolBufferException exception)
        {
            throw new CoreException("Error encountered while parsing protobuf bytestream",
                    exception);
        }

        final AtlasSize atlasSize = new AtlasSize(protoAtlasMetaData.getEdgeNumber(),
                protoAtlasMetaData.getNodeNumber(), protoAtlasMetaData.getAreaNumber(),
                protoAtlasMetaData.getLineNumber(), protoAtlasMetaData.getPointNumber(),
                protoAtlasMetaData.getRelationNumber());

        final boolean original = protoAtlasMetaData.getOriginal();
        final String codeVersion = protoAtlasMetaData.getCodeVersion();
        final String dataVersion = protoAtlasMetaData.getDataVersion();
        final String country = protoAtlasMetaData.getCountry();
        final String shardName = protoAtlasMetaData.getShardName();
        final Map<String, String> tags = PROTOTAG_LIST_CONVERTER
                .convert(protoAtlasMetaData.getTagsList());

        final AtlasMetaData atlasMetaData = new AtlasMetaData(atlasSize, original, codeVersion,
                dataVersion, country, shardName, tags);

        return atlasMetaData;
    }

    @Override
    public byte[] serialize(final ProtoSerializable serializable)
    {
        if (!(serializable instanceof AtlasMetaData))
        {
            throw new CoreException(
                    "Invalid ProtoSerializable type was provided to {}: cannot serialize {}",
                    this.getClass().getName(), serializable.getClass().getName());
        }
        final AtlasMetaData atlasMetaData = (AtlasMetaData) serializable;

        final ProtoAtlasMetaData.Builder builder = ProtoAtlasMetaData.newBuilder();
        builder.setEdgeNumber(atlasMetaData.getSize().getEdgeNumber());
        builder.setNodeNumber(atlasMetaData.getSize().getNodeNumber());
        builder.setAreaNumber(atlasMetaData.getSize().getAreaNumber());
        builder.setLineNumber(atlasMetaData.getSize().getLineNumber());
        builder.setPointNumber(atlasMetaData.getSize().getPointNumber());
        builder.setRelationNumber(atlasMetaData.getSize().getRelationNumber());
        builder.setOriginal(atlasMetaData.isOriginal());

        atlasMetaData.getCodeVersion().ifPresent(value ->
        {
            builder.setCodeVersion(value);
        });

        atlasMetaData.getDataVersion().ifPresent(value ->
        {
            builder.setDataVersion(value);
        });

        atlasMetaData.getCountry().ifPresent(value ->
        {
            builder.setCountry(value);
        });

        atlasMetaData.getShardName().ifPresent(value ->
        {
            builder.setShardName(value);
        });

        final Map<String, String> tags = atlasMetaData.getTags();
        builder.addAllTags(ProtoAtlasMetaDataAdapter.PROTOTAG_LIST_CONVERTER.backwardConvert(tags));

        final ProtoAtlasMetaData protoMetaData = builder.build();
        return protoMetaData.toByteArray();
    }
}
