package org.openstreetmap.atlas.utilities.testing;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.builder.text.TextAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.pbf.OsmPbfLoader;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.ClassResource;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.tags.BuildingPartTag;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area.Known;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Building;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.SizeEstimate;

/**
 * Handler implementation for Atlas fields annotated with the TestAtlas annotation
 *
 * @author cstaylor
 */
public class TestAtlasHandler implements FieldHandler
{
    private static Map<String, String> mergeTags(final Map<String, String> firstTags,
            final Map<String, String> secondTags)
    {
        final Map<String, String> returnValue = new HashMap<>();
        returnValue.putAll(firstTags);
        returnValue.putAll(secondTags);
        return returnValue;
    }

    private static Map<String, String> mergeTags(final String[] firstTags,
            final String[] secondTags)
    {
        final List<String> allTags = new ArrayList<>(Arrays.asList(firstTags));
        allTags.addAll(Arrays.asList(secondTags));
        return parseTags(allTags.toArray(new String[0]));
    }

    private static Map<String, String> parseTags(final String... tags)
    {
        final Map<String, String> tagmap = new HashMap<>();
        for (final String tagAndValue : tags)
        {
            final StringList fullySplit = StringList.split(tagAndValue, "=");
            if (fullySplit.size() == 2)
            {
                // Standard key=value case
                tagmap.put(fullySplit.get(0), fullySplit.get(1));
            }
            else if (fullySplit.size() == 1)
            {
                // Case of a key without a value. The value becomes an empty string
                tagmap.put(fullySplit.get(0), "");
            }
            else
            {
                // Erroneous case
                throw new CoreException("{} isn't a valid tag description", tagAndValue);
            }
        }
        return tagmap;
    }

    @Override
    public void create(final Field field, final CoreTestRule rule, final CreationContext context)
    {
        final TestAtlas testAtlas = field.getAnnotation(TestAtlas.class);
        if (StringUtils.isNotEmpty(testAtlas.loadFromTextResource()))
        {
            try
            {
                loadFromTextResource(field, rule, context, testAtlas.loadFromTextResource());
            }
            catch (final Throwable e)
            {
                throw new CoreException("Error creating field {}", field, e);
            }
        }
        else if (StringUtils.isNotEmpty(testAtlas.loadFromJosmOsmResource()))
        {
            try
            {
                loadFromJosmOsmResource(field, rule, context, testAtlas.loadFromJosmOsmResource());
            }
            catch (final Throwable e)
            {
                throw new CoreException("Error creating field {}", field, e);
            }
        }
        else
        {
            createDirectly(testAtlas, field, rule, context);
        }
    }

    @Override
    public boolean handles(final Field field)
    {
        final Class<?> fieldClass = field.getType();
        return Atlas.class.isAssignableFrom(fieldClass);
    }

    private long addArea(final PackedAtlasBuilder builder, final FeatureIDGenerator areaIDGenerator,
            final Area area, final String... additionalTags)
    {
        final long areaId = areaIDGenerator.nextId(area.id());
        builder.addArea(areaId, buildAreaPolygon(area), mergeTags(area.tags(), additionalTags));
        return areaId;
    }

    private Polygon buildAreaPolygon(final Area area)
    {
        if (area.known() == Known.USE_COORDINATES)
        {
            if (area.coordinates().length == 0)
            {
                return new Polygon(Location.TEST_1);
            }
            return convertPolygon(area.coordinates());
        }
        if (area.known() == Known.BUILDING_1)
        {
            return Polygon.TEST_BUILDING;
        }
        if (area.known() == Known.BUILDING_2)
        {
            return Polygon.TEST_BUILDING_PART;
        }
        // Right now we only have a single constant for areas
        return Polygon.SILICON_VALLEY;
    }

    private Location convertLoc(final Loc point)
    {
        if (point.value().equalsIgnoreCase(Loc.USE_LATLON)
                && (point.lat() == Loc.BAD_VALUE || point.lon() == Loc.BAD_VALUE))
        {
            throw new CoreException("Loc doesn't have a valid string value or lat/lon values");
        }

        if (point.value().equalsIgnoreCase(Loc.USE_LATLON))
        {
            return new Location(Latitude.degrees(point.lat()), Longitude.degrees(point.lon()));
        }

        if (point.value().equalsIgnoreCase(Loc.TEST_1))
        {
            return Location.TEST_1;
        }
        return Location.forString(point.value());
    }

    private Polygon convertPolygon(final Loc[] points)
    {
        return new Polygon(pointsToLocations(points));
    }

    private PolyLine convertPolyLine(final Loc[] points)
    {
        return new PolyLine(pointsToLocations(points));
    }

    private AtlasSize convertSizeEstimates(final SizeEstimate estimate)
    {
        return new AtlasSize(estimate.edges(), estimate.nodes(), estimate.areas(), estimate.lines(),
                estimate.point(), estimate.relations());
    }

    private void createDirectly(final TestAtlas testAtlas, final Field field,
            final CoreTestRule rule, final CreationContext context)
    {

        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        builder.setSizeEstimates(convertSizeEstimates(testAtlas.size()));

        handle(builder, testAtlas.nodes());
        handle(builder, testAtlas.edges());
        handle(builder, testAtlas.areas());
        handle(builder, testAtlas.lines());
        handle(builder, testAtlas.points());
        handle(builder, testAtlas.relations());
        handle(builder, testAtlas.buildings());

        try
        {
            field.set(rule, builder.get());
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
            throw new CoreException("Error creating test atlas", e);
        }
    }

    private void handle(final PackedAtlasBuilder builder, final Area... areas)
    {
        final FeatureIDGenerator areaIDGenerator = new FeatureIDGenerator();
        for (final Area area : areas)
        {
            addArea(builder, areaIDGenerator, area);
        }
    }

    private void handle(final PackedAtlasBuilder builder, final Building... buildings)
    {
        final FeatureIDGenerator buildingGenerator = new FeatureIDGenerator();
        for (final Building building : buildings)
        {
            final TreeSet<Long> outerIds = new TreeSet<>();
            final TreeSet<Long> innerIds = new TreeSet<>();
            final TreeSet<Long> partIds = new TreeSet<>();

            outerIds.add(addArea(builder, buildingGenerator, building.outer()));
            for (final Area inner : building.inners())
            {
                innerIds.add(addArea(builder, buildingGenerator, inner));
            }
            for (final Area part : building.parts())
            {
                partIds.add(addArea(builder, buildingGenerator, part, BuildingPartTag.KEY,
                        BuildingPartTag.YES.getTagValue()));
            }

            final RelationBean outline = new RelationBean();
            outline.addItem(outerIds.first(), RelationTypeTag.MULTIPOLYGON_ROLE_OUTER,
                    ItemType.AREA);
            for (final Long innerId : innerIds)
            {
                outline.addItem(innerId, RelationTypeTag.MULTIPOLYGON_ROLE_INNER, ItemType.AREA);
            }

            final long outlineId = buildingGenerator.nextId(TestAtlas.AUTO_GENERATED);
            builder.addRelation(outlineId, outlineId, outline,
                    mergeTags(parseTags(building.outlineTags()),
                            Validators.toMap(RelationTypeTag.MULTIPOLYGON)));

            final RelationBean multipart = new RelationBean();
            multipart.addItem(outlineId, BuildingTag.BUILDING_ROLE_OUTLINE, ItemType.RELATION);
            for (final Long partId : partIds)
            {
                multipart.addItem(partId, BuildingTag.BUILDING_ROLE_PART, ItemType.AREA);
            }

            final long buildingId = buildingGenerator.nextId(TestAtlas.AUTO_GENERATED);
            builder.addRelation(buildingId, buildingId, multipart, mergeTags(
                    parseTags(building.tags()), Validators.toMap(RelationTypeTag.BUILDING)));
        }
    }

    private void handle(final PackedAtlasBuilder builder, final Edge... edges)
    {
        final FeatureIDGenerator edgeIDGenerator = new FeatureIDGenerator();
        for (final Edge edge : edges)
        {
            builder.addEdge(edgeIDGenerator.nextId(edge.id()), convertPolyLine(edge.coordinates()),
                    parseTags(edge.tags()));
        }
    }

    private void handle(final PackedAtlasBuilder builder, final Line... lines)
    {
        final FeatureIDGenerator lineIDGenerator = new FeatureIDGenerator();
        for (final Line line : lines)
        {
            builder.addLine(lineIDGenerator.nextId(line.id()), convertPolyLine(line.coordinates()),
                    parseTags(line.tags()));
        }
    }

    private void handle(final PackedAtlasBuilder builder, final Node... nodes)
    {
        final FeatureIDGenerator nodeIDGenerator = new FeatureIDGenerator();
        for (final Node node : nodes)
        {
            builder.addNode(nodeIDGenerator.nextId(node.id()), convertLoc(node.coordinates()),
                    parseTags(node.tags()));
        }
    }

    private void handle(final PackedAtlasBuilder builder, final Point... points)
    {
        final FeatureIDGenerator pointIDGenerator = new FeatureIDGenerator();
        for (final Point point : points)
        {
            builder.addPoint(pointIDGenerator.nextId(point.id()), convertLoc(point.coordinates()),
                    parseTags(point.tags()));
        }
    }

    private void handle(final PackedAtlasBuilder builder, final Relation... relations)
    {
        final FeatureIDGenerator relationIDGenerator = new FeatureIDGenerator();
        for (final Relation relation : relations)
        {
            final RelationBean bean = new RelationBean();
            for (final Member member : relation.members())
            {
                bean.addItem(Long.parseLong(member.id()), member.role(),
                        Enum.valueOf(ItemType.class, member.type().toUpperCase()));
            }
            final long identifier = relationIDGenerator.nextId(relation.id());
            builder.addRelation(identifier, identifier, bean, parseTags(relation.tags()));
        }
    }

    private void loadFromJosmOsmResource(final Field field, final CoreTestRule rule,
            final CreationContext context, final String resourcePath)
    {
        final String packageName = rule.getClass().getPackage().getName().replaceAll("\\.", "/");
        final String completeName = String.format("%s/%s", packageName, resourcePath);
        final ClassResource resource = new ClassResource(completeName);
        FileSuffix.suffixFor(completeName).ifPresent(suffix ->
        {
            if (suffix == FileSuffix.GZIP)
            {
                resource.setDecompressor(Decompressor.GZIP);
            }
        });
        try
        {
            final StringResource osmFile = new StringResource();
            new OsmFileParser().update(resource, osmFile);
            final ByteArrayResource pbfFile = new ByteArrayResource();
            new OsmFileToPbf().update(osmFile, pbfFile);
            field.set(rule, new OsmPbfLoader(pbfFile, AtlasLoadingOption.withNoFilter()).read());
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
            throw new CoreException("Error loading from JOSM osm resource {}", resourcePath, e);
        }
    }

    private void loadFromTextResource(final Field field, final CoreTestRule rule,
            final CreationContext context, final String resourcePath)
    {
        final String packageName = rule.getClass().getPackage().getName().replaceAll("\\.", "/");
        final String completeName = String.format("%s/%s", packageName, resourcePath);
        final ClassResource resource = new ClassResource(completeName);
        FileSuffix.suffixFor(completeName).ifPresent(suffix ->
        {
            if (suffix == FileSuffix.GZIP)
            {
                resource.setDecompressor(Decompressor.GZIP);
            }
        });
        try
        {
            field.set(rule, new TextAtlasBuilder().read(resource));
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
            throw new CoreException("Error loading from text resource {}", resourcePath, e);
        }
    }

    private List<Location> pointsToLocations(final Loc[] points)
    {
        final List<Location> locations = new ArrayList<>();
        for (final Loc point : points)
        {
            locations.add(convertLoc(point));
        }
        return locations;
    }
}
