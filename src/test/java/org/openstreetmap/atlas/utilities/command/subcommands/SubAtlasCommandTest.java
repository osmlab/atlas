package org.openstreetmap.atlas.utilities.command.subcommands;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.util.Eval;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.sub.AtlasCutType;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.collections.Maps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author Yazad Khambata
 */
public class SubAtlasCommandTest extends Object {


    @Test
    public void testEval() {
        final Atlas atlas = new AtlasResourceLoader().load(new File("/tmp/AIA_7-41-57.atlas"));

        //atlas.entities().forEach(System.out::println);

        final String expression = "atlasEntity.getTag('highway').isPresent()";

        final GroovyCodeSource groovyCodeSource = new GroovyCodeSource(expression, "ThePredicate", GroovyShell.DEFAULT_CODE_BASE);

        groovyCodeSource.setCachable(true);

        final GroovyClassLoader groovyClassLoader = new GroovyClassLoader(this.getClass().getClassLoader());

        final Class<Script> scriptClass = groovyClassLoader.parseClass(groovyCodeSource);

        final Predicate<AtlasEntity> predicate1 = atlasEntity -> {
            try {
                Binding b = new Binding();
                b.setProperty("atlasEntity", atlasEntity);

                Script script = scriptClass.getDeclaredConstructor(Binding.class).newInstance(b);

                return (boolean) script.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        final Predicate<AtlasEntity> predicate2 = atlasEntity -> (boolean)Eval.me("atlasEntity", atlasEntity, expression);

        //final Optional<Atlas> subbedAtlas = (Optional<Atlas>) Eval.xyz(atlas, predicate1, AtlasCutType.SOFT_CUT, "x.subAtlas(y, z)");

        final Optional<Atlas> subbedAtlas = atlas.subAtlas(predicate1, AtlasCutType.SOFT_CUT);

        //final Optional<Atlas> subbedAtlas = (Optional<Atlas>) Eval.xyz(atlas, predicate2, AtlasCutType.SOFT_CUT, "x.subAtlas(y, z)");

        subbedAtlas.get().entities().forEach(System.out::println);
    }


    @Test
    public void test() {
        final Binding binding = new Binding();

        final SecureASTCustomizer securityCustomizer = new SecureASTCustomizer();

        securityCustomizer.setIndirectImportCheckEnabled(true);

        securityCustomizer.setStarImportsWhitelist(
                Arrays.asList("java.lang", "java.math.BigDecimal", "java.math.BigInteger", "java.util", "groovy.lang"
                        , "groovy.util", "org.codehaus.groovy.runtime.DefaultGroovyMethods"));

        securityCustomizer.setStarImportsWhitelist(Arrays.asList("java.util.function",
                "org.openstreetmap.atlas.geography.atlas.items", "java.lang"));

        //securityCustomizer.setStaticImportsWhitelist(Arrays.asList("java.lang.Math"));
        securityCustomizer.setPackageAllowed(false);
        securityCustomizer.setMethodDefinitionAllowed(false);

                final ImportCustomizer importCustomizer = new ImportCustomizer();
        importCustomizer.addStaticStars(java.lang.Math.class.getName());
        importCustomizer.addStarImports("java.util.function",
                "org.openstreetmap.atlas.geography.atlas.items", "java.lang");

        final CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.addCompilationCustomizers(securityCustomizer);
        compilerConfiguration.addCompilationCustomizers(importCustomizer);

        final GroovyShell shell = new GroovyShell(binding, compilerConfiguration);
        final Closure<AtlasEntity> predicate = (Closure) shell.evaluate(
                "{ e -> println e; println e.getTag(\"highway\") println e.getTag(\"highway\").isPresent()}");

        System.out.println(predicate.call(new CompleteNode(1L, null, Maps.hashMap("highway", "lucas"), null, null,
                null)));
        ;
    }

    // GROOVY-8135
    @Test
    public void testStarImportsWhiteListWithIndirectImportCheckEnabled() {
        SecureASTCustomizer customizer = new SecureASTCustomizer();
        customizer.setIndirectImportCheckEnabled(true);

        List<String> starImportsWhitelist = new ArrayList<>();
        starImportsWhitelist.add("java.lang");
        customizer.setStarImportsWhitelist(starImportsWhitelist);

        CompilerConfiguration cc = new CompilerConfiguration();
        cc.addCompilationCustomizers(customizer);

        ClassLoader parent = getClass().getClassLoader();
        GroovyShell shell = new GroovyShell(parent, cc);
        shell.evaluate("Object object = new Object(); println 'Hello'");
        shell.evaluate("Object object = new Object(); object.hashCode()");
        shell.evaluate("Object[] array = new Object[0]; array.size()");
        shell.evaluate("Object[][] array = new Object[0][0]; array.size()");
    }
}