/*
 * Copyright (C) 2006-2017 Hitachi Systems CBT S.p.A. All rights reserved.
 */
package com.github.lucapino.jira;

import com.github.lucapino.jira.helpers.IssuesReportHelper;
import com.github.lucapino.jira.helpers.JiraClient;
import java.util.HashMap;
import java.util.Map;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;

/**
 *
 * @author Tagliani
 */
public abstract class AbstractJiraMavenReport extends AbstractMavenReport {

    /**
     * Report output encoding. Note that this parameter is only relevant if the
     * goal is run from the command line or from the default build lifecycle. If
     * the goal is run indirectly as part of a site generation, the output
     * encoding configured in the Maven Site Plugin is used instead.
     *
     * @property parameter="outputEncoding"
     * default-value="${project.reporting.outputEncoding}"
     */
    protected String outputEncoding;
    /**
     * Skip plugin execution
     *
     * @parameter default-value="false"
     */
    public boolean skip;
    /**
     * Max number of issues to return
     *
     * @parameter parameter="maxIssues" default-value="100"
     * @required
     */
    int maxIssues = 100;
    /**
     * Valid JIRA columns.
     */
    protected static final Map<String, Integer> JIRA_COLUMNS = new HashMap<String, Integer>(15);

    static {
        JIRA_COLUMNS.put("Assignee", Integer.valueOf(IssuesReportHelper.COLUMN_ASSIGNEE));
        JIRA_COLUMNS.put("Component", Integer.valueOf(IssuesReportHelper.COLUMN_COMPONENT));
        JIRA_COLUMNS.put("Created", Integer.valueOf(IssuesReportHelper.COLUMN_CREATED));
        JIRA_COLUMNS.put("Fix Version", Integer.valueOf(IssuesReportHelper.COLUMN_FIX_VERSION));
        JIRA_COLUMNS.put("Id", Integer.valueOf(IssuesReportHelper.COLUMN_ID));
        JIRA_COLUMNS.put("Key", Integer.valueOf(IssuesReportHelper.COLUMN_KEY));
        JIRA_COLUMNS.put("Priority", Integer.valueOf(IssuesReportHelper.COLUMN_PRIORITY));
        JIRA_COLUMNS.put("Reporter", Integer.valueOf(IssuesReportHelper.COLUMN_REPORTER));
        JIRA_COLUMNS.put("Resolution", Integer.valueOf(IssuesReportHelper.COLUMN_RESOLUTION));
        JIRA_COLUMNS.put("Status", Integer.valueOf(IssuesReportHelper.COLUMN_STATUS));
        JIRA_COLUMNS.put("Summary", Integer.valueOf(IssuesReportHelper.COLUMN_SUMMARY));
        JIRA_COLUMNS.put("Type", Integer.valueOf(IssuesReportHelper.COLUMN_TYPE));
        JIRA_COLUMNS.put("Updated", Integer.valueOf(IssuesReportHelper.COLUMN_UPDATED));
    }
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
    protected String url;
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
     * JQL Template to generate release notes. Parameter 0 = Project Key
     * Parameter 1 = Fix version
     *
     * @parameter parameter="jqlTemplate" default-value="project = ''{0}'' AND
     * status in (Resolved, Closed) AND fixVersion = ''{1}''"
     * @required
     */
    protected String jqlTemplate = "project = ''{0}'' AND status in (Resolved, Closed) AND fixVersion = ''{1}''";
    /**
     * Sets the names of the columns that you want in the report. The columns
     * will appear in the report in the same order as you specify them here.
     * Multiple values can be separated by commas.
     * <p>
     * Valid columns are: <code>Assignee</code>, <code>Component</code>,
     * <code>Created</code>, <code>Fix Version</code>, <code>Id</code>,
     * <code>Key</code>, <code>Priority</code>, <code>Reporter</code>,
     * <code>Resolution</code>, <code>Status</code>, <code>Summary</code>,
     * <code>Type</code>, <code>Updated</code> and <code>Version</code>.
     * </p>
     *
     * @parameter default-value="Key,Summary,Status,Resolution,Assignee"
     */
    protected String columnNames;

    /**
     * @parameter parameter="settings"
     */
    protected Settings settings;
    /**
     * Released Version
     *
     * @parameter parameter="releaseVersion" default-value="${project.version}"
     * @required
     */
    public String releaseVersion;

    protected JiraClient client;

    public AbstractJiraMavenReport() {
        loadUserCredentials();
    }

    public JiraClient getClient() throws MojoFailureException {
        if (client == null) {
            getLog().debug("Connecting to JIRA server");
            try {
                client = new JiraClient(username, password, url);
                getLog().info("Successfuly connected to JIRA server");
            } catch (Exception e) {
                getLog().error("Unable to connect to JIRA server", e);
            }
        }
        return client;
    }

    private void loadUserCredentials() {
        if (serverId == null) {
            serverId = url;
        }
        // read credentials from settings.xml if user has not set them in configuration
        if ((username == null || password == null) && settings != null) {
            Server server = settings.getServer(serverId);
            if (server != null) {
                if (username == null) {
                    username = server.getUsername();
                }
                if (password == null) {
                    password = server.getPassword();
                }
            }
        }
    }
}
