/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.develocity.gradle;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.gradle.IsBuildGradle;
import org.openrewrite.gradle.IsSettingsGradle;
import org.openrewrite.groovy.GroovyIsoVisitor;
import org.openrewrite.groovy.tree.G;

@Value
@EqualsAndHashCode(callSuper = false)
public class MigratePublishMethods extends Recipe {

    @Override
    public String getDisplayName() {
        return "Migrate publish methods";
    }

    @Override
    public String getDescription() {
        return "Migrates strict conditional publication APIs to the new publishing.onlyIf API.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(Preconditions.or(new IsBuildGradle<>(), new IsSettingsGradle<>()), new RenamePublishMethodInvocation());
    }

    private static class RenamePublishMethodInvocation extends GroovyIsoVisitor<ExecutionContext> {
        @Override
        public G.MethodInvocation visitMethodInvocation(G.MethodInvocation mi, ExecutionContext ctx) {
            mi = super.visitMethodInvocation(mi, ctx);
            switch (mi.getSimpleName()) {
                case "publishAlways()":
                    return mi;
                case "publishAlwaysIf()":
                    return mi;
                case "publishOnFailure()":
                    return mi;
                case "publishOnFailureIf()":
                    return mi;
                default:
                    return mi;
            }
        }

        private add() {
            Statement statement = GradleParser.builder().build()
                    .parseInputs(singletonList(Parser.Input.fromString(source)), null, ctx)
                    .findFirst()
                    .map(parsed -> {
                        if (parsed instanceof ParseError) {
                            throw ((ParseError) parsed).toException();
                        }
                        return ((G.CompilationUnit) parsed);
                    })
                    .map(parsed -> parsed.getStatements().get(0))
                    .orElseThrow(() -> new IllegalArgumentException("Could not parse as Gradle"));
        }
    }
}
