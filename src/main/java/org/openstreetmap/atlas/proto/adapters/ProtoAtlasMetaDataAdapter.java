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

        AtlasSize atlasSize = null;
        final boolean hasAllAtlasSizeFeatures = protoAtlasMetaData.hasEdgeNumber()
                && protoAtlasMetaData.hasNodeNumber() && protoAtlasMetaData.hasAreaNumber()
                && protoAtlasMetaData.hasLineNumber() && protoAtlasMetaData.hasPointNumber()
                && protoAtlasMetaData.hasRelationNumber();
        if (hasAllAtlasSizeFeatures)
        {
            atlasSize = new AtlasSize(protoAtlasMetaData.getEdgeNumber(),
                    protoAtlasMetaData.getNodeNumber(), protoAtlasMetaData.getAreaNumber(),
                    protoAtlasMetaData.getLineNumber(), protoAtlasMetaData.getPointNumber(),
                    protoAtlasMetaData.getRelationNumber());
        }

        final boolean original = protoAtlasMetaData.getOriginal();

        String codeVersion = null;
        if (protoAtlasMetaData.hasCodeVersion())
        {
            codeVersion = protoAtlasMetaData.getCodeVersion();
        }
        String dataVersion = null;
        if (protoAtlasMetaData.hasDataVersion())
        {
            dataVersion = protoAtlasMetaData.getDataVersion();
        }
        String country = null;
        if (protoAtlasMetaData.hasCountry())
        {
            country = protoAtlasMetaData.getCountry();
        }
        String shardName = null;
        if (protoAtlasMetaData.hasShardName())
        {
            shardName = protoAtlasMetaData.getShardName();
        }
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

        final ProtoAtlasMetaData.Builder protoMetaDataBuilder = ProtoAtlasMetaData.newBuilder();

        if (atlasMetaData.getSize() != null)
        {
            protoMetaDataBuilder.setEdgeNumber(atlasMetaData.getSize().getEdgeNumber());
            protoMetaDataBuilder.setNodeNumber(atlasMetaData.getSize().getNodeNumber());
            protoMetaDataBuilder.setAreaNumber(atlasMetaData.getSize().getAreaNumber());
            protoMetaDataBuilder.setLineNumber(atlasMetaData.getSize().getLineNumber());
            protoMetaDataBuilder.setPointNumber(atlasMetaData.getSize().getPointNumber());
            protoMetaDataBuilder.setRelationNumber(atlasMetaData.getSize().getRelationNumber());
        }

        protoMetaDataBuilder.setOriginal(atlasMetaData.isOriginal());

        atlasMetaData.getCodeVersion().ifPresent(value ->
        {
            protoMetaDataBuilder.setCodeVersion(value);
        });

        atlasMetaData.getDataVersion().ifPresent(value ->
        {
            protoMetaDataBuilder.setDataVersion(value);
        });

        atlasMetaData.getCountry().ifPresent(value ->
        {
            protoMetaDataBuilder.setCountry(value);
        });

        atlasMetaData.getShardName().ifPresent(value ->
        {
            protoMetaDataBuilder.setShardName(value);
        });

        if (atlasMetaData.getTags() != null)
        {
            protoMetaDataBuilder.addAllTags(ProtoAtlasMetaDataAdapter.PROTOTAG_LIST_CONVERTER
                    .backwardConvert(atlasMetaData.getTags()));
        }

        return protoMetaDataBuilder.build().toByteArray();
    }
}
