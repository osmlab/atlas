package org.openstreetmap.atlas.utilities.conversion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * Convert a boolean expression string to a {@link Predicate}. The converter uses the Groovy
 * interpreter to create a {@link Predicate} from the boolean input expression. The type T is bound
 * to a variable called 'e', and so the expression string should use 'e'. E.g. "e.getType() ==
 * ItemType.POINT" (if T is {@link AtlasEntity}) or 'e.equals("foo")' (if T is {@link String}).
 *
 * @author lcram
 * @param <T>
 *            the type of the predicate
 */
public class StringToPredicateConverter<T> implements Converter<String, Predicate<T>>
{
    private static final Logger logger = LoggerFactory.getLogger(StringToPredicateConverter.class);
    private static final String SCRIPT = "%s Predicate predicate = { e -> return (%s); }; return predicate;";
    private static final List<String> DEFAULT_IMPORTS = Arrays.asList("java.lang", "groovy.lang",
            "java.util.function");

    private final List<String> additionalAllowListPackages;

    public StringToPredicateConverter()
    {
        this.additionalAllowListPackages = new ArrayList<>();
    }

    /**
     * Convert a {@link String} representing a boolean condition into a {@link Predicate} that
     * checks for the condition's validity. The binding assumes the variable under consideration is
     * named 'e'. Multi-statement expressions are supported. For example, to get a predicate that
     * checks if a given string has the contents "foo", one could supply "e.equals(\"foo\")" as an
     * argument. This would generate a predicate that looks approximately like:
     * <p>
     * <code>Predicate predicate = e -&gt; { return (e.equals("foo")); };</code>
     * </p>
     *
     * @param booleanExpressionString
     *            a boolean expression involving the object 'e' under consideration
     * @return the {@link Predicate} object
     */
    @Override
    public Predicate<T> convert(final String booleanExpressionString)
    {
        final Class<Script> scriptClass = checkExpressionSafety(booleanExpressionString);
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

    /**
     * Similar to {@link StringToPredicateConverter#convert(String)}, but uses some hacks to improve
     * runtime in certain cases. This version is EXTREMELY INSECURE and should never be used. Please
     * use {@link StringToPredicateConverter#convert(String)} unless you are sure you want unsafe
     * conversion and understand what security features you are sacrificing. Note also that due to
     * the limitations of the improvement hacks, this conversion only supports single statement
     * expressions.
     *
     * @param booleanExpressionString
     *            a boolean expression involving the object 'e' under consideration
     * @return the {@link Predicate} object
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public Predicate<T> convertUnsafe(final String booleanExpressionString)
    {
        checkExpressionSafety(booleanExpressionString);

        final Binding binding = new Binding();
        final GroovyShell shell = new GroovyShell(binding);

        final StringBuilder importsBuilder = new StringBuilder();
        final List<String> importsAllowList = new ArrayList<>(DEFAULT_IMPORTS);
        importsAllowList.addAll(this.additionalAllowListPackages);
        for (final String importPackage : importsAllowList)
        {
            importsBuilder.append("import ");
            importsBuilder.append(importPackage);
            importsBuilder.append(".*; ");
        }
        final String fullScript = String.format(SCRIPT, importsBuilder.toString(),
                booleanExpressionString);
        logger.warn("Acquiring predicate with unsafe script: {}", fullScript);

        return (Predicate<T>) shell.evaluate(fullScript);
    }

    /**
     * Add some imports to execute before the predicate.
     *
     * @param allowList
     *            the packages to star import.
     * @return the updated converter
     */
    public StringToPredicateConverter<T> withAddedStarImportPackages(final List<String> allowList)
    {
        for (final String importString : allowList)
        {
            checkImportStringFormat(importString);
        }
        this.additionalAllowListPackages.addAll(allowList);
        return this;
    }

    /**
     * Add some imports to execute before the predicate.
     *
     * @param allowList
     *            the packages to star import.
     * @return the updated converter
     */
    public StringToPredicateConverter<T> withAddedStarImportPackages(final String... allowList)
    {
        for (final String importString : allowList)
        {
            checkImportStringFormat(importString);
        }
        this.additionalAllowListPackages.addAll(Arrays.asList(allowList));
        return this;
    }

    /**
     * Clear any previously added imports.
     *
     * @return the updated converter
     */
    public StringToPredicateConverter<T> withClearedStarImportPackages()
    {
        this.additionalAllowListPackages.clear();
        return this;
    }

    @SuppressWarnings("unchecked")
    private Class<Script> checkExpressionSafety(final String booleanExpressionString)
    {
        final SecureASTCustomizer securityCustomizer = new SecureASTCustomizer();
        final List<String> importsAllowList = new ArrayList<>(DEFAULT_IMPORTS);
        importsAllowList.addAll(this.additionalAllowListPackages);

        securityCustomizer.setStarImportsWhitelist(importsAllowList);
        securityCustomizer.setPackageAllowed(false);
        securityCustomizer.setMethodDefinitionAllowed(false);
        securityCustomizer.setIndirectImportCheckEnabled(true);

        final ImportCustomizer importCustomizer = new ImportCustomizer();
        importCustomizer.addStarImports(importsAllowList.toArray(new String[0]));

        final CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.addCompilationCustomizers(securityCustomizer);
        compilerConfiguration.addCompilationCustomizers(importCustomizer);

        final GroovyCodeSource groovyCodeSource = new GroovyCodeSource(booleanExpressionString,
                "ThePredicate", GroovyShell.DEFAULT_CODE_BASE);
        groovyCodeSource.setCachable(true);
        try (GroovyClassLoader groovyClassLoader = new GroovyClassLoader(
                this.getClass().getClassLoader(), compilerConfiguration))
        {
            return groovyClassLoader.parseClass(groovyCodeSource);
        }
        catch (final Exception exception)
        {
            throw new CoreException("Unable to parse {} into a predicate.", booleanExpressionString,
                    exception);
        }
    }

    /*
     * Attempt some basic checks to make sure imports are not trying to sneak in extra code.
     */
    private void checkImportStringFormat(final String importString)
    {
        if (!importString.matches("^[a-zA-Z0-9\\\\.]+$"))
        {
            throw new CoreException("Invalid import '{}'", importString);
        }
    }
}
