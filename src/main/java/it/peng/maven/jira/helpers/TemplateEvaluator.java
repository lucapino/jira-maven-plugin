/*
 * Copyright 2011 Tomasz Maciejewski
 * Copyright 2013 Luca Tagliani
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
package it.peng.maven.jira.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map.Entry;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class TemplateEvaluator {

    private final VelocityEngine engine = new VelocityEngine();
    private final VelocityContext context = new VelocityContext();

    public TemplateEvaluator(MavenProject project) {
        if (project != null) {
            context.put("project", project);
            for (Entry<Object, Object> p : project.getProperties().entrySet()) {
                context.put(p.getKey().toString(), p.getValue());
            }
        }
    }

    public String evaluate(File file, HashMap<Object, Object> properties) throws FileNotFoundException, UnsupportedEncodingException {
        FileInputStream fis = new FileInputStream(file); 
        InputStreamReader reader = new InputStreamReader(fis, "UTF-8");
        StringWriter writer = new StringWriter();
        if (properties != null) {
            for (Entry<Object, Object> p : properties.entrySet()) {
                context.put(p.getKey().toString(), p.getValue());
            }
        }
        engine.evaluate(context, writer, "[Jira]", reader);
        return writer.toString();
    }

    public String evaluate(String text, HashMap<Object, Object> properties) {
        StringWriter writer = new StringWriter();
        if (properties != null) {
            for (Entry<Object, Object> p : properties.entrySet()) {
                context.put(p.getKey().toString(), p.getValue());
            }
        }
        engine.evaluate(context, writer, "[Jira]", text);
        return writer.toString();
    }
}
