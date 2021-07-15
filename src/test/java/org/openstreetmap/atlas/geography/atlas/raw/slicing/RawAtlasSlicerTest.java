package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.Iterator;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.atlas.items.complex.buildings.ComplexBuilding;
import org.openstreetmap.atlas.geography.atlas.items.complex.buildings.ComplexBuildingFinder;
import org.openstreetmap.atlas.geography.atlas.items.complex.water.ComplexWaterEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.water.finder.ComplexWaterEntityFinder;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.CountrySlicingIdentifierFactory;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.SyntheticBoundaryNodeTag;
import org.openstreetmap.atlas.tags.SyntheticGeometrySlicedTag;
import org.openstreetmap.atlas.tags.SyntheticInvalidGeometryTag;
import org.openstreetmap.atlas.tags.SyntheticInvalidMultiPolygonRelationMembersRemovedTag;
import org.openstreetmap.atlas.tags.SyntheticRelationMemberAdded;
import org.openstreetmap.atlas.tags.SyntheticSyntheticRelationMemberTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * Tests covering the slicing process
 *
 * @author samg
 */

public class RawAtlasSlicerTest
{
    private static final CountryBoundaryMap boundary;
    private static final RelationOrAreaToMultiPolygonConverter converter;
    private static final JtsMultiPolygonToMultiPolygonConverter jtsConverter;
    static
    {
        boundary = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> RawAtlasSlicerTest.class
                        .getResourceAsStream("CIV_GIN_LBR_osm_boundaries.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));
        converter = new RelationOrAreaToMultiPolygonConverter();
        jtsConverter = new JtsMultiPolygonToMultiPolygonConverter();
    }

    @Rule
    public final RawAtlasSlicerTestRule setup = new RawAtlasSlicerTestRule();

    /**
     * This test uses a simple relation that is tagged as boundary, and has only a very small
     * percentage crossing the boundary. As a result, it meets both the tagging and percentage
     * criteria for being consolidated, and only one slice should be generated
     */
    @Test
    public void testBoundaryRelationsConsolidatedSpanningTwoCountries()
    {
        final Atlas rawAtlas = this.setup.getSimpleBoundaryRelationConsolidateAtlas();
        final Atlas civSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("CIV"),
                rawAtlas).slice();
        final Atlas lbrSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();

        Assert.assertEquals(0, civSlicedAtlas.numberOfPoints());
        Assert.assertEquals(2, civSlicedAtlas.numberOfLines());
        Assert.assertEquals(2, civSlicedAtlas.numberOfAreas());
        Assert.assertEquals(1, civSlicedAtlas.numberOfRelations());
        Assert.assertTrue(Iterables.stream(civSlicedAtlas.entities())
                .allMatch(entity -> entity.getTag(ISOCountryTag.KEY).get().equals("CIV")));

        Assert.assertEquals(1, lbrSlicedAtlas.numberOfLines());
        Assert.assertEquals(3, lbrSlicedAtlas.numberOfAreas());
        Assert.assertEquals(0, lbrSlicedAtlas.numberOfPoints());
        Assert.assertEquals(0, lbrSlicedAtlas.numberOfRelations());
        Assert.assertTrue(Iterables.stream(lbrSlicedAtlas.entities())
                .allMatch(entity -> entity.getTag(ISOCountryTag.KEY).get().equals("LBR")));

        final SortedSet<String> civSyntheticRelationMembers = new TreeSet<>();
        civSlicedAtlas.lines().forEach(line ->
        {
            if (line.getTag(SyntheticGeometrySlicedTag.KEY).isPresent())
            {
                final Area rawArea = rawAtlas.areas(rawAreaCandidate -> rawAreaCandidate
                        .getOsmIdentifier() == line.getOsmIdentifier()).iterator().next();
                Assert.assertNotNull(civSlicedAtlas.area(line.getIdentifier()));
                Assert.assertEquals(rawArea.getOsmTags(), line.getOsmTags());
                Assert.assertEquals(rawArea.getOsmTags(),
                        civSlicedAtlas.area(line.getIdentifier()).getOsmTags());
                Assert.assertEquals(1, line.relations().size());
                Assert.assertEquals(0,
                        civSlicedAtlas.area(line.getIdentifier()).relations().size());
            }
            else
            {
                Assert.assertTrue(line.getTag(SyntheticSyntheticRelationMemberTag.KEY).isPresent());
                civSyntheticRelationMembers.add(Long.toString(line.getIdentifier()));
            }
        });

        final SortedSet<String> lbrSyntheticRelationMembers = new TreeSet<>();
        lbrSlicedAtlas.lines().forEach(line ->
        {
            if (line.getTag(SyntheticGeometrySlicedTag.KEY).isPresent())
            {
                final Area rawArea = rawAtlas.areas(rawAreaCandidate -> rawAreaCandidate
                        .getOsmIdentifier() == line.getOsmIdentifier()).iterator().next();
                Assert.assertNotNull(lbrSlicedAtlas.area(line.getIdentifier()));
                Assert.assertEquals(rawArea.getOsmTags(), line.getOsmTags());
                Assert.assertEquals(rawArea.getOsmTags(),
                        lbrSlicedAtlas.area(line.getIdentifier()).getOsmTags());
                Assert.assertEquals(0, line.relations().size());
                Assert.assertEquals(0,
                        lbrSlicedAtlas.area(line.getIdentifier()).relations().size());
            }
            else if (rawAtlas.line(line.getIdentifier()) != null)
            {
                Assert.assertEquals(rawAtlas.line(line.getIdentifier()).getOsmTags(),
                        line.getOsmTags());
                Assert.assertEquals(rawAtlas.line(line.getIdentifier()).asPolyLine(),
                        line.asPolyLine());
                Assert.assertEquals(1, line.relations().size());
            }
            else
            {
                Assert.assertTrue(line.getTag(SyntheticSyntheticRelationMemberTag.KEY).isPresent());
                lbrSyntheticRelationMembers.add(Long.toString(line.getIdentifier()));
            }
        });

        final CountrySlicingIdentifierFactory relationIdentifierFactory = new CountrySlicingIdentifierFactory(
                1);

        final Relation civRelation = civSlicedAtlas
                .relation(relationIdentifierFactory.nextIdentifier());

        Assert.assertEquals("CIV", civRelation.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                civRelation.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(
                String.join(SyntheticRelationMemberAdded.MEMBER_DELIMITER,
                        civSyntheticRelationMembers),
                civRelation.getTag(SyntheticRelationMemberAdded.KEY).get());
        Assert.assertTrue(civRelation
                .getTag(SyntheticInvalidMultiPolygonRelationMembersRemovedTag.KEY).isEmpty());
        Assert.assertTrue(jtsConverter.backwardConvert(converter.convert(civRelation)).isValid());
    }

    /**
     * This test uses the same simple relation for the multipolygon tests, but is tagged as
     * boundary. While it meets meets the tagging criteria, it significant pieces on both sides and
     * thus does not meet the percentage cutoff and should not be consolidated
     */
    @Test
    public void testBoundaryRelationsSpanningTwoCountries()
    {
        final Atlas rawAtlas = this.setup.getSimpleBoundaryRelationAtlas();
        final Atlas civSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("CIV"),
                rawAtlas).slice();
        final Atlas lbrSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();

        Assert.assertEquals(0, civSlicedAtlas.numberOfPoints());
        Assert.assertEquals(4, civSlicedAtlas.numberOfLines());
        Assert.assertEquals(2, civSlicedAtlas.numberOfAreas());
        Assert.assertEquals(1, civSlicedAtlas.numberOfRelations());
        Assert.assertTrue(Iterables.stream(civSlicedAtlas.entities())
                .allMatch(entity -> entity.getTag(ISOCountryTag.KEY).get().equals("CIV")));

        Assert.assertEquals(4, lbrSlicedAtlas.numberOfLines());
        Assert.assertEquals(3, lbrSlicedAtlas.numberOfAreas());
        Assert.assertEquals(0, lbrSlicedAtlas.numberOfPoints());
        Assert.assertEquals(1, lbrSlicedAtlas.numberOfRelations());
        Assert.assertTrue(Iterables.stream(lbrSlicedAtlas.entities())
                .allMatch(entity -> entity.getTag(ISOCountryTag.KEY).get().equals("LBR")));

        final SortedSet<String> civSyntheticRelationMembers = new TreeSet<>();
        civSlicedAtlas.lines().forEach(line ->
        {
            if (line.getTag(SyntheticGeometrySlicedTag.KEY).isPresent())
            {
                final Area rawArea = rawAtlas.areas(rawAreaCandidate -> rawAreaCandidate
                        .getOsmIdentifier() == line.getOsmIdentifier()).iterator().next();
                Assert.assertNotNull(civSlicedAtlas.area(line.getIdentifier()));
                Assert.assertEquals(rawArea.getOsmTags(), line.getOsmTags());
                Assert.assertEquals(rawArea.getOsmTags(),
                        civSlicedAtlas.area(line.getIdentifier()).getOsmTags());
                Assert.assertEquals(1, line.relations().size());
                Assert.assertEquals(0,
                        civSlicedAtlas.area(line.getIdentifier()).relations().size());
            }
            else
            {
                Assert.assertTrue(line.getTag(SyntheticSyntheticRelationMemberTag.KEY).isPresent());
                civSyntheticRelationMembers.add(Long.toString(line.getIdentifier()));
            }
        });

        final SortedSet<String> lbrSyntheticRelationMembers = new TreeSet<>();
        lbrSlicedAtlas.lines().forEach(line ->
        {
            if (line.getTag(SyntheticGeometrySlicedTag.KEY).isPresent())
            {
                final Area rawArea = rawAtlas.areas(rawAreaCandidate -> rawAreaCandidate
                        .getOsmIdentifier() == line.getOsmIdentifier()).iterator().next();
                Assert.assertNotNull(lbrSlicedAtlas.area(line.getIdentifier()));
                Assert.assertEquals(rawArea.getOsmTags(), line.getOsmTags());
                Assert.assertEquals(rawArea.getOsmTags(),
                        lbrSlicedAtlas.area(line.getIdentifier()).getOsmTags());
                Assert.assertEquals(1, line.relations().size());
                Assert.assertEquals(0,
                        lbrSlicedAtlas.area(line.getIdentifier()).relations().size());
            }
            else if (rawAtlas.line(line.getIdentifier()) != null)
            {
                Assert.assertEquals(rawAtlas.line(line.getIdentifier()).getOsmTags(),
                        line.getOsmTags());
                Assert.assertEquals(rawAtlas.line(line.getIdentifier()).asPolyLine(),
                        line.asPolyLine());
                Assert.assertEquals(1, line.relations().size());
            }
            else
            {
                Assert.assertTrue(line.getTag(SyntheticSyntheticRelationMemberTag.KEY).isPresent());
                lbrSyntheticRelationMembers.add(Long.toString(line.getIdentifier()));
            }
        });

        final CountrySlicingIdentifierFactory relationIdentifierFactory = new CountrySlicingIdentifierFactory(
                1);

        final Relation civRelation = civSlicedAtlas
                .relation(relationIdentifierFactory.nextIdentifier());
        final Relation lbrRelation = lbrSlicedAtlas
                .relation(relationIdentifierFactory.nextIdentifier());

        Assert.assertEquals("CIV", civRelation.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                civRelation.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(
                String.join(SyntheticRelationMemberAdded.MEMBER_DELIMITER,
                        civSyntheticRelationMembers),
                civRelation.getTag(SyntheticRelationMemberAdded.KEY).get());
        Assert.assertTrue(civRelation
                .getTag(SyntheticInvalidMultiPolygonRelationMembersRemovedTag.KEY).isEmpty());

        Assert.assertEquals("LBR", lbrRelation.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                lbrRelation.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(
                String.join(SyntheticRelationMemberAdded.MEMBER_DELIMITER,
                        lbrSyntheticRelationMembers),
                lbrRelation.getTag(SyntheticRelationMemberAdded.KEY).get());
        Assert.assertTrue(lbrRelation
                .getTag(SyntheticInvalidMultiPolygonRelationMembersRemovedTag.KEY).isEmpty());

        Assert.assertTrue(jtsConverter.backwardConvert(converter.convert(civRelation)).isValid());
        Assert.assertTrue(jtsConverter.backwardConvert(converter.convert(lbrRelation)).isValid());
    }

    /**
     * This test examines a number of important behaviors. For a closed edge, it's important that
     * slicing logic recognizes that it should be sliced linearly, as slicing polygonally would
     * destroy its ability to be converted to an Edge. Additionally, this test checks that the
     * line's points are preserved despite no specific tagging, and that synthetic boundary nodes
     * are present and match the slice location for both sliced lines.
     */
    @Test
    public void testClosedEdgeSpanningTwoCountries()
    {
        final Atlas rawAtlas = this.setup.getClosedEdgeSpanningTwoCountriesAtlas();
        final Atlas civSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("CIV"),
                rawAtlas).slice();
        final Atlas lbrSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();

        final Line rawLine = rawAtlas.line(1);

        Assert.assertEquals(1, civSlicedAtlas.numberOfLines());
        Assert.assertEquals(1, lbrSlicedAtlas.numberOfLines());

        final CountrySlicingIdentifierFactory lineIdentifierFactory = new CountrySlicingIdentifierFactory(
                rawLine.getIdentifier());

        final Line civLine = civSlicedAtlas.line(lineIdentifierFactory.nextIdentifier());
        Assert.assertFalse(civLine.isClosed());

        final Optional<String> civLineTag = civLine.getTag(ISOCountryTag.KEY);
        Assert.assertTrue(civLineTag.isPresent());
        Assert.assertEquals("CIV", civLineTag.get());
        Assert.assertEquals(rawLine.getOsmTags(), civLine.getOsmTags());

        final Optional<String> civLineSlicedGeometryTag = civLine
                .getTag(SyntheticGeometrySlicedTag.KEY);
        Assert.assertTrue(civLineSlicedGeometryTag.isPresent());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                civLineSlicedGeometryTag.get());

        final Line lbrLine = lbrSlicedAtlas.line(lineIdentifierFactory.nextIdentifier());
        Assert.assertFalse(lbrLine.isClosed());

        final Optional<String> lbrLineTag = lbrLine.getTag(ISOCountryTag.KEY);
        Assert.assertTrue(lbrLineTag.isPresent());
        Assert.assertEquals("LBR", lbrLineTag.get());
        Assert.assertEquals(rawLine.getOsmTags(), lbrLine.getOsmTags());

        final Optional<String> lbrLineSlicedGeometryTag = lbrLine
                .getTag(SyntheticGeometrySlicedTag.KEY);
        Assert.assertTrue(lbrLineSlicedGeometryTag.isPresent());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                lbrLineSlicedGeometryTag.get());

        // Check Point correctness
        Assert.assertEquals(4, civSlicedAtlas.numberOfPoints());
        Assert.assertEquals(4, lbrSlicedAtlas.numberOfPoints());

        for (final Point point : civSlicedAtlas.points())
        {
            Assert.assertTrue(point.getTag(ISOCountryTag.KEY).isPresent());
            if (point.getIdentifier() == 1 || point.getIdentifier() == 2)
            {
                Assert.assertEquals(rawAtlas.point(point.getIdentifier()).getOsmTags(),
                        point.getOsmTags());
                Assert.assertEquals("CIV", point.getTag(ISOCountryTag.KEY).get());
                Assert.assertFalse(point.getTag(SyntheticBoundaryNodeTag.KEY).isPresent());
            }
            else
            {
                Assert.assertEquals("CIV,LBR", point.getTag(ISOCountryTag.KEY).get());
                Assert.assertTrue(point.getOsmTags().isEmpty());
                Assert.assertTrue(point.getTag(SyntheticBoundaryNodeTag.KEY).isPresent());
                Assert.assertEquals(SyntheticBoundaryNodeTag.YES.toString(),
                        point.getTag(SyntheticBoundaryNodeTag.KEY).get());
                civSlicedAtlas.linesContaining(point.getLocation()).forEach(lineContaining ->
                {
                    Assert.assertTrue(lineContaining.asPolyLine().last().equals(point.getLocation())
                            || lineContaining.asPolyLine().first().equals(point.getLocation()));
                });

                // boundary nodes should be in both Atlases!
                final Point lbrBoundaryNode = lbrSlicedAtlas.point(point.getIdentifier());
                Assert.assertEquals(point.getLocation(), lbrBoundaryNode.getLocation());
                Assert.assertEquals(point.getTags(), lbrBoundaryNode.getTags());
            }
        }

        for (final Point point : lbrSlicedAtlas.points())
        {
            Assert.assertTrue(point.getTag(ISOCountryTag.KEY).isPresent());
            if (point.getIdentifier() == 3 || point.getIdentifier() == 4)
            {
                Assert.assertEquals(rawAtlas.point(point.getIdentifier()).getOsmTags(),
                        point.getOsmTags());
                Assert.assertEquals("LBR", point.getTag(ISOCountryTag.KEY).get());
                Assert.assertFalse(point.getTag(SyntheticBoundaryNodeTag.KEY).isPresent());
            }
            else
            {
                Assert.assertEquals("CIV,LBR", point.getTag(ISOCountryTag.KEY).get());
                Assert.assertTrue(point.getOsmTags().isEmpty());
                Assert.assertTrue(point.getTag(SyntheticBoundaryNodeTag.KEY).isPresent());
                Assert.assertEquals(SyntheticBoundaryNodeTag.YES.toString(),
                        point.getTag(SyntheticBoundaryNodeTag.KEY).get());
                civSlicedAtlas.linesContaining(point.getLocation()).forEach(lineContaining ->
                {
                    Assert.assertTrue(lineContaining.asPolyLine().last().equals(point.getLocation())
                            || lineContaining.asPolyLine().first().equals(point.getLocation()));
                });

                // boundary nodes should be in both Atlases!
                final Point civBoundaryNode = civSlicedAtlas.point(point.getIdentifier());
                Assert.assertEquals(point.getLocation(), civBoundaryNode.getLocation());
                Assert.assertEquals(point.getTags(), civBoundaryNode.getTags());
            }
        }
    }

    /**
     * This is a pretty straightforward case-- just looking to confirm the geometry isn't altered,
     * the country tag is updated, and the tagless points are removed
     */
    @Test
    public void testClosedLineInsideSingleCountry()
    {
        final Atlas rawAtlas = this.setup.getClosedLineFullyInOneCountryAtlas();
        final Atlas slicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("GIN"),
                rawAtlas).slice();

        Assert.assertEquals(1, slicedAtlas.numberOfLines());
        Assert.assertTrue(slicedAtlas.line(1).isClosed());
        Assert.assertEquals(rawAtlas.line(1).asPolyLine(), slicedAtlas.line(1).asPolyLine());
        Assert.assertEquals("GIN", slicedAtlas.line(1).getTag(ISOCountryTag.KEY).get());
        Assert.assertTrue(slicedAtlas.line(1).getTag(SyntheticGeometrySlicedTag.KEY).isEmpty());
        Assert.assertEquals(0, slicedAtlas.numberOfPoints());
    }

    /**
     * This is a pretty straightforward case-- just looking to confirm the geometry isn't altered,
     * the country tag is updated, and the tagless points are <i>not</i> removed ("keepAll" option)
     */
    @Test
    public void testClosedLineInsideSingleCountryKeepAll()
    {
        final Atlas rawAtlas = this.setup.getClosedLineFullyInOneCountryAtlas();
        final Atlas slicedAtlas = new RawAtlasSlicer(AtlasLoadingOption
                .createOptionWithAllEnabled(boundary).setCountryCode("GIN").setKeepAll(true),
                rawAtlas).slice();

        Assert.assertEquals(1, slicedAtlas.numberOfLines());
        Assert.assertTrue(slicedAtlas.line(1).isClosed());
        Assert.assertEquals(rawAtlas.line(1).asPolyLine(), slicedAtlas.line(1).asPolyLine());
        Assert.assertEquals("GIN", slicedAtlas.line(1).getTag(ISOCountryTag.KEY).get());
        Assert.assertTrue(slicedAtlas.line(1).getTag(SyntheticGeometrySlicedTag.KEY).isEmpty());
        Assert.assertEquals(4, slicedAtlas.numberOfPoints());
    }

    /**
     * This is the same geometry as the line in testClosedEdgeSpanningTwoCountries(), but the
     * tagging here no longer qualifies it as an Edge. The result should be different, accordingly,
     * as the closed line will now be sliced as a polygon and tagless points will be removed.
     */
    @Test
    public void testClosedLineSpanningTwoCountries()
    {
        final Atlas rawAtlas = this.setup.getClosedLineSpanningTwoCountriesAtlas();
        final Atlas civSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("CIV"),
                rawAtlas).slice();
        final Atlas lbrSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();

        Assert.assertEquals(1, civSlicedAtlas.numberOfAreas());
        Assert.assertEquals(1, lbrSlicedAtlas.numberOfAreas());

        final Area rawArea = rawAtlas.area(1);
        final CountrySlicingIdentifierFactory lineIdentifierFactory = new CountrySlicingIdentifierFactory(
                rawArea.getIdentifier());

        final Area civSlicedArea = civSlicedAtlas.area(lineIdentifierFactory.nextIdentifier());
        Assert.assertEquals("CIV", civSlicedArea.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                civSlicedArea.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(rawArea.getOsmTags(), civSlicedArea.getOsmTags());
        Assert.assertFalse(civSlicedArea.asPolygon().isClockwise());

        final Area lbrSlicedArea = lbrSlicedAtlas.area(lineIdentifierFactory.nextIdentifier());
        Assert.assertEquals("LBR", lbrSlicedArea.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                lbrSlicedArea.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(rawArea.getOsmTags(), lbrSlicedArea.getOsmTags());
        Assert.assertFalse(lbrSlicedArea.asPolygon().isClockwise());

        Assert.assertEquals(0, civSlicedAtlas.numberOfPoints());
        Assert.assertEquals(0, lbrSlicedAtlas.numberOfPoints());
    }

    /**
     * Test examining the creation of synthetic boundary nodes for existing points. Should slice
     * line into two pieces, one for CIV and one for LBR. All points should be preserved, and point
     * 2 should be tagged as a SyntheticBoundaryNode.EXISTING as well as be the first location of
     * the CIV sliced line and the last location of the LBR sliced line.
     */
    @Test
    public void testCreatingExistingSyntheticBoundaryNode()
    {
        final Atlas rawAtlas = this.setup.getRoadAcrossTwoCountriesWithPointOnBorderAtlas();
        final Atlas civSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("CIV"),
                rawAtlas).slice();
        final Atlas lbrSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();

        Assert.assertEquals(1, civSlicedAtlas.numberOfLines());
        Assert.assertEquals(1, lbrSlicedAtlas.numberOfLines());

        final Line rawLine = rawAtlas.line(1);
        final CountrySlicingIdentifierFactory lineIdentifierFactory = new CountrySlicingIdentifierFactory(
                rawLine.getIdentifier());

        final Line civLine = civSlicedAtlas.line(lineIdentifierFactory.nextIdentifier());
        Assert.assertEquals("CIV", civLine.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                civLine.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(rawLine.getOsmTags(), civLine.getOsmTags());

        final Line lbrLine = lbrSlicedAtlas.line(lineIdentifierFactory.nextIdentifier());
        Assert.assertEquals("LBR", lbrLine.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                lbrLine.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(rawLine.getOsmTags(), lbrLine.getOsmTags());

        Assert.assertEquals(2, civSlicedAtlas.numberOfPoints());
        Assert.assertEquals(2, lbrSlicedAtlas.numberOfPoints());

        Assert.assertEquals("CIV", civSlicedAtlas.point(3).getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals("LBR", lbrSlicedAtlas.point(1).getTag(ISOCountryTag.KEY).get());

        Assert.assertEquals(SyntheticBoundaryNodeTag.EXISTING.toString(),
                civSlicedAtlas.point(2).getTag(SyntheticBoundaryNodeTag.KEY).get());
        Assert.assertEquals(SyntheticBoundaryNodeTag.EXISTING.toString(),
                lbrSlicedAtlas.point(2).getTag(SyntheticBoundaryNodeTag.KEY).get());

        Assert.assertEquals("CIV,LBR", civSlicedAtlas.point(2).getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals("CIV,LBR", lbrSlicedAtlas.point(2).getTag(ISOCountryTag.KEY).get());

        Assert.assertEquals(civSlicedAtlas.point(2).getLocation(), civLine.asPolyLine().first());
        Assert.assertEquals(lbrSlicedAtlas.point(2).getLocation(), lbrLine.asPolyLine().last());
    }

    /**
     * This line is a highway entirely inside a country and should only be updated with an
     * ISOCountryTag. Points should be preserved since it will be an Edge
     */
    @Test
    public void testEdgeFullyInsideOneCountry()
    {
        final Atlas rawAtlas = this.setup.getRoadFullyInOneCountryAtlas();
        final Atlas slicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("CIV"),
                rawAtlas).slice();

        Assert.assertEquals(rawAtlas.numberOfLines(), slicedAtlas.numberOfLines());
        Assert.assertEquals("CIV", slicedAtlas.line(1).getTag(ISOCountryTag.KEY).get());
        Assert.assertTrue(slicedAtlas.line(1).getTag(SyntheticGeometrySlicedTag.KEY).isEmpty());
        Assert.assertEquals(rawAtlas.line(1).getOsmTags(), slicedAtlas.line(1).getOsmTags());

        // Check Point correctness
        Assert.assertEquals(rawAtlas.numberOfPoints(), slicedAtlas.numberOfPoints());

        slicedAtlas.points().forEach(point ->
        {
            Assert.assertEquals("CIV", point.getTag(ISOCountryTag.KEY).get());
            Assert.assertFalse(point.getTag(SyntheticBoundaryNodeTag.KEY).isPresent());
            Assert.assertEquals(rawAtlas.point(point.getIdentifier()).getOsmTags(),
                    point.getOsmTags());
        });
    }

    /**
     * This line is an Edge candidate that goes across the boundary multiple times. Expect geometry
     * to be sliced and new synthetic nodes to be made.
     */
    @Test
    public void testEdgeWeavingAcrossBoundary()
    {
        final Atlas rawAtlas = this.setup.getRoadWeavingAlongBoundaryAtlas();
        final Atlas civSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("CIV"),
                rawAtlas).slice();
        final Atlas lbrSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();

        Assert.assertEquals(2, civSlicedAtlas.numberOfLines());
        Assert.assertEquals(2, lbrSlicedAtlas.numberOfLines());

        final CountrySlicingIdentifierFactory lineIdentifierFactory = new CountrySlicingIdentifierFactory(
                1);

        final Line firstCreatedLine = civSlicedAtlas.line(lineIdentifierFactory.nextIdentifier());
        Assert.assertNotNull("Check new way addition", firstCreatedLine);
        Assert.assertEquals("Expect the first segment to be on the Ivory Coast side", "CIV",
                firstCreatedLine.getTag(ISOCountryTag.KEY).get());

        final Line secondCreatedLine = civSlicedAtlas.line(lineIdentifierFactory.nextIdentifier());
        Assert.assertNotNull("Check new way addition", secondCreatedLine);
        Assert.assertEquals("Expect the second segment to be on the Ivory Coast side", "CIV",
                secondCreatedLine.getTag(ISOCountryTag.KEY).get());

        final Line thirdCreatedLine = lbrSlicedAtlas.line(lineIdentifierFactory.nextIdentifier());
        Assert.assertNotNull("Check new way addition", thirdCreatedLine);
        Assert.assertEquals("Expect the third segment to be on the Liberia side", "LBR",
                thirdCreatedLine.getTag(ISOCountryTag.KEY).get());

        final Line fourthCreatedLine = lbrSlicedAtlas.line(lineIdentifierFactory.nextIdentifier());
        Assert.assertNotNull("Check new way addition", fourthCreatedLine);
        Assert.assertEquals("Expect the fourth segment to be on the Liberia side", "LBR",
                fourthCreatedLine.getTag(ISOCountryTag.KEY).get());

        Assert.assertEquals(6, civSlicedAtlas.numberOfPoints());
        Assert.assertEquals(5, lbrSlicedAtlas.numberOfPoints());
        for (final Point point : civSlicedAtlas.points())
        {
            if (rawAtlas.point(point.getIdentifier()) != null)
            {
                Assert.assertEquals(rawAtlas.point(point.getIdentifier()).getOsmTags(),
                        point.getOsmTags());
                Assert.assertEquals("CIV", point.getTag(ISOCountryTag.KEY).get());
            }
            else
            {
                Assert.assertEquals("CIV,LBR", point.getTag(ISOCountryTag.KEY).get());
                Assert.assertEquals(SyntheticBoundaryNodeTag.YES.toString(),
                        point.getTag(SyntheticBoundaryNodeTag.KEY).get());
                Assert.assertNotNull(lbrSlicedAtlas.point(point.getIdentifier()));
                civSlicedAtlas.linesContaining(point.getLocation()).forEach(lineContaining ->
                {
                    Assert.assertTrue(lineContaining.asPolyLine().first()
                            .equals(point.getLocation())
                            || lineContaining.asPolyLine().last().equals(point.getLocation()));
                });
            }
        }

        for (final Point point : lbrSlicedAtlas.points())
        {
            if (rawAtlas.point(point.getIdentifier()) != null)
            {
                Assert.assertEquals(rawAtlas.point(point.getIdentifier()).getOsmTags(),
                        point.getOsmTags());
                Assert.assertEquals("LBR", point.getTag(ISOCountryTag.KEY).get());
            }
            else
            {
                Assert.assertEquals("CIV,LBR", point.getTag(ISOCountryTag.KEY).get());
                Assert.assertEquals(SyntheticBoundaryNodeTag.YES.toString(),
                        point.getTag(SyntheticBoundaryNodeTag.KEY).get());
                Assert.assertNotNull(civSlicedAtlas.point(point.getIdentifier()));
                civSlicedAtlas.linesContaining(point.getLocation()).forEach(lineContaining ->
                {
                    Assert.assertTrue(lineContaining.asPolyLine().first()
                            .equals(point.getLocation())
                            || lineContaining.asPolyLine().last().equals(point.getLocation()));
                });
            }
        }
    }

    /**
     * This line is an Edge candidate that goes across the boundary multiple times. Expect geometry
     * to be sliced and new synthetic nodes to be made. The nodes/points should all still be there.
     */
    @Test
    public void testEdgeWeavingAcrossBoundaryKeepAll()
    {
        final Atlas rawAtlas = this.setup.getRoadWeavingAlongBoundaryAtlas();
        final Atlas civSlicedAtlas = new RawAtlasSlicer(AtlasLoadingOption
                .createOptionWithAllEnabled(boundary).setCountryCode("CIV").setKeepAll(true),
                rawAtlas).slice();
        final Atlas lbrSlicedAtlas = new RawAtlasSlicer(AtlasLoadingOption
                .createOptionWithAllEnabled(boundary).setCountryCode("LBR").setKeepAll(true),
                rawAtlas).slice();

        final CountrySlicingIdentifierFactory lineIdentifierFactory = new CountrySlicingIdentifierFactory(
                1);

        final Line firstCreatedLine = civSlicedAtlas.line(lineIdentifierFactory.nextIdentifier());
        Assert.assertNotNull("Check new way addition", firstCreatedLine);
        Assert.assertEquals("Expect the first segment to be on the Ivory Coast side", "CIV",
                firstCreatedLine.getTag(ISOCountryTag.KEY).get());

        final Line secondCreatedLine = civSlicedAtlas.line(lineIdentifierFactory.nextIdentifier());
        Assert.assertNotNull("Check new way addition", secondCreatedLine);
        Assert.assertEquals("Expect the second segment to be on the Ivory Coast side", "CIV",
                secondCreatedLine.getTag(ISOCountryTag.KEY).get());

        final Line thirdCreatedLine = lbrSlicedAtlas.line(lineIdentifierFactory.nextIdentifier());
        Assert.assertNotNull("Check new way addition", thirdCreatedLine);
        Assert.assertEquals("Expect the third segment to be on the Liberia side", "LBR",
                thirdCreatedLine.getTag(ISOCountryTag.KEY).get());

        final Line fourthCreatedLine = lbrSlicedAtlas.line(lineIdentifierFactory.nextIdentifier());
        Assert.assertNotNull("Check new way addition", fourthCreatedLine);
        Assert.assertEquals("Expect the fourth segment to be on the Liberia side", "LBR",
                fourthCreatedLine.getTag(ISOCountryTag.KEY).get());

        // We keep all the points
        Assert.assertEquals(8, civSlicedAtlas.numberOfPoints());
        Assert.assertEquals(5, Iterables.stream(civSlicedAtlas.points())
                .filter(point -> point.getIdentifier() > 0).collectToList().size());
        Assert.assertEquals(8, lbrSlicedAtlas.numberOfPoints());
        Assert.assertEquals(5, Iterables.stream(lbrSlicedAtlas.points())
                .filter(point -> point.getIdentifier() > 0).collectToList().size());
        for (final Point point : civSlicedAtlas.points())
        {
            if (rawAtlas.point(point.getIdentifier()) != null)
            {
                Assert.assertEquals(rawAtlas.point(point.getIdentifier()).getOsmTags(),
                        point.getOsmTags());
                if ("CIV".equals(
                        boundary.getCountryCodeISO3(point.getLocation()).getIso3CountryCode()))
                {
                    Assert.assertEquals("CIV", point.getTag(ISOCountryTag.KEY).get());
                }
            }
            else
            {
                Assert.assertEquals("CIV,LBR", point.getTag(ISOCountryTag.KEY).get());
                Assert.assertEquals(SyntheticBoundaryNodeTag.YES.toString(),
                        point.getTag(SyntheticBoundaryNodeTag.KEY).get());
                Assert.assertNotNull(lbrSlicedAtlas.point(point.getIdentifier()));
                civSlicedAtlas.linesContaining(point.getLocation()).forEach(lineContaining ->
                {
                    Assert.assertTrue(lineContaining.asPolyLine().first()
                            .equals(point.getLocation())
                            || lineContaining.asPolyLine().last().equals(point.getLocation()));
                });
            }
        }

        for (final Point point : lbrSlicedAtlas.points())
        {
            if (rawAtlas.point(point.getIdentifier()) != null)
            {
                Assert.assertEquals(rawAtlas.point(point.getIdentifier()).getOsmTags(),
                        point.getOsmTags());
                if ("LBR".equals(
                        boundary.getCountryCodeISO3(point.getLocation()).getIso3CountryCode()))
                {
                    Assert.assertEquals("LBR", point.getTag(ISOCountryTag.KEY).get());
                }
                else if ("CIV".equals(
                        boundary.getCountryCodeISO3(point.getLocation()).getIso3CountryCode()))
                {
                    Assert.assertEquals("CIV", point.getTag(ISOCountryTag.KEY).get());
                }
            }
            else
            {
                Assert.assertEquals("CIV,LBR", point.getTag(ISOCountryTag.KEY).get());
                Assert.assertEquals(SyntheticBoundaryNodeTag.YES.toString(),
                        point.getTag(SyntheticBoundaryNodeTag.KEY).get());
                Assert.assertNotNull(civSlicedAtlas.point(point.getIdentifier()));
                civSlicedAtlas.linesContaining(point.getLocation()).forEach(lineContaining ->
                {
                    Assert.assertTrue(lineContaining.asPolyLine().first()
                            .equals(point.getLocation())
                            || lineContaining.asPolyLine().last().equals(point.getLocation()));
                });
            }
        }
    }

    /**
     * This tests a relation with two closed lines in LBR that overlap, forming an invalid
     * multipolygon. Expect only country code assignment, all points removed (since they have no
     * tags), no geometry slicing, and no relation slicing
     */
    @Test
    public void testInnerIntersectingOuterRelation()
    {
        final Atlas rawAtlas = this.setup.getIntersectingInnerAndOuterMembersAtlas();
        final Atlas slicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();
        Assert.assertEquals(0, slicedAtlas.numberOfPoints());
        Assert.assertEquals(rawAtlas.numberOfLines(), slicedAtlas.numberOfLines());
        Assert.assertEquals(rawAtlas.numberOfRelations(), slicedAtlas.numberOfRelations());

        Assert.assertTrue(Iterables.stream(slicedAtlas.entities())
                .allMatch(entity -> entity.getTag(ISOCountryTag.KEY).get().equals("LBR")
                        && entity.getTag(SyntheticGeometrySlicedTag.KEY).isEmpty()));
    }

    /**
     * This relation is made up two closed lines, both on the Liberia side. However, the inner and
     * outer roles are reversed, causing an invalid multipolygon. Expect only country code
     * assignment, all points removed (since they have no tags), no geometry slicing, and no
     * relation slicing
     */
    @Test
    public void testInnerOutsideOuterRelation()
    {
        final Atlas rawAtlas = this.setup.getInnerOutsideOuterRelationAtlas();
        final Atlas slicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();

        // Assert that we cannot build a valid building with this relation
        new ComplexBuildingFinder().find(slicedAtlas)
                .forEach(building -> Assert.assertTrue(building.getError().isPresent()));

        // Nothing should have been sliced, verify identical counts
        Assert.assertEquals(0, slicedAtlas.numberOfPoints());
        Assert.assertEquals(rawAtlas.numberOfLines(), slicedAtlas.numberOfLines());
        Assert.assertEquals(rawAtlas.numberOfRelations(), slicedAtlas.numberOfRelations());

        Assert.assertTrue(Iterables.stream(slicedAtlas.entities())
                .allMatch(entity -> entity.getTag(ISOCountryTag.KEY).get().equals("LBR")
                        && entity.getTag(SyntheticGeometrySlicedTag.KEY).isEmpty()));
    }

    /**
     * This relation is made up of a single closed line straddling the CIV/LBR boundary, which is
     * the sole member in the relation with inner role. This is an invalid multipolygon, since it
     * doesn't have an outer. Expect only country code assignment, all points removed (since they
     * have no tags), no geometry slicing, and no relation slicing. Additionally, because
     * multipolygon relation slicing requires lines sliced linearly, but the line is closed, expect
     * to see both the linearly sliced line in the atlas Lines, and the polygonally sliced line in
     * the atlas Areas. The areas should *not* be part of the relation.
     */
    @Test
    public void testInnerWithoutOuterAcrossBoundary()
    {
        final Atlas rawAtlas = this.setup.getInnerWithoutOuterAcrossBoundaryAtlas();
        final Atlas civSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("CIV"),
                rawAtlas).slice();
        final Atlas lbrSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();

        Assert.assertEquals(0, civSlicedAtlas.numberOfPoints());
        Assert.assertEquals(0, lbrSlicedAtlas.numberOfPoints());

        // Line was cut into two pieces, and each relation contains the piece as an inner
        Assert.assertEquals(1, civSlicedAtlas.numberOfAreas());
        Assert.assertEquals(1, lbrSlicedAtlas.numberOfAreas());
        Assert.assertEquals(1, civSlicedAtlas.numberOfRelations());
        Assert.assertEquals(1, lbrSlicedAtlas.numberOfRelations());

        final CountrySlicingIdentifierFactory lineIdentifierFactory = new CountrySlicingIdentifierFactory(
                108768000000L);
        final Area rawArea = rawAtlas.area(108768000000L);
        final Line civLine = civSlicedAtlas.line(lineIdentifierFactory.nextIdentifier());
        Assert.assertEquals("CIV", civLine.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                civLine.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(rawArea.getOsmTags(), civLine.getOsmTags());

        final Line lbrLine = lbrSlicedAtlas.line(lineIdentifierFactory.nextIdentifier());
        Assert.assertEquals("LBR", lbrLine.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                lbrLine.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(rawArea.getOsmTags(), lbrLine.getOsmTags());

        final Area civArea = civSlicedAtlas.area(civLine.getIdentifier());
        Assert.assertEquals("CIV", civArea.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                civArea.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(rawArea.getOsmTags(), civArea.getOsmTags());

        final Area lbrArea = lbrSlicedAtlas.area(lbrLine.getIdentifier());
        Assert.assertEquals("LBR", lbrArea.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                lbrArea.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(rawArea.getOsmTags(), lbrArea.getOsmTags());

        Assert.assertEquals("CIV", civSlicedAtlas.relation(1).getTag(ISOCountryTag.KEY).get());
        Assert.assertTrue(
                civSlicedAtlas.relation(1).getTag(SyntheticGeometrySlicedTag.KEY).isEmpty());
        Assert.assertEquals(1, civSlicedAtlas.relation(1).members().size());
        Assert.assertEquals(ItemType.LINE,
                civSlicedAtlas.relation(1).members().get(0).getEntity().getType());
        Assert.assertEquals(civLine.getIdentifier(),
                civSlicedAtlas.relation(1).members().get(0).getEntity().getIdentifier());
        Assert.assertTrue(civSlicedAtlas.relation(1)
                .getTag(SyntheticInvalidMultiPolygonRelationMembersRemovedTag.KEY).isEmpty());
        Assert.assertEquals(rawAtlas.relation(1).getOsmTags(),
                civSlicedAtlas.relation(1).getOsmTags());

        Assert.assertEquals("LBR", lbrSlicedAtlas.relation(1).getTag(ISOCountryTag.KEY).get());
        Assert.assertTrue(
                lbrSlicedAtlas.relation(1).getTag(SyntheticGeometrySlicedTag.KEY).isEmpty());
        Assert.assertEquals(1, lbrSlicedAtlas.relation(1).members().size());
        Assert.assertEquals(ItemType.LINE,
                lbrSlicedAtlas.relation(1).members().get(0).getEntity().getType());
        Assert.assertEquals(lbrLine.getIdentifier(),
                lbrSlicedAtlas.relation(1).members().get(0).getEntity().getIdentifier());
        Assert.assertTrue(lbrSlicedAtlas.relation(1)
                .getTag(SyntheticInvalidMultiPolygonRelationMembersRemovedTag.KEY).isEmpty());
        Assert.assertEquals(rawAtlas.relation(1).getOsmTags(),
                lbrSlicedAtlas.relation(1).getOsmTags());
    }

    /**
     * This relation is made up of a single closed line inside LBR, which is the sole member in the
     * relation with inner role. This is an invalid multipolygon, since it doesn't have an outer.
     * Expect only country code assignment, all points removed (since they have no tags), no
     * geometry slicing, and no relation slicing
     */
    @Test
    public void testInnerWithoutOuterRelationInOneCountry()
    {
        final Atlas rawAtlas = this.setup.getInnerWithoutOuterInOneCountryAtlas();
        final Atlas slicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();

        // Assert that we cannot build a valid building with this relation
        new ComplexBuildingFinder().find(slicedAtlas)
                .forEach(building -> Assert.assertTrue(building.getError().isPresent()));

        // Nothing should have been sliced, verify identical counts
        Assert.assertEquals(0, slicedAtlas.numberOfPoints());
        Assert.assertEquals(rawAtlas.numberOfLines(), slicedAtlas.numberOfLines());
        Assert.assertEquals(rawAtlas.numberOfRelations(), slicedAtlas.numberOfRelations());

        Assert.assertTrue(Iterables.stream(slicedAtlas.entities())
                .allMatch(entity -> entity.getTag(ISOCountryTag.KEY).get().equals("LBR")
                        && entity.getTag(SyntheticGeometrySlicedTag.KEY).isEmpty()));
    }

    /**
     * This relation is made up of three closed lines, each serving as an outer to a multipolygon
     * relation. Two of the outers span the border of two countries, while one is entirely within a
     * country.
     */
    @Test
    public void testMultiPolygonRelationSpanningTwoCountries()
    {
        final Atlas rawAtlas = this.setup.getSimpleMultiPolygonAtlas();
        final Atlas civSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("CIV"),
                rawAtlas).slice();
        final Atlas lbrSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();

        Assert.assertEquals(0, civSlicedAtlas.numberOfPoints());
        Assert.assertEquals(4, civSlicedAtlas.numberOfLines());
        Assert.assertEquals(2, civSlicedAtlas.numberOfAreas());
        Assert.assertEquals(1, civSlicedAtlas.numberOfRelations());
        Assert.assertTrue(Iterables.stream(civSlicedAtlas.entities())
                .allMatch(entity -> entity.getTag(ISOCountryTag.KEY).get().equals("CIV")));

        Assert.assertEquals(4, lbrSlicedAtlas.numberOfLines());
        Assert.assertEquals(3, lbrSlicedAtlas.numberOfAreas());
        Assert.assertEquals(0, lbrSlicedAtlas.numberOfPoints());
        Assert.assertEquals(1, lbrSlicedAtlas.numberOfRelations());
        Assert.assertTrue(Iterables.stream(lbrSlicedAtlas.entities())
                .allMatch(entity -> entity.getTag(ISOCountryTag.KEY).get().equals("LBR")));

        final SortedSet<String> civSyntheticRelationMembers = new TreeSet<>();
        civSlicedAtlas.lines().forEach(line ->
        {
            if (line.getTag(SyntheticGeometrySlicedTag.KEY).isPresent())
            {
                final Area rawArea = rawAtlas.areas(rawAreaCandidate -> rawAreaCandidate
                        .getOsmIdentifier() == line.getOsmIdentifier()).iterator().next();
                Assert.assertNotNull(civSlicedAtlas.area(line.getIdentifier()));
                Assert.assertEquals(rawArea.getOsmTags(), line.getOsmTags());
                Assert.assertEquals(rawArea.getOsmTags(),
                        civSlicedAtlas.area(line.getIdentifier()).getOsmTags());
                Assert.assertEquals(1, line.relations().size());
                Assert.assertEquals(0,
                        civSlicedAtlas.area(line.getIdentifier()).relations().size());
            }
            else
            {
                Assert.assertTrue(line.getTag(SyntheticSyntheticRelationMemberTag.KEY).isPresent());
                civSyntheticRelationMembers.add(Long.toString(line.getIdentifier()));
            }
        });

        final SortedSet<String> lbrSyntheticRelationMembers = new TreeSet<>();
        lbrSlicedAtlas.lines().forEach(line ->
        {
            if (line.getTag(SyntheticGeometrySlicedTag.KEY).isPresent())
            {
                final Area rawArea = rawAtlas.areas(rawAreaCandidate -> rawAreaCandidate
                        .getOsmIdentifier() == line.getOsmIdentifier()).iterator().next();
                Assert.assertNotNull(lbrSlicedAtlas.area(line.getIdentifier()));
                Assert.assertEquals(rawArea.getOsmTags(), line.getOsmTags());
                Assert.assertEquals(rawArea.getOsmTags(),
                        lbrSlicedAtlas.area(line.getIdentifier()).getOsmTags());
                Assert.assertEquals(1, line.relations().size());
                Assert.assertEquals(0,
                        lbrSlicedAtlas.area(line.getIdentifier()).relations().size());
            }
            else if (rawAtlas.line(line.getIdentifier()) != null)
            {
                Assert.assertEquals(rawAtlas.line(line.getIdentifier()).getOsmTags(),
                        line.getOsmTags());
                Assert.assertEquals(rawAtlas.line(line.getIdentifier()).asPolyLine(),
                        line.asPolyLine());
                Assert.assertEquals(1, line.relations().size());
            }
            else
            {
                Assert.assertTrue(line.getTag(SyntheticSyntheticRelationMemberTag.KEY).isPresent());
                lbrSyntheticRelationMembers.add(Long.toString(line.getIdentifier()));
            }
        });

        final CountrySlicingIdentifierFactory relationIdentifierFactory = new CountrySlicingIdentifierFactory(
                1);

        final Relation civRelation = civSlicedAtlas
                .relation(relationIdentifierFactory.nextIdentifier());
        final Relation lbrRelation = lbrSlicedAtlas
                .relation(relationIdentifierFactory.nextIdentifier());

        Assert.assertEquals("CIV", civRelation.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                civRelation.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(
                String.join(SyntheticRelationMemberAdded.MEMBER_DELIMITER,
                        civSyntheticRelationMembers),
                civRelation.getTag(SyntheticRelationMemberAdded.KEY).get());
        Assert.assertTrue(civRelation
                .getTag(SyntheticInvalidMultiPolygonRelationMembersRemovedTag.KEY).isEmpty());

        Assert.assertEquals("LBR", lbrRelation.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                lbrRelation.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(
                String.join(SyntheticRelationMemberAdded.MEMBER_DELIMITER,
                        lbrSyntheticRelationMembers),
                lbrRelation.getTag(SyntheticRelationMemberAdded.KEY).get());
        Assert.assertTrue(lbrRelation
                .getTag(SyntheticInvalidMultiPolygonRelationMembersRemovedTag.KEY).isEmpty());

        Assert.assertTrue(jtsConverter.backwardConvert(converter.convert(civRelation)).isValid());
        Assert.assertTrue(jtsConverter.backwardConvert(converter.convert(lbrRelation)).isValid());
    }

    /**
     * This relation is made up of closed lines, tied together by a relation, to create a
     * MultiPolygon with the outer spanning two countries and the inner fully inside one country.
     */
    @Test
    public void testMultiPolygonWithClosedLinesSpanningTwoCountries()
    {
        final Atlas rawAtlas = this.setup.getComplexMultiPolygonWithHoleUsingClosedLinesAtlas();
        final Atlas civSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("CIV"),
                rawAtlas).slice();
        final Atlas lbrSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();

        Assert.assertEquals(2, civSlicedAtlas.numberOfLines());
        Assert.assertEquals(1, civSlicedAtlas.numberOfAreas());
        Assert.assertEquals(0, civSlicedAtlas.numberOfPoints());
        Assert.assertEquals(1, civSlicedAtlas.numberOfRelations());
        final Iterable<ComplexWaterEntity> civWaterEntities = new ComplexWaterEntityFinder()
                .find(civSlicedAtlas, Finder::ignore);
        Assert.assertEquals(1, Iterables.size(civWaterEntities));

        Assert.assertEquals(2, lbrSlicedAtlas.numberOfLines());
        Assert.assertEquals(2, lbrSlicedAtlas.numberOfAreas());
        Assert.assertEquals(0, lbrSlicedAtlas.numberOfPoints());
        Assert.assertEquals(1, lbrSlicedAtlas.numberOfRelations());
        final Iterable<ComplexWaterEntity> lbrWaterEntities = new ComplexWaterEntityFinder()
                .find(lbrSlicedAtlas, Finder::ignore);
        Assert.assertEquals(1, Iterables.size(lbrWaterEntities));

        final SortedSet<String> civSyntheticRelationMembers = new TreeSet<>();
        civSlicedAtlas.lines().forEach(line ->
        {
            if (line.getTag(SyntheticGeometrySlicedTag.KEY).isPresent())
            {
                final Area rawArea = rawAtlas.areas(rawAreaCandidate -> rawAreaCandidate
                        .getOsmIdentifier() == line.getOsmIdentifier()).iterator().next();
                Assert.assertNotNull(civSlicedAtlas.area(line.getIdentifier()));
                Assert.assertEquals(rawArea.getOsmTags(), line.getOsmTags());
                Assert.assertEquals(rawArea.getOsmTags(),
                        civSlicedAtlas.area(line.getIdentifier()).getOsmTags());
                Assert.assertEquals(1, line.relations().size());
                Assert.assertEquals(0,
                        civSlicedAtlas.area(line.getIdentifier()).relations().size());
            }
            else if (rawAtlas.line(line.getIdentifier()) != null)
            {
                Assert.assertEquals(rawAtlas.line(line.getIdentifier()).getOsmTags(),
                        line.getOsmTags());
                Assert.assertEquals(rawAtlas.line(line.getIdentifier()).asPolyLine(),
                        line.asPolyLine());
                Assert.assertEquals(1, line.relations().size());
            }
            else
            {
                Assert.assertTrue(line.getTag(SyntheticSyntheticRelationMemberTag.KEY).isPresent());
                civSyntheticRelationMembers.add(Long.toString(line.getIdentifier()));
            }
        });

        final SortedSet<String> lbrSyntheticRelationMembers = new TreeSet<>();
        lbrSlicedAtlas.lines().forEach(line ->
        {
            if (line.getTag(SyntheticGeometrySlicedTag.KEY).isPresent())
            {
                final Area rawArea = rawAtlas.areas(rawAreaCandidate -> rawAreaCandidate
                        .getOsmIdentifier() == line.getOsmIdentifier()).iterator().next();
                Assert.assertNotNull(lbrSlicedAtlas.area(line.getIdentifier()));
                Assert.assertEquals(rawArea.getOsmTags(), line.getOsmTags());
                Assert.assertEquals(rawArea.getOsmTags(),
                        lbrSlicedAtlas.area(line.getIdentifier()).getOsmTags());
                Assert.assertEquals(1, line.relations().size());
                Assert.assertEquals(0,
                        lbrSlicedAtlas.area(line.getIdentifier()).relations().size());
            }
            else if (rawAtlas.line(line.getIdentifier()) != null)
            {
                Assert.assertEquals(rawAtlas.line(line.getIdentifier()).getOsmTags(),
                        line.getOsmTags());
                Assert.assertEquals(rawAtlas.line(line.getIdentifier()).asPolyLine(),
                        line.asPolyLine());
                Assert.assertEquals(1, line.relations().size());
            }
            else
            {
                Assert.assertTrue(line.getTag(SyntheticSyntheticRelationMemberTag.KEY).isPresent());
                lbrSyntheticRelationMembers.add(Long.toString(line.getIdentifier()));
            }
        });

        final CountrySlicingIdentifierFactory relationIdentifierFactory = new CountrySlicingIdentifierFactory(
                214805000000L);

        final Relation civRelation = civSlicedAtlas
                .relation(relationIdentifierFactory.nextIdentifier());
        final Relation lbrRelation = lbrSlicedAtlas
                .relation(relationIdentifierFactory.nextIdentifier());

        Assert.assertEquals("CIV", civRelation.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                civRelation.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(
                String.join(SyntheticRelationMemberAdded.MEMBER_DELIMITER,
                        civSyntheticRelationMembers),
                civRelation.getTag(SyntheticRelationMemberAdded.KEY).get());
        Assert.assertTrue(civRelation
                .getTag(SyntheticInvalidMultiPolygonRelationMembersRemovedTag.KEY).isEmpty());

        Assert.assertEquals("LBR", lbrRelation.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                lbrRelation.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(
                String.join(SyntheticRelationMemberAdded.MEMBER_DELIMITER,
                        lbrSyntheticRelationMembers),
                lbrRelation.getTag(SyntheticRelationMemberAdded.KEY).get());
        Assert.assertTrue(lbrRelation
                .getTag(SyntheticInvalidMultiPolygonRelationMembersRemovedTag.KEY).isEmpty());
    }

    /**
     * This test is the same as testSimpleMultiPolygonWithHoleSpanningTwoCountries(), but this
     * version contains invalid members. Check to see multipolygon relation is still properly sliced
     * but that invalid members have been filtered out and tag has been updated correctly
     */

    @Test
    public void testMultiPolygonWithInvalidMembers()
    {
        final Atlas rawAtlas = this.setup.getRelationWithInvalidMultiPolygonMembers();
        final Atlas civSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("CIV"),
                rawAtlas).slice();
        final Atlas lbrSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();

        Assert.assertEquals(0, civSlicedAtlas.numberOfPoints());
        Assert.assertEquals(5, civSlicedAtlas.numberOfLines());
        Assert.assertEquals(3, civSlicedAtlas.numberOfAreas());
        Assert.assertEquals(1, civSlicedAtlas.numberOfRelations());
        final Iterable<ComplexBuilding> civBuildings = new ComplexBuildingFinder()
                .find(civSlicedAtlas, Finder::ignore);
        Assert.assertEquals(1, Iterables.size(civBuildings));

        Assert.assertEquals(0, lbrSlicedAtlas.numberOfPoints());
        Assert.assertEquals(5, lbrSlicedAtlas.numberOfLines());
        Assert.assertEquals(3, lbrSlicedAtlas.numberOfAreas());
        Assert.assertEquals(1, lbrSlicedAtlas.numberOfRelations());
        final Iterable<ComplexBuilding> lbrBuildings = new ComplexBuildingFinder()
                .find(lbrSlicedAtlas, Finder::ignore);
        Assert.assertEquals(1, Iterables.size(lbrBuildings));

        final SortedSet<String> civSyntheticRelationMembers = new TreeSet<>();
        civSlicedAtlas.lines().forEach(line ->
        {
            if (line.getTag(SyntheticGeometrySlicedTag.KEY).isPresent())
            {
                final Area rawArea = rawAtlas.areas(rawAreaCandidate -> rawAreaCandidate
                        .getOsmIdentifier() == line.getOsmIdentifier()).iterator().next();
                Assert.assertEquals(rawArea.getOsmTags(), line.getOsmTags());
            }
            else
            {
                Assert.assertTrue(line.getTag(SyntheticSyntheticRelationMemberTag.KEY).isPresent());
                civSyntheticRelationMembers.add(Long.toString(line.getIdentifier()));
            }
        });

        final SortedSet<String> lbrSyntheticRelationMembers = new TreeSet<>();
        lbrSlicedAtlas.lines().forEach(line ->
        {
            if (line.getTag(SyntheticGeometrySlicedTag.KEY).isPresent())
            {
                final Area rawArea = rawAtlas.areas(rawAreaCandidate -> rawAreaCandidate
                        .getOsmIdentifier() == line.getOsmIdentifier()).iterator().next();
                Assert.assertEquals(rawArea.getOsmTags(), line.getOsmTags());
            }
            else if (rawAtlas.line(line.getIdentifier()) != null)
            {
                Assert.assertEquals(rawAtlas.line(line.getIdentifier()).getOsmTags(),
                        line.getOsmTags());
                Assert.assertEquals(rawAtlas.line(line.getIdentifier()).asPolyLine(),
                        line.asPolyLine());
                Assert.assertEquals(1, line.relations().size());
            }
            else
            {
                Assert.assertTrue(line.getTag(SyntheticSyntheticRelationMemberTag.KEY).isPresent());
                lbrSyntheticRelationMembers.add(Long.toString(line.getIdentifier()));
            }
        });

        final CountrySlicingIdentifierFactory relationIdentifierFactory = new CountrySlicingIdentifierFactory(
                0L);

        final Relation civRelation = civSlicedAtlas
                .relation(relationIdentifierFactory.nextIdentifier());
        final Relation lbrRelation = lbrSlicedAtlas
                .relation(relationIdentifierFactory.nextIdentifier());

        Assert.assertEquals("CIV", civRelation.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                civRelation.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(
                String.join(SyntheticRelationMemberAdded.MEMBER_DELIMITER,
                        civSyntheticRelationMembers),
                civRelation.getTag(SyntheticRelationMemberAdded.KEY).get());
        Assert.assertEquals("5,1000,108752000000", civRelation
                .getTag(SyntheticInvalidMultiPolygonRelationMembersRemovedTag.KEY).get());

        Assert.assertEquals("LBR", lbrRelation.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                lbrRelation.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(
                String.join(SyntheticRelationMemberAdded.MEMBER_DELIMITER,
                        lbrSyntheticRelationMembers),
                lbrRelation.getTag(SyntheticRelationMemberAdded.KEY).get());
        Assert.assertEquals("5,2000,108752000000", lbrRelation
                .getTag(SyntheticInvalidMultiPolygonRelationMembersRemovedTag.KEY).get());
    }

    /**
     * This relation is made up of open lines, tied together by a relation to create a MultiPolygon
     * with the outer spanning two countries and the inner fully inside one country.
     */
    @Test
    public void testMultiPolygonWithOpenLinesSpanningTwoCountries()
    {

        final Atlas rawAtlas = this.setup.getComplexMultiPolygonWithHoleUsingOpenLinesAtlas();
        final Atlas civSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("CIV"),
                rawAtlas).slice();
        final Atlas lbrSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();

        Assert.assertEquals(3, civSlicedAtlas.numberOfLines());
        Assert.assertEquals(0, civSlicedAtlas.numberOfAreas());
        Assert.assertEquals(0, civSlicedAtlas.numberOfPoints());
        Assert.assertEquals(1, civSlicedAtlas.numberOfRelations());
        final Iterable<ComplexWaterEntity> civWaterEntities = new ComplexWaterEntityFinder()
                .find(civSlicedAtlas, Finder::ignore);
        Assert.assertEquals(1, Iterables.size(civWaterEntities));

        Assert.assertEquals(5, lbrSlicedAtlas.numberOfLines());
        Assert.assertEquals(0, lbrSlicedAtlas.numberOfAreas());
        Assert.assertEquals(0, lbrSlicedAtlas.numberOfPoints());
        Assert.assertEquals(1, lbrSlicedAtlas.numberOfRelations());
        final Iterable<ComplexWaterEntity> lbrWaterEntities = new ComplexWaterEntityFinder()
                .find(lbrSlicedAtlas, Finder::ignore);
        Assert.assertEquals(1, Iterables.size(lbrWaterEntities));

        final SortedSet<String> civSyntheticRelationMembers = new TreeSet<>();
        civSlicedAtlas.lines().forEach(line ->
        {
            if (line.getTag(SyntheticGeometrySlicedTag.KEY).isPresent())
            {
                final Line rawLine = rawAtlas.lines(rawLineCandidate -> rawLineCandidate
                        .getOsmIdentifier() == line.getOsmIdentifier()).iterator().next();
                Assert.assertEquals(rawLine.getOsmTags(), line.getOsmTags());
                Assert.assertEquals(1, line.relations().size());
            }
            else
            {
                Assert.assertTrue(line.getTag(SyntheticSyntheticRelationMemberTag.KEY).isPresent());
                civSyntheticRelationMembers.add(Long.toString(line.getIdentifier()));
            }
        });

        final SortedSet<String> lbrSyntheticRelationMembers = new TreeSet<>();
        lbrSlicedAtlas.lines().forEach(line ->
        {
            if (line.getTag(SyntheticGeometrySlicedTag.KEY).isPresent())
            {
                final Line rawLine = rawAtlas.lines(rawLineCandidate -> rawLineCandidate
                        .getOsmIdentifier() == line.getOsmIdentifier()).iterator().next();
                Assert.assertEquals(rawLine.getOsmTags(), line.getOsmTags());
                Assert.assertEquals(1, line.relations().size());
            }
            else if (rawAtlas.line(line.getIdentifier()) != null)
            {
                Assert.assertEquals(rawAtlas.line(line.getIdentifier()).getOsmTags(),
                        line.getOsmTags());
                Assert.assertEquals(rawAtlas.line(line.getIdentifier()).asPolyLine(),
                        line.asPolyLine());
                Assert.assertEquals(1, line.relations().size());
            }
            else
            {
                Assert.assertTrue(line.getTag(SyntheticSyntheticRelationMemberTag.KEY).isPresent());
                lbrSyntheticRelationMembers.add(Long.toString(line.getIdentifier()));
            }
        });

        final CountrySlicingIdentifierFactory relationIdentifierFactory = new CountrySlicingIdentifierFactory(
                214805000000L);

        final Relation civRelation = civSlicedAtlas
                .relation(relationIdentifierFactory.nextIdentifier());
        final Relation lbrRelation = lbrSlicedAtlas
                .relation(relationIdentifierFactory.nextIdentifier());

        Assert.assertEquals("CIV", civRelation.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                civRelation.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(
                String.join(SyntheticRelationMemberAdded.MEMBER_DELIMITER,
                        civSyntheticRelationMembers),
                civRelation.getTag(SyntheticRelationMemberAdded.KEY).get());
        Assert.assertTrue(civRelation
                .getTag(SyntheticInvalidMultiPolygonRelationMembersRemovedTag.KEY).isEmpty());

        Assert.assertEquals("LBR", lbrRelation.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                lbrRelation.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(
                String.join(SyntheticRelationMemberAdded.MEMBER_DELIMITER,
                        lbrSyntheticRelationMembers),
                lbrRelation.getTag(SyntheticRelationMemberAdded.KEY).get());
        Assert.assertTrue(lbrRelation
                .getTag(SyntheticInvalidMultiPolygonRelationMembersRemovedTag.KEY).isEmpty());
    }

    @Test
    public void testMultiPolygonWithOverlappingSlicedInners()
    {
        final Atlas rawAtlas = this.setup.getMultiPolygonWithOverlappingSlicedInners();
        final Atlas lbrSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();
        lbrSlicedAtlas.relations().forEach(relation ->
        {
            Assert.assertTrue(relation.getTag(SyntheticInvalidGeometryTag.KEY).isPresent());
            Assert.assertFalse(relation.getTag(SyntheticGeometrySlicedTag.KEY).isPresent());
        });
        final Atlas civSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("CIV"),
                rawAtlas).slice();
        civSlicedAtlas.relations().forEach(relation ->
        {
            Assert.assertTrue(relation.getTag(SyntheticInvalidGeometryTag.KEY).isPresent());
            Assert.assertFalse(relation.getTag(SyntheticGeometrySlicedTag.KEY).isPresent());
        });
    }

    @Test
    public void testMultiPolygonWithOverlappingUnslicedInners()
    {
        final Atlas rawAtlas = this.setup.getMultiPolygonWithOverlappingUnslicedInners();
        final Atlas lbrSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();
        lbrSlicedAtlas.relations().forEach(relation ->
        {
            Assert.assertFalse(relation.getTag(SyntheticInvalidGeometryTag.KEY).isPresent());
            Assert.assertTrue(relation.getTag(SyntheticGeometrySlicedTag.KEY).isPresent());
        });
        final Atlas civSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("CIV"),
                rawAtlas).slice();
        civSlicedAtlas.relations().forEach(relation ->
        {
            Assert.assertFalse(relation.getTag(SyntheticInvalidGeometryTag.KEY).isPresent());
            Assert.assertTrue(relation.getTag(SyntheticGeometrySlicedTag.KEY).isPresent());
        });
    }

    /**
     * This relation is made up of a single open line straddling the country boundary, which is the
     * sole member in the relation with outer role. This is an invalid multipolygon, since it isn't
     * closed. We expect slicing to add a new point, on the boundary and create a relation for each
     * country, holding a piece of the sliced line in each one.
     */
    @Test
    public void testOpenMultiPolygonRelationAcrossBoundary()
    {
        final Atlas rawAtlas = this.setup.getOpenMultiPolygonAcrossBoundaryAtlas();
        final Atlas civSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("CIV"),
                rawAtlas).slice();
        final Atlas lbrSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();

        Assert.assertEquals(0, civSlicedAtlas.numberOfPoints());
        Assert.assertEquals(1, civSlicedAtlas.numberOfLines());
        Assert.assertEquals(0, civSlicedAtlas.numberOfAreas());
        Assert.assertEquals(1, civSlicedAtlas.numberOfRelations());
        new ComplexBuildingFinder().find(civSlicedAtlas)
                .forEach(building -> Assert.assertTrue(building.getError().isPresent()));

        Assert.assertEquals(0, lbrSlicedAtlas.numberOfPoints());
        Assert.assertEquals(1, lbrSlicedAtlas.numberOfLines());
        Assert.assertEquals(0, lbrSlicedAtlas.numberOfAreas());
        Assert.assertEquals(1, lbrSlicedAtlas.numberOfRelations());
        new ComplexBuildingFinder().find(lbrSlicedAtlas)
                .forEach(building -> Assert.assertTrue(building.getError().isPresent()));

        final Relation civRelation = civSlicedAtlas.relation(1);
        Assert.assertEquals(1, civRelation.members().size());
        Assert.assertEquals("CIV", civRelation.getTag(ISOCountryTag.KEY).get());

        final Relation lbrRelation = lbrSlicedAtlas.relation(1);
        Assert.assertEquals(1, lbrRelation.members().size());
        Assert.assertEquals("LBR", lbrRelation.getTag(ISOCountryTag.KEY).get());
    }

    /**
     * This relation is made up of a single open line inside LBR, which is the sole member in the
     * relation with outer role. This is an invalid multipolygon, since it isn't closed. We expect
     * slicing to add country codes to all points/lines/relations, but leave the geometry and roles
     * unchanged.
     */
    @Test
    public void testOpenMultiPolygonRelationInOneCountry()
    {

        final Atlas rawAtlas = this.setup.getOpenMultiPolygonInOneCountryAtlas();
        final Atlas lbrSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();

        Assert.assertEquals(0, lbrSlicedAtlas.numberOfPoints());
        Assert.assertEquals(1, lbrSlicedAtlas.numberOfLines());
        Assert.assertEquals(0, lbrSlicedAtlas.numberOfAreas());
        Assert.assertEquals(1, lbrSlicedAtlas.numberOfRelations());
        new ComplexBuildingFinder().find(lbrSlicedAtlas)
                .forEach(building -> Assert.assertTrue(building.getError().isPresent()));

        Assert.assertTrue(Iterables.stream(lbrSlicedAtlas.entities())
                .allMatch(entity -> entity.getTag(ISOCountryTag.KEY).get().equals("LBR")
                        && entity.getTag(SyntheticGeometrySlicedTag.KEY).isEmpty()));
    }

    /**
     * This relation is made up of two lines. The first one is a closed line on the LBR side. The
     * second is an open line spanning the boundary of LBR and CIV. Both lines are outer members in
     * a relation. We expect slicing to leave the closed line and to cut the open line as well as
     * create a new relation on the CIV side with a piece of the outer open line as a single member.
     */
    @Test
    public void testRelationWithOneClosedAndOpenMember()
    {
        final Atlas rawAtlas = this.setup.getRelationWithOneClosedAndOneOpenMemberAtlas();
        final Atlas civSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("CIV"),
                rawAtlas).slice();
        final Atlas lbrSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();

        Assert.assertEquals(0, civSlicedAtlas.numberOfPoints());
        Assert.assertEquals(1, civSlicedAtlas.numberOfLines());
        Assert.assertEquals(0, civSlicedAtlas.numberOfAreas());
        Assert.assertEquals(1, civSlicedAtlas.numberOfRelations());
        new ComplexBuildingFinder().find(civSlicedAtlas)
                .forEach(building -> Assert.assertTrue(building.getError().isPresent()));

        Assert.assertEquals(0, lbrSlicedAtlas.numberOfPoints());
        Assert.assertEquals(2, lbrSlicedAtlas.numberOfLines());
        Assert.assertEquals(0, lbrSlicedAtlas.numberOfAreas());
        Assert.assertEquals(1, lbrSlicedAtlas.numberOfRelations());
        new ComplexBuildingFinder().find(lbrSlicedAtlas)
                .forEach(building -> Assert.assertTrue(building.getError().isPresent()));
    }

    /**
     * This relation is made up of two lines. The first is a closed line, with an inner role, fully
     * on the LBR side. The second is a self-intersecting closed outer, stretching across the
     * boundary. Since that line is invalid geometry, it should not be sliced and instead be tagged
     * with both CIV and LBR country tags, and this should propogate up to the unsliced relation
     * which should have the same country tags.
     */
    @Test
    public void testSelfIntersectingOuterRelationAcrossBoundary()
    {
        final Atlas rawAtlas = this.setup
                .getSelfIntersectingOuterMemberRelationAcrossBoundaryAtlas();
        final Atlas civSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("CIV"),
                rawAtlas).slice();
        final Atlas lbrSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();

        Assert.assertEquals(0, civSlicedAtlas.numberOfPoints());
        Assert.assertEquals(0, civSlicedAtlas.numberOfLines());
        Assert.assertEquals(1, civSlicedAtlas.numberOfAreas());
        Assert.assertEquals(1, civSlicedAtlas.numberOfRelations());
        Assert.assertEquals("CIV,LBR",
                civSlicedAtlas.area(108768000000L).getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(rawAtlas.area(108768000000L).asPolygon(),
                civSlicedAtlas.area(108768000000L).asPolygon());
        Assert.assertEquals("CIV,LBR", civSlicedAtlas.relation(1).getTag(ISOCountryTag.KEY).get());
        Assert.assertTrue(
                civSlicedAtlas.relation(1).getTag(SyntheticGeometrySlicedTag.KEY).isEmpty());
        Assert.assertTrue(
                civSlicedAtlas.relation(1).getTag(SyntheticRelationMemberAdded.KEY).isEmpty());
        Assert.assertTrue(civSlicedAtlas.relation(1)
                .getTag(SyntheticInvalidMultiPolygonRelationMembersRemovedTag.KEY).isEmpty());

        Assert.assertEquals(0, lbrSlicedAtlas.numberOfPoints());
        Assert.assertEquals(0, lbrSlicedAtlas.numberOfLines());
        Assert.assertEquals(2, lbrSlicedAtlas.numberOfAreas());
        Assert.assertEquals(1, lbrSlicedAtlas.numberOfRelations());
        Assert.assertEquals("CIV,LBR",
                lbrSlicedAtlas.area(108768000000L).getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(rawAtlas.area(108768000000L).asPolygon(),
                lbrSlicedAtlas.area(108768000000L).asPolygon());
        Assert.assertEquals("CIV,LBR", lbrSlicedAtlas.relation(1).getTag(ISOCountryTag.KEY).get());
        Assert.assertTrue(
                lbrSlicedAtlas.relation(1).getTag(SyntheticGeometrySlicedTag.KEY).isEmpty());
        Assert.assertTrue(
                lbrSlicedAtlas.relation(1).getTag(SyntheticRelationMemberAdded.KEY).isEmpty());
        Assert.assertTrue(lbrSlicedAtlas.relation(1)
                .getTag(SyntheticInvalidMultiPolygonRelationMembersRemovedTag.KEY).isEmpty());
    }

    /**
     * This relation is made up of two lines. Both lines are on the LBR side. The first is a closed
     * line, with an inner role. The second is a self-intersecting closed outer. We expect no
     * slicing or merging to take place, other than country code assignment. The line with invalid
     * geometry will get tagged as CIV,LBR due to a quirk in geometry polygon handling, but this is
     * not very concerning as invalid geometry is not expected to be fully supported by slicing--
     * the country codes here are a best guess.
     */
    @Test
    public void testSelfIntersectingOuterRelationInOneCountry()
    {
        final Atlas rawAtlas = this.setup.getSelfIntersectingOuterMemberRelationAtlas();
        final Atlas lbrSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();

        // Assert that we CAN build a valid building with this relation
        new ComplexBuildingFinder().find(lbrSlicedAtlas)
                .forEach(building -> Assert.assertFalse(building.getError().isPresent()));

        Assert.assertEquals(0, lbrSlicedAtlas.numberOfPoints());
        Assert.assertEquals(rawAtlas.numberOfLines(), lbrSlicedAtlas.numberOfLines());
        Assert.assertEquals(rawAtlas.numberOfRelations(), lbrSlicedAtlas.numberOfRelations());
        Assert.assertTrue(Iterables.stream(lbrSlicedAtlas.entities())
                .allMatch(entity -> (entity.getTag(ISOCountryTag.KEY).get().equals("LBR")
                        || entity.getTag(ISOCountryTag.KEY).get().equals("CIV,LBR"))
                        && entity.getTag(SyntheticGeometrySlicedTag.KEY).isEmpty()));
    }

    /**
     * This relation is made up of two closed lines, forming a multi-polygon with one inner and one
     * outer, both spanning the boundary of two countries.
     */
    @Test
    public void testSimpleMultiPolygonWithHoleSpanningTwoCountries()
    {
        final Atlas rawAtlas = this.setup.getSimpleMultiPolygonWithHoleAtlas();
        final Atlas civSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("CIV"),
                rawAtlas).slice();
        final Atlas lbrSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();

        Assert.assertEquals(0, civSlicedAtlas.numberOfPoints());
        Assert.assertEquals(4, civSlicedAtlas.numberOfLines());
        Assert.assertEquals(2, civSlicedAtlas.numberOfAreas());
        Assert.assertEquals(1, civSlicedAtlas.numberOfRelations());
        final Iterable<ComplexBuilding> civBuildings = new ComplexBuildingFinder()
                .find(civSlicedAtlas, Finder::ignore);
        Assert.assertEquals(1, Iterables.size(civBuildings));

        Assert.assertEquals(0, lbrSlicedAtlas.numberOfPoints());
        Assert.assertEquals(4, lbrSlicedAtlas.numberOfLines());
        Assert.assertEquals(2, lbrSlicedAtlas.numberOfAreas());
        Assert.assertEquals(1, lbrSlicedAtlas.numberOfRelations());
        final Iterable<ComplexBuilding> lbrBuildings = new ComplexBuildingFinder()
                .find(lbrSlicedAtlas, Finder::ignore);
        Assert.assertEquals(1, Iterables.size(lbrBuildings));

        final SortedSet<String> civSyntheticRelationMembers = new TreeSet<>();
        civSlicedAtlas.lines().forEach(line ->
        {
            if (line.getTag(SyntheticGeometrySlicedTag.KEY).isPresent())
            {
                final Area rawArea = rawAtlas.areas(rawAreaCandidate -> rawAreaCandidate
                        .getOsmIdentifier() == line.getOsmIdentifier()).iterator().next();
                Assert.assertEquals(rawArea.getOsmTags(), line.getOsmTags());
                Assert.assertEquals(1, line.relations().size());
            }
            else
            {
                Assert.assertTrue(line.getTag(SyntheticSyntheticRelationMemberTag.KEY).isPresent());
                civSyntheticRelationMembers.add(Long.toString(line.getIdentifier()));
            }
        });

        final SortedSet<String> lbrSyntheticRelationMembers = new TreeSet<>();
        lbrSlicedAtlas.lines().forEach(line ->
        {
            if (line.getTag(SyntheticGeometrySlicedTag.KEY).isPresent())
            {
                final Area rawArea = rawAtlas.areas(rawAreaCandidate -> rawAreaCandidate
                        .getOsmIdentifier() == line.getOsmIdentifier()).iterator().next();
                Assert.assertEquals(rawArea.getOsmTags(), line.getOsmTags());
                Assert.assertEquals(1, line.relations().size());
            }
            else if (rawAtlas.line(line.getIdentifier()) != null)
            {
                Assert.assertEquals(rawAtlas.line(line.getIdentifier()).getOsmTags(),
                        line.getOsmTags());
                Assert.assertEquals(rawAtlas.line(line.getIdentifier()).asPolyLine(),
                        line.asPolyLine());
                Assert.assertEquals(1, line.relations().size());
            }
            else
            {
                Assert.assertTrue(line.getTag(SyntheticSyntheticRelationMemberTag.KEY).isPresent());
                lbrSyntheticRelationMembers.add(Long.toString(line.getIdentifier()));
            }
        });

        final CountrySlicingIdentifierFactory relationIdentifierFactory = new CountrySlicingIdentifierFactory(
                0L);

        final Relation civRelation = civSlicedAtlas
                .relation(relationIdentifierFactory.nextIdentifier());
        final Relation lbrRelation = lbrSlicedAtlas
                .relation(relationIdentifierFactory.nextIdentifier());

        Assert.assertEquals("CIV", civRelation.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                civRelation.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(
                String.join(SyntheticRelationMemberAdded.MEMBER_DELIMITER,
                        civSyntheticRelationMembers),
                civRelation.getTag(SyntheticRelationMemberAdded.KEY).get());
        Assert.assertTrue(civRelation
                .getTag(SyntheticInvalidMultiPolygonRelationMembersRemovedTag.KEY).isEmpty());

        Assert.assertEquals("LBR", lbrRelation.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                lbrRelation.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(
                String.join(SyntheticRelationMemberAdded.MEMBER_DELIMITER,
                        lbrSyntheticRelationMembers),
                lbrRelation.getTag(SyntheticRelationMemberAdded.KEY).get());
        Assert.assertTrue(lbrRelation
                .getTag(SyntheticInvalidMultiPolygonRelationMembersRemovedTag.KEY).isEmpty());
    }

    /**
     * This simple test covers the case of a line with invalid geometry due to only having one node,
     * repeated
     */
    @Test
    public void testSingleNodeLine()
    {
        final Atlas rawAtlas = this.setup.getSingleNodeLine();
        final Atlas lbrSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();
        Assert.assertEquals(0, lbrSlicedAtlas.numberOfPoints());
        Assert.assertEquals(2, lbrSlicedAtlas.numberOfLines());
        Assert.assertEquals(0, lbrSlicedAtlas.numberOfAreas());
        Assert.assertEquals(0, lbrSlicedAtlas.numberOfRelations());

        Assert.assertTrue(Iterables.stream(lbrSlicedAtlas.entities())
                .allMatch(entity -> (entity.getTag(ISOCountryTag.KEY).get().equals("LBR")
                        || entity.getTag(ISOCountryTag.KEY).get().equals("CIV,LBR"))
                        && entity.getTag(SyntheticGeometrySlicedTag.KEY).isEmpty()));
    }

    /**
     * This relation is made up of two open lines, both Edges, both crossing the country boundary
     * and forming a multipolygon with one outer.
     */
    @Test
    public void testSingleOuterMadeOfOpenLinesSpanningTwoCountries()
    {
        final Atlas rawAtlas = this.setup.getSingleOuterMadeOfOpenLinesSpanningTwoCountriesAtlas();
        final Atlas civSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("CIV"),
                rawAtlas).slice();
        final Atlas lbrSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();

        Assert.assertEquals(3, civSlicedAtlas.numberOfPoints());
        Assert.assertEquals(3, civSlicedAtlas.numberOfLines());
        Assert.assertEquals(0, civSlicedAtlas.numberOfAreas());
        Assert.assertEquals(1, civSlicedAtlas.numberOfRelations());

        Assert.assertEquals(8, lbrSlicedAtlas.numberOfPoints());
        Assert.assertEquals(3, lbrSlicedAtlas.numberOfLines());
        Assert.assertEquals(0, lbrSlicedAtlas.numberOfAreas());
        Assert.assertEquals(1, lbrSlicedAtlas.numberOfRelations());

        final SortedSet<String> civSyntheticRelationMembers = new TreeSet<>();
        civSlicedAtlas.lines().forEach(line ->
        {
            if (line.getTag(SyntheticGeometrySlicedTag.KEY).isPresent())
            {
                final Line rawLine = rawAtlas.lines(rawLineCandidate -> rawLineCandidate
                        .getOsmIdentifier() == line.getOsmIdentifier()).iterator().next();
                Assert.assertEquals(rawLine.getOsmTags(), line.getOsmTags());
                Assert.assertEquals(1, line.relations().size());
            }
            else
            {
                Assert.assertTrue(line.getTag(SyntheticSyntheticRelationMemberTag.KEY).isPresent());
                civSyntheticRelationMembers.add(Long.toString(line.getIdentifier()));
            }
        });

        final SortedSet<String> lbrSyntheticRelationMembers = new TreeSet<>();
        lbrSlicedAtlas.lines().forEach(line ->
        {
            if (line.getTag(SyntheticGeometrySlicedTag.KEY).isPresent())
            {
                final Line rawLine = rawAtlas.lines(rawLineCandidate -> rawLineCandidate
                        .getOsmIdentifier() == line.getOsmIdentifier()).iterator().next();
                Assert.assertEquals(rawLine.getOsmTags(), line.getOsmTags());
                Assert.assertEquals(1, line.relations().size());
            }
            else if (rawAtlas.line(line.getIdentifier()) != null)
            {
                Assert.assertEquals(rawAtlas.line(line.getIdentifier()).getOsmTags(),
                        line.getOsmTags());
                Assert.assertEquals(rawAtlas.line(line.getIdentifier()).asPolyLine(),
                        line.asPolyLine());
                Assert.assertEquals(1, line.relations().size());
            }
            else
            {
                Assert.assertTrue(line.getTag(SyntheticSyntheticRelationMemberTag.KEY).isPresent());
                lbrSyntheticRelationMembers.add(Long.toString(line.getIdentifier()));
            }
        });

        final CountrySlicingIdentifierFactory relationIdentifierFactory = new CountrySlicingIdentifierFactory(
                214805000000L);
        final Relation civRelation = civSlicedAtlas
                .relation(relationIdentifierFactory.nextIdentifier());
        Assert.assertTrue(jtsConverter.backwardConvert(converter.convert(civRelation)).isValid());
        Assert.assertEquals("CIV", civRelation.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                civRelation.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(
                String.join(SyntheticRelationMemberAdded.MEMBER_DELIMITER,
                        civSyntheticRelationMembers),
                civRelation.getTag(SyntheticRelationMemberAdded.KEY).get());
        Assert.assertTrue(civRelation
                .getTag(SyntheticInvalidMultiPolygonRelationMembersRemovedTag.KEY).isEmpty());

        final Relation lbrRelation = lbrSlicedAtlas
                .relation(relationIdentifierFactory.nextIdentifier());
        Assert.assertTrue(jtsConverter.backwardConvert(converter.convert(lbrRelation)).isValid());
        Assert.assertEquals("LBR", lbrRelation.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                lbrRelation.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(
                String.join(SyntheticRelationMemberAdded.MEMBER_DELIMITER,
                        lbrSyntheticRelationMembers),
                lbrRelation.getTag(SyntheticRelationMemberAdded.KEY).get());
        Assert.assertTrue(lbrRelation
                .getTag(SyntheticInvalidMultiPolygonRelationMembersRemovedTag.KEY).isEmpty());
    }

    /**
     * This relation is made up of two open lines, both crossing the country boundary and forming a
     * multipolygon with one outer.
     */
    @Test
    public void testSingleOuterMadeOfOpenLinesSpanningTwoCountriesWithDuplicatePoints()
    {

        final Atlas rawAtlas = this.setup
                .getSingleOuterMadeOfOpenLinesSpanningTwoCountriesAtlasWithDuplicatePoints();
        final Atlas civSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("CIV"),
                rawAtlas).slice();
        final Atlas lbrSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();

        Assert.assertEquals(3, civSlicedAtlas.numberOfPoints());
        Assert.assertEquals(3, civSlicedAtlas.numberOfLines());
        Assert.assertEquals(0, civSlicedAtlas.numberOfAreas());
        Assert.assertEquals(1, civSlicedAtlas.numberOfRelations());

        Assert.assertEquals(8, lbrSlicedAtlas.numberOfPoints());
        Assert.assertEquals(3, lbrSlicedAtlas.numberOfLines());
        Assert.assertEquals(0, lbrSlicedAtlas.numberOfAreas());
        Assert.assertEquals(1, lbrSlicedAtlas.numberOfRelations());

        final SortedSet<String> civSyntheticRelationMembers = new TreeSet<>();
        civSlicedAtlas.lines().forEach(line ->
        {
            final Iterator<Location> lineLocations = line.iterator();
            Location previous = lineLocations.next();
            while (lineLocations.hasNext())
            {
                final Location current = lineLocations.next();
                Assert.assertNotEquals(current, previous);
                previous = current;
            }

            if (line.getTag(SyntheticGeometrySlicedTag.KEY).isPresent())
            {
                final Line rawLine = rawAtlas.lines(rawLineCandidate -> rawLineCandidate
                        .getOsmIdentifier() == line.getOsmIdentifier()).iterator().next();
                Assert.assertEquals(rawLine.getOsmTags(), line.getOsmTags());
                Assert.assertEquals(1, line.relations().size());
            }
            else
            {
                Assert.assertTrue(line.getTag(SyntheticSyntheticRelationMemberTag.KEY).isPresent());
                civSyntheticRelationMembers.add(Long.toString(line.getIdentifier()));
            }
        });

        final SortedSet<String> lbrSyntheticRelationMembers = new TreeSet<>();
        lbrSlicedAtlas.lines().forEach(line ->
        {
            final Iterator<Location> lineLocations = line.iterator();
            Location previous = lineLocations.next();
            while (lineLocations.hasNext())
            {
                final Location current = lineLocations.next();
                Assert.assertNotEquals(current, previous);
                previous = current;
            }

            if (line.getTag(SyntheticGeometrySlicedTag.KEY).isPresent())
            {
                final Line rawLine = rawAtlas.lines(rawLineCandidate -> rawLineCandidate
                        .getOsmIdentifier() == line.getOsmIdentifier()).iterator().next();
                Assert.assertEquals(rawLine.getOsmTags(), line.getOsmTags());
                Assert.assertEquals(1, line.relations().size());
            }
            else if (rawAtlas.line(line.getIdentifier()) != null)
            {
                Assert.assertEquals(rawAtlas.line(line.getIdentifier()).getOsmTags(),
                        line.getOsmTags());
                Assert.assertEquals(rawAtlas.line(line.getIdentifier()).asPolyLine(),
                        line.asPolyLine());
                Assert.assertEquals(1, line.relations().size());
            }
            else
            {
                Assert.assertTrue(line.getTag(SyntheticSyntheticRelationMemberTag.KEY).isPresent());
                lbrSyntheticRelationMembers.add(Long.toString(line.getIdentifier()));
            }
        });

        final CountrySlicingIdentifierFactory relationIdentifierFactory = new CountrySlicingIdentifierFactory(
                214805000000L);
        final Relation civRelation = civSlicedAtlas
                .relation(relationIdentifierFactory.nextIdentifier());
        Assert.assertTrue(jtsConverter.backwardConvert(converter.convert(civRelation)).isValid());
        Assert.assertEquals("CIV", civRelation.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                civRelation.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(
                String.join(SyntheticRelationMemberAdded.MEMBER_DELIMITER,
                        civSyntheticRelationMembers),
                civRelation.getTag(SyntheticRelationMemberAdded.KEY).get());
        Assert.assertTrue(civRelation
                .getTag(SyntheticInvalidMultiPolygonRelationMembersRemovedTag.KEY).isEmpty());

        final Relation lbrRelation = lbrSlicedAtlas
                .relation(relationIdentifierFactory.nextIdentifier());
        Assert.assertTrue(jtsConverter.backwardConvert(converter.convert(lbrRelation)).isValid());
        Assert.assertEquals("LBR", lbrRelation.getTag(ISOCountryTag.KEY).get());
        Assert.assertEquals(SyntheticGeometrySlicedTag.YES.toString(),
                lbrRelation.getTag(SyntheticGeometrySlicedTag.KEY).get());
        Assert.assertEquals(
                String.join(SyntheticRelationMemberAdded.MEMBER_DELIMITER,
                        lbrSyntheticRelationMembers),
                lbrRelation.getTag(SyntheticRelationMemberAdded.KEY).get());
        Assert.assertTrue(lbrRelation
                .getTag(SyntheticInvalidMultiPolygonRelationMembersRemovedTag.KEY).isEmpty());

    }

    @Test
    public void testSlicingOnRelationWithOnlyRelationsAsMembers()
    {
        final Atlas rawAtlas = this.setup.getRelationWithOnlyRelationsAsMembers();
        final Atlas lbrSlicedAtlas = new RawAtlasSlicer(
                AtlasLoadingOption.createOptionWithAllEnabled(boundary).setCountryCode("LBR"),
                rawAtlas).slice();
        for (final Relation relation : lbrSlicedAtlas.relations())
        {
            Assert.assertEquals("LBR", relation.getTag(ISOCountryTag.KEY).get());
        }
    }
}
