package org.openstreetmap.atlas.geography.atlas.statistics.coverage.poi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.atlas.statistics.coverage.Coverage;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.scalars.Surface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is specifically made for feature counts. It can count shops, lakes, bus stops, etc.
 * This is simpler than other metrics, so all the definitions of counts to run are defined in a
 * resource (text file for example), one per line.
 *
 * @author matthieun
 * @param <T>
 *            The type of {@link AtlasEntity} to count.
 */
public abstract class SimpleCoverage<T extends AtlasEntity> extends Coverage<T>
{
    private static final Logger logger = LoggerFactory.getLogger(SimpleCoverage.class);

    private static final String TYPE_SEPARATOR = ";";
    private static final String VALUES_SEPARATOR = ",";
    private static final String COMMENTED_LINE = "#";
    private static final int COVERAGE_TYPE_INDEX = 3;

    private static final String NODES = "nodes";
    private static final String EDGES = "edges";
    private static final String LINES = "lines";
    private static final String AREAS = "areas";
    private static final String POINTS = "points";
    private static final String RELATIONS = "relations";

    private final Predicate<Taggable> filter;
    private final CoverageType coverageType;

    /**
     * Parse a count configuration file
     *
     * @param atlas
     *            The Atlas to crawl
     * @param coverages
     *            The configuration file
     * @return All the {@link SimpleCoverage}s defined in the file
     */
    public static Iterable<SimpleCoverage<AtlasEntity>> parseSimpleCoverages(final Atlas atlas,
            final Iterable<String> coverages)
    {
        final List<SimpleCoverage<AtlasEntity>> result = new ArrayList<>();
        final Iterable<String> filteredCoverages = Iterables.filter(coverages,
                line -> !(line.startsWith(COMMENTED_LINE) || "".equals(line)));
        filteredCoverages.forEach(definition ->
        {
            try
            {
                // Each line
                final StringList split = StringList.split(definition, TYPE_SEPARATOR);
                final StringList sources = StringList.split(split.get(1), VALUES_SEPARATOR);
                final String type = split.get(0);
                final String coverageTypes = split.size() > COVERAGE_TYPE_INDEX
                        ? split.get(COVERAGE_TYPE_INDEX) : CoverageType.COUNT.name();
                final Set<CoverageType> coverageTypeSet = StringList
                        .split(coverageTypes, VALUES_SEPARATOR).stream().map(CoverageType::forName)
                        .collect(Collectors.toSet());
                final Predicate<Taggable> allowedTags = TaggableFilter.forDefinition(split.get(2));
                final BiFunction<String, CoverageType, SimpleCoverage<AtlasEntity>> simpleCoverageFunction = (
                        metricName, sampleCoverageType) -> new SimpleCoverage<AtlasEntity>(
                                LoggerFactory.getLogger(metricName), atlas, sampleCoverageType)
                        {
                            @Override
                            protected Iterable<AtlasEntity> getEntities()
                            {
                                if (sources.contains("all"))
                                {
                                    return getAtlas();
                                }
                                final List<Iterable<? extends AtlasEntity>> result = new ArrayList<>();
                                if (sources.contains(NODES))
                                {
                                    result.add(getAtlas().nodes());
                                }
                                if (sources.contains(EDGES))
                                {
                                    result.add(getAtlas().edges());
                                }
                                if (sources.contains(AREAS))
                                {
                                    result.add(getAtlas().areas());
                                }
                                if (sources.contains(LINES))
                                {
                                    result.add(getAtlas().lines());
                                }
                                if (sources.contains(POINTS))
                                {
                                    result.add(getAtlas().points());
                                }
                                if (sources.contains(RELATIONS))
                                {
                                    result.add(getAtlas().relations());
                                }
                                return new MultiIterable<>(result);
                            }

                            @Override
                            protected String type()
                            {
                                return metricName;
                            }

                            @Override
                            protected Predicate<Taggable> validKeyValuePairs()
                            {
                                return allowedTags;
                            }
                        };
                coverageTypeSet.forEach(localCoverageType ->
                {
                    final String appendix = CoverageType.COUNT.equals(localCoverageType) ? ""
                            : "_" + localCoverageType.name().toLowerCase();
                    final String metricName = type + appendix;
                    result.add(simpleCoverageFunction.apply(metricName, localCoverageType));
                });
            }
            catch (final Exception e)
            {
                throw new CoreException("Error parsing {}", definition, e);
            }
        });
        return result;
    }

    public SimpleCoverage(final Logger logger, final Atlas atlas, final CoverageType coverageType)
    {
        super(logger, atlas);
        this.coverageType = coverageType;
        this.filter = validKeyValuePairs();
    }

    public SimpleCoverage(final Logger logger, final Atlas atlas, final Predicate<T> filter,
            final CoverageType coverageType)
    {
        super(logger, atlas, filter);
        this.coverageType = coverageType;
        this.filter = validKeyValuePairs();
    }

    @Override
    protected CoverageType coverageType()
    {
        return this.coverageType;
    }

    @Override
    protected Set<String> getKeys(final AtlasEntity item)
    {
        // Only All will be represented here
        return new HashSet<>();
    }

    @Override
    protected String getUnit()
    {
        switch (coverageType())
        {
            case COUNT:
                return "count unit";
            case DISTANCE:
                return "kilometers";
            case SURFACE:
                return "square kilometers";
            default:
                throw new CoreException("Unknown coverage type: {}", this.coverageType.name());
        }
    }

    @Override
    protected double getValue(final T item)
    {
        switch (coverageType())
        {
            case COUNT:
                return 1;
            case DISTANCE:
                return getDistance(item).asKilometers();
            case SURFACE:
                return getSurface(item).asKilometerSquared();
            default:
                throw new CoreException("Unknown coverage type: {}", this.coverageType.name());
        }
    }

    @Override
    protected boolean isCounted(final T item)
    {
        return this.filter.test(item);
    }

    @Override
    protected String subType()
    {
        return "true";
    }

    /**
     * @return All the tag key/value pairs that an entity needs to have to be counted. Extending
     *         this definition, if a TagGroup is empty, then all features are valid and will be
     *         counted.
     */
    protected abstract Predicate<Taggable> validKeyValuePairs();

    /**
     * Gets the length of the item to count
     *
     * @param item
     *            The item to count
     * @return The length of the item to count. In case this is a {@link Relation}, the length is
     *         the sum of all the relation's lowest order {@link LineItem}s.
     */
    private Distance getDistance(final AtlasEntity item)
    {
        Distance result = Distance.ZERO;
        if (item instanceof LineItem)
        {
            return ((LineItem) item).asPolyLine().length();
        }
        else if (item instanceof Relation)
        {
            for (final RelationMember member : ((Relation) item).members())
            {
                result = result.add(getDistance(member.getEntity()));
            }
        }
        return result;
    }

    /**
     * Gets the area of the item to count
     *
     * @param item
     *            The item to count
     * @return The area of the item to count. In case this is a {@link Relation}, and the
     *         {@link Relation} is type=multipolygon, the area is the area of the multipolygon. In
     *         case the relation is of another type, the area is the sum of all the lowest order
     *         areas that make the relation.
     */
    private Surface getSurface(final AtlasEntity item)
    {
        Surface result = Surface.MINIMUM;
        if (item instanceof Relation && ((Relation) item).isMultiPolygon() || item instanceof Area)
        {
            try
            {
                final RelationOrAreaToMultiPolygonConverter converter = new RelationOrAreaToMultiPolygonConverter();
                result = result.add(converter.apply(item).surface());
                return result;
            }
            catch (final CoreException e)
            {
                // Many features will not be multipolygons.
            }
            catch (final IllegalArgumentException e)
            {
                logger.error("AtlasStatistics cannot compute surface of {}", item, e);
            }
            return result;
        }
        if (item instanceof Relation)
        {
            // Relation that is not of type multipolygon
            for (final RelationMember member : ((Relation) item).members())
            {
                result = result.add(getSurface(member.getEntity()));
            }
        }
        return result;
    }
}
