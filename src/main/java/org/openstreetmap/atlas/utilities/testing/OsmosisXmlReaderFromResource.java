package org.openstreetmap.atlas.utilities.testing;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.common.CompressionActivator;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlReader;
import org.openstreetmap.osmosis.xml.v0_6.impl.OsmHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * From {@link XmlReader}. Almost everything here is copied from osmosis-xml version 0.44.1. The
 * only change is to allow reading from a {@link Resource} instead from a {@link File}.
 *
 * @author Brett Henderson
 * @author matthieun
 */
public class OsmosisXmlReaderFromResource implements RunnableSource
{
    private static Logger log = Logger.getLogger(OsmosisXmlReaderFromResource.class.getName());

    private Sink sink;

    private final Resource resource;
    private final boolean enableDateParsing;
    private final CompressionMethod compressionMethod;

    /**
     * Creates a new instance.
     *
     * @param resource
     *            The resource to read.
     * @param enableDateParsing
     *            If true, dates will be parsed from xml data, else the current date will be used
     *            thus saving parsing time.
     * @param compressionMethod
     *            Specifies the compression method to employ.
     */
    public OsmosisXmlReaderFromResource(final Resource resource, final boolean enableDateParsing,
            final CompressionMethod compressionMethod)
    {
        this.resource = resource;
        this.enableDateParsing = enableDateParsing;
        this.compressionMethod = compressionMethod;
    }

    /**
     * Reads all data from the file and send it to the sink.
     */
    @Override
    public void run()
    {
        InputStream inputStream = null;
        try
        {
            final SAXParser parser;
            this.sink.initialize(Collections.<String, Object> emptyMap());
            // make "-" an alias for /dev/stdin
            if (this.resource.getName() != null && this.resource.getName().equals("-"))
            {
                inputStream = System.in;
            }
            else
            {
                inputStream = this.resource.read();
            }
            inputStream = new CompressionActivator(this.compressionMethod)
                    .createCompressionInputStream(inputStream);
            parser = createParser();
            parser.parse(inputStream, new OsmHandler(this.sink, this.enableDateParsing));
            this.sink.complete();
        }
        catch (final SAXParseException e)
        {
            throw new OsmosisRuntimeException(
                    "Unable to parse xml resource " + this.resource.getName() + ".  publicId=("
                            + e.getPublicId() + "), systemId=(" + e.getSystemId() + "), lineNumber="
                            + e.getLineNumber() + ", columnNumber=" + e.getColumnNumber() + ".",
                    e);
        }
        catch (final SAXException e)
        {
            throw new OsmosisRuntimeException("Unable to parse XML.", e);
        }
        catch (final IOException e)
        {
            throw new OsmosisRuntimeException(
                    "Unable to read XML file " + this.resource.getName() + ".", e);
        }
        finally
        {
            this.sink.release();
            if (inputStream != null)
            {
                try
                {
                    inputStream.close();
                }
                catch (final IOException e)
                {
                    log.log(Level.SEVERE, "Unable to close input stream.", e);
                }
                inputStream = null;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSink(final Sink sink)
    {
        this.sink = sink;
    }

    /**
     * Creates a new SAX parser.
     *
     * @return The newly created SAX parser.
     */
    private SAXParser createParser()
    {
        try
        {
            return SAXParserFactory.newInstance().newSAXParser();
        }
        catch (final ParserConfigurationException e)
        {
            throw new OsmosisRuntimeException("Unable to create SAX Parser.", e);
        }
        catch (final SAXException e)
        {
            throw new OsmosisRuntimeException("Unable to create SAX Parser.", e);
        }
    }
}
