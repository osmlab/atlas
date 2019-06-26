package org.openstreetmap.atlas.geography.atlas.items.complex.roundabout;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * Unit test rule for {@link ComplexRoundaboutTest}.
 *
 * @author bbreithaupt
 * @author savannahostrowski
 */
public class ComplexRoundaboutTestRule extends CoreTestRule
{
    private static final String CLOCKWISE_1 = "38.905336130818505,-77.03197002410889";
    private static final String CLOCKWISE_2 = "38.90558660084624,-77.03158378601074";
    private static final String CLOCKWISE_3 = "38.905995699991145,-77.0318305492401";
    private static final String CLOCKWISE_4 = "38.90582872103308,-77.03239917755127";
    private static final String CLOCKWISE_5 = "38.905494761938684,-77.03236699104309";

    private static final String COUNTER_CLOCKWISE_1 = "38.905361177861046,-77.03205585479736";
    private static final String COUNTER_CLOCKWISE_2 = "38.905528157918816,-77.03158378601074";
    private static final String COUNTER_CLOCKWISE_3 = "38.905937257400495,-77.031809091568";
    private static final String COUNTER_CLOCKWISE_4 = "38.90588716371307,-77.03230261802673";
    private static final String COUNTER_CLOCKWISE_5 = "38.90551980892527,-77.03236699104309";

    // Clockwise roundabout, left driving country
    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_5)) },
            // edges
            edges = {
                    @TestAtlas.Edge(id = "1234", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_1),
                            @TestAtlas.Loc(value = CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @TestAtlas.Edge(id = "1235", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_5),
                            @TestAtlas.Loc(value = CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @TestAtlas.Edge(id = "1236", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_4),
                            @TestAtlas.Loc(value = CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @TestAtlas.Edge(id = "1237", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_3),
                            @TestAtlas.Loc(value = CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @TestAtlas.Edge(id = "1238", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_2),
                            @TestAtlas.Loc(value = CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }) })
    private Atlas clockwiseRoundaboutLeftDrivingAtlas;

    // Clockwise roundabout, right driving country
    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_5)) },
            // edges
            edges = {
                    @TestAtlas.Edge(id = "1234", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_1),
                            @TestAtlas.Loc(value = CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @TestAtlas.Edge(id = "1235", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_5),
                            @TestAtlas.Loc(value = CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @TestAtlas.Edge(id = "1236", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_4),
                            @TestAtlas.Loc(value = CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @TestAtlas.Edge(id = "1237", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_3),
                            @TestAtlas.Loc(value = CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @TestAtlas.Edge(id = "1238", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_2),
                            @TestAtlas.Loc(value = CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }) })
    private Atlas clockwiseRoundaboutRightDrivingAtlas;

    // Counterclockwise roundabout, left driving country
    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_5)) },
            // edges
            edges = { @TestAtlas.Edge(id = "1234", coordinates = {
                    @TestAtlas.Loc(value = COUNTER_CLOCKWISE_1),
                    @TestAtlas.Loc(value = COUNTER_CLOCKWISE_2) }, tags = { "junction=roundabout",
                            "iso_country_code=SGP", "highway=primary" }),
                    @TestAtlas.Edge(id = "1235", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_2),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_3) }, tags = {
                                    "junction=roundabout", "iso_country_code=SGP",
                                    "highway=primary" }),
                    @TestAtlas.Edge(id = "1236", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_3),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_4) }, tags = {
                                    "junction=roundabout", "iso_country_code=SGP",
                                    "highway=primary" }),
                    @TestAtlas.Edge(id = "1237", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_4),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_5) }, tags = {
                                    "junction=roundabout", "iso_country_code=SGP",
                                    "highway=primary" }),
                    @TestAtlas.Edge(id = "1238", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_5),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_1) }, tags = {
                                    "junction=roundabout", "iso_country_code=SGP",
                                    "highway=primary" }) })
    private Atlas counterClockwiseRoundaboutLeftDrivingAtlas;

    // Counterclockwise roundabout, right driving country
    @TestAtlas(
            // nodes
            nodes = {
                    @TestAtlas.Node(id = "10", coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_1)),
                    @TestAtlas.Node(id = "11", coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_2)),
                    @TestAtlas.Node(id = "12", coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_3)),
                    @TestAtlas.Node(id = "13", coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_4)),
                    @TestAtlas.Node(id = "14", coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_5)) },
            // edges
            edges = { @TestAtlas.Edge(id = "1244", coordinates = {
                    @TestAtlas.Loc(value = COUNTER_CLOCKWISE_1),
                    @TestAtlas.Loc(value = COUNTER_CLOCKWISE_2) }, tags = { "junction=roundabout",
                            "iso_country_code=USA", "highway=primary" }),
                    @TestAtlas.Edge(id = "1245", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_2),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_3) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary" }),
                    @TestAtlas.Edge(id = "1246", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_3),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_4) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary" }),
                    @TestAtlas.Edge(id = "1247", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_4),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_5) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary" }),
                    @TestAtlas.Edge(id = "1248", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_5),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_1) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary" }) })
    private Atlas counterClockwiseRoundaboutRightDrivingAtlas;

    // Multi-directional Atlas
    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_3)) },
            // edges
            edges = { @TestAtlas.Edge(id = "1234", coordinates = {
                    @TestAtlas.Loc(value = COUNTER_CLOCKWISE_1),
                    @TestAtlas.Loc(value = COUNTER_CLOCKWISE_2) }, tags = { "junction=roundabout",
                            "iso_country_code=USA", "highway=primary" }),
                    @TestAtlas.Edge(id = "1235", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_2),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_3) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary" }),
                    @TestAtlas.Edge(id = "1236", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_3),
                            @TestAtlas.Loc(value = CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @TestAtlas.Edge(id = "1237", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_2),
                            @TestAtlas.Loc(value = CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @TestAtlas.Edge(id = "1238", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_1),
                            @TestAtlas.Loc(value = CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }) })
    private Atlas multiDirectionalRoundaboutAtlas;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_5)) },
            // edges
            edges = {
                    @TestAtlas.Edge(id = "1234", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_1),
                            @TestAtlas.Loc(value = CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @TestAtlas.Edge(id = "1235", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_5),
                            @TestAtlas.Loc(value = CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @TestAtlas.Edge(id = "1236", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_4),
                            @TestAtlas.Loc(value = CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @TestAtlas.Edge(id = "1237", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_3),
                            @TestAtlas.Loc(value = CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @TestAtlas.Edge(id = "1238", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_2),
                            @TestAtlas.Loc(value = CLOCKWISE_1) }, tags = { "iso_country_code=SGP",
                                    "highway=primary" }) })
    private Atlas clockwiseRoundaboutLeftDrivingMissingTagAtlas;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_5)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_5)) },
            // edges
            edges = { @TestAtlas.Edge(id = "1234", coordinates = {
                    @TestAtlas.Loc(value = COUNTER_CLOCKWISE_1),
                    @TestAtlas.Loc(value = COUNTER_CLOCKWISE_2) }, tags = { "junction=roundabout",
                            "iso_country_code=USA", "highway=primary" }),
                    @TestAtlas.Edge(id = "1235", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_2),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_3) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary" }),
                    @TestAtlas.Edge(id = "1236", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_3),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_4) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary" }),
                    @TestAtlas.Edge(id = "1237", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_4),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_5) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary" }),
                    @TestAtlas.Edge(id = "1238", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_5),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_1) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary" }),
                    @TestAtlas.Edge(id = "1239", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_1),
                            @TestAtlas.Loc(value = CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @TestAtlas.Edge(id = "1240", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_1),
                            @TestAtlas.Loc(value = CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @TestAtlas.Edge(id = "1241", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_2),
                            @TestAtlas.Loc(value = CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @TestAtlas.Edge(id = "1242", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_3),
                            @TestAtlas.Loc(value = CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @TestAtlas.Edge(id = "1243", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_4),
                            @TestAtlas.Loc(value = CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @TestAtlas.Edge(id = "1244", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_5),
                            @TestAtlas.Loc(value = CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }) })
    private Atlas counterClockwiseConnectedDoubleRoundaboutRightDrivingAtlas;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_5)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_4)) },
            // edges
            edges = { @TestAtlas.Edge(id = "1234", coordinates = {
                    @TestAtlas.Loc(value = COUNTER_CLOCKWISE_1),
                    @TestAtlas.Loc(value = COUNTER_CLOCKWISE_2) }, tags = { "junction=roundabout",
                            "iso_country_code=USA", "highway=primary" }),
                    @TestAtlas.Edge(id = "1235", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_2),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_3) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary" }),
                    @TestAtlas.Edge(id = "1236", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_3),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_4) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary" }),
                    @TestAtlas.Edge(id = "1237", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_4),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_5) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary" }),
                    @TestAtlas.Edge(id = "1238", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_5),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_1) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary" }),
                    @TestAtlas.Edge(id = "1239", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_4),
                            @TestAtlas.Loc(value = CLOCKWISE_4) }, tags = { "iso_country_code=USA",
                                    "highway=primary" }) })
    private Atlas counterClockwiseRoundaboutRightDrivingOutsideConnectionAtlas;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_5)) },
            // edges
            edges = { @TestAtlas.Edge(id = "1234", coordinates = {
                    @TestAtlas.Loc(value = COUNTER_CLOCKWISE_1),
                    @TestAtlas.Loc(value = COUNTER_CLOCKWISE_2) }, tags = { "junction=roundabout",
                            "iso_country_code=USA", "highway=primary", "oneway=no" }),
                    @TestAtlas.Edge(id = "-1234", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_2),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_1) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary", "oneway=no" }),
                    @TestAtlas.Edge(id = "1235", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_2),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_3) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary", "oneway=no" }),
                    @TestAtlas.Edge(id = "-1235", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_3),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_2) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary", "oneway=no" }),
                    @TestAtlas.Edge(id = "1236", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_3),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_4) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary", "oneway=no" }),
                    @TestAtlas.Edge(id = "-1236", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_4),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_3) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary", "oneway=no" }),
                    @TestAtlas.Edge(id = "1237", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_4),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_5) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary", "oneway=no" }),
                    @TestAtlas.Edge(id = "-1237", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_5),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_4) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary", "oneway=no" }),
                    @TestAtlas.Edge(id = "1238", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_5),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_1) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary", "oneway=no" }),
                    @TestAtlas.Edge(id = "-1238", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_1),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_5) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary", "oneway=no" }) })
    private Atlas counterClockwiseRoundaboutRightDrivingOneWayNoAtlas;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = COUNTER_CLOCKWISE_5)) },
            // edges
            edges = { @TestAtlas.Edge(id = "1234", coordinates = {
                    @TestAtlas.Loc(value = COUNTER_CLOCKWISE_1),
                    @TestAtlas.Loc(value = COUNTER_CLOCKWISE_2) }, tags = { "junction=roundabout",
                            "iso_country_code=USA", "highway=elevator" }),
                    @TestAtlas.Edge(id = "1235", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_2),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_3) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary" }),
                    @TestAtlas.Edge(id = "1236", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_3),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_4) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary" }),
                    @TestAtlas.Edge(id = "1237", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_4),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_5) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary" }),
                    @TestAtlas.Edge(id = "1238", coordinates = {
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_5),
                            @TestAtlas.Loc(value = COUNTER_CLOCKWISE_1) }, tags = {
                                    "junction=roundabout", "iso_country_code=USA",
                                    "highway=primary" }) })
    private Atlas counterClockwiseRoundaboutRightDrivingNonCarNavigableAtlas;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_2)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_3)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_4)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = CLOCKWISE_5)) },
            // edges
            edges = {
                    @TestAtlas.Edge(id = "1234", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_1),
                            @TestAtlas.Loc(value = CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @TestAtlas.Edge(id = "1235", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_5),
                            @TestAtlas.Loc(value = CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @TestAtlas.Edge(id = "1236", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_4),
                            @TestAtlas.Loc(value = CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @TestAtlas.Edge(id = "1237", coordinates = {
                            @TestAtlas.Loc(value = CLOCKWISE_3),
                            @TestAtlas.Loc(value = CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }) })
    private Atlas clockwiseRoundaboutRightDrivingIncompleteAtlas;

    public Atlas clockwiseRoundaboutLeftDrivingAtlas()
    {
        return this.clockwiseRoundaboutLeftDrivingAtlas;
    }

    public Atlas clockwiseRoundaboutLeftDrivingMissingTagAtlas()
    {
        return this.clockwiseRoundaboutLeftDrivingMissingTagAtlas;
    }

    public Atlas clockwiseRoundaboutRightDrivingAtlas()
    {
        return this.clockwiseRoundaboutRightDrivingAtlas;
    }

    public Atlas clockwiseRoundaboutRightDrivingIncompleteAtlas()
    {
        return this.clockwiseRoundaboutRightDrivingIncompleteAtlas;
    }

    public Atlas counterClockwiseConnectedDoubleRoundaboutRightDrivingAtlas()
    {
        return this.counterClockwiseConnectedDoubleRoundaboutRightDrivingAtlas;
    }

    public Atlas counterClockwiseRoundaboutLeftDrivingAtlas()
    {
        return this.counterClockwiseRoundaboutLeftDrivingAtlas;
    }

    public Atlas counterClockwiseRoundaboutRightDrivingAtlas()
    {
        return this.counterClockwiseRoundaboutRightDrivingAtlas;
    }

    public Atlas counterClockwiseRoundaboutRightDrivingNonCarNavigableAtlas()
    {
        return this.counterClockwiseRoundaboutRightDrivingNonCarNavigableAtlas;
    }

    public Atlas counterClockwiseRoundaboutRightDrivingOneWayNoAtlas()
    {
        return this.counterClockwiseRoundaboutRightDrivingOneWayNoAtlas;
    }

    public Atlas counterClockwiseRoundaboutRightDrivingOutsideConnectionAtlas()
    {
        return this.counterClockwiseRoundaboutRightDrivingOutsideConnectionAtlas;
    }

    public Atlas multiDirectionalRoundaboutAtlas()
    {
        return this.multiDirectionalRoundaboutAtlas;
    }
}
