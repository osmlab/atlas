package org.openstreetmap.atlas.geography.clipping;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.GeometricObject;
import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.MultiPolygonTest;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;

/**
 * @author matthieun
 */
public class GeometryOperationTest
{
    private final MultiPolygon multiPolygon1 = MultiPolygon.forOuters(Polygon.wkt(
            "POLYGON ((55.4529644 -4.6425404, 55.4527599 -4.6423107, 55.4524513 -4.6423687, 55.452295 -4.6424066,"
                    + " 55.4521828 -4.6425984, 55.4519884 -4.642868, 55.4520144 -4.6430778, 55.4522369 -4.6431817,"
                    + " 55.4525876 -4.6431737, 55.4528842 -4.6430798, 55.4530065 -4.6429939, 55.4528762 -4.6428181,"
                    + " 55.4528862 -4.6426603, 55.4529644 -4.6425404))"));
    private final MultiPolygon multiPolygon2 = MultiPolygon.forOuters(Polygon.wkt(
            "POLYGON ((55.4704179 -4.6550696, 55.4722205 -4.6581544, 55.4702705 -4.6603804, 55.467663 -4.6590245,"
                    + " 55.4665973 -4.6614991, 55.4665025 -4.6616483, 55.466354 -4.6617547, 55.465372 -4.662019,"
                    + " 55.4643567 -4.6610388, 55.4631883 -4.6606733, 55.4629301 -4.6609095, 55.4626936 -4.6606842,"
                    + " 55.4627858 -4.659018, 55.463193 -4.6585292, 55.4628348 -4.65804, 55.4629354 -4.656064,"
                    + " 55.4621532 -4.654595, 55.4659511 -4.6540752, 55.4661665 -4.6560414, 55.46891 -4.6564482,"
                    + " 55.4691254 -4.6553182, 55.4684792 -4.6544368, 55.4694089 -4.6539848, 55.4704179 -4.6550696))"));
    private final MultiPolygon multiPolygon3 = MultiPolygon.forOuters(Polygon.wkt(
            "POLYGON ((55.4203385 -4.6274556, 55.4209807 -4.6276984, 55.4211268 -4.6274921, 55.4213636 -4.6271103,"
                    + " 55.4213314 -4.6269499, 55.4209505 -4.6268376, 55.4207682 -4.6270421, 55.4203122 -4.6268858,"
                    + " 55.4203712 -4.6265181, 55.4201352 -4.6261051, 55.4203658 -4.6257736, 55.4200493 -4.6253365,"
                    + " 55.419411 -4.6250624, 55.4188101 -4.6245759, 55.417807 -4.6243847, 55.4172652 -4.6247896,"
                    + " 55.4174315 -4.62495, 55.41844 -4.626383, 55.4192661 -4.6270567, 55.4203385 -4.6274556))"));
    private final MultiPolygon multiPolygon4 = MultiPolygon.forOuters(Polygon.wkt(
            "POLYGON ((55.4203385 -4.6274556, 55.4199796 -4.6284897, 55.419346 -4.6289794, 55.4195558 -4.6294307,"
                    + " 55.4201512 -4.629666, 55.4204624 -4.6293345, 55.4209344 -4.6283079, 55.4206769 -4.6281207,"
                    + " 55.4209807 -4.6276984, 55.4203385 -4.6274556))"));
    private final MultiPolygon multiPolygon5 = MultiPolygon.forOuters(Polygon.wkt(
            "POLYGON ((55.455052 -4.6419746, 55.4550627 -4.6425627, 55.4547516 -4.6426162, 55.4547355 -4.6429263,"
                    + " 55.4542978 -4.6429263, 55.4534019 -4.6427926, 55.4530065 -4.6429939, 55.4528762 -4.6428181,"
                    + " 55.4528862 -4.6426603, 55.4529644 -4.6425404, 55.4527599 -4.6423107, 55.4524513 -4.6423687,"
                    + " 55.4521123 -4.6420173, 55.4518462 -4.6417019, 55.4515222 -4.6414934, 55.4516639 -4.6412474,"
                    + " 55.4524503 -4.6415682, 55.4530565 -4.6416484, 55.4534824 -4.6417286, 55.4531906 -4.6421243,"
                    + " 55.4532925 -4.642429, 55.4546121 -4.6423007, 55.4546357 -4.6419264, 55.455052 -4.6419746))"));

    @Test
    public void testHuggingPolygons()
    {
        final MultiPolygon multiPolygon = MultiPolygonTest.getFrom("testHuggingPolygons.josm.osm",
                GeometryOperationTest.class);
        final Optional<GeometricObject> resultOption = GeometryOperation
                .intersection(multiPolygon.outers());
        Assert.assertTrue(resultOption.isPresent());
        final GeometricObject result = resultOption.get();
        Assert.assertTrue(result instanceof PolyLine && !(result instanceof Polygon));
        Assert.assertEquals("LINESTRING (5.9349989 43.102859, 5.935294 43.1015547)",
                ((PolyLine) result).toWkt());
    }

    @Test
    public void testMultiplePolygons()
    {
        final GeometricSurface result = GeometryOperation
                .union(this.multiPolygon1, this.multiPolygon2, this.multiPolygon3,
                        this.multiPolygon4, this.multiPolygon5)
                .orElseThrow(() -> new CoreException("fail"));
        Assert.assertEquals(
                "MULTIPOLYGON (((55.455052 -4.6419746, 55.4550627 -4.6425627, 55.4547516 -4.6426162, 55.4547355 -4.6429263,"
                        + " 55.4542978 -4.6429263, 55.4534019 -4.6427926, 55.4530065 -4.6429939, 55.4528842 -4.6430798,"
                        + " 55.4525876 -4.6431737, 55.4522369 -4.6431817, 55.4520144 -4.6430778, 55.4519884 -4.642868,"
                        + " 55.4521828 -4.6425984, 55.452295 -4.6424066, 55.4524513 -4.6423687, 55.4521123 -4.6420173,"
                        + " 55.4518462 -4.6417019, 55.4515222 -4.6414934, 55.4516639 -4.6412474, 55.4524503 -4.6415682,"
                        + " 55.4530565 -4.6416484, 55.4534824 -4.6417286, 55.4531906 -4.6421243, 55.4532925 -4.642429,"
                        + " 55.4546121 -4.6423007, 55.4546357 -4.6419264, 55.455052 -4.6419746)),"
                        + " ((55.4704179 -4.6550696, 55.4722205 -4.6581544, 55.4702705 -4.6603804, 55.467663 -4.6590245,"
                        + " 55.4665973 -4.6614991, 55.4665025 -4.6616483, 55.466354 -4.6617547, 55.465372 -4.662019,"
                        + " 55.4643567 -4.6610388, 55.4631883 -4.6606733, 55.4629301 -4.6609095, 55.4626936 -4.6606842,"
                        + " 55.4627858 -4.659018, 55.463193 -4.6585292, 55.4628348 -4.65804, 55.4629354 -4.656064,"
                        + " 55.4621532 -4.654595, 55.4659511 -4.6540752, 55.4661665 -4.6560414, 55.46891 -4.6564482,"
                        + " 55.4691254 -4.6553182, 55.4684792 -4.6544368, 55.4694089 -4.6539848, 55.4704179 -4.6550696)),"
                        + " ((55.4209807 -4.6276984, 55.4206769 -4.6281207, 55.4209344 -4.6283079, 55.4204624 -4.6293345,"
                        + " 55.4201512 -4.629666, 55.4195558 -4.6294307, 55.419346 -4.6289794, 55.4199796 -4.6284897,"
                        + " 55.4203385 -4.6274556, 55.4192661 -4.6270567, 55.41844 -4.626383, 55.4174315 -4.62495,"
                        + " 55.4172652 -4.6247896, 55.417807 -4.6243847, 55.4188101 -4.6245759, 55.419411 -4.6250624,"
                        + " 55.4200493 -4.6253365, 55.4203658 -4.6257736, 55.4201352 -4.6261051, 55.4203712 -4.6265181,"
                        + " 55.4203122 -4.6268858, 55.4207682 -4.6270421, 55.4209505 -4.6268376, 55.4213314 -4.6269499,"
                        + " 55.4213636 -4.6271103, 55.4211268 -4.6274921, 55.4209807 -4.6276984)))",
                result.toWkt());
    }

    @Test
    public void testOverlappingPolygons()
    {
        final GeometricSurface result = GeometryOperation
                .union(this.multiPolygon1, this.multiPolygon1)
                .orElseThrow(() -> new CoreException("fail"));
        Assert.assertEquals(
                "POLYGON ((55.4527599 -4.6423107, 55.4529644 -4.6425404, 55.4528862 -4.6426603, 55.4528762 -4.6428181,"
                        + " 55.4530065 -4.6429939, 55.4528842 -4.6430798, 55.4525876 -4.6431737, 55.4522369 -4.6431817,"
                        + " 55.4520144 -4.6430778, 55.4519884 -4.642868, 55.4521828 -4.6425984, 55.452295 -4.6424066,"
                        + " 55.4524513 -4.6423687, 55.4527599 -4.6423107))",
                result.toWkt());
    }

    @Test
    public void testOverlappingPolygonsToMultiPolygon()
    {
        final MultiPolygon multiPolygon = MultiPolygonTest.getFrom(
                "testOverlappingPolygonsToMultiPolygon.josm.osm", GeometryOperationTest.class);
        final Optional<GeometricObject> resultOption = GeometryOperation
                .intersection(multiPolygon.outers());
        Assert.assertTrue(resultOption.isPresent());
        final GeometricObject result = resultOption.get();
        Assert.assertTrue(result instanceof MultiPolygon);
        Assert.assertEquals(
                "MULTIPOLYGON (((5.9351685 43.1021092, 5.9352199 43.1018822, 5.9347228 43.1019763, 5.9351685 43.1021092)), "
                        + "((5.9350863 43.1024728, 5.9351358 43.1022541, 5.9344967 43.1023663, 5.9350863 43.1024728)))",
                ((MultiPolygon) result).toWkt());
    }

    @Test
    public void testOverlappingPolygonsToPolygon()
    {
        final MultiPolygon multiPolygon = MultiPolygonTest
                .getFrom("testOverlappingPolygonsToPolygon.josm.osm", GeometryOperationTest.class);
        final Optional<GeometricObject> resultOption = GeometryOperation
                .intersection(multiPolygon.outers());
        Assert.assertTrue(resultOption.isPresent());
        final GeometricObject result = resultOption.get();
        Assert.assertTrue(result instanceof Polygon);
        Assert.assertEquals(
                "POLYGON ((5.9350863 43.1024728, 5.9351896 43.1020163, 5.9344967 43.1023663, 5.9350863 43.1024728))",
                ((Polygon) result).toWkt());
    }

    @Test
    public void testTouchingPolygons()
    {
        final MultiPolygon multiPolygon = MultiPolygonTest.getFrom("testTouchingPolygons.josm.osm",
                GeometryOperationTest.class);
        final Optional<GeometricObject> resultOption = GeometryOperation
                .intersection(multiPolygon.outers());
        Assert.assertTrue(resultOption.isPresent());
        final GeometricObject result = resultOption.get();
        Assert.assertTrue(result instanceof Location);
        Assert.assertEquals("POINT (5.9349989 43.102859)", ((Location) result).toWkt());
    }
}
