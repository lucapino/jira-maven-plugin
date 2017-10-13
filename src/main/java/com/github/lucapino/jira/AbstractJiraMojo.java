/*
 * Copyright 2011 Tomasz Maciejewski
 * Copyright 2012 George Gastaldi
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
package com.github.lucapino.jira;

import com.github.lucapino.jira.helpers.JiraClient;
import com.github.lucapino.jira.helpers.TemplateEvaluator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;

/**
 * This class allows the use of {@link JiraClient} in JIRA Actions
 *
 * @author george
 *
 */
public abstract class AbstractJiraMojo extends AbstractMojo {

    @Parameter(defaultValue = "${settings}", readonly = true, required = true)
    private Settings settings;
    /**
     * Server's id in settings.xml to look up jiraUser and jiraPassword.
     */
    @Parameter(name = "serverId")
    protected String serverId;
    /**
     * JIRA Installation URL. If not informed, it will use the
     * project.issueManagement.jiraURL info.
     *
     * @required
     */
    @Parameter(name = "jira.url", defaultValue = "${project.issueManagement.url}", required = true)
    protected String jiraURL;
    /**
     * JIRA Authentication User.
     */
    @Parameter(name = "jiraUser", defaultValue = "${scmUsername}")
    protected String jiraUser;
    /**
     * JIRA Authentication Password.
     */
    @Parameter(name = "jiraPassword", defaultValue = "${scmPassword}")
    protected String jiraPassword;
    /**
     * JIRA Project Key.
     */
    @Parameter(name = "jiraProjectKey")
    protected String jiraProjectKey;

    /**
     * The Maven project
     */
    @Parameter(defaultValue = "${project}")
    protected MavenProject project;
    /**
     * Returns if this plugin is enabled for this context
     */
    @Parameter(name = "skip")
    protected boolean skip;

    private TemplateEvaluator evaluator;
    protected JiraClient jiraClient;

    private void initJiraClient() throws MojoFailureException {
        if (jiraClient == null) {
            loadUserCredentials();
            getLog().debug("Connecting to JIRA server");
            try {
                jiraClient = new JiraClient(jiraUser, jiraPassword, jiraURL);
                getLog().info("Successfuly connected to JIRA server");
            } catch (Exception e) {
                throw fail("Unable to connect to JIRA server", e);
            }
        }
    }

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();
        if (isSkip()) {
            log.info("Skipping Plugin execution.");
            return;
        }
        try {
            initJiraClient();
            doExecute();
            jiraClient.getRestClient().close();
        } catch (Exception e) {
            log.error("Error when executing mojo", e);
            // XXX: Por enquanto nao faz nada.
        }
    }

    public TemplateEvaluator getEvaluator() {
        if (evaluator == null) {
            getLog().debug("Initializing Template Helper...");
            evaluator = new TemplateEvaluator(project);
            getLog().debug("Template Helper initialized");
        }
        return evaluator;
    }

    public abstract void doExecute() throws Exception;

    public boolean isSkip() {
        return skip;
    }

    public void setJiraProjectKey(String jiraProjectKey) {
        this.jiraProjectKey = jiraProjectKey;
    }

    public void setJiraPassword(String jiraPassword) {
        this.jiraPassword = jiraPassword;
    }

    public void setJiraURL(String jiraURL) {
        this.jiraURL = jiraURL;
    }

    public void setJiraUser(String jiraUser) {
        this.jiraUser = jiraUser;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    protected MojoFailureException fail(String message, Exception e) {
        getLog().error(message, e);
        return new MojoFailureException(e, message, e.getMessage());
    }

    private void loadUserCredentials() {
        if (serverId == null) {
            serverId = jiraURL;
        }
        // read credentials from settings.xml if user has not set them in configuration
        if ((jiraUser == null || jiraPassword == null) && this.settings != null) {
            Server server = settings.getServer(serverId);
            if (server != null) {
                if (jiraUser == null) {
                    jiraUser = server.getUsername();
                }
                if (jiraPassword == null) {
                    jiraPassword = server.getPassword();
                }
            }
        }
    }
}
