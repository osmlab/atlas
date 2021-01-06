package org.openstreetmap.atlas.utilities.testing;

import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.builder.text.TextAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.raw.creation.RawAtlasGenerator;
import org.openstreetmap.atlas.geography.atlas.raw.sectioning.AtlasSectionProcessor;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.RawAtlasSlicer;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.AbstractResource;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.ClassResource;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.tags.BuildingPartTag;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Maps;
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
 * @author bbreithaupt
 */
public class TestAtlasHandler implements FieldHandler
{
    public static Atlas getAtlasFromJosmOsmResource(final boolean josmFormat,
            final AbstractResource resource, final String fileName)
    {
        return getAtlasFromJosmOsmResource(josmFormat, resource, fileName, Optional.empty());
    }

    public static Atlas getAtlasFromJosmOsmResource(final boolean josmFormat,
            final AbstractResource resource, final String fileName, final Optional<String> iso)
    {
        FileSuffix.suffixFor(fileName).ifPresent(suffix ->
        {
            if (suffix == FileSuffix.GZIP)
            {
                resource.setDecompressor(Decompressor.GZIP);
            }
        });
        final ByteArrayResource pbfFile = new ByteArrayResource();
        if (josmFormat)
        {
            // If the XML file is in JOSM format, fix it to look like an OSM file
            final StringResource osmFile = new StringResource();
            new OsmFileParser().update(resource, osmFile);
            new OsmFileToPbf().update(osmFile, pbfFile);
        }
        else
        {
            new OsmFileToPbf().update(resource, pbfFile);
        }
        return buildAtlasFromPbf(pbfFile, iso);
    }

    /**
     * Builds an {@link Atlas} from the given pbf resource, using the raw atlas flow. This does NOT
     * country-slice the pbf resource since no corresponding boundary file is supplied and the flow
     * is not meant to test slicing logic.
     *
     * @param pbfResource
     *            The pbf input resource to use
     * @param iso
     *            ISO code to be applied to all features
     * @return the resulting Atlas
     */
    private static Atlas buildAtlasFromPbf(final Resource pbfResource, final Optional<String> iso)
    {
        // Create raw Atlas
        final AtlasLoadingOption loadingOption = AtlasLoadingOption.withNoFilter();
        if (iso.isPresent())
        {
            loadingOption.setCountrySlicing(true);
            loadingOption.setCountryBoundaryMap(CountryBoundaryMap
                    .fromBoundaryMap(Collections.singletonMap(iso.get(), MultiPolygon.MAXIMUM)));
        }
        final Atlas rawAtlas = new RawAtlasGenerator(pbfResource, loadingOption,
                MultiPolygon.MAXIMUM).build();

        // Country Slice and Way-Section
        return new AtlasSectionProcessor(
                iso.isPresent() ? new RawAtlasSlicer(loadingOption, rawAtlas).slice() : rawAtlas,
                loadingOption).run();
    }

    private static Map<String, String> mergeTags(final Map<String, String> firstTags,
            final Map<String, String> secondTags)
    {
        final Map<String, String> returnValue = new HashMap<>();
        returnValue.putAll(firstTags);
        returnValue.putAll(secondTags);
        return returnValue;
    }

    private static String[] mergeTags(final String[] firstTags, final String[] secondTags)
    {
        final List<String> allTags = new ArrayList<>(Arrays.asList(firstTags));
        allTags.addAll(Arrays.asList(secondTags));
        return allTags.toArray(new String[0]);
    }

    private static Map<String, String> parseTags(final Optional<String> iso, final String... tags)
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
        // Add a country code if one does not already exist
        if (iso.isPresent() && !tagmap.containsKey(ISOCountryTag.KEY.toLowerCase()))
        {
            tagmap.put(ISOCountryTag.KEY.toLowerCase(), iso.get());
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
                loadFromJosmOsmResource(field, rule, context, testAtlas.loadFromJosmOsmResource(),
                        true);
            }
            catch (final Throwable e)
            {
                throw new CoreException("Error creating field {}", field, e);
            }
        }
        else if (StringUtils.isNotEmpty(testAtlas.loadFromOsmResource()))
        {
            try
            {
                loadFromJosmOsmResource(field, rule, context, testAtlas.loadFromOsmResource(),
                        false);
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
            final Area area, final Optional<String> iso, final String... additionalTags)
    {
        final long areaId = areaIDGenerator.nextId(area.id());
        builder.addArea(areaId, buildAreaPolygon(area),
                parseTags(iso, mergeTags(area.tags(), additionalTags)));
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

    private PolyLine convertPolyLine(final Loc[] points)
    {
        return new PolyLine(pointsToLocations(points));
    }

    private Polygon convertPolygon(final Loc[] points)
    {
        return new Polygon(pointsToLocations(points));
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
        final AtlasSize size = convertSizeEstimates(testAtlas.size());
        final Optional<String> iso = testAtlas.iso().equals(TestAtlas.UNKNOWN_ISO_COUNTRY)
                ? Optional.empty()
                : Optional.of(testAtlas.iso());
        if (iso.isPresent())
        {
            final AtlasMetaData metaData = new AtlasMetaData(size, true, null, null, iso.get(),
                    null, Maps.hashMap());
            builder.withMetaData(metaData);
        }
        else
        {
            builder.setSizeEstimates(size);
        }
        handle(builder, iso, testAtlas.nodes());
        handle(builder, iso, testAtlas.edges());
        handle(builder, iso, testAtlas.areas());
        handle(builder, iso, testAtlas.lines());
        handle(builder, iso, testAtlas.points());
        handle(builder, iso, testAtlas.relations());
        handle(builder, iso, testAtlas.buildings());

        try
        {
            field.set(rule, builder.get());
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
            throw new CoreException("Error creating test atlas", e);
        }
    }

    private void handle(final PackedAtlasBuilder builder, final Optional<String> iso,
            final Area... areas)
    {
        final FeatureIDGenerator areaIDGenerator = new FeatureIDGenerator();
        for (final Area area : areas)
        {
            addArea(builder, areaIDGenerator, area, iso);
        }
    }

    private void handle(final PackedAtlasBuilder builder, final Optional<String> iso,
            final Building... buildings)
    {
        final FeatureIDGenerator buildingGenerator = new FeatureIDGenerator();
        for (final Building building : buildings)
        {
            final TreeSet<Long> outerIds = new TreeSet<>();
            final TreeSet<Long> innerIds = new TreeSet<>();
            final TreeSet<Long> partIds = new TreeSet<>();

            outerIds.add(addArea(builder, buildingGenerator, building.outer(), iso));
            for (final Area inner : building.inners())
            {
                innerIds.add(addArea(builder, buildingGenerator, inner, iso));
            }
            for (final Area part : building.parts())
            {
                partIds.add(addArea(builder, buildingGenerator, part, iso, BuildingPartTag.KEY,
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
                    mergeTags(parseTags(iso, building.outlineTags()),
                            Validators.toMap(RelationTypeTag.MULTIPOLYGON)));

            final RelationBean multipart = new RelationBean();
            multipart.addItem(outlineId, BuildingTag.BUILDING_ROLE_OUTLINE, ItemType.RELATION);
            for (final Long partId : partIds)
            {
                multipart.addItem(partId, BuildingTag.BUILDING_ROLE_PART, ItemType.AREA);
            }

            final long buildingId = buildingGenerator.nextId(TestAtlas.AUTO_GENERATED);
            builder.addRelation(buildingId, buildingId, multipart, mergeTags(
                    parseTags(iso, building.tags()), Validators.toMap(RelationTypeTag.BUILDING)));
        }
    }

    private void handle(final PackedAtlasBuilder builder, final Optional<String> iso,
            final Edge... edges)
    {
        final FeatureIDGenerator edgeIDGenerator = new FeatureIDGenerator();
        for (final Edge edge : edges)
        {
            builder.addEdge(edgeIDGenerator.nextId(edge.id()), convertPolyLine(edge.coordinates()),
                    parseTags(iso, edge.tags()));
        }
    }

    private void handle(final PackedAtlasBuilder builder, final Optional<String> iso,
            final Line... lines)
    {
        final FeatureIDGenerator lineIDGenerator = new FeatureIDGenerator();
        for (final Line line : lines)
        {
            builder.addLine(lineIDGenerator.nextId(line.id()), convertPolyLine(line.coordinates()),
                    parseTags(iso, line.tags()));
        }
    }

    private void handle(final PackedAtlasBuilder builder, final Optional<String> iso,
            final Node... nodes)
    {
        final FeatureIDGenerator nodeIDGenerator = new FeatureIDGenerator();
        for (final Node node : nodes)
        {
            builder.addNode(nodeIDGenerator.nextId(node.id()), convertLoc(node.coordinates()),
                    parseTags(iso, node.tags()));
        }
    }

    private void handle(final PackedAtlasBuilder builder, final Optional<String> iso,
            final Point... points)
    {
        final FeatureIDGenerator pointIDGenerator = new FeatureIDGenerator();
        for (final Point point : points)
        {
            builder.addPoint(pointIDGenerator.nextId(point.id()), convertLoc(point.coordinates()),
                    parseTags(iso, point.tags()));
        }
    }

    private void handle(final PackedAtlasBuilder builder, final Optional<String> iso,
            final Relation... relations)
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
            final long osmIdentifier = relation.osmId().equals(TestAtlas.DEFAULT_OSM_ID)
                    ? identifier
                    : relationIDGenerator.nextId(relation.osmId());
            builder.addRelation(identifier, osmIdentifier, bean, parseTags(iso, relation.tags()));
        }
    }

    private void loadFromJosmOsmResource(final Field field, final CoreTestRule rule,
            final CreationContext context, final String resourcePath, final boolean josmFormat)
    {
        final String packageName = rule.getClass().getPackage().getName().replaceAll("\\.", "/");
        final String completeName = String.format("%s/%s", packageName, resourcePath);
        try
        {
            field.set(rule, getAtlasFromJosmOsmResource(josmFormat, new ClassResource(completeName),
                    Paths.get(completeName).getFileName().toString(),
                    field.getAnnotation(TestAtlas.class).iso().equals(TestAtlas.UNKNOWN_ISO_COUNTRY)
                            ? Optional.empty()
                            : Optional.of(field.getAnnotation(TestAtlas.class).iso())));
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
