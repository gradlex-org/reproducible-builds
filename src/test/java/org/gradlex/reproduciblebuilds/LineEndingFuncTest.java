// SPDX-License-Identifier: Apache-2.0
package org.gradlex.reproduciblebuilds;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.gradlex.reproduciblebuilds.fixture.GradleBuild;
import org.gradlex.reproduciblebuilds.fixture.TestProject;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class LineEndingFuncTest {

    @Test
    @DisabledIfSystemProperty(named = "gradleVersionUnderTest", matches = "8\\.3")
    void annotation_processors_always_use_unix_line_ending(@TestProject GradleBuild build) {
        build.getBuildFile()
                .writeText(
                        """
                plugins {
                    id 'java-library'
                    id 'org.gradlex.reproducible-builds'
                }
                repositories { mavenCentral() }
                dependencies {
                    implementation 'tools.jackson.core:jackson-core:3.0.0'
                    implementation 'com.google.auto.service:auto-service-annotations:1.1.1'
                    annotationProcessor 'com.google.auto.service:auto-service:1.1.1'
                }
                """);

        build.getProjectDir()
                .file("src/main/java/org/example/JsonFactory2.java")
                .writeText(
                        """
                package org.example;
                import com.google.auto.service.AutoService;
                import tools.jackson.core.TokenStreamFactory;
                import tools.jackson.core.json.JsonFactory;

                @AutoService(TokenStreamFactory.class)
                public class JsonFactory2 extends JsonFactory { }
                """);

        build.run("compileJava");

        String servicesFile = build.output("classes/java/main/META-INF/services/tools.jackson.core.TokenStreamFactory");
        assertEquals("org.example.JsonFactory2\n", servicesFile);
    }
}
