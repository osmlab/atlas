package org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.gradle

import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.Linter
import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain.Source
import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.lintlet.Lintlet

import java.util.stream.Collectors

/**
 * Allows easy integration into gradle.
 *
 * @author Yazad Khambata
 */
class LinterGradlePluginHelper {
    static void main(String[] args) {
        final String projectDir = args[0]

        ["main", "test", "integrationTest"].stream().forEach {
            final List<Lintlet> lintletClasses = Arrays.stream(args, 1, args.length)
                    .map { arg -> Class.forName(arg) }
                    .map { lintletClass -> lintletClass.newInstance() }
                    .collect(Collectors.toList())

            Linter.instance.lint(
                    Source.PATH,
                    "${projectDir}/src/${it}/resources/aql-files",
                    lintletClasses)
        }
    }
}
