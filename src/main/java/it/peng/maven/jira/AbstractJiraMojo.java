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
package it.peng.maven.jira;

import com.atlassian.jira.rpc.soap.beans.JiraSoapService;
import it.peng.maven.jira.helpers.JiraClient;
import it.peng.maven.jira.helpers.TemplateEvaluator;
import java.net.URL;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.wagon.authentication.AuthenticationInfo;

/**
 * This class allows the use of {@link JiraSoapService} in JIRA Actions
 *
 * @author george
 *
 */
public abstract class AbstractJiraMojo extends AbstractMojo {

    /**
     * @parameter parameter="settings"
     */
    Settings settings;
    /**
     * Server's id in settings.xml to look up username and password.
     *
     * @parameter parameter="serverId"
     */
    protected String serverId;
    /**
     * JIRA Installation URL. If not informed, it will use the
     * project.issueManagement.url info.
     *
     * @parameter parameter="jira.url"
     * default-value="${project.issueManagement.url}"
     * @required
     */
    protected URL url;
    /**
     * JIRA Authentication User.
     *
     * @parameter parameter="username" default-value="${scmUsername}"
     */
    protected String username;
    /**
     * JIRA Authentication Password.
     *
     * @parameter parameter="password" default-value="${scmPassword}"
     */
    protected String password;
    /**
     * JIRA Project Key.
     *
     * @parameter parameter="jiraProjectKey"
     */
    protected String jiraProjectKey;
    /**
     * The Maven Wagon manager to use when obtaining server authentication
     * details.
     *
     * @component role="org.apache.maven.artifact.manager.WagonManager"
     * @required
     * @readonly
     */
    protected WagonManager wagonManager;
    private TemplateEvaluator evaluator;
    protected JiraClient client;
    /**
     * The Maven project
     *
     * @parameter default-value="${project}"
     * @readonly
     */
    protected MavenProject project;
    /**
     * Returns if this plugin is enabled for this context
     *
     * @parameter parameter="skip"
     */
    protected boolean skip;

    public AbstractJiraMojo() {
    }

    public AbstractJiraMojo(AbstractJiraMojo mojo) {
        this.serverId = mojo.serverId;
        this.url = mojo.url;
        this.project = mojo.project;
        this.wagonManager = mojo.wagonManager;
        this.evaluator = mojo.evaluator;
        this.client = mojo.client;
        this.setLog(mojo.getLog());
        this.setPluginContext(mojo.getPluginContext());
    }

    public JiraClient getClient() throws MojoFailureException {
        if (client == null) {
            getLog().debug("Connecting to JIRA server");
            try {
                AuthenticationInfo info = wagonManager.getAuthenticationInfo(serverId);
                client = new JiraClient(info.getUserName(), info.getPassword(), url);
                getLog().info("Successfuly connected to JIRA server");
            } catch (Exception e) {
                throw fail("Unable to connect to JIRA server", e);
            }
        }
        return client;
    }

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();
        if (isSkip()) {
            log.info("Skipping Plugin execution.");
            return;
        }
        try {
            doExecute();
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

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public void setSettingsKey(String settingsKey) {
        this.serverId = settingsKey;
    }

    protected MojoFailureException fail(String message, Exception e) {
        getLog().error(message, e);
        return new MojoFailureException(e, message, e.getMessage());
    }
}