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

/**
 * @author lcram
 */
public class TaggableMatcher implements Predicate<Taggable>, Serializable
{
    private static final long serialVersionUID = -3505184622005535575L;

    private final ASTNode node;

    public static TaggableMatcher from(final String definition)
    {
        final List<Token> tokens = new Lexer().lex(definition);
        final ASTNode root = new Parser(tokens, definition).parse();
        new SemanticChecker().check(root);
        return new TaggableMatcher(root);
    }

    private TaggableMatcher(final ASTNode node)
    {
        this.node = node;
    }

    /**
     * Print this {@link TaggableMatcher} in syntax tree form.
     *
     * @return this {@link TaggableMatcher} as a tree
     */
    public String prettyPrint()
    {
        return "TODO";
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

        return this.node.match(keys, values);
    }

    @Override
    public String toString()
    {
        return "TODO";
    }
}
