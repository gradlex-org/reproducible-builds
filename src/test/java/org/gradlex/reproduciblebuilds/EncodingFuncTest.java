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

package org.gradlex.reproduciblebuilds;

import org.gradle.internal.impldep.org.bouncycastle.util.Strings;
import org.gradle.testkit.runner.BuildResult;
import org.gradlex.reproduciblebuilds.fixture.GradleBuild;
import org.gradlex.reproduciblebuilds.fixture.TestProject;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class EncodingFuncTest {

    @Test
    void plugin_sets_encoding_to_utf8(@TestProject GradleBuild build) {
        String data;
        try(var is = EncodingFuncTest.class.getResourceAsStream("utf8.txt")) {
            data = Strings.fromUTF8ByteArray(Objects.requireNonNull(is).readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Test Data: " + data);
        
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
        build.getProjectDir().file("src/main/groovy/JavaClassInGroovyFolder.java").writeText("""
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

        BuildResult result = build.run("build", "run", "-q");

        assertEquals(
                """
                0 $DATA
                1 $DATA
                2 $DATA
                3 $DATA
                4 $DATA
                """.replace("$DATA", data), result.getOutput().replace("\r\n", "\n"));

        assertTrue(build.output("docs/javadoc/JavaClass.html").contains(data));
        assertTrue(build.output("docs/javadoc/JavaClassInGroovyFolder.html").contains(data));
        assertTrue(build.output("docs/javadoc/JavaClassInScalaFolder.html").contains(data));
        // assertTrue(build.output("docs/groovydoc/DefaultPackage/GroovyClass.html").contains(data));
        // assertTrue(build.output("docs/scaladoc/ScalaClass$.html").contains(data));
    }
}
