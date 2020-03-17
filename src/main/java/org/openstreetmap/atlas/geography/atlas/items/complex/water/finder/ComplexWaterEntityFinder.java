package org.openstreetmap.atlas.geography.atlas.items.complex.water.finder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.geography.atlas.items.complex.water.ComplexWaterEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.water.WaterType;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Finder to find all {@link ComplexWaterEntity} from and {@link Atlas} by using multiple
 * configuration readers for each {@link WaterType}
 *
 * @author sbhalekar
 */
public class ComplexWaterEntityFinder implements Finder<ComplexWaterEntity>
{
    private static final Predicate<Relation> RELATION_FILTER = relation -> Validators.isOfType(
            relation, RelationTypeTag.class, RelationTypeTag.MULTIPOLYGON, RelationTypeTag.BOUNDARY,
            RelationTypeTag.WATERWAY);

    private static final Logger logger = LoggerFactory.getLogger(ComplexWaterEntityFinder.class);

    private final List<WaterConfigurationReader> waterConfigurationReaders;

    public ComplexWaterEntityFinder()
    {
        this.waterConfigurationReaders = new ArrayList<>();

        // read in the default configuration files with default mappings for each water body type
        this.waterConfigurationReaders
                .add(new DefaultWaterConfigurationReader("lake.json", WaterType.LAKE));
        this.waterConfigurationReaders
                .add(new DefaultWaterConfigurationReader("river.json", WaterType.RIVER));
        this.waterConfigurationReaders
                .add(new DefaultWaterConfigurationReader("lagoon.json", WaterType.LAGOON));
        this.waterConfigurationReaders
                .add(new DefaultWaterConfigurationReader("wetland.json", WaterType.WETLAND));
        this.waterConfigurationReaders
                .add(new DefaultWaterConfigurationReader("reservoir.json", WaterType.RESERVOIR));
        this.waterConfigurationReaders
                .add(new DefaultWaterConfigurationReader("pool.json", WaterType.POOL));
        this.waterConfigurationReaders
                .add(new DefaultWaterConfigurationReader("pond.json", WaterType.POND));
        this.waterConfigurationReaders
                .add(new DefaultWaterConfigurationReader("harbour.json", WaterType.HARBOUR));
        this.waterConfigurationReaders
                .add(new DefaultWaterConfigurationReader("canal.json", WaterType.CANAL));
        this.waterConfigurationReaders
                .add(new DefaultWaterConfigurationReader("creek.json", WaterType.CREEK));
        this.waterConfigurationReaders
                .add(new DefaultWaterConfigurationReader("ditch.json", WaterType.DITCH));
    }

    public ComplexWaterEntityFinder(final WaterConfigurationReader... waterConfigurationReaders)
    {
        // use the passed in configuration readers for the filters
        this.waterConfigurationReaders = Arrays.asList(waterConfigurationReaders);
    }

    @Override
    public Iterable<ComplexWaterEntity> find(final Atlas atlas)
    {

        final Stream<Line> lineStream = StreamSupport.stream(atlas.lines().spliterator(), false);
        final Stream<Area> areaStream = StreamSupport.stream(atlas.areas().spliterator(), false);
        final Stream<Relation> relationStream = StreamSupport
                .stream(atlas.relations(RELATION_FILTER).spliterator(), false);

        return Stream.concat(Stream.concat(lineStream, areaStream), relationStream)
                .map(this::processEntity).filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<WaterConfigurationReader> getWaterConfigurationReaders()
    {
        return this.waterConfigurationReaders;
    }

    /**
     * Convert {@link AtlasEntity} to an Optional {@link ComplexWaterEntity}. Sometimes an
     * {@link AtlasEntity} might not pass any of the filters in the configuration files. In that
     * case return empty optional
     *
     * @param atlasEntity
     *            {@link AtlasEntity} which needs to be converted
     * @return Optional {@link ComplexWaterEntity}
     */
    public Optional<ComplexWaterEntity> processEntity(final AtlasEntity atlasEntity)
    {
        // pass the atlas entity through all the filters in the configuration files and try to
        // create a complex water entity for each passed filter
        final List<ComplexWaterEntity> complexWaterEntities = this.waterConfigurationReaders
                .stream()
                .map(waterConfigurationReader -> waterConfigurationReader.convert(atlasEntity))
                .filter(Optional::isPresent).map(Optional::get)
                .filter(object -> object instanceof ComplexWaterEntity)
                .map(object -> (ComplexWaterEntity) object).collect(Collectors.toList());

        if (complexWaterEntities.isEmpty())
        {
            logger.error("AtlasEntity: {} did not match any water type filters", atlasEntity);
            return Optional.empty();
        }
        else if (complexWaterEntities.size() > 1)
        {
            // Each AtlasEntity should pass only one WaterType configuration. If is passes more than
            // one means the taggable filters specified in the configurations has an overlap. Due to
            // ambiguous taggable filters specified this method would return empty optional
            logger.error("Skipping AtlasEnity : {} as it got mapped to {}", atlasEntity,
                    complexWaterEntities.stream().map(ComplexWaterEntity::getWaterType)
                            .map(WaterType::toString).collect(Collectors.joining(",")));
            return Optional.empty();
        }

        // return the ComplexWaterEntity matched with the only water type configuration
        return Optional.of(complexWaterEntities.get(0));
    }
}
