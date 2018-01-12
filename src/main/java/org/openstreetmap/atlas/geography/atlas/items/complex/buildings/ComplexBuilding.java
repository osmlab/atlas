package org.openstreetmap.atlas.geography.atlas.items.complex.buildings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.complex.ComplexEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A complex building, that can be made of Parts, and have holes (MultiPolygon). It can also be very
 * simple and be made of only one {@link Area}.
 *
 * @author matthieun
 */
public class ComplexBuilding extends ComplexEntity
{
    private static final long serialVersionUID = 5351464852316720525L;

    private static final Logger logger = LoggerFactory.getLogger(ComplexBuilding.class);
    private static final RelationOrAreaToMultiPolygonConverter RELATION_OR_AREA_TO_MULTI_POLYGON_CONVERTER = new RelationOrAreaToMultiPolygonConverter();
    private static final HeightConverter HEIGHT_CONVERTER = new HeightConverter();

    private final Set<BuildingPart> buildingParts;
    private MultiPolygon outline = null;
    private AtlasEntity outlineSource;

    private Set<Long> containedOSMIDs;

    protected ComplexBuilding(final AtlasEntity source)
    {
        super(source);
        this.buildingParts = new HashSet<>();
        this.containedOSMIDs = new HashSet<>();
        try
        {
            this.populateBuildingPartsAndOutline();
        }
        catch (final Exception e)
        {
            setInvalidReason("Unable to create complex building", e);
            logger.warn("Unable to create complex building from {}", source, e);
            return;
        }
    }

    public boolean containsOSMIdentifier(final long identifier)
    {
        return this.containedOSMIDs.contains(identifier);
    }

    @Override
    public List<ComplexEntityError> getAllInvalidations()
    {
        final List<ComplexEntityError> returnValue = new ArrayList<>();
        if (!isValid())
        {
            getError().ifPresent(returnValue::add);
            this.buildingParts.stream().filter(part -> !part.isValid())
                    .map(ComplexEntity::getAllInvalidations).flatMap(List::stream)
                    .forEach(returnValue::add);
        }
        return returnValue;
    }

    public Set<BuildingPart> getBuildingParts()
    {
        return this.buildingParts;
    }

    /**
     * @return The outline of the building.
     */
    public MultiPolygon getOutline()
    {
        return this.outline;
    }

    /**
     * @return The {@link AtlasEntity} representing the outline of the building
     */
    public AtlasEntity getOutlineSource()
    {
        return this.outlineSource;
    }

    @Override
    public boolean isValid()
    {
        if (super.isValid())
        {
            for (final BuildingPart part : this.buildingParts)
            {
                if (!part.isValid())
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * @return The building's top height
     */
    public Optional<Distance> topHeight()
    {
        Map<String, String> tags = getSource().getTags();
        String heightTag = tags.get("height");
        try
        {
            if (heightTag != null)
            {
                return Optional.of(HEIGHT_CONVERTER.convert(heightTag));
            }
            tags = this.outlineSource.getTags();
            heightTag = tags.get("height");
            if (heightTag != null)
            {
                return Optional.of(HEIGHT_CONVERTER.convert(heightTag));
            }
        }
        catch (final Exception e)
        {
            logger.warn("Invalid height {} for building id {}", heightTag,
                    getSource().getIdentifier());
        }
        return Optional.empty();
    }

    @Override
    public String toString()
    {
        final StringBuilder parts = new StringBuilder();
        for (final BuildingPart part : this.buildingParts)
        {
            parts.append(part);
        }
        return String.format("[ComplexBuilding:\n\tOutline = %s,\n\tParts = %s]",
                this.outline == null ? "MISSING" : this.outline.toReadableString(),
                parts.toString());
    }

    protected void populateBuildingPartsAndOutline()
    {
        this.containedOSMIDs.add(getOsmIdentifier());
        final AtlasEntity source = getSource();
        if (source instanceof Area)
        {
            // Simple case, yay!
            this.outline = RELATION_OR_AREA_TO_MULTI_POLYGON_CONVERTER.convert(source);
            this.outlineSource = source;
        }
        else if (source instanceof Relation)
        {
            final Relation relation = (Relation) source;
            final String type = relation.tag(RelationTypeTag.KEY);
            // Two cases here. The relation can be a multipolygon (in case there are just holes and
            // no parts) or a building relation, in case there are building parts.
            if (RelationTypeTag.MULTIPOLYGON_TYPE.equals(type))
            {
                // 1. Multipolygon. Relatively easy, there will be no building parts.
                this.outline = RELATION_OR_AREA_TO_MULTI_POLYGON_CONVERTER.convert(relation);
                this.outlineSource = relation;
            }
            else if (BuildingTag.KEY.equals(type))
            {
                // 2. This relation is of an OSM 3D building. It should contain a member Area that
                // is the outline, tagged as a building. It should also contain zero to many
                // building:part=yes areas.
                // 2.a. Loop through the roles and find the outline
                for (final RelationMember member : relation.members())
                {
                    this.containedOSMIDs.add(member.getEntity().getOsmIdentifier());
                    if (RelationTypeTag.BUILDING_ROLE_OUTLINE.equals(member.getRole()))
                    {
                        this.outline = RELATION_OR_AREA_TO_MULTI_POLYGON_CONVERTER
                                .convert(member.getEntity());
                        this.outlineSource = member.getEntity();
                    }
                }
                if (this.outline == null)
                {
                    throw new CoreException(
                            "Building part relation does not contain a building outline member");
                }
                for (final RelationMember member : relation.members())
                {
                    this.containedOSMIDs.add(member.getEntity().getOsmIdentifier());
                    if (RelationTypeTag.BUILDING_ROLE_PART.equals(member.getRole()))
                    {
                        this.buildingParts.add(new BuildingPart(member.getEntity()));
                    }
                }
            }
            else
            {
                throw new CoreException(
                        "A building relation can only be of type=multipolygon or type=building");
            }
        }
        else
        {
            throw new CoreException(
                    "A building can only be made of a Relation or an Area. This was a {}",
                    source.getClass().getName());
        }
        if (this.outline.outers().isEmpty())
        {
            throw new CoreException("A building cannot have no geometry.");
        }

        try
        {
            // By fetching the surface we calculate the area: if that area is negative we throw an
            // exception
            this.outline.surface();
        }
        catch (final IllegalArgumentException oops)
        {
            throw new CoreException("Negative surface area", oops);
        }
    }
}
