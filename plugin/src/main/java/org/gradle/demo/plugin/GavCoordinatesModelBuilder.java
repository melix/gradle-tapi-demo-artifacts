/*
 * Copyright 2003-2012 the original author or authors.
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
package org.gradle.demo.plugin;

import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenArtifact;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.demo.model.AllGavCoordinates;
import org.gradle.demo.model.DefaultAllGavCoordinates;
import org.gradle.demo.model.DefaultGavCoordinates;
import org.gradle.demo.model.GavCoordinates;
import org.gradle.tooling.provider.model.ToolingModelBuilder;

import java.util.LinkedHashSet;
import java.util.Set;

public class GavCoordinatesModelBuilder implements ToolingModelBuilder {

    private static final String MODEL_NAME = AllGavCoordinates.class.getName();

    @Override
    public boolean canBuild(String modelName) {
        return modelName.equals(MODEL_NAME);
    }

    @Override
    public Object buildAll(String modelName, Project project) {
        Set<GavCoordinates> allCoordinates = new LinkedHashSet<>();
        project.allprojects(p -> {
            p.getPluginManager().withPlugin("maven-publish", unused -> {
                var publishingExtension = p.getExtensions().getByType(PublishingExtension.class);
                allCoordinates.addAll(publishingExtension.getPublications()
                    .stream()
                    .filter(MavenPublication.class::isInstance)
                    .map(MavenPublication.class::cast)
                    .map(pub -> {
                        var artifacts = pub.getArtifacts();
                        return new DefaultGavCoordinates(pub.getGroupId(), pub.getArtifactId(), pub.getVersion(),
                            artifacts.stream().map(MavenArtifact::getFile).toList()
                        );
                    })
                    .toList());
            });
        });
        return new DefaultAllGavCoordinates(allCoordinates);
    }
}
