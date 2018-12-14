package org.openstreetmap.atlas.utilities.command.documentation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * @author lcram
 */
public class DocumentationRegistrar
{
    private static final String DESCRIPTION_HEADER = "DESCRIPTION";

    private final Map<String, List<Tuple<DocumentationFormatType, String>>> sections;

    public DocumentationRegistrar()
    {
        this.sections = new LinkedHashMap<>();
    }

    public void addCodeLineToSection(final String section, final String codeLine)
    {
        final String capsSection = section.toUpperCase();
        if (!this.sections.containsKey(capsSection))
        {
            throw new CoreException("Section {} has not been added", capsSection);
        }
        final List<Tuple<DocumentationFormatType, String>> list = this.sections.get(capsSection);
        list.add(new Tuple<>(DocumentationFormatType.CODE, codeLine));
    }

    public void addManualPageSection(final String section)
    {
        final String capsSection = section.toUpperCase();
        if (this.sections.containsKey(capsSection))
        {
            throw new CoreException("Manpage section {} was already added", capsSection);
        }
        this.sections.put(capsSection, new ArrayList<>());
    }

    public void addParagraphToSection(final String section, final String paragraph)
    {
        final String capsSection = section.toUpperCase();
        if (!this.sections.containsKey(capsSection))
        {
            throw new CoreException("Section {} has not been added", capsSection);
        }
        final List<Tuple<DocumentationFormatType, String>> list = this.sections.get(capsSection);
        list.add(new Tuple<>(DocumentationFormatType.PARAGRAPH, paragraph));
    }

    public String getDescriptionHeader()
    {
        return DESCRIPTION_HEADER;
    }

    public List<Tuple<DocumentationFormatType, String>> getSectionContents(final String section)
    {
        return this.sections.get(section);
    }

    public Set<String> getSections()
    {
        return this.sections.keySet();
    }

    public boolean hasDescriptionSection()
    {
        return this.sections.keySet().contains(DESCRIPTION_HEADER);
    }
}
