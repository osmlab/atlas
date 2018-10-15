package org.openstreetmap.atlas.geography.atlas;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.proto.ProtoSerializable;
import org.openstreetmap.atlas.proto.adapters.ProtoAdapter;
import org.openstreetmap.atlas.proto.adapters.ProtoAtlasMetaDataAdapter;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.utilities.collections.Maps;

/**
 * Meta data for an {@link Atlas}
 *
 * @author matthieun
 * @author lcram
 */
public final class AtlasMetaData implements Serializable, Taggable, ProtoSerializable
{
    private static final long serialVersionUID = -285346019736489425L;

    public static final String EDGE_CONFIGURATION = "edgeConfiguration";
    public static final String AREA_CONFIGURATION = "areaConfiguration";
    public static final String WAY_SECTIONING_CONFIGURATION = "waySectioningConfiguration";
    public static final String OSM_PBF_WAY_CONFIGURATION = "osmPbfWayConfiguration";
    public static final String OSM_PBF_NODE_CONFIGURATION = "osmPbfNodeConfiguration";
    public static final String OSM_PBF_RELATION_CONFIGURATION = "osmPbfRelationConfiguration";

    private static final String UNKNOWN_VALUE = "unknown";

    private final AtlasSize size;
    private final boolean original;
    private final String codeVersion;
    private final String dataVersion;
    private final String country;
    private final String shardName;
    private final Map<String, String> tags;

    public AtlasMetaData()
    {
        this(AtlasSize.DEFAULT);
    }

    public AtlasMetaData(final AtlasSize size)
    {
        this(size, true, UNKNOWN_VALUE, UNKNOWN_VALUE, UNKNOWN_VALUE, UNKNOWN_VALUE,
                Maps.hashMap());
    }

    public AtlasMetaData(final AtlasSize size, final boolean original, final String codeVersion,
            final String dataVersion, final String country, final String shardName,
            final Map<String, String> tags)
    {
        this.size = size;
        this.original = original;
        this.codeVersion = codeVersion;
        this.dataVersion = dataVersion;
        this.country = country;
        this.shardName = shardName;
        this.tags = tags;
    }

    public AtlasMetaData copyWithNewShardName(final String shardName)
    {
        return new AtlasMetaData(this.size, this.original, this.codeVersion, this.dataVersion,
                this.country, shardName, this.tags);
    }

    public AtlasMetaData copyWithNewSize(final AtlasSize size)
    {
        return new AtlasMetaData(size, this.original, this.codeVersion, this.dataVersion,
                this.country, this.shardName, this.tags);
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof AtlasMetaData)
        {
            if (this == other)
            {
                return true;
            }
            final AtlasMetaData that = (AtlasMetaData) other;
            if (!Objects.equals(this.getSize(), that.getSize()))
            {
                return false;
            }
            if (this.isOriginal() != that.isOriginal())
            {
                return false;
            }
            if (!Objects.equals(this.getCodeVersion(), that.getCodeVersion()))
            {
                return false;
            }
            if (!Objects.equals(this.getDataVersion(), that.getDataVersion()))
            {
                return false;
            }
            if (!Objects.equals(this.getCountry(), that.getCountry()))
            {
                return false;
            }
            if (!Objects.equals(this.getShardName(), that.getShardName()))
            {
                return false;
            }
            if (!Objects.equals(this.getTags(), that.getTags()))
            {
                return false;
            }
            return true;
        }
        return false;
    }

    public Optional<String> getCodeVersion()
    {
        return Optional.ofNullable(this.codeVersion);
    }

    public Optional<String> getCountry()
    {
        return Optional.ofNullable(this.country);
    }

    public Optional<String> getDataVersion()
    {
        return Optional.ofNullable(this.dataVersion);
    }

    @Override
    public ProtoAdapter getProtoAdapter()
    {
        return new ProtoAtlasMetaDataAdapter();
    }

    public Optional<String> getShardName()
    {
        return Optional.ofNullable(this.shardName);
    }

    public AtlasSize getSize()
    {
        return this.size;
    }

    @Override
    public Optional<String> getTag(final String key)
    {
        return Optional.ofNullable(this.tags.get(key));
    }

    @Override
    public Map<String, String> getTags()
    {
        if (this.tags == null)
        {
            return new HashMap<>();
        }
        return new HashMap<>(this.tags);
    }

    @Override
    public int hashCode()
    {
        final int sizeHash = this.getSize().hashCode();
        return Objects.hash(Integer.valueOf(sizeHash), Boolean.valueOf(this.original),
                this.codeVersion, this.dataVersion, this.country, this.shardName, this.tags);
    }

    public boolean isOriginal()
    {
        return this.original;
    }

    public String toReadableString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("Size: ");
        builder.append("\n\tNodes: ");
        builder.append(this.size.getNodeNumber());
        builder.append("\n\tEdges: ");
        builder.append(this.size.getEdgeNumber());
        builder.append("\n\tAreas: ");
        builder.append(this.size.getAreaNumber());
        builder.append("\n\tLines: ");
        builder.append(this.size.getLineNumber());
        builder.append("\n\tPoints: ");
        builder.append(this.size.getPointNumber());
        builder.append("\n\tRelations: ");
        builder.append(this.size.getRelationNumber());
        builder.append("\n");
        builder.append("Original: ");
        builder.append(this.original);
        builder.append("\n");
        builder.append("Code Version: ");
        builder.append(this.codeVersion);
        builder.append("\n");
        builder.append("Data Version: ");
        builder.append(this.dataVersion);
        builder.append("\n");
        builder.append("Country: ");
        builder.append(this.country);
        builder.append("\n");
        builder.append("Shard: ");
        builder.append(this.shardName);
        builder.append("\n");
        builder.append("Tags: ");
        this.tags.forEach((key, value) ->
        {
            builder.append("\n\t");
            builder.append(key);
            builder.append(" -> ");
            builder.append(value);
        });
        builder.append("\n");
        return builder.toString();
    }

    @Override
    public String toString()
    {
        return "[AtlasMetaData: size=" + this.size + ", original=" + this.original
                + ", codeVersion=" + this.codeVersion + ", dataVersion=" + this.dataVersion
                + ", country=" + this.country + ", shardName=" + this.shardName + ", tags="
                + this.tags + "]";
    }
}
