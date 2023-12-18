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
package org.gradle.demo.tapi;

import org.gradle.demo.model.AllGavCoordinates;
import org.gradle.demo.model.OutgoingArtifactsModel;
import org.gradle.demo.plugin.Beacon;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;

public class App {
    public static void main(String... args) throws Exception {
        GradleConnector connector = GradleConnector.newConnector();
        connector.forProjectDirectory(findProjectPath(args));
        ProjectConnection connection = null;
        try {
            connection = connector.connect();
            var customModelBuilder = connection.model(AllGavCoordinates.class);
            customModelBuilder.withArguments("--init-script", copyInitScript().getAbsolutePath());
            var model = customModelBuilder.get();
            for (var gav : model.coordinates()) {
                System.out.println("gav = " + gav);
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private static File copyInitScript() throws IOException, URISyntaxException {
        Path init = Files.createTempFile("init", ".gradle");
        StringBuilder sb = new StringBuilder();
        File pluginJar = lookupJar(Beacon.class);
        File modelJar = lookupJar(OutgoingArtifactsModel.class);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(App.class.getResourceAsStream("/init.gradle"))
        )) {
            reader.lines()
                    .forEach(line -> {
                        String repl = line.replace("%%PLUGIN_JAR%%", pluginJar.getAbsolutePath())
                                .replace("%%MODEL_JAR%%", modelJar.getAbsolutePath());
                        // fix paths if we're on Windows
                        if (File.separatorChar=='\\') {
                            repl = repl.replace('\\', '/');
                        }
                        sb.append(repl)
                                .append("\n");
                    });
        }
        Files.copy(new ByteArrayInputStream(sb.toString().getBytes(Charset.defaultCharset())),
                init,
                StandardCopyOption.REPLACE_EXISTING);
        return init.toFile();
    }

    private static File lookupJar(Class<?> beaconClass) throws URISyntaxException {
        CodeSource codeSource = beaconClass.getProtectionDomain().getCodeSource();
        return new File(codeSource.getLocation().toURI());
    }

    private static File findProjectPath(String... args) {
        if (args.length == 0) {
            return new File(".").getAbsoluteFile();
        }
        return new File(args[0]);
    }
}
