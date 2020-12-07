package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.List;

import org.openstreetmap.atlas.tags.filters.matcher.parsing.Lexer;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.Parser;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.ASTNode;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.tree.TreePrinter;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.terminal.TTYAttribute;

/**
 * @author lcram
 */
public class TaggableMatcherPrinter extends AbstractAtlasShellToolsCommand
{
    public static void main(final String[] args)
    {
        new TaggableMatcherPrinter().runSubcommandAndExit(args);
    }

    @Override
    public int execute()
    {
        final List<String> definitions = this.getOptionAndArgumentDelegate()
                .getVariadicArgument("matchers");
        for (final String definition : definitions)
        {
            this.getCommandOutputDelegate().printlnStdout(definition, TTYAttribute.BOLD,
                    TTYAttribute.GREEN);
            final ASTNode root = new Parser(new Lexer().lex(definition), definition).parse();
            this.getCommandOutputDelegate().printlnStdout(TreePrinter.print(root));
            this.getCommandOutputDelegate().printlnStdout("");
        }

        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "print-matcher";
    }

    @Override
    public String getSimpleDescription()
    {
        return "print a TaggableMatcher as a tree";
    }

    @Override
    public void registerManualPageSections()
    {
        // TODO fill these in
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerArgument("matchers", ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED);
        registerOptionWithOptionalArgument("page", 'p',
                "Run through a local pager program, /usr/bin/less by default.",
                OptionOptionality.OPTIONAL, "pager");
        super.registerOptionsAndArguments();
    }
}
