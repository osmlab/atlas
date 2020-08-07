package org.openstreetmap.atlas.utilities.command.subcommands;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.locale.IsoCountry;
import org.openstreetmap.atlas.locale.IsoCountryFuzzyMatcher;

/**
 * @author lcram
 */
public class IsoCountryCodeCommandTest
{
    @Test
    public void test()
    {
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        IsoCountryCodeCommand command = new IsoCountryCodeCommand();
        command.setNewOutStream(new PrintStream(outContent));
        command.setNewErrStream(new PrintStream(errContent));
        command.runSubcommand("USA");
        Assert.assertEquals("ISO code 'USA' matched: \n" + "US   USA   United States\n",
                outContent.toString());
        Assert.assertEquals("", errContent.toString());

        final ByteArrayOutputStream outContent2 = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent2 = new ByteArrayOutputStream();
        command = new IsoCountryCodeCommand();
        command.setNewOutStream(new PrintStream(outContent2));
        command.setNewErrStream(new PrintStream(errContent2));
        command.runSubcommand("FOO");
        Assert.assertEquals(
                "Display country name 'FOO' had no exact matches. 3 closest matches are:\n"
                        + "ST   STP   São Tomé & Príncipe\n" + "TG   TGO   Togo\n"
                        + "IM   IMN   Isle of Man\n",
                outContent2.toString());
        Assert.assertEquals("", errContent2.toString());

        final ByteArrayOutputStream outContent3 = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent3 = new ByteArrayOutputStream();
        command = new IsoCountryCodeCommand();
        command.setNewOutStream(new PrintStream(outContent3));
        command.setNewErrStream(new PrintStream(errContent3));
        command.runSubcommand("US");
        Assert.assertEquals("ISO code 'US' matched: \n" + "US   USA   United States\n",
                outContent3.toString());
        Assert.assertEquals("", errContent3.toString());

        final ByteArrayOutputStream outContent4 = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent4 = new ByteArrayOutputStream();
        command = new IsoCountryCodeCommand();
        command.setNewOutStream(new PrintStream(outContent4));
        command.setNewErrStream(new PrintStream(errContent4));
        command.runSubcommand("United States");
        Assert.assertEquals(
                "Display country name 'United States' matched: \n" + "US   USA   United States\n",
                outContent4.toString());
        Assert.assertEquals("", errContent4.toString());

        final ByteArrayOutputStream outContent5 = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent5 = new ByteArrayOutputStream();
        command = new IsoCountryCodeCommand();
        command.setNewOutStream(new PrintStream(outContent5));
        command.setNewErrStream(new PrintStream(errContent5));
        command.runSubcommand("Martin");
        Assert.assertEquals(
                "Display country name 'Martin' had no exact matches. 3 closest matches are:\n"
                        + "MF   MAF   St. Martin\n" + "SM   SMR   San Marino\n"
                        + "SX   SXM   Sint Maarten\n",
                outContent5.toString());
        Assert.assertEquals("", errContent5.toString());

        final ByteArrayOutputStream outContent6 = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent6 = new ByteArrayOutputStream();
        command = new IsoCountryCodeCommand();
        command.setNewOutStream(new PrintStream(outContent6));
        command.setNewErrStream(new PrintStream(errContent6));
        command.runSubcommand("Martin", "USA", "United Kingdom");
        Assert.assertEquals(
                "Display country name 'Martin' had no exact matches. 3 closest matches are:\n"
                        + "MF   MAF   St. Martin\n" + "SM   SMR   San Marino\n"
                        + "SX   SXM   Sint Maarten\n" + "\n" + "ISO code 'USA' matched: \n"
                        + "US   USA   United States\n" + "\n"
                        + "Display country name 'United Kingdom' matched: \n"
                        + "GB   GBR   United Kingdom\n",
                outContent6.toString());
        Assert.assertEquals("", errContent6.toString());

        final ByteArrayOutputStream outContent7 = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent7 = new ByteArrayOutputStream();
        command = new IsoCountryCodeCommand();
        command.setNewOutStream(new PrintStream(outContent7));
        command.setNewErrStream(new PrintStream(errContent7));
        command.runSubcommand("usA");
        Assert.assertEquals(
                "Display country name 'usA' had no exact matches. 3 closest matches are:\n"
                        + "UM   UMI   U.S. Outlying Islands\n" + "CU   CUB   Cuba\n"
                        + "SM   SMR   San Marino\n",
                outContent7.toString());
        Assert.assertEquals("iso-country-code: warn: did you mean case-sensitive ISO code 'USA'?\n",
                errContent7.toString());
    }

    @Test
    public void testAllCount()
    {
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        final IsoCountryCodeCommand command = new IsoCountryCodeCommand();
        command.setNewOutStream(new PrintStream(outContent));
        command.setNewErrStream(new PrintStream(errContent));
        command.runSubcommand("--all");
        Assert.assertEquals(250L, outContent.toString().lines().count());
    }

    @Test
    public void testFuzzyMatcher()
    {
        final List<IsoCountry> results = IsoCountryFuzzyMatcher.forDisplayCountryTopMatches(1,
                "United States");
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("US", results.get(0).getCountryCode());
    }
}
