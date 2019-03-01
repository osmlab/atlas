package org.openstreetmap.atlas.geography.atlas.items.complex.waters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler.CanalHandler;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler.CreekHandler;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler.DitchHandler;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler.HarbourHandler;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler.LagoonHandler;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler.LakeHandler;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler.PondHandler;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler.PoolHandler;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler.ReservoirHandler;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler.RiverHandler;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler.SeaHandler;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler.WaterHandler;
import org.openstreetmap.atlas.geography.atlas.items.complex.waters.handler.WetlandHandler;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;

/**
 * @author Sid
 */
public class ComplexWaterEntityFinder implements Finder<ComplexWaterEntity>
{
    private static final Predicate<Relation> RELATION_FILTER = (relation) -> Validators.isOfType(
            relation, RelationTypeTag.class, RelationTypeTag.MULTIPOLYGON, RelationTypeTag.BOUNDARY,
            RelationTypeTag.WATERWAY);

    private final Map<WaterType, WaterHandler> handlers;

    public ComplexWaterEntityFinder()
    {
        this.handlers = new HashMap<>();

        this.handlers.put(WaterType.CANAL, new CanalHandler());
        this.handlers.put(WaterType.CREEK, new CreekHandler());
        this.handlers.put(WaterType.DITCH, new DitchHandler());
        this.handlers.put(WaterType.HARBOUR, new HarbourHandler());
        this.handlers.put(WaterType.LAGOON, new LagoonHandler());
        this.handlers.put(WaterType.LAKE, new LakeHandler());
        this.handlers.put(WaterType.POND, new PondHandler());
        this.handlers.put(WaterType.POOL, new PoolHandler());
        this.handlers.put(WaterType.RESERVOIR, new ReservoirHandler());
        this.handlers.put(WaterType.RIVER, new RiverHandler());
        this.handlers.put(WaterType.SEA, new SeaHandler());
        this.handlers.put(WaterType.WETLAND, new WetlandHandler());
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

    private List<ComplexWaterEntity> processEntity(final AtlasObject object)
    {
        final List<ComplexWaterEntity> complexWaterEntities = new ArrayList<>();
        if (object instanceof AtlasEntity)
        {
            final AtlasEntity entity = (AtlasEntity) object;
            this.handlers.forEach((waterType, handler) ->
            {
                final Optional<ComplexWaterEntity> complexWaterEntity = handler.handle(entity);
                if (complexWaterEntity.isPresent())
                {
                    complexWaterEntities.add(complexWaterEntity.get());
                }
                return;
            });
        }
        return complexWaterEntities;
    }
}
