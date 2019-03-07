package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.junit.Assert;
import org.junit.Test;

import groovy.lang.GroovyShell;

/**
 * @author Yazad Khambata
 */
public class SubAtlasCommandTest extends Object
{
    // GROOVY-8135
    @Test
    public void testStarImportsWhiteListWithIndirectImportCheckEnabled()
    {
        final SecureASTCustomizer customizer = new SecureASTCustomizer();
        customizer.setIndirectImportCheckEnabled(true);

        final List<String> starImportsWhitelist = new ArrayList<>();
        starImportsWhitelist.add("java.lang");
        customizer.setStarImportsWhitelist(starImportsWhitelist);

        final CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.addCompilationCustomizers(customizer);

        final ClassLoader parent = getClass().getClassLoader();
        final GroovyShell shell = new GroovyShell(parent, compilerConfiguration);
        final String hello = (String) shell
                .evaluate("Object object = new Object(); return 'hello'");
        Assert.assertEquals("hello", hello);
    }
}
