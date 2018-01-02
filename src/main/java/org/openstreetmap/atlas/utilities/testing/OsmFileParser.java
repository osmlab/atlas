package org.openstreetmap.atlas.utilities.testing;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * Parses an OSM file created by JOSM and makes it look like real OSM data
 *
 * @author matthieun
 */
public class OsmFileParser
{
    // List of regexes being replaced. The first item in the tuple is the regex, and the second is
    // the replacement.
    private final List<Tuple<String, String>> replacements;

    public OsmFileParser()
    {
        this.replacements = new ArrayList<>();
        this.replacements.add(new Tuple<>("id=\\'-", "id=\\'"));
        this.replacements.add(new Tuple<>("ref=\\'-", "ref=\\'"));
        this.replacements.add(new Tuple<>("action=\\'modify\\'",
                "uid=\\'1\\' version=\\'1\\' changeset=\\'1\\' user=\\'myself\\'"
                        // Here the timestamp is meaningless, just there so osmosis can read the XML
                        // file.
                        + " timestamp=\\'2017-12-19T21:43:02Z\\' action=\\'modify\\'"));
        this.replacements.add(new Tuple<>("generator=\\'JOSM\\'",
                "generator=\\'JOSM\\' timestamp=\\'2017-12-19T21:43:02Z\\'"));
    }

    public void update(final Resource josmOsmFile, final WritableResource osmFile)
    {
        final StringList result = new StringList();
        for (final String line : josmOsmFile.lines())
        {
            String replaced = line;
            for (final Tuple<String, String> replacement : this.replacements)
            {
                replaced = replaced.replaceAll(replacement.getFirst(), replacement.getSecond());
            }
            result.add(replaced);
        }
        osmFile.writeAndClose(result.join(System.lineSeparator()));
    }
}
