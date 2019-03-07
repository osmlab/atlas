package org.openstreetmap.atlas.utilities.conversion;

import java.util.Arrays;
import java.util.function.Predicate;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * Convert a boolean expression string to a {@link Predicate}. While the converter can handle
 * generic predicates, it includes some default imports that make it ideal for use as
 * Predicate&lt;AtlasEntity&gt;. The converter uses the Groovy interpreter to create a
 * {@link Predicate} from the boolean input expression. The type T is bound to a variable called
 * 'e', and so the expression string should use 'e'. E.g. "e.getType() == ItemType.POINT" (if T is
 * {@link AtlasEntity}) or 'e.equals("foo")' (if T is {@link String}).
 *
 * @author lcram
 * @param <T>
 *            the type of the predicate
 */
public class StringToPredicateConverter<T> implements Converter<String, Predicate<T>>
{
    @Override
    public Predicate<T> convert(final String string)
    {
        final SecureASTCustomizer securityCustomizer = new SecureASTCustomizer();
        securityCustomizer.setStarImportsWhitelist(Arrays.asList("java.lang", "groovy.lang",
                "java.util.function", "org.openstreetmap.atlas.geography.atlas.items"));
        securityCustomizer.setPackageAllowed(false);
        securityCustomizer.setMethodDefinitionAllowed(false);
        securityCustomizer.setIndirectImportCheckEnabled(true);

        final ImportCustomizer importCustomizer = new ImportCustomizer();
        importCustomizer.addStarImports("java.util.function",
                "org.openstreetmap.atlas.geography.atlas.items", "java.lang");

        final CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.addCompilationCustomizers(securityCustomizer);
        compilerConfiguration.addCompilationCustomizers(importCustomizer);

        final GroovyCodeSource groovyCodeSource = new GroovyCodeSource(string, "ThePredicate",
                GroovyShell.DEFAULT_CODE_BASE);
        groovyCodeSource.setCachable(true);
        try (GroovyClassLoader groovyClassLoader = new GroovyClassLoader(
                this.getClass().getClassLoader(), compilerConfiguration);)
        {
            @SuppressWarnings("unchecked")
            final Class<Script> scriptClass = groovyClassLoader.parseClass(groovyCodeSource);
            return element ->
            {
                try
                {
                    final Binding binding = new Binding();
                    binding.setProperty("e", element);
                    final Script script = scriptClass.getDeclaredConstructor(Binding.class)
                            .newInstance(binding);
                    return (boolean) script.run();
                }
                catch (final Exception exception)
                {
                    throw new CoreException("Something went wrong with this predicate ", exception);
                }
            };
        }
        catch (final Exception exception)
        {
            return null;
        }
    }
}
