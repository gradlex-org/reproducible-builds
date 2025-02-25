/*
 * Copyright the GradleX team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        return pc.isAnnotated(TestProject.class)
                && pc.getParameter().getType().isAssignableFrom(GradleBuild.class);
    }

    @Override
    public Object resolveParameter(ParameterContext pc, ExtensionContext ec)
            throws ParameterResolutionException {
        return ec.getStore(NAMESPACE)
                .getOrComputeIfAbsent(
                        KEY,
                        __ -> new GradleBuildJunitAdapter(),
                        GradleBuildJunitAdapter.class
                ).adapted;
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
