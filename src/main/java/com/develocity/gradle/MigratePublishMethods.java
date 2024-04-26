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
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Statement;

import java.util.stream.Collectors;

@Value
@EqualsAndHashCode(callSuper = false)
public class MigratePublishMethods extends Recipe {

    @Override
    public String getDisplayName() {
        return "Gradle Enterprise to Develocity (Gradle builds)";
    }

    @Override
    public String getDescription() {
        return "Migrate Gradle builds from Gradle Enterprise to Develocity.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(Preconditions.or(new IsBuildGradle<>(), new IsSettingsGradle<>()), new RenameGradleEnterpriseMethodInvocation());
    }


    private static class RenameGradleEnterpriseMethodInvocation extends GroovyIsoVisitor<ExecutionContext> {
        private static final String GE_METHOD_NAME = "gradleEnterprise";
        private static final String DV_METHOD_NAME = "develocity";

        @Override
        public G.CompilationUnit visitCompilationUnit(G.CompilationUnit cu, ExecutionContext ctx) {
            cu = super.visitCompilationUnit(cu, ctx);
            return cu.withStatements(cu.getStatements().stream().map(this::renameGradleEnterpriseMethodInvocation).collect(Collectors.toList()));
        }

        private Statement renameGradleEnterpriseMethodInvocation(Statement statement) {
            if (statement instanceof J.MethodInvocation) {
                J.MethodInvocation m = (J.MethodInvocation) statement;
                if (GE_METHOD_NAME.equals(m.getSimpleName())) {
                    JavaType.Method type = m.getMethodType();
                    if (type != null) {
                        type = type.withName(DV_METHOD_NAME);
                    }
                    m = m.withName(m.getName().withSimpleName(DV_METHOD_NAME).withType(type))
                            .withMethodType(type);
                }
                return m;
            }
            return statement;
        }
    }
}
