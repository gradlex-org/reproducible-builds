// SPDX-License-Identifier: Apache-2.0
package org.gradlex.reproduciblebuilds.fixture;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class TestProjectExtension implements ParameterResolver {

    private static final Namespace NAMESPACE = Namespace.create(TestProjectExtension.class);
    public static final String KEY = "gradle.build";

    @Override
    public boolean supportsParameter(ParameterContext pc, ExtensionContext ec) {
        return pc.isAnnotated(TestProject.class) && pc.getParameter().getType().isAssignableFrom(GradleBuild.class);
    }

    @Override
    public Object resolveParameter(ParameterContext pc, ExtensionContext ec) throws ParameterResolutionException {
        return ec.getStore(NAMESPACE)
                .getOrComputeIfAbsent(KEY, __ -> new GradleBuildJunitAdapter(), GradleBuildJunitAdapter.class)
                .adapted;
    }

    private static class GradleBuildJunitAdapter implements ExtensionContext.Store.CloseableResource {
        private final GradleBuild adapted;

        private GradleBuildJunitAdapter() {
            adapted = GradleBuild.create();
        }

        @Override
        public void close() {
            adapted.close();
        }
    }
}
