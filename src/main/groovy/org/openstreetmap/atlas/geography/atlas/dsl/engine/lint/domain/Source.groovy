package org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain

import org.openstreetmap.atlas.geography.atlas.dsl.path.PathQueryFilePackCollection
import org.openstreetmap.atlas.geography.atlas.dsl.path.PathUtil

/**
 * Built in sources supported in AQL (typically used with "using" command)
 *
 * @author Yazad Khambata
 */
enum Source {
    PATH {
        @Override
        PathQueryFilePackCollection aqlFilesFrom(final String root) {
            PathUtil.instance.aqlFilesFromPath(root)
        }
    },

    CLASSPATH {
        @Override
        PathQueryFilePackCollection aqlFilesFrom(final String root) {
            PathUtil.instance.aqlFilesFromClasspath(root)
        }
    };

    abstract PathQueryFilePackCollection aqlFilesFrom(final String root)
}
