/*
 * Copyright 2013-2017 Luca Tagliani.
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
package com.github.lucapino.jira;

import com.github.lucapino.jira.helpers.IssuesDownloader;
import com.github.lucapino.jira.helpers.IssuesReportGenerator;
import com.github.lucapino.jira.helpers.IssuesReportHelper;
import com.github.lucapino.jira.model.JiraIssue;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.reporting.MavenReportException;

/**
 * Creates a report of a JIRA version.
 *
 * @author tagliani
 */
@Mojo(name = "jira-report")
public class CreateReportMojo extends AbstractJiraMavenReport {

    /**
     * @return true if we should generate the report.
     * @see org.apache.maven.reporting.AbstractMavenReport#canGenerateReport()
     */
    @Override
    public boolean canGenerateReport() {
        return !skip;
    }

    @Override
    public void executeReport(Locale locale) throws MavenReportException {
        // Validate parameters
        List<Integer> columnIds = IssuesReportHelper.getColumnIds(columnNames, JIRA_COLUMNS);
        if (columnIds.isEmpty()) {
            // This can happen if the user has configured column names and they are all invalid
            throw new MavenReportException("jira-maven-plugin: None of the configured columnNames '" + columnNames + "' are valid.");
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
        } catch (MojoFailureException e) {
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
        issueDownloader.setMaxIssues(maxIssues);
        issueDownloader.setJiraUser(username);
        issueDownloader.setJiraPassword(password);
        issueDownloader.setJqlTemplate(jqlTemplate);
        issueDownloader.setReleaseVersion(releaseVersion);
        issueDownloader.setJiraProjectKey(jiraProjectKey);
        issueDownloader.setClient(client);
    }
}
