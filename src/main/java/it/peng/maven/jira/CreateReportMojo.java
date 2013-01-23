/*
 * Copyright 2013 tagliani.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.peng.maven.jira;

import it.peng.maven.jira.helpers.IssuesDownloader;
import it.peng.maven.jira.helpers.IssuesReportGenerator;
import it.peng.maven.jira.helpers.IssuesReportHelper;
import it.peng.maven.jira.model.JiraIssue;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.settings.Settings;

/**
 * Creates a report of a jira version
 * @goal jira-report
 * @author tagliani
 */
public class CreateReportMojo extends AbstractMavenReport {

    /**
     * Report output directory. Note that this parameter is only relevant if the goal is run from the command line or
     * from the default build lifecycle. If the goal is run indirectly as part of a site generation, the output
     * directory configured in the Maven Site Plugin is used instead.
     * @parameter default-value="${project.reporting.outputDirectory}"
     */
    private File outputDirectory;
    /**
     * Report output encoding. Note that this parameter is only relevant if the goal is run from the command line or
     * from the default build lifecycle. If the goal is run indirectly as part of a site generation, the output
     * encoding configured in the Maven Site Plugin is used instead.
     * @property parameter="outputEncoding" default-value="${project.reporting.outputEncoding}"
     */
    private String outputEncoding;
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
    private static final Map<String, Integer> JIRA_COLUMNS = new HashMap<String, Integer>(15);

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
    private String serverId;
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
     * JQL Template to generate release notes. Parameter 0 = Project Key
     * Parameter 1 = Fix version
     *
     * @parameter parameter="jqlTemplate" default-value="project = ''{0}'' AND status in (Resolved, Closed) AND fixVersion = ''{1}''"
     * @required
     */
    String jqlTemplate = "project = ''{0}'' AND status in (Resolved, Closed) AND fixVersion = ''{1}''";
    /**
     * Sets the names of the columns that you want in the report. The columns
     * will appear in the report in the same order as you specify them here.
     * Multiple values can be separated by commas.
     * <p>
     * Valid columns are:
     * <code>Assignee</code>,
     * <code>Component</code>,
     * <code>Created</code>,
     * <code>Fix Version</code>,
     * <code>Id</code>,
     * <code>Key</code>,
     * <code>Priority</code>,
     * <code>Reporter</code>,
     * <code>Resolution</code>,
     * <code>Status</code>,
     * <code>Summary</code>,
     * <code>Type</code>,
     * <code>Updated</code> and
     * <code>Version</code>.
     * </p>
     *
     * @parameter default-value="Key,Summary,Status,Resolution,Assignee"
     */
    private String columnNames;
    /**
     * Doxia Site Renderer.
     *
     * @component
     */
    protected Renderer siteRenderer;
    /**
     * The Maven Project.
     *
     * @component
     */
    protected MavenProject project;
    /**
     * The Maven Wagon manager to use when obtaining server authentication
     * details.
     *
     * @component role="org.apache.maven.artifact.manager.WagonManager"
     * @required
     * @readonly
     */
    protected WagonManager wagonManager;
    /**
     * @parameter parameter="settings"
     */
    Settings settings;
    /**
     * Released Version
     *
     * @parameter parameter="releaseVersion" default-value="${project.version}"
     * @required
     */
    String releaseVersion;

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#getProject()
     */
    @Override
    protected MavenProject getProject() {
        return project;
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#getSiteRenderer()
     */
    @Override
    protected Renderer getSiteRenderer() {
        return siteRenderer;
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#getOutputDirectory()
     */
    @Override
    protected String getOutputDirectory() {
        return outputDirectory.getAbsolutePath();
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#canGenerateReport()
     */
    @Override
    public boolean canGenerateReport() {
        if (skip) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void executeReport(Locale locale)
            throws MavenReportException {
        // Validate parameters
        List<Integer> columnIds = IssuesReportHelper.getColumnIds(columnNames, JIRA_COLUMNS);
        if (columnIds.isEmpty()) {
            // This can happen if the user has configured column names and they are all invalid
            throw new MavenReportException(
                    "jira-maven-plugin: None of the configured columnNames '" + columnNames + "' are valid.");
        }

        try {
            // Download issues
            IssuesDownloader issueDownloader = new IssuesDownloader();
            configureIssueDownloader(issueDownloader);
            List<JiraIssue> issueList = issueDownloader.getIssueList();

            // Generate the report
            IssuesReportGenerator report = new IssuesReportGenerator(IssuesReportHelper.toIntArray(columnIds));

            if (issueList.isEmpty()) {
                report.doGenerateEmptyReport(getBundle(locale), getSink());
            } else {
                report.doGenerateReport(getBundle(locale), getSink(), issueList);
            }
        } catch (Exception e) {
            getLog().warn(e);
        }
    }

    @Override
    public String getOutputName() {
        return "jira-report";
    }

    @Override
    public String getName(Locale locale) {
        return getBundle(locale).getString("report.issues.name");
    }

    @Override
    public String getDescription(Locale locale) {
        return getBundle(locale).getString("report.issues.description");
    }

    /* --------------------------------------------------------------------- */
    /* Private methods                                                       */
    /* --------------------------------------------------------------------- */
    private ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle("jira-report", locale, this.getClass().getClassLoader());
    }

    private void configureIssueDownloader(IssuesDownloader issueDownloader) {
        issueDownloader.setLog(getLog());
        issueDownloader.setMavenProject(project);
        issueDownloader.setMaxIssues(maxIssues);
        issueDownloader.setJiraUser(username);
        issueDownloader.setJiraPassword(password);
        issueDownloader.setSettings(settings);
        issueDownloader.setJqlTemplate(jqlTemplate);
        issueDownloader.setReleaseVersion(releaseVersion);
        issueDownloader.setServerId(serverId);
        issueDownloader.setWagonManager(wagonManager);
        issueDownloader.setJiraProjectKey(jiraProjectKey);
        issueDownloader.setUrl(url);
    }
}
