package org.openstreetmap.atlas.geography.atlas.builder.text;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.geography.converters.PolyLineStringConverter;
import org.openstreetmap.atlas.geography.converters.PolygonStringConverter;
import org.openstreetmap.atlas.streaming.resource.LineWriter;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build some Atlas from a simple text file
 *
 * @author matthieun
 */
public class TextAtlasBuilder
{
    /**
     * @author matthieun
     */
    private enum WriteMode
    {
        NODE(NODES_HEADER),
        EDGE(EDGES_HEADER),
        AREA(AREAS_HEADER),
        LINE(LINES_HEADER),
        POINT(POINTS_HEADER),
        RELATION(RELATIONS_HEADER);

        private final String header;

        protected static WriteMode forHeader(final String header)
        {
            switch (header)
            {
                case NODES_HEADER:
                    return WriteMode.NODE;
                case EDGES_HEADER:
                    return WriteMode.EDGE;
                case AREAS_HEADER:
                    return WriteMode.AREA;
                case LINES_HEADER:
                    return WriteMode.LINE;
                case POINTS_HEADER:
                    return WriteMode.POINT;
                case RELATIONS_HEADER:
                    return WriteMode.RELATION;
                default:
                    throw new CoreException("Invalid Header {}", header);
            }
        }

        WriteMode(final String header)
        {
            this.header = header;
        }

        protected String getHeader()
        {
            return this.header;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(TextAtlasBuilder.class);
    private static final String NODES_HEADER = "# Nodes";
    private static final String EDGES_HEADER = "# Edges";
    private static final String AREAS_HEADER = "# Areas";
    private static final String LINES_HEADER = "# Lines";
    private static final String POINTS_HEADER = "# Points";
    private static final String RELATIONS_HEADER = "# Relations";
    private static final String SEPARATOR = " && ";
    private static final String SECONDARY_SEPARATOR = " || ";
    private static final String TERTIARY_SEPARATOR = " -> ";
    private static final String SEPARATOR_REPLACEMENT = " ";

    public static String getNodesHeader()
    {
        return NODES_HEADER;
    }

    public PackedAtlas read(final Resource resource)
    {
        WriteMode mode = null;
        long numberOfNodes = 0L;
        long numberOfEdges = 0L;
        long numberOfAreas = 0L;
        long numberOfLines = 0L;
        long numberOfPoints = 0L;
        long numberOfRelations = 0L;
        for (final String line : resource.lines())
        {
            if (line.startsWith("#"))
            {
                mode = WriteMode.forHeader(line);
            }
            else
            {
                if (mode == null)
                {
                    throw new CoreException(
                            "Failed reading {}. Is that a text Atlas? Is it compressed?", resource);
                }
                switch (mode)
                {
                    case NODE:
                        numberOfNodes++;
                        break;
                    case EDGE:
                        numberOfEdges++;
                        break;
                    case AREA:
                        numberOfAreas++;
                        break;
                    case LINE:
                        numberOfLines++;
                        break;
                    case POINT:
                        numberOfPoints++;
                        break;
                    case RELATION:
                        numberOfRelations++;
                        break;
                    default:
                        break;
                }
            }
        }
        final AtlasSize size = new AtlasSize(numberOfEdges, numberOfNodes, numberOfAreas,
                numberOfLines, numberOfPoints, numberOfRelations);

        if (size.getEntityNumber() == 0)
        {
            throw new CoreException("Invalid text Atlas, it appears to be empty!");
        }

        if (size.getNonRelationEntityNumber() == 0 && size.getRelationNumber() > 0)
        {
            throw new CoreException("Invalid text Atlas, it only contained Relations!");
        }

        final AtlasMetaData metaData = new AtlasMetaData(size, true, "unknown", "TextAtlas",
                "unknown", "unknown", Maps.hashMap());
        final PackedAtlasBuilder builder = new PackedAtlasBuilder().withSizeEstimates(size)
                .withMetaData(metaData).withName(resource.getName());
        for (final String line : resource.lines())
        {
            if (line.startsWith("#"))
            {
                mode = WriteMode.forHeader(line);
            }
            else
            {
                switch (mode)
                {
                    case NODE:
                        parseNode(builder, line);
                        break;
                    case EDGE:
                        parseEdge(builder, line);
                        break;
                    case AREA:
                        parseArea(builder, line);
                        break;
                    case LINE:
                        parseLine(builder, line);
                        break;
                    case POINT:
                        parsePoint(builder, line);
                        break;
                    case RELATION:
                        parseRelation(builder, line);
                        break;
                    default:
                        break;
                }
            }
        }
        final PackedAtlas atlas = (PackedAtlas) builder.get();
        if (atlas == null)
        {
            throw new CoreException("Atlas resulting from PackedAtlasBuilder was null.");
        }
        return atlas;
    }

    public void write(final Atlas atlas, final WritableResource resource)
    {
        try (LineWriter writer = new LineWriter(resource))
        {
            for (final WriteMode mode : WriteMode.values())
            {
                writer.writeLine(mode.getHeader());
                switch (mode)
                {
                    case NODE:
                        atlas.nodes().forEach(item -> writer.writeLine(convertNode(item)));
                        break;
                    case EDGE:
                        atlas.edges().forEach(item -> writer.writeLine(convertEdge(item)));
                        break;
                    case AREA:
                        atlas.areas().forEach(item -> writer.writeLine(convertArea(item)));
                        break;
                    case LINE:
                        atlas.lines().forEach(item -> writer.writeLine(convertLine(item)));
                        break;
                    case POINT:
                        atlas.points().forEach(item -> writer.writeLine(convertPoint(item)));
                        break;
                    case RELATION:
                        atlas.relations().forEach(item -> writer.writeLine(convertRelation(item)));
                        break;
                    default:
                        break;
                }
            }
        }
        catch (final Exception e)
        {
            throw new CoreException("Unable to write Atlas to {}", resource, e);
        }
    }

    private String cleanupTags(final String value)
    {
        if (value != null)
        {
            String result = value;
            if (value.contains(SEPARATOR))
            {
                logger.warn("Tag {} contains {}. Replacing it with \"{}\"", value, SEPARATOR,
                        SEPARATOR_REPLACEMENT);
                result = result.replace(SEPARATOR, SEPARATOR_REPLACEMENT);
            }
            if (value.contains(SECONDARY_SEPARATOR))
            {
                logger.warn("Tag {} contains {}. Replacing it with \"{}\"", value,
                        SECONDARY_SEPARATOR, SEPARATOR_REPLACEMENT);
                result = result.replace(SECONDARY_SEPARATOR, SEPARATOR_REPLACEMENT);
            }
            if (value.contains(TERTIARY_SEPARATOR))
            {
                logger.warn("Tag {} contains {}. Replacing it with \"{}\"", value,
                        TERTIARY_SEPARATOR, SEPARATOR_REPLACEMENT);
                result = result.replace(TERTIARY_SEPARATOR, SEPARATOR_REPLACEMENT);
            }
            if (value.contains(System.lineSeparator()))
            {
                logger.warn("Tag {} contains a new line. Removing it.", value);
                result = result.replace(System.lineSeparator(), "");
            }
            return result;
        }
        return value;
    }

    private String convertArea(final Area item)
    {
        final StringList list = new StringList();
        list.add(item.getIdentifier());
        list.add(item.asPolygon().toCompactString());
        list.add(convertTags(item));
        return list.join(SEPARATOR);
    }

    private String convertEdge(final Edge item)
    {
        final StringList list = new StringList();
        list.add(item.getIdentifier());
        list.add(item.asPolyLine().toCompactString());
        list.add(convertTags(item));
        return list.join(SEPARATOR);
    }

    private String convertLine(final Line item)
    {
        final StringList list = new StringList();
        list.add(item.getIdentifier());
        list.add(item.asPolyLine().toCompactString());
        list.add(convertTags(item));
        return list.join(SEPARATOR);
    }

    private String convertNode(final Node item)
    {
        final StringList list = new StringList();
        list.add(item.getIdentifier());
        list.add(item.getLocation().toCompactString());
        list.add(convertTags(item));
        return list.join(SEPARATOR);
    }

    private String convertPoint(final Point item)
    {
        final StringList list = new StringList();
        list.add(item.getIdentifier());
        list.add(item.getLocation().toCompactString());
        list.add(convertTags(item));
        return list.join(SEPARATOR);
    }

    private String convertRelation(final Relation item)
    {
        final StringList list = new StringList();
        list.add(item.getIdentifier());
        list.add(convertRelationBean(item));
        list.add(convertTags(item));
        return list.join(SEPARATOR);
    }

    private String convertRelationBean(final Relation relation)
    {
        final StringList bean = new StringList();
        for (final RelationMember member : relation.members())
        {
            final StringList list = new StringList();
            list.add(member.getEntity().getIdentifier());
            list.add(member.getRole());
            final ItemType type = ItemType.forEntity(member.getEntity());
            list.add(type.toShortString());
            bean.add(list.join(TERTIARY_SEPARATOR));
        }
        return bean.join(SECONDARY_SEPARATOR);
    }

    private String convertTags(final Taggable taggable)
    {
        final StringList tags = new StringList();
        for (final Entry<String, String> entry : taggable.getTags().entrySet())
        {
            final StringBuilder builder = new StringBuilder();
            builder.append(cleanupTags(entry.getKey()));
            builder.append(TERTIARY_SEPARATOR);
            builder.append(cleanupTags(entry.getValue()));
            tags.add(builder.toString());
        }
        return tags.join(SECONDARY_SEPARATOR);
    }

    private void parseArea(final PackedAtlasBuilder builder, final String line)
    {
        final StringList split = StringList.split(line, SEPARATOR);
        final long identifier = Long.parseLong(split.get(0));
        final Polygon geometry = new PolygonStringConverter().convert(split.get(1));
        final Map<String, String> tags = new HashMap<>();
        if (split.size() > 2)
        {
            tags.putAll(parseTags(split.get(2)));
        }
        builder.addArea(identifier, geometry, tags);
    }

    private void parseEdge(final PackedAtlasBuilder builder, final String line)
    {
        final StringList split = StringList.split(line, SEPARATOR);
        final long identifier = Long.parseLong(split.get(0));
        final PolyLine geometry = new PolyLineStringConverter().convert(split.get(1));
        final Map<String, String> tags = new HashMap<>();
        if (split.size() > 2)
        {
            tags.putAll(parseTags(split.get(2)));
        }
        builder.addEdge(identifier, geometry, tags);
    }

    private void parseLine(final PackedAtlasBuilder builder, final String line)
    {
        final StringList split = StringList.split(line, SEPARATOR);
        final long identifier = Long.parseLong(split.get(0));
        final PolyLine geometry = new PolyLineStringConverter().convert(split.get(1));
        final Map<String, String> tags = new HashMap<>();
        if (split.size() > 2)
        {
            tags.putAll(parseTags(split.get(2)));
        }
        builder.addLine(identifier, geometry, tags);
    }

    private void parseNode(final PackedAtlasBuilder builder, final String line)
    {
        final StringList split = StringList.split(line, SEPARATOR);
        final long identifier = Long.parseLong(split.get(0));
        final Location geometry = Location.forString(split.get(1));
        final Map<String, String> tags = new HashMap<>();
        if (split.size() > 2)
        {
            tags.putAll(parseTags(split.get(2)));
        }
        builder.addNode(identifier, geometry, tags);
    }

    private void parsePoint(final PackedAtlasBuilder builder, final String line)
    {
        final StringList split = StringList.split(line, SEPARATOR);
        final long identifier = Long.parseLong(split.get(0));
        final Location geometry = Location.forString(split.get(1));
        final Map<String, String> tags = new HashMap<>();
        if (split.size() > 2)
        {
            tags.putAll(parseTags(split.get(2)));
        }
        builder.addPoint(identifier, geometry, tags);
    }

    private void parseRelation(final PackedAtlasBuilder builder, final String line)
    {
        final StringList split = StringList.split(line, SEPARATOR);
        final long identifier = Long.parseLong(split.get(0));
        final RelationBean structure = parseRelationBean(split.get(1));
        final Map<String, String> tags = new HashMap<>();
        if (split.size() > 2)
        {
            tags.putAll(parseTags(split.get(2)));
        }
        builder.addRelation(identifier, identifier, structure, tags);
    }

    private RelationBean parseRelationBean(final String value)
    {
        final RelationBean bean = new RelationBean();
        final StringList split = StringList.split(value, SECONDARY_SEPARATOR);
        for (final String beanValue : split)
        {
            final StringList valueSplit = StringList.split(beanValue, TERTIARY_SEPARATOR);
            final long identifier = Long.parseLong(valueSplit.get(0));
            final String role = valueSplit.get(1);
            final ItemType itemType = ItemType.shortValueOf(valueSplit.get(2));
            bean.addItem(identifier, role, itemType);
        }
        return bean;
    }

    private Map<String, String> parseTags(final String value)
    {
        try
        {
            final Map<String, String> result = Maps.hashMap();
            final StringList split = StringList.split(value, SECONDARY_SEPARATOR);
            for (final String tag : split)
            {
                final StringList tagSplit = StringList.split(tag, TERTIARY_SEPARATOR);
                if (tagSplit.size() == 2)
                {
                    result.put(tagSplit.get(0), tagSplit.get(1));
                }
                if (tagSplit.size() == 1)
                {
                    result.put(tagSplit.get(0), "");
                }
            }
            return result;
        }
        catch (final Throwable error)
        {
            throw new CoreException("Unable to parse tags from \"{}\"", value, error);
        }

    }
}
