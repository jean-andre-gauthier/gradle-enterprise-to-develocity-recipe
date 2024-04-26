package com.develocity.gradle;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.marker.BuildTool;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.Tree.randomId;
import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.Assertions.settingsGradle;
import static org.openrewrite.groovy.Assertions.groovy;
import static org.openrewrite.java.Assertions.java;

class GradleEnterpriseToDevelocityGroovyTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("com.develocity.gradle.GradleEnterpriseToDevelocityGroovy")
          .parser(JavaParser.fromJavaVersion().logCompilationWarningsAndErrors(true).classpath("rewrite-core", "rewrite-gradle"));
    }

    @Test
    void firstTest() {
        rewriteRun(
            spec -> spec.allSources(s -> s.markers(new BuildTool(randomId(), BuildTool.Type.Gradle, "8.7"))),
                // language=groovy
            settingsGradle(
                """
                plugins {
                    id("com.gradle.enterprise") version "3.16.2"
                    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
                }
                
                gradleEnterprise {
                    server = "http://localhost:8180/"
                    allowUntrustedServer = true
                    buildScan {
                        capture { taskInputFiles = true }
                        uploadInBackground = true
                        publishAlways()
                    }
                }
                
                buildCache {
                    remote(gradleEnterprise.buildCache) {
                        enabled = true
                        push = false
                        allowInsecureProtocol = true
                    }
                }
                
                rootProject.name = "gradle-build-scan-quickstart"
                """,
            """
                plugins {
                    id("com.gradle.develocity") version "3.17"
                    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
                }
                
                develocity {
                    server = "http://localhost:8180/"
                    allowUntrustedServer = true
                    buildScan {
                        capture { taskInputFiles = true }
                        uploadInBackground = true
                        publishAlways()
                    }
                }
                
                buildCache {
                    remote(gradleEnterprise.buildCache) {
                        enabled = true
                        push = false
                        allowInsecureProtocol = true
                    }
                }
                
                rootProject.name = "gradle-build-scan-quickstart"
                """
              )
            );
    }
}
