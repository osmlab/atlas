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
    // Keep track of changes made during Point/Line and Relation slicing
    private final SimpleChangeSet slicedPointAndLineChanges = new SimpleChangeSet();
    private final RelationChangeSet slicedRelationChanges = new RelationChangeSet();

    // The countries and boundaries used for slicing
    private final CountryBoundaryMap countryBoundaryMap;
    private final Set<IsoCountry> countries;

    // Mapping between Coordinate and created Temporary Point identifiers. This is to avoid
    // duplicate points at the same locations and to allow fast lookup to construct new lines
    // requiring the temporary point as a Line shape point
    private final CoordinateToNewPointMapping newPointCoordinates = new CoordinateToNewPointMapping();

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
        final RawAtlasSlicer pointAndLineSlicer = new RawAtlasPointAndLineSlicer(this.countries,
                this.countryBoundaryMap, rawAtlas, this.slicedPointAndLineChanges,
                this.newPointCoordinates);
        final Atlas slicedPointsAndLinesAtlas = pointAndLineSlicer.slice();

        final RawAtlasSlicer relationSlicer = new RawAtlasRelationSlicer(slicedPointsAndLinesAtlas,
                this.countries, this.countryBoundaryMap, this.slicedPointAndLineChanges,
                this.slicedRelationChanges, this.newPointCoordinates);
        return relationSlicer.slice();
    }
}
