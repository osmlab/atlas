package org.openstreetmap.atlas.streaming.resource;

import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Piggybacking on that classic IO class for concatenating the contents of several streams into one
 *
 * @author cstaylor
 */
public class StreamOfResourceStreams extends SequenceInputStream
{
    public StreamOfResourceStreams(final Resource... resources)
    {
        super(Collections.enumeration(Stream.of(resources).map(resource -> resource.read())
                .collect(Collectors.toList())));
    }
}
