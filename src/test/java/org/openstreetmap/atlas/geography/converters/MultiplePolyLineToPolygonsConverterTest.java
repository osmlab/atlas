package org.openstreetmap.atlas.geography.converters;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.converters.MultiplePolyLineToPolygonsConverter.OpenPolygonException;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author matthieun
 * @author mgostintsev
 */
public class MultiplePolyLineToPolygonsConverterTest
{
    private static final MultiplePolyLineToPolygonsConverter CONVERTER = new MultiplePolyLineToPolygonsConverter();

    private static final Location ONE = Location.TEST_6;
    private static final Location TWO = Location.TEST_2;
    private static final Location THR = Location.TEST_1;
    private static final Location FOR = Location.TEST_5;
    private static final Location FVE = Location.TEST_4;

    private static final Polygon POLYGON_LOOP = new Polygon(ONE, TWO, THR, FOR, FVE);
    private static final PolyLine POLYLINE_LOOP = new PolyLine(ONE, TWO, THR, FOR, FVE, ONE);

    private static final WktPolyLineConverter WKT_POLY_LINE_CONVERTER = new WktPolyLineConverter();
    private static final WktPolygonConverter WKT_POLYGON_CONVERTER = new WktPolygonConverter();

    // Following polylines from http://www.openstreetmap.org/relation/409391 version #4
    private static final PolyLine EDGE1 = new PolyLineStringConverter().convert(
            "18.3875057,-74.0412515:" + "18.3893694,-74.0470441:" + "18.3923348,-74.0490819:"
                    + "18.3936336,-74.0503812:" + "18.3957615,-74.0530296:"
                    + "18.3973108,-74.0556245:" + "18.399442,-74.0571181:"
                    + "18.4015712,-74.0582142:" + "18.403311,-74.0609596:"
                    + "18.4047097,-74.0642088:" + "18.4047239,-74.0671055:"
                    + "18.405441,-74.0714471:" + "18.405117,-74.0754345:"
                    + "18.4054675,-74.0768812:" + "18.4067034,-74.0782947:"
                    + "18.407543,-74.0818403:" + "18.4068213,-74.0858583");
    private static final PolyLine EDGE2 = new PolyLineStringConverter()
            .convert("18.3875057,-74.0412515:" + "18.3931334,-74.0407471:"
                    + "18.3969804,-74.0383875:" + "18.4015135,-74.0358821:"
                    + "18.4068233,-74.033268:" + "18.4099864,-74.0315273:"
                    + "18.4132471,-74.0311588:" + "18.4177298,-74.031219:"
                    + "18.4225155,-74.0304252:" + "18.4257431,-74.0289017:"
                    + "18.4282312,-74.0276947:" + "18.4307202,-74.0284382:"
                    + "18.4340427,-74.0295842:" + "18.433509,-74.0312442:"
                    + "18.4326188,-74.0339003:" + "18.4325629,-74.0371106:"
                    + "18.4357384,-74.037803:" + "18.4398946,-74.0358198:"
                    + "18.4438752,-74.0330989:" + "18.4490069,-74.0328145:"
                    + "18.4528982,-74.0338533:" + "18.4531964,-74.0373933:"
                    + "18.4547577,-74.0387577:" + "18.4561214,-74.04036:"
                    + "18.4565557,-74.0422515:" + "18.4581912,-74.0421951:"
                    + "18.459685,-74.0409368:" + "18.4607417,-74.0408078:"
                    + "18.4618023,-74.0414459:" + "18.4629325,-74.0415533:"
                    + "18.4640673,-74.0407893:" + "18.4651883,-74.0408589:"
                    + "18.4656794,-74.0414717:" + "18.4668101,-74.0416738:"
                    + "18.4682445,-74.0411923:" + "18.4707062,-74.0419255:"
                    + "18.4720272,-74.041728:" + "18.4725118,-74.0413371:"
                    + "18.4734976,-74.0402072:" + "18.4742729,-74.0395303:"
                    + "18.4764658,-74.0391494:" + "18.4787394,-74.0381316:"
                    + "18.4795547,-74.0367348:" + "18.479653,-74.0347454:"
                    + "18.4804524,-74.0337937:" + "18.4802471,-74.0307642:"
                    + "18.4807934,-74.0297665:" + "18.4818949,-74.0295898:"
                    + "18.4837607,-74.0303845:" + "18.4868311,-74.0299409:"
                    + "18.4889283,-74.0300523:" + "18.4914733,-74.0311462");
    private static final PolyLine EDGE3 = new PolyLineStringConverter()
            .convert("18.5335978,-74.0368872:" + "18.53112,-74.036579:" + "18.5289457,-74.0372922:"
                    + "18.5273132,-74.034374:" + "18.5254488,-74.035616:"
                    + "18.5242828,-74.0337941:" + "18.5212489,-74.0307702:"
                    + "18.5181947,-74.0308443:" + "18.5176216,-74.03188:"
                    + "18.5176394,-74.0353472:" + "18.517026,-74.0373495:"
                    + "18.5137583,-74.0380971:" + "18.5115861,-74.039246:"
                    + "18.5096258,-74.0393801:" + "18.5054015,-74.038608");
    private static final PolyLine EDGE4 = new PolyLineStringConverter().convert(
            "18.5230901,-74.0754088:" + "18.523199,-74.0718082:" + "18.5224555,-74.0694912:"
                    + "18.5213616,-74.0675645:" + "18.5226289,-74.0643177:"
                    + "18.5250127,-74.0620784:" + "18.5254792,-74.0613843:"
                    + "18.5246008,-74.0592196:" + "18.5237511,-74.0573579:"
                    + "18.525581,-74.054629:" + "18.524995,-74.0530974:" + "18.5261824,-74.0520014:"
                    + "18.5252714,-74.050557:" + "18.5265494,-74.0495078:"
                    + "18.5268307,-74.0479241:" + "18.5286325,-74.0468152:"
                    + "18.531375,-74.045862:" + "18.5326575,-74.0439127:" + "18.5332768,-74.041285:"
                    + "18.5341273,-74.039717:" + "18.5341949,-74.0387977:"
                    + "18.5335978,-74.0368872");
    private static final PolyLine EDGE5 = new PolyLineStringConverter().convert(
            "18.4345179,-74.1196672:" + "18.4327579,-74.1182084:" + "18.4299982,-74.1155429:"
                    + "18.4263207,-74.1157227:" + "18.4251045,-74.1146021:"
                    + "18.4222402,-74.11278:" + "18.4187323,-74.110573:" + "18.4160356,-74.107879:"
                    + "18.4125933,-74.1080767:" + "18.403404,-74.106524");
    private static final PolyLine EDGE6 = new PolyLineStringConverter()
            .convert("18.5230901,-74.0754088:" + "18.520754,-74.0763403:"
                    + "18.5185178,-74.0773754:" + "18.5169688,-74.0784637:"
                    + "18.5156572,-74.0800906:" + "18.5114384,-74.082273:"
                    + "18.5094846,-74.0837801:" + "18.5077898,-74.0846511:"
                    + "18.5048729,-74.0851024:" + "18.5022438,-74.087134:"
                    + "18.5007805,-74.0872838:" + "18.4992088,-74.0874436:"
                    + "18.4979241,-74.0890323:" + "18.4989472,-74.0913097:"
                    + "18.4989661,-74.0919123:" + "18.4989665,-74.0919302:"
                    + "18.4989662,-74.0919643:" + "18.4990328,-74.0940847:"
                    + "18.4981478,-74.0961165:" + "18.4943862,-74.095672:"
                    + "18.4916892,-74.0948621:" + "18.4879321,-74.0972687:"
                    + "18.4833534,-74.0998405:" + "18.4799753,-74.1021408:"
                    + "18.4780311,-74.1019141:" + "18.476033,-74.0998029:"
                    + "18.4729982,-74.1002072:" + "18.470605,-74.1005417:"
                    + "18.4678104,-74.098217:" + "18.4639369,-74.0970533:"
                    + "18.4608976,-74.0964916:" + "18.4567638,-74.0975833:"
                    + "18.4531758,-74.0994771:" + "18.4509348,-74.1014301:"
                    + "18.4490696,-74.1045174:" + "18.4473182,-74.1068181:"
                    + "18.4466008,-74.107996:" + "18.4464353,-74.1081067:"
                    + "18.4463879,-74.1081384:" + "18.4440332,-74.1097138:"
                    + "18.4417798,-74.1110132:" + "18.4378665,-74.1129176:"
                    + "18.4356025,-74.1157794:" + "18.4345179,-74.1196672");
    private static final PolyLine EDGE7 = new PolyLineStringConverter()
            .convert("18.4914733,-74.0311462:" + "18.4907226,-74.032789:"
                    + "18.4896023,-74.0328426:" + "18.4889917,-74.0336038:"
                    + "18.4896319,-74.0351157:" + "18.4929014,-74.0364898:"
                    + "18.4938275,-74.0373655:" + "18.495212,-74.0377177:" + "18.4968,-74.0389875:"
                    + "18.4976593,-74.0409246:" + "18.4981439,-74.0412372:"
                    + "18.5009698,-74.0414745:" + "18.5009449,-74.0415097:"
                    + "18.5025891,-74.0400445:" + "18.503446,-74.0379274:"
                    + "18.5051641,-74.0385303:" + "18.5054015,-74.038608");
    private static final PolyLine EDGE8 = new PolyLineStringConverter()
            .convert("18.4068213,-74.0858583:" + "18.4039542,-74.0948388:"
                    + "18.4026541,-74.1008287:" + "18.403404,-74.106524");

    @Test
    public void testBoundary()
    {
        final List<PolyLine> list = new ArrayList<>();
        list.add(EDGE1);
        list.add(EDGE2);
        list.add(EDGE3);
        list.add(EDGE4);
        list.add(EDGE5);
        list.add(EDGE6);
        list.add(EDGE7);
        list.add(EDGE8);
        CONVERTER.convert(list);
    }

    @Test(expected = OpenPolygonException.class)
    public void testHole()
    {
        final List<PolyLine> list = new ArrayList<>();
        list.add(new PolyLine(ONE, TWO));
        list.add(new PolyLine(TWO, THR, FOR));
        list.add(new PolyLine(FVE, ONE));
        CONVERTER.convert(list);
    }

    @Test
    public void testRegular()
    {
        final List<PolyLine> list = new ArrayList<>();
        list.add(new PolyLine(ONE, TWO));
        list.add(new PolyLine(TWO, THR, FOR));
        list.add(new PolyLine(FOR, FVE, ONE));
        final Polygon result = CONVERTER.convert(list).iterator().next();
        Assert.assertEquals(result, POLYGON_LOOP);
    }

    @Test
    public void testReversed()
    {
        final List<PolyLine> list = new ArrayList<>();
        list.add(new PolyLine(ONE, TWO));
        list.add(new PolyLine(TWO, THR, FOR));
        // Reversed!
        list.add(new PolyLine(ONE, FVE, FOR));
        final Polygon result = CONVERTER.convert(list).iterator().next();
        Assert.assertEquals(result, POLYGON_LOOP);
    }

    @Test
    public void testSingleClosedPolygon()
    {
        final List<PolyLine> list = new ArrayList<>();
        list.add(POLYGON_LOOP);
        CONVERTER.convert(list);
    }

    @Test
    public void testSingleClosedPolyLine()
    {
        final List<PolyLine> list = new ArrayList<>();
        list.add(POLYLINE_LOOP);
        CONVERTER.convert(list);
    }

    @Test
    public void testSingleClosedPolyLineWithinGroup()
    {
        final List<PolyLine> input = new InputStreamResource(
                () -> MultiplePolyLineToPolygonsConverterTest.class
                        .getResourceAsStream("multiplePolyLines.txt")).linesList().stream()
                                .map(WKT_POLY_LINE_CONVERTER::backwardConvert)
                                .collect(Collectors.toList());
        final List<Polygon> expected = new InputStreamResource(
                () -> MultiplePolyLineToPolygonsConverterTest.class
                        .getResourceAsStream("expectedPolygons.txt")).linesList().stream()
                                .map(WKT_POLYGON_CONVERTER::backwardConvert)
                                .collect(Collectors.toList());
        Assert.assertEquals(expected, Iterables.asList(CONVERTER.convert(input)));
    }

    @Test(expected = OpenPolygonException.class)
    public void testSingleOpenPolyLine()
    {
        final List<PolyLine> list = new ArrayList<>();
        list.add(new PolyLine(ONE, TWO));
        CONVERTER.convert(list);
    }
}
