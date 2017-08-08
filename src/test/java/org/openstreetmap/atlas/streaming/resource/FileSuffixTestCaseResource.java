package org.openstreetmap.atlas.streaming.resource;

import java.io.InputStream;

/**
 * This is for testing the FileSuffix classes, so we only need the getName method
 *
 * @author Jack
 */
class FileSuffixTestCaseResource implements Resource
{
    private final String name;

    FileSuffixTestCaseResource(final String name)
    {
        this.name = name;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public long length()
    {
        return 0;
    }

    @Override
    public InputStream read()
    {
        return null;
    }

}
