package org.openstreetmap.atlas.geography.atlas.items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.WktPrintable;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder.LocationIterableProperties;
import org.openstreetmap.atlas.geography.geojson.GeoJsonUtils;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * An OSM relation
 *
 * @author matthieun
 * @author Sid
 * @author hallahan
 */
public abstract class Relation extends AtlasEntity implements Iterable<RelationMember>
{
    /**
     * The ring type of a {@link MultiPolygon} member.
     *
     * @author matthieun
     */
    public enum Ring
    {
        OUTER,
        INNER
    }

    private static final Logger logger = LoggerFactory.getLogger(Relation.class);
    private static final long serialVersionUID = -9013894610780915685L;

    public static final Comparator<Relation> RELATION_ID_COMPARATOR = Comparator
            .comparingLong(AtlasObject::getIdentifier);

    private static final RelationOrAreaToMultiPolygonConverter MULTI_POLYGON_CONVERTER = new RelationOrAreaToMultiPolygonConverter();

    protected Relation(final Atlas atlas)
    {
        super(atlas);
    }

    /**
     * @return All the members of this relation's OSM ancestor. If this relation has not been
     *         sliced, then this will return the same as <code>members()</code>. If this relation is
     *         sliced, and is part of a pool of other relations that belong to the same OSM
     *         ancestor, this method will poll together all the members of all those relations in
     *         its Atlas.
     */
    public abstract RelationMemberList allKnownOsmMembers();

    public abstract List<Relation> allRelationsWithSameOsmIdentifier();

    @Override
    public JsonObject asGeoJsonGeometry()
    {
        final JsonObject properties = geoJsonProperties();

        // Can't be final due to catch block may reassign.
        JsonObject geometry;

        // We should only be writing relations as GeoJSON when they are polygons and multipolygons.
        // We want multipolygons, but not boundaries, as we can render boundaries' ways by
        // themselves fine.
        // The isMultiPolygon() method also includes boundaries, which we do not want.
        if (Validators.isOfType(this, RelationTypeTag.class, RelationTypeTag.MULTIPOLYGON))
        {
            try
            {
                final MultiPolygon multiPolygon = MULTI_POLYGON_CONVERTER.convert(this);
                geometry = multiPolygon.asGeoJsonGeometry();
            }
            // It seems like we get caught in this exception a lot! We don't ingest coastline
            // features, so polygons that touch coastlines will fail. It's good to include
            // the exception in the data, along with the bounding box. That way, we can
            // notice the problem when browsing the map.
            catch (final CoreException exception)
            {
                final String message = String.format("%s - %s",
                        exception.getClass().getSimpleName(), exception.getMessage());
                properties.addProperty("exception", message);
                logger.warn("Unable to recreate multipolygon for relation {}.", getIdentifier(),
                        message);
                geometry = GeoJsonUtils.boundsToPolygonGeometry(bounds());
            }
        }
        // Otherwise, we'll fall back to just providing the properties of the relation with the
        // bounding box as a polygon geometry.
        else
        {
            geometry = GeoJsonUtils.boundsToPolygonGeometry(bounds());
        }

        addMembersToProperties(properties);

        return GeoJsonUtils.feature(geometry, properties);
    }

    @Override
    public Rectangle bounds()
    {
        return boundsInternal(new LinkedHashSet<>());
    }

    public String configurableString(final String betweenEachMemberAndRelation,
            final String betweenEachMember)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("[Relation: id=");
        builder.append(getIdentifier());
        builder.append(", [Members: \n\t\t\t\t");
        final StringList list = new StringList();
        for (final RelationMember member : this)
        {
            list.add(betweenEachMemberAndRelation + betweenEachMember + member.toString());
        }
        builder.append(list.join(", \n\t\t\t\t"));
        builder.append("\n\t\t\t");
        builder.append(betweenEachMemberAndRelation);
        builder.append("], ");
        builder.append(tagString());
        builder.append("]");
        return builder.toString();
    }

    /**
     * "Flattens" the relation by returning the set of non-Relation members. Adds any non-Relation
     * members to the set, then loops on any Relation members to add their non-Relation members as
     * well. Keeps track of Relations whose identifiers have already been operated on, so that
     * recursively defined relations don't cause problems.
     *
     * @return a Set of AtlasObjects all related to this Relation, with no Relations.
     */
    public Set<AtlasObject> flatten()
    {
        final Set<AtlasObject> relationMembers = new HashSet<>();
        final Deque<AtlasObject> toProcess = new LinkedList<>();
        final Set<Long> relationsSeen = new HashSet<>();
        AtlasObject polledMember;

        toProcess.add(this);
        while (!toProcess.isEmpty())
        {
            polledMember = toProcess.poll();
            if (polledMember instanceof Relation)
            {
                if (relationsSeen.contains(polledMember.getIdentifier()))
                {
                    continue;
                }
                ((Relation) polledMember).members()
                        .forEach(member -> toProcess.add(member.getEntity()));
                relationsSeen.add(polledMember.getIdentifier());
            }
            else
            {
                relationMembers.add(polledMember);
            }
        }
        return relationMembers;
    }

    /**
     * @return the {@link RelationBean} representation of the Relation
     */
    public RelationBean getBean()
    {
        final RelationBean bean = new RelationBean();
        for (final RelationMember member : this)
        {
            final AtlasEntity entity = member.getEntity();
            bean.addItem(entity.getIdentifier(), member.getRole(), entity.getType());
        }
        return bean;
    }

    @Override
    public ItemType getType()
    {
        return ItemType.RELATION;
    }

    public boolean hasMultiPolygonMembers(final Ring ring)
    {
        if (isMultiPolygon())
        {
            for (final RelationMember member : members())
            {
                switch (ring)
                {
                    case OUTER:
                        if (RelationTypeTag.MULTIPOLYGON_ROLE_OUTER.equals(member.getRole()))
                        {
                            return true;
                        }
                        break;
                    case INNER:
                        if (RelationTypeTag.MULTIPOLYGON_ROLE_INNER.equals(member.getRole()))
                        {
                            return true;
                        }
                        break;
                    default:
                        throw new CoreException("Unknown ring type: {}", ring);
                }
            }
        }
        return false;
    }

    @Override
    public boolean intersects(final GeometricSurface surface)
    {
        return intersectsInternal(surface, new LinkedHashSet<>());
    }

    public boolean isMultiPolygon()
    {
        return Validators.isOfType(this, RelationTypeTag.class, RelationTypeTag.MULTIPOLYGON,
                RelationTypeTag.BOUNDARY);
    }

    @Override
    public Iterator<RelationMember> iterator()
    {
        return members().iterator();
    }

    /**
     * @return All the members of this specific (potentially sliced) relation.
     */
    public abstract RelationMemberList members();

    /**
     * In case a {@link Relation} is spanning multiple {@link Atlas}, keep track of the parent OSM
     * relation identifier to be able to match it back to other sliced relations.
     *
     * @return The OSM identifier
     */
    public abstract Long osmRelationIdentifier();

    @Override
    public String toDiffViewFriendlyString()
    {
        final String relationsString = this.parentRelationsAsDiffViewFriendlyString();

        final StringBuilder builder = new StringBuilder();
        builder.append("[Relation: id=");
        builder.append(getIdentifier());
        builder.append(", [Members: ");
        final StringList list = new StringList();
        for (final RelationMember member : this)
        {
            list.add(member.toString());
        }
        builder.append(list.join(", "));
        builder.append("], ");
        builder.append("relations=(" + relationsString + "), ");
        builder.append(tagString());
        builder.append("]");

        return builder.toString();
    }

    @Override
    public LocationIterableProperties toGeoJsonBuildingBlock()
    {
        final Map<String, String> tags = getTags();
        tags.put("identifier", String.valueOf(getIdentifier()));
        tags.put("osmIdentifier", String.valueOf(getOsmIdentifier()));
        tags.put("itemType", String.valueOf(getType()));
        tags.put("relation", this.toSimpleString());

        final Optional<String> shardName = getAtlas().metaData().getShardName();
        shardName.ifPresent(shard -> tags.put("shard", shard));

        return new GeoJsonBuilder.LocationIterableProperties(bounds().center(), tags);
    }

    public String toSimpleString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("[Relation: id=");
        builder.append(getIdentifier());
        builder.append(", [Members: ");
        final StringList list = new StringList();
        for (final RelationMember member : this)
        {
            list.add(member.toString());
        }
        builder.append(list.join(", "));
        builder.append("], ");
        builder.append(tagString());
        builder.append("]");
        return builder.toString();
    }

    @Override
    public String toString()
    {
        return configurableString("", "");
    }

    @Override
    public byte[] toWkb()
    {
        throw new UnsupportedOperationException("Relation.toWkb not implemented yet.");
    }

    @Override
    public String toWkt()
    {
        return WktPrintable.toWktCollection(leafMembers().collect(Collectors.toList()));
    }

    /**
     * Return {@code true} if this Relation has all members fully within the supplied
     * {@link GeometricSurface}.
     *
     * @param surface
     *            The {@link GeometricSurface} to check for
     * @return {@code true} if the relation has all members within the given
     *         {@link GeometricSurface}
     */
    public boolean within(final GeometricSurface surface)
    {
        return withinInternal(surface, new LinkedHashSet<>());
    }

    /**
     * Avoid stack overflows in case a relation has looping members. This should never happen with a
     * {@link PackedAtlas} but could happen when two {@link Atlas} are combined into a
     * {@link MultiAtlas}.
     *
     * @param parentRelationIdentifiers
     *            The identifiers of the parent relations that have already been visited.
     * @return The bounds
     */
    protected Rectangle boundsInternal(final Set<Long> parentRelationIdentifiers)
    {
        if (this.members().isEmpty())
        {
            return Rectangle.MINIMUM;
        }
        final List<Located> itemsToConsider = new ArrayList<>();
        for (final AtlasEntity member : Iterables.stream(this).map(RelationMember::getEntity)
                .filter(Objects::nonNull))
        {
            if (member instanceof Relation)
            {
                final long identifier = member.getIdentifier();
                if (parentRelationIdentifiers.contains(identifier))
                {
                    continue;
                }
                else
                {
                    parentRelationIdentifiers.add(identifier);
                    itemsToConsider
                            .add(((Relation) member).boundsInternal(parentRelationIdentifiers));
                }
            }
            else
            {
                final Rectangle bounds = member.bounds();
                if (bounds != null)
                {
                    itemsToConsider.add(bounds);
                }
            }
        }
        if (Iterables.size(itemsToConsider) == 0)
        {
            return Rectangle.MINIMUM;
        }
        return Rectangle.forLocated(itemsToConsider);
    }

    /**
     * Avoid stack overflows in case a relation has looping members. This should never happen with a
     * {@link PackedAtlas} but could happen when two {@link Atlas} are combined into a
     * {@link MultiAtlas}.
     *
     * @param surface
     *            The {@link GeometricSurface} to check for
     * @param parentRelationIdentifiers
     *            The identifiers of the parent relations that have already been visited.
     * @return True if the relation intersects the geometricSurface
     */
    protected boolean intersectsInternal(final GeometricSurface surface,
            final Set<Long> parentRelationIdentifiers)
    {
        for (final RelationMember member : this)
        {
            final AtlasEntity entity = member.getEntity();
            if (entity instanceof Relation)
            {
                final long identifier = entity.getIdentifier();
                if (parentRelationIdentifiers.contains(identifier))
                {
                    continue;
                }
                else
                {
                    parentRelationIdentifiers.add(identifier);
                    if (((Relation) entity).intersectsInternal(surface, parentRelationIdentifiers))
                    {
                        return true;
                    }
                }
            }
            else if (entity.intersects(surface))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Avoid stack overflows in case a relation has looping members. This should never happen with a
     * {@link PackedAtlas} but could happen when two {@link Atlas} are combined into a
     * {@link MultiAtlas}.
     *
     * @param surface
     *            The {@link GeometricSurface} to check for
     * @param parentRelationIdentifiers
     *            The identifiers of the parent relations that have already been visited.
     * @return {@code true} if the relation has all members within the given
     *         {@link GeometricSurface}
     */
    protected boolean withinInternal(final GeometricSurface surface,
            final Set<Long> parentRelationIdentifiers)
    {
        for (final RelationMember member : this)
        {
            final AtlasEntity entity = member.getEntity();
            if (entity instanceof Relation)
            {
                final long identifier = entity.getIdentifier();
                if (parentRelationIdentifiers.contains(identifier))
                {
                    continue;
                }
                else
                {
                    parentRelationIdentifiers.add(identifier);
                    if (!((Relation) entity).withinInternal(surface, parentRelationIdentifiers))
                    {
                        return false;
                    }
                }
            }
            else if (isUnenclosedNonRelationEntity(surface, entity))
            {
                return false;
            }
        }
        return true;
    }

    private boolean isUnenclosedNonRelationEntity(final GeometricSurface surface,
            final AtlasEntity entity)
    {
        switch (entity.getType())
        {
            case NODE:
            case POINT:
                return isUnenclosedLocationItem(entity, surface);
            case EDGE:
            case LINE:
                return isUnenclosedLineItem(entity, surface);
            case AREA:
                return isUnenclosedArea(entity, surface);
            case RELATION:
            default:
                throw new CoreException("Relations not supported in this method");
        }
    }

    private boolean isUnenclosedArea(final AtlasEntity entity, final GeometricSurface surface)
    {
        return entity instanceof Area
                && !surface.fullyGeometricallyEncloses(((Area) entity).asPolygon());
    }

    private boolean isUnenclosedLocationItem(final AtlasEntity entity,
            final GeometricSurface surface)
    {
        return entity instanceof LocationItem
                && !surface.fullyGeometricallyEncloses(((LocationItem) entity).getLocation());
    }

    private boolean isUnenclosedLineItem(final AtlasEntity entity, final GeometricSurface surface)
    {
        return entity instanceof LineItem
                && !surface.fullyGeometricallyEncloses(((LineItem) entity).asPolyLine());
    }

    /**
     * We explicitly want to add member metadata to the properties of Relations, but only when we
     * are serializing relation entities. Overriding geoJsonProperties() would not work properly,
     * because that gets called when you are listing metadata about relations a non-relation entity
     * may be in. Calling this method, only in this class, avoids a recursive call that would list
     * members of relations in relation metadata for non-relation entities.
     *
     * @param properties
     *            The JsonObject properties object we will add member metadata to.
     */
    private void addMembersToProperties(final JsonObject properties)
    {
        final RelationMemberList members = members();
        final JsonArray membersArray = new JsonArray();
        properties.add("members", membersArray);
        for (final RelationMember member : members)
        {
            final JsonObject memberObject = new JsonObject();
            membersArray.add(memberObject);
            final AtlasEntity entity = member.getEntity();
            if (entity != null)
            {
                final long identifier = entity.getIdentifier();
                memberObject.addProperty(GeoJsonUtils.IDENTIFIER, identifier);
                memberObject.addProperty("itemType", entity.getType().name());
            }
            else
            {
                // We shouldn't get here, but if we do, let's know about it in the data...
                memberObject.addProperty(GeoJsonUtils.IDENTIFIER, "MISSING");
                logger.warn("Missing identifier for relation entity: Relation ID: {}",
                        getIdentifier());
            }

            // Sometimes a member doesnt have a role. That's normal.
            final String role = member.getRole();
            if (role != null)
            {
                // And sometimes the role is "", but we should keep it that way...
                memberObject.addProperty("role", role);
            }
        }
    }

    private Stream<AtlasItem> leafMembers()
    {
        final Stream<AtlasItem> nonRelationMembers = members().stream()
                .map(RelationMember::getEntity).filter(entity -> !(entity instanceof Relation))
                .map(entity -> (AtlasItem) entity);
        final Stream<AtlasItem> relationMembers = members().stream().map(RelationMember::getEntity)
                .filter(entity -> entity instanceof Relation).map(entity -> (Relation) entity)
                .flatMap(Relation::leafMembers);
        return Stream.concat(nonRelationMembers, relationMembers);
    }
}
