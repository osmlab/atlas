package org.openstreetmap.atlas.geography.atlas.change;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean.RelationBeanItem;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.geography.converters.jts.JtsPrecisionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Relation} that references a {@link ChangeAtlas}. That {@link Relation} makes sure that all
 * the member entitiess are "Change" types, and that all the parent {@link Relation}s are
 * {@link ChangeRelation}s.
 * <p>
 * NOSONAR here to avoid "Subclasses that add fields should override "equals" (squid:S2160)". Here
 * the equals from the parent works.
 *
 * @author matthieun
 */
public class ChangeRelation extends Relation // NOSONAR
{
    private static final long serialVersionUID = 4353679260691518275L;
    private static final Logger logger = LoggerFactory.getLogger(ChangeRelation.class);

    private final Relation source;
    private final Relation override;

    // Computing ChangeRelation members is very expensive, so we cache it here.
    private transient RelationMemberList membersCache;
    private transient Object membersCacheLock = new Object();

    private transient Optional<MultiPolygon> geometryCache;
    private transient Object geometryCacheLock = new Object();

    // Computing Parent Relations is very expensive, so we cache it here.
    private transient Set<Relation> relationsCache;
    private transient Object relationsCacheLock = new Object();

    protected ChangeRelation(final ChangeAtlas atlas, final Relation source,
            final Relation override)
    {
        super(atlas);
        this.source = source;
        this.override = override;
    }

    @Override
    public RelationMemberList allKnownOsmMembers()
    {
        return membersFor(
                attribute(Relation::allKnownOsmMembers, "all known osm members").asBean());
    }

    @Override
    public List<Relation> allRelationsWithSameOsmIdentifier()
    {
        return attribute(Relation::allRelationsWithSameOsmIdentifier,
                "all relations with same osm identifier").stream()
                        .map(relation -> getChangeAtlas().relation(relation.getIdentifier()))
                        .collect(Collectors.toList());
    }

    @Override
    public Optional<MultiPolygon> asMultiPolygon()
    {
        if (!this.isGeometric())
        {
            return Optional.ofNullable(null);
        }
        final Supplier<Optional<MultiPolygon>> creator = () ->
        {
            if (this.override != null && ((CompleteRelation) this.override).isOverrideGeometry())
            {
                return this.override.asMultiPolygon();
            }
            else if (this.source != null)
            {
                final Optional<MultiPolygon> sourceJtsGeometry = ChangeEntity
                        .getAttribute(this.source, Relation::asMultiPolygon);
                if (sourceJtsGeometry.isPresent())
                {
                    // don't do anything to invalid geom
                    if (!sourceJtsGeometry.get().isValid())
                    {
                        return sourceJtsGeometry;
                    }
                    final org.locationtech.jts.geom.MultiPolygon sourceGeom;
                    sourceGeom = sourceJtsGeometry.get();

                    final Set<LineString> removed = removedMembers();
                    final Set<LineString> added = addedMembers();

                    if (removed.isEmpty() && added.isEmpty())
                    {
                        return sourceJtsGeometry;
                    }

                    Geometry updatedGeometry = convertMultiPolygonToLineCollection(sourceGeom);

                    for (final Geometry memberGeometry : removed)
                    {
                        updatedGeometry = OverlayNG.overlay(updatedGeometry, memberGeometry,
                                OverlayNG.DIFFERENCE);
                    }

                    for (final Geometry memberGeometry : added)
                    {
                        updatedGeometry = OverlayNG.overlay(updatedGeometry, memberGeometry,
                                OverlayNG.UNION);
                    }

                    final Polygonizer update = new Polygonizer(true);
                    update.add(updatedGeometry);
                    final Polygon[] polygons = (Polygon[]) update.getPolygons()
                            .toArray(new Polygon[update.getPolygons().size()]);
                    updatedGeometry = new MultiPolygon(polygons,
                            JtsPrecisionManager.getGeometryFactory());
                    if (!updatedGeometry.isValid())
                    {
                        final Geometry fixed = GeometryFixer.fix(updatedGeometry);
                        if (fixed instanceof Polygon)
                        {
                            updatedGeometry = new MultiPolygon(new Polygon[] { (Polygon) fixed },
                                    JtsPrecisionManager.getGeometryFactory());
                        }
                        else if (fixed instanceof MultiPolygon)
                        {
                            updatedGeometry = fixed;
                        }
                        else
                        {
                            throw new CoreException(
                                    "Fixed geometry for relation {} included unexpected type! {}",
                                    this.getIdentifier(), fixed.toText());
                        }
                        logger.error("Had to fix geometry for relation {}", this.getIdentifier());
                    }
                    return Optional.ofNullable((MultiPolygon) updatedGeometry);
                }
            }
            else if (this.override != null
                    && !((CompleteRelation) this.override).asMultiPolygon().isPresent())
            {
                // new ChangeRelation that never had geometry-- reconstruct it
                ((CompleteRelation) this.override).updateGeometry();
            }
            return attribute(Relation::asMultiPolygon, "geometry");
        };
        return ChangeEntity.getOrCreateCache(this.geometryCache,
                cache -> this.geometryCache = cache, this.geometryCacheLock, creator);

    }

    @Override
    public long getIdentifier()
    {
        return attribute(Relation::getIdentifier, "identifier");
    }

    @Override
    public Map<String, String> getTags()
    {
        return attribute(Relation::getTags, "tags");
    }

    @Override
    public RelationMemberList members()
    {
        final Supplier<RelationMemberList> creator = () ->
        {
            final List<RelationMemberList> availableMemberLists = allAvailableAttributes(
                    Relation::members, "members");
            final RelationBean mergedMembersBean = availableMemberLists.stream()
                    .map(RelationMemberList::asBean)
                    .reduce(new RelationBean(), RelationBean::merge);
            final RelationBean filteredAndMergedMembersBean = new RelationBean();
            mergedMembersBean.forEach(relationBeanItem ->
            {
                if (getChangeAtlas().entity(relationBeanItem.getIdentifier(),
                        relationBeanItem.getType()) != null)
                {
                    filteredAndMergedMembersBean.addItem(relationBeanItem);
                }
            });
            return membersFor(filteredAndMergedMembersBean);
        };

        return ChangeEntity.getOrCreateCache(this.membersCache, cache -> this.membersCache = cache,
                this.membersCacheLock, creator);
    }

    @Override
    public Long osmRelationIdentifier()
    {
        return attribute(Relation::osmRelationIdentifier, "osm relation identifier");
    }

    public boolean preservedValidGeometry()
    {
        if (this.source != null && (!addedMembers().isEmpty() || !removedMembers().isEmpty())
                && this.source.asMultiPolygon().isPresent()
                && !this.source.asMultiPolygon().isEmpty()
                && this.source.asMultiPolygon().get().isValid())
        {
            return this.asMultiPolygon().isPresent() && !this.asMultiPolygon().get().isEmpty()
                    && this.asMultiPolygon().get().isValid();
        }
        return true;
    }

    @Override
    public Set<Relation> relations()
    {
        final Supplier<Set<Relation>> creator = () -> ChangeEntity
                .filterRelations(attribute(AtlasEntity::relations, "relations"), getChangeAtlas());
        return ChangeEntity.getOrCreateCache(this.relationsCache,
                cache -> this.relationsCache = cache, this.relationsCacheLock, creator);
    }

    private Set<LineString> addedMembers()
    {
        if (this.override == null)
        {
            return new HashSet<>();
        }
        return ((CompleteRelation) this.override).getAddedGeometry();
    }

    private <T extends Object> List<T> allAvailableAttributes(
            final Function<Relation, T> memberExtractor, final String name)
    {
        return ChangeEntity.getAttributeAndOptionallyBackup(this.source, this.override,
                memberExtractor, name);
    }

    private <T extends Object> T attribute(final Function<Relation, T> memberExtractor,
            final String name)
    {
        return ChangeEntity.getAttributeOrBackup(this.source, this.override, memberExtractor, name);
    }

    private GeometryCollection convertMultiPolygonToLineCollection(final MultiPolygon multipolygon)
    {
        final List<LineString> linestrings = new ArrayList<>();
        for (int i = 0; i < multipolygon.getNumGeometries(); i++)
        {
            final Polygon part = (Polygon) multipolygon.getGeometryN(i);
            linestrings.add(part.getExteriorRing());
            for (int j = 0; j < part.getNumInteriorRing(); j++)
            {
                linestrings.add(part.getInteriorRingN(j));
            }
        }
        Geometry[] geometries = new Geometry[linestrings.size()];
        geometries = linestrings.toArray(geometries);
        return new GeometryCollection(geometries, JtsPrecisionManager.getGeometryFactory());
    }

    private ChangeAtlas getChangeAtlas()
    {
        return (ChangeAtlas) getAtlas();
    }

    private RelationMemberList membersFor(final RelationBean bean)
    {
        if (bean == null)
        {
            return null;
        }
        final List<RelationMember> memberList = new ArrayList<>();
        for (final RelationBeanItem item : bean)
        {
            final AtlasEntity memberChangeEntity = getChangeAtlas().entity(item.getIdentifier(),
                    item.getType());
            if (memberChangeEntity != null)
            {
                memberList.add(
                        new RelationMember(item.getRole(), memberChangeEntity, getIdentifier()));
            }
        }
        return new RelationMemberList(memberList);
    }

    private Set<LineString> removedMembers()
    {
        if (this.override == null)
        {
            return new HashSet<>();
        }
        return ((CompleteRelation) this.override).getRemovedGeometry();
    }
}
