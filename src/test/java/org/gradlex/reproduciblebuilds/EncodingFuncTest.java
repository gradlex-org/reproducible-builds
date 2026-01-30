// SPDX-License-Identifier: Apache-2.0
package org.gradlex.reproduciblebuilds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.gradle.testkit.runner.BuildResult;
import org.gradlex.reproduciblebuilds.fixture.GradleBuild;
import org.gradlex.reproduciblebuilds.fixture.TestProject;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class EncodingFuncTest {

    @Test
    void plugin_sets_encoding_to_utf8(@TestProject GradleBuild build) {
        String data;
        try (var is = EncodingFuncTest.class.getResourceAsStream("utf8.txt")) {
            data = new String(Objects.requireNonNull(is).readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        build.getBuildFile().writeText("""
                plugins {
                    id 'java'
                    id 'groovy'
                    id 'scala'
                    id 'application'
                    id 'org.gradlex.reproducible-builds'
                }

                repositories {
                    mavenCentral()
                }

                application {
                    mainClass = "Test"
                }
                tasks.named('compileGroovy') {
                    classpath = sourceSets.main.compileClasspath
                }
                tasks.named('compileScala') {
                    classpath = sourceSets.main.compileClasspath
                }
                tasks.named('compileJava') {
                    classpath += files(sourceSets.main.groovy.classesDirectory, sourceSets.main.scala.classesDirectory)
                }
                tasks.named('javadoc') {
                    options.addStringOption("Xdoclint:-missing", "-Xwerror")
                }
                tasks.named('build') {
                    dependsOn('javadoc', 'groovydoc', 'scaladoc')
                }

                dependencies {
                    implementation(localGroovy())
                    implementation('org.scala-lang:scala-library:2.13.12')
                }
                """);

        build.getProjectDir().file("src/main/java/Test.java").writeText("""
                public class Test {
                    public static void main(String[] args) {
                        JavaClass.print();
                        JavaClassInGroovyFolder.print();
                        JavaClassInScalaFolder.print();
                        GroovyClass.print();
                        ScalaClass.print();
                    }
                }
        """);

        build.getProjectDir().file("src/main/java/JavaClass.java").writeText("""
                /**
                 * $DATA
                 */
                public class JavaClass { public static void print() { System.out.println("0 $DATA"); } }
                """.replace("$DATA", data));
        build.getProjectDir()
                .file("src/main/groovy/JavaClassInGroovyFolder.java")
                .writeText("""
                /**
                 * $DATA
                 */
                public class JavaClassInGroovyFolder { public static void print() { System.out.println("1 $DATA"); }; }
                """.replace("$DATA", data));
        build.getProjectDir().file("src/main/scala/JavaClassInScalaFolder.java").writeText("""
                /**
                 * $DATA
                 */
                public class JavaClassInScalaFolder { public static void print() { System.out.println("2 $DATA"); } }
                """.replace("$DATA", data));
        build.getProjectDir().file("src/main/groovy/GroovyClass.groovy").writeText("""
                /**
                 * $DATA
                 */
                class GroovyClass { static print() { println("3 $DATA") } }
                """.replace("$DATA", data));
        build.getProjectDir().file("src/main/scala/ScalaClass.scala").writeText("""
                /**
                 * $DATA
                 */
                object ScalaClass { def print = { println("4 $DATA"); } }
                """.replace("$DATA", data));

        // Run 'groovydoc' first so that it is UP-TO-DATE in the next execution.
        // Workaround for: https://github.com/gradle/gradle/issues/33288
        build.run("groovydoc");
        BuildResult result = build.run("build", "run", "-q");

        assertEquals("""
                0 $DATA
                1 $DATA
                2 $DATA
                3 $DATA
                4 $DATA
                """.replace("$DATA", data), result.getOutput().replace("\r\n", "\n"));

        assertTrue(build.output("docs/javadoc/JavaClass.html").contains(data));
        assertTrue(build.output("docs/javadoc/JavaClassInGroovyFolder.html").contains(data));
        assertTrue(build.output("docs/javadoc/JavaClassInScalaFolder.html").contains(data));
        assertTrue(build.output("docs/scaladoc/ScalaClass$.html").contains(data));
        // There seems to be no way to configure encoding of the Groovydoc task
        // assertTrue(build.output("docs/groovydoc/DefaultPackage/GroovyClass.html").contains(data));
    }
}
