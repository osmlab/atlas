package org.openstreetmap.atlas.geography.atlas.items.complex.waters;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class loops over the atlas lines, areas and relations to find water entities and return a
 * list of @{link ComplexWaterEntity} objects
 * 
 * @author Sid
 * @author sbhalekar
 */
public class ComplexWaterEntityFinder implements Finder<ComplexWaterEntity>
{
    private static final Predicate<Relation> RELATION_FILTER = relation -> Validators.isOfType(
            relation, RelationTypeTag.class, RelationTypeTag.MULTIPOLYGON, RelationTypeTag.BOUNDARY,
            RelationTypeTag.WATERWAY);

    /**
     * Default water handler configuration from the resources
     */
    public static final String WATER_RESOURCE = "water-handlers.json";

    private final WaterConfigurationHandler waterConfigurationHandler;

    private static final Logger logger = LoggerFactory.getLogger(ComplexWaterEntityFinder.class);

    public ComplexWaterEntityFinder()
    {
        this(new InputStreamResource(
                () -> ComplexWaterEntityFinder.class.getResourceAsStream(WATER_RESOURCE)));
    }

    public ComplexWaterEntityFinder(final Resource resource)
    {
        this.waterConfigurationHandler = new WaterConfigurationHandler(resource);
    }

    public WaterConfigurationHandler getWaterConfigurationHandler()
    {
        return this.waterConfigurationHandler;
    }

    @Override
    public Iterable<ComplexWaterEntity> find(final Atlas atlas)
    {
        final Iterable<ComplexWaterEntity> areaEntities = Iterables.translateMulti(atlas.areas(),
                this::processEntity);
        final Iterable<ComplexWaterEntity> lineEntities = Iterables
                .translateMulti(atlas.lineItems(), this::processEntity);
        final Iterable<ComplexWaterEntity> relationEntities = Iterables
                .translateMulti(atlas.relations(RELATION_FILTER), this::processEntity);
        return new MultiIterable<>(areaEntities, lineEntities, relationEntities);
    }

    /**
     * Take in an atlas object and run through all the water handlers created from the configuration
     * file
     * 
     * @param object
     *            Atlas object to work with
     * @return List of {@link ComplexWaterEntity} created based on the configuration
     */
    public List<ComplexWaterEntity> processEntity(final AtlasObject object)
    {
        final List<ComplexWaterEntity> complexWaterEntities = new ArrayList<>();
        if (object instanceof AtlasEntity)
        {
            final AtlasEntity entity = (AtlasEntity) object;
            this.waterConfigurationHandler.getWaterHandlers()
                    .forEach((waterBodyType, configuredFilter) ->
                    {
                        if (configuredFilter.test(entity))
                        {
                            try
                            {
                                if (entity instanceof Relation || entity instanceof Area)
                                {
                                    complexWaterEntities
                                            .add(new ComplexWaterbody(entity, waterBodyType));
                                }
                                else if (entity instanceof Line)
                                {
                                    complexWaterEntities
                                            .add(new ComplexWaterway(entity, waterBodyType));
                                }
                            }
                            catch (final Exception e)
                            {
                                logger.warn("Skipping entity : {}", entity, e);
                            }
                        }
                    });
        }
        if (complexWaterEntities.isEmpty())
        {
            logger.debug("Could not create complex water entity from {} with osm id {}",
                    object.getIdentifier(), object.getOsmIdentifier());
        }
        return complexWaterEntities;
    }
}
