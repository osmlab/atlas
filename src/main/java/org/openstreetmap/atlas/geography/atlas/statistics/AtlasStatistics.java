package org.openstreetmap.atlas.geography.atlas.statistics;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.Streams;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StringList;

/**
 * @author matthieun
 */
public class AtlasStatistics implements Iterable<AtlasStatistics.StatisticKey>, Serializable
{
    /**
     * @author matthieun
     */
    public static class StatisticKey implements Serializable
    {
        private static final long serialVersionUID = -4581103889690712256L;

        private final String tag;
        private final String type;
        private final String subType;

        public StatisticKey(final String tag, final String type, final String subType)
        {
            this.tag = tag;
            this.type = type;
            this.subType = subType;
        }

        @Override
        public boolean equals(final Object other)
        {
            if (other instanceof StatisticKey)
            {
                final StatisticKey that = (StatisticKey) other;
                return this.getTag().equalsIgnoreCase(that.getTag())
                        && this.getType().equals(that.getType())
                        && this.getSubType().equals(that.getSubType());
            }
            return false;
        }

        public String getSubType()
        {
            return this.subType;
        }

        public String getTag()
        {
            return this.tag;
        }

        public String getType()
        {
            return this.type;
        }

        @Override
        public int hashCode()
        {
            return this.tag.toLowerCase().hashCode() + this.type.hashCode()
                    + this.subType.hashCode();
        }

        @Override
        public String toString()
        {
            return this.tag + "," + this.type + "," + StringEscapeUtils.escapeCsv(this.subType);
        }
    }

    /**
     * @author matthieun
     */
    public static class StatisticValue implements Serializable
    {
        private static final long serialVersionUID = 1693304125915623196L;

        private final double count;
        private final double totalCount;

        public StatisticValue(final double count, final double totalCount)
        {
            this.count = count;
            this.totalCount = totalCount;
        }

        @Override
        public boolean equals(final Object other)
        {
            if (other instanceof StatisticValue)
            {
                final StatisticValue that = (StatisticValue) other;
                return this.getCount() == that.getCount()
                        && this.getTotalCount() == that.getTotalCount();
            }
            return false;
        }

        public double getCount()
        {
            return this.count;
        }

        public double getTotalCount()
        {
            return this.totalCount;
        }

        @Override
        public int hashCode()
        {
            return Double.hashCode(this.count) + Double.hashCode(this.totalCount);
        }

        public StatisticValue merge(final StatisticValue other)
        {
            return new StatisticValue(getCount() + other.getCount(),
                    getTotalCount() + other.getTotalCount());
        }

        @Override
        public String toString()
        {
            return String.format("%.2f", this.count) + "," + String.format("%.2f", this.totalCount);
        }
    }

    private static final long serialVersionUID = 1564587872667339612L;
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final int TAG = 0;
    private static final int TYPE = 1;
    private static final int SUB_TYPE = 2;
    private static final int COUNT = 3;
    private static final int TOTAL = 4;

    private final Map<StatisticKey, StatisticValue> data;

    public static String csvHeader()
    {
        final StringList header = new StringList();
        header.add("# tag");
        header.add("type");
        header.add("sub_type");
        header.add("count");
        header.add("total_count");
        return header.join(",");
    }

    public static AtlasStatistics fromResource(final Resource resource)
    {
        final AtlasStatistics result = new AtlasStatistics();
        resource.lines().forEach(line ->
        {
            if (line.startsWith("#"))
            {
                return;
            }
            final StringList split = StringList.split(line, ",");
            final StatisticKey key = new StatisticKey(split.get(TAG), split.get(TYPE),
                    split.get(SUB_TYPE));
            final StatisticValue value = new StatisticValue(Double.valueOf(split.get(COUNT)),
                    Double.valueOf(split.get(TOTAL)));
            result.add(key, value);
        });
        return result;
    }

    public static AtlasStatistics merge(final AtlasStatistics... statistics)
    {
        return merge(Iterables.asList(statistics));
    }

    public static AtlasStatistics merge(final Iterable<AtlasStatistics> statistics)
    {
        final Map<StatisticKey, StatisticValue> mergedData = new HashMap<>();
        for (final AtlasStatistics source : statistics)
        {
            for (final StatisticKey key : source)
            {
                StatisticValue value = source.get(key);
                if (mergedData.containsKey(key))
                {
                    value = value.merge(mergedData.get(key));
                    mergedData.remove(key);
                }
                mergedData.put(key, value);
            }
        }
        return new AtlasStatistics(mergedData);
    }

    public AtlasStatistics()
    {
        this.data = new HashMap<>();
    }

    private AtlasStatistics(final Map<StatisticKey, StatisticValue> data)
    {
        this.data = data;
    }

    @Override
    public boolean equals(final Object other)
    {
        return other instanceof AtlasStatistics
                ? ((AtlasStatistics) other).getData().equals(getData()) : false;
    }

    public StatisticValue get(final StatisticKey key)
    {
        return this.data.get(key);
    }

    public StatisticValue get(final String tag, final String type, final String subType)
    {
        return this.data.get(new StatisticKey(tag, type, subType));
    }

    public Map<StatisticKey, StatisticValue> getData()
    {
        return this.data;
    }

    @Override
    public int hashCode()
    {
        return this.data.hashCode();
    }

    @Override
    public Iterator<StatisticKey> iterator()
    {
        return this.data.keySet().iterator();
    }

    public void save(final WritableResource writableResource)
    {
        BufferedWriter out = null;
        try
        {
            out = new BufferedWriter(
                    new OutputStreamWriter(writableResource.write(), StandardCharsets.UTF_8));
            out.write(toString());
            Streams.close(out);
        }
        catch (final Exception e)
        {
            Streams.close(out);
            throw new CoreException("Could not save AtlasStatistics to {}", e, writableResource);
        }
    }

    @Override
    public String toString()
    {
        final StringList result = new StringList();
        result.add(csvHeader());
        this.data.forEach((key, value) -> result.add(key + "," + value));
        return result.join(LINE_SEPARATOR);
    }

    protected void add(final StatisticKey key, final StatisticValue value)
    {
        this.data.put(key, value);
    }

    protected void append(final Map<StatisticKey, StatisticValue> statistics)
    {
        this.data.putAll(statistics);
    }
}
