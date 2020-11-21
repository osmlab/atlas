package org.openstreetmap.atlas.tags.filters.matcher.parsing;

import java.util.Scanner;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * TODO delete this class
 * 
 * @author lcram
 */
public class TODOInteractiveTest
{
    public static void main(final String[] args)
    {
        final Scanner scanner = new Scanner(System.in);
        while (true)
        {
            System.out.print("> ");
            final String input = scanner.nextLine();
            try
            {
                new SemanticChecker().check(new Parser(new Lexer().lex(input), input).parse());
            }
            catch (final CoreException exception)
            {
                System.err.println(exception.getMessage());
            }
        }
    }
}
