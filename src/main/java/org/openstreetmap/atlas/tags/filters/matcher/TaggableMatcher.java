package org.openstreetmap.atlas.tags.filters.matcher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.Lexer;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.Parser;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.SemanticChecker;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.Token;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.ASTNode;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.TreePrinter;

/**
 * @author lcram
 */
public final class TaggableMatcher implements Predicate<Taggable>, Serializable
{
    private static final long serialVersionUID = -3505184622005535575L;

    private final ASTNode rootNode;
    private final String definition;

    public static TaggableMatcher from(final String definition)
    {
        final List<Token> tokens = new Lexer().lex(definition);
        final ASTNode rootNode = new Parser(tokens, definition).parse();
        new SemanticChecker().check(rootNode);
        return new TaggableMatcher(rootNode, definition);
    }

    private TaggableMatcher(final ASTNode rootNode, final String definition)
    {
        this.rootNode = rootNode;
        this.definition = definition;
    }

    /**
     * Print this {@link TaggableMatcher} in syntax tree form.
     *
     * @return this {@link TaggableMatcher} as a tree
     */
    public String prettyPrintTree()
    {
        return TreePrinter.print(this.rootNode);
    }

    @Override
    public boolean test(final Taggable taggable)
    {
        final Map<String, String> tags = taggable.getTags();
        final List<String> keys = new ArrayList<>();
        final List<String> values = new ArrayList<>();
        for (final Map.Entry<String, String> entry : tags.entrySet())
        {
            keys.add(entry.getKey());
            values.add(entry.getValue());
        }

        return this.rootNode.match(keys, values);
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + "(" + this.definition + ")";
    }
}
