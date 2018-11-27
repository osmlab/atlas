package org.openstreetmap.atlas.geography.atlas.sub;

import java.util.Optional;
import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;

/**
 * SubAtlas is a subset of an existing {@link Atlas}. It's created by cutting a given {@link Atlas}
 * based on some condition (Predicate).
 *
 * @author mgostintsev
 */
public interface SubAtlas
{
    Optional<Atlas> hardCutAllEntities(Polygon boundary);

    Optional<Atlas> hardCutAllEntities(Predicate<AtlasEntity> matcher);

    Optional<Atlas> hardCutRelationsOnly(Predicate<AtlasEntity> matcher);

    Optional<Atlas> softCut(Polygon boundary, boolean hardCutRelations);

    Optional<Atlas> softCut(Predicate<AtlasEntity> matcher);
}
