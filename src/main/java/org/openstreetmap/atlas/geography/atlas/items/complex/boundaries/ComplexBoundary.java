package org.openstreetmap.atlas.geography.atlas.items.complex.boundaries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.complex.ComplexEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.geojson.GeoJsonObject;
import org.openstreetmap.atlas.locale.IsoCountry;
import org.openstreetmap.atlas.tags.AdministrativeLevelTag;
import org.openstreetmap.atlas.tags.BoundaryTag;
import org.openstreetmap.atlas.tags.Iso31662CountryTag;
import org.openstreetmap.atlas.tags.Iso31663CountryTag;
import org.openstreetmap.atlas.tags.Iso3166DefaultCountryTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A representation of the Administrative Boundaries defined in the {@link BoundaryTag}.
 *
 * @author matthieun
 */
public class ComplexBoundary extends ComplexEntity
{
    private static final long serialVersionUID = 3836743004772506528L;

    private static final Logger logger = LoggerFactory.getLogger(ComplexBoundary.class);
    private static final RelationOrAreaToMultiPolygonConverter RELATION_OR_AREA_TO_MULTI_POLYGON_CONVERTER = new RelationOrAreaToMultiPolygonConverter();

    private MultiPolygon outline;
    private Integer administrativeLevel;
    private Iterable<IsoCountry> countries;
    // The sub-areas as defined by the relation member role subarea
    private Set<ComplexBoundary> subAreas = new HashSet<>();

    private final List<ComplexEntityError> invalidations = new ArrayList<>();
    private final Optional<Integer> administrativeLevelFilter;
    private final boolean withSubAreas;

    protected ComplexBoundary(final AtlasEntity source, final boolean withSubAreas,
            final Optional<Integer> administrativeLevelFilter)
    {
        super(source);
        this.administrativeLevelFilter = administrativeLevelFilter;
        this.withSubAreas = withSubAreas;
        try
        {
            this.populateAdministrativeLevelAndOutline();
            if (withSubAreas)
            {
                this.populateSubAreas();
                for (final ComplexBoundary boundary : this.subAreas)
                {
                    if (!boundary.isValid())
                    {
                        setInvalidReason("Some subAreas are invalid",
                                new CoreException("Some subArea(s) are invalid: {}", boundary,
                                        boundary.getError().orElse(null).getException()));
                    }
                }
            }
        }
        catch (final Exception e)
        {
            setInvalidReason("Unable to create complex boundary from " + source, e);
            logger.warn("Unable to create complex boundary from {}, id {}. Reason: {}",
                    source.getType(), source.getIdentifier(), e.getMessage());
            return;
        }
    }

    public GeoJsonObject asGeoJson()
    {
        return this.outline.asGeoJson();
    }

    public int getAdministrativeLevel()
    {
        return this.administrativeLevel;
    }

    @Override
    public List<ComplexEntityError> getAllInvalidations()
    {
        return this.invalidations;
    }

    public Iterable<IsoCountry> getCountries()
    {
        return this.countries;
    }

    public MultiPolygon getOutline()
    {
        return this.outline;
    }

    public Set<ComplexBoundary> getSubAreas()
    {
        return this.subAreas;
    }

    public boolean hasCountryCode()
    {
        return isValid() && Iterables.size(this.countries) > 0;
    }

    public void removeOuter(final Polygon outerToRemove)
    {
        final MultiMap<Polygon, Polygon> outersToInners = new MultiMap<>();
        this.outline.outers().forEach(outer ->
        {
            final List<Polygon> innersForThisOuter = this.outline.innersOf(outer);
            outersToInners.put(outer, innersForThisOuter);
        });
        outersToInners.remove(outerToRemove);
        setOutline(new MultiPolygon(outersToInners));
    }

    public void setOutline(final MultiPolygon outline)
    {
        this.outline = outline;
    }

    @Override
    public String toString()
    {
        return toString("");
    }

    protected String toString(final String header)
    {
        return String.format(
                header + "[ComplexBoundary: Source = [%s, ID = %s]\n\t" + header
                        + "Administrative Level = %s\n\t" + header + "Countries = %s\n\t" + header
                        + "Name = %s\n\t" + header + "Outline = %s\n\t" + header
                        + "Children = \n%s\n" + header + "]",
                this.getSource().getType(), this.getSource().getIdentifier(),
                this.administrativeLevel, this.countries, NameTag.getNameOf(getSource()).orElse(""),
                this.outline == null ? "MISSING" : this.outline.toReadableString(),
                new StringList(
                        this.subAreas.stream().map(subArea -> subArea.toString(header + "\t"))
                                .collect(Collectors.toList())).join("\n"));
    }

    /**
     * Find the administrative level and the outline of the boundary
     */
    private void populateAdministrativeLevelAndOutline()
    {
        final AtlasEntity source = getSource();
        if (source instanceof Relation || source instanceof Area)
        {
            final Optional<Integer> administrativeLevelOption = AdministrativeLevelTag
                    .getAdministrativeLevel(source);
            this.administrativeLevel = administrativeLevelOption.orElseThrow(
                    () -> new CoreException("Invalid or missing administrative level for {} {}",
                            source.getType(), source.getIdentifier()));
            // Store countries in a set to avoid duplicates.
            this.countries = Iterables
                    .stream(new MultiIterable<>(Iso31663CountryTag.all(source),
                            Iso31662CountryTag.all(source), Iso3166DefaultCountryTag.all(source)))
                    .collectToSet();
            // Don't bother if the admin level is not the one expected
            if (this.administrativeLevelFilter.isPresent())
            {
                if (!this.administrativeLevel.equals(this.administrativeLevelFilter.get()))
                {
                    throw new CoreException("Administrative Level {} is not being queried.",
                            this.administrativeLevel);
                }
            }
            this.outline = RELATION_OR_AREA_TO_MULTI_POLYGON_CONVERTER.convert(source);
        }
        else
        {
            throw new CoreException("Supports only relations and areas.");
        }
    }

    /**
     * Find the sub-areas if any
     */
    private void populateSubAreas()
    {
        this.subAreas = new HashSet<>();
        final AtlasEntity source = getSource();
        if (source instanceof Relation)
        {
            for (final RelationMember member : ((Relation) source).members())
            {
                final AtlasEntity childEntity = member.getEntity();
                if (BoundaryTag.isAdministrative(childEntity)
                        && RelationTypeTag.ADMINISTRATIVE_BOUNDARY_ROLE_SUB_AREA
                                .equals(member.getRole()))
                {
                    final ComplexBoundary child = new ComplexBoundary(childEntity,
                            this.withSubAreas, this.administrativeLevelFilter);
                    this.subAreas.add(child);
                    if (!child.isValid())
                    {
                        this.invalidations.addAll(child.getAllInvalidations());
                    }
                }
            }
        }
    }
}
