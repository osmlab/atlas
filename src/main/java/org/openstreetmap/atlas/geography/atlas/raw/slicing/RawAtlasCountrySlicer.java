package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.Set;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.changeset.RelationChangeSet;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.changeset.SimpleChangeSet;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.locale.IsoCountry;

/**
 * Main entry point to initiate raw {@link Atlas} country-slicing.
 *
 * @author mgostintsev
 */
public class RawAtlasCountrySlicer
{
    // The countries to be sliced with
    private final CountryBoundaryMap countryBoundaryMap;

    // The boundaries used for slicing
    private final Set<IsoCountry> countries;

    /**
     * Slices against the given country set and boundary map. Note: Will assume the entire world to
     * be the target MultiPolygon to use.
     *
     * @param countries
     *            The Set of countries to be sliced against
     * @param countryBoundaryMap
     *            The {@link CountryBoundaryMap} to use when slicing
     */
    public RawAtlasCountrySlicer(final Set<IsoCountry> countries,
            final CountryBoundaryMap countryBoundaryMap)
    {
        this.countries = countries;
        this.countryBoundaryMap = countryBoundaryMap;
    }

    /**
     * Given a raw {@link Atlas}, slice all entities ({@link Point}s, {@link Line}s and
     * {@link Relation}s).
     *
     * @param rawAtlas
     *            The {@link Atlas} to slice
     * @return the sliced {@link Atlas}
     */
    public Atlas slice(final Atlas rawAtlas)
    {
        // Keep track of changes made during Point/Line and Relation slicing
        final SimpleChangeSet slicedPointAndLineChanges = new SimpleChangeSet();
        final RelationChangeSet slicedRelationChanges = new RelationChangeSet();

        // Mapping between Coordinate and created Temporary Point identifiers. This is to avoid
        // duplicate points at the same locations and to allow fast lookup to construct new lines
        // requiring the temporary point as a Line shape point
        final CoordinateToNewPointMapping newPointCoordinates = new CoordinateToNewPointMapping();

        final RawAtlasSlicer pointAndLineSlicer = new RawAtlasPointAndLineSlicer(this.countries,
                this.countryBoundaryMap, rawAtlas, slicedPointAndLineChanges, newPointCoordinates);
        final Atlas slicedPointsAndLinesAtlas = pointAndLineSlicer.slice();

        final RawAtlasSlicer relationSlicer = new RawAtlasRelationSlicer(slicedPointsAndLinesAtlas,
                this.countries, this.countryBoundaryMap, slicedPointAndLineChanges,
                slicedRelationChanges, newPointCoordinates);
        return relationSlicer.slice();
    }
}
