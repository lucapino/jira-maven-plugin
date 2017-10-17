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
package com.github.lucapino.jira.helpers;

import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.BasicPriority;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.github.lucapino.jira.model.JiraIssue;
import static java.text.MessageFormat.format;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 *
 * @author Luca Tagliani
 */
public class IssuesDownloader {

    protected static final String UTF_8 = "UTF-8";
    /**
     * Log for debug output.
     */
    protected Log log;
    /**
     * The maximum number of entries to show.
     */
    protected int maxIssues;
    /**
     * The username to log into JIRA.
     */
    protected String jiraUser;
    /**
     * The password to log into JIRA.
     */
    protected String jiraPassword;
    /**
     * The JIRA project key.
     */
    protected String jiraProjectKey;
    /**
     * The JIRA release version.
     */
    protected String releaseVersion;
    /**
     * The JQL query template.
     */
    protected String jqlTemplate;
    /**
     * The JIRA Rest client.
     */
    protected JiraClient client;

    public List<JiraIssue> getIssueList() throws MojoFailureException {
        List<JiraIssue> issues = new ArrayList<>();
        // strip out -SNAPSHOT from releaseVersion
        releaseVersion = WordUtils.capitalize(releaseVersion.replace("-SNAPSHOT", "").replace("-", " "));
        String jql = format(jqlTemplate, jiraProjectKey, releaseVersion);
        if (log.isInfoEnabled()) {
            log.info("JQL: " + jql);
        }

        try {
            SearchResult remoteIssues = client.getRestClient().getSearchClient().searchJql(jql, maxIssues, 0, null).claim();
            if (log.isInfoEnabled()) {
                log.info("Issues: " + remoteIssues.getTotal());
            }
            for (Issue remoteIssue : remoteIssues.getIssues()) {
                JiraIssue jiraIssue = new JiraIssue();
                fillIssue(jiraIssue, remoteIssue);
                issues.add(jiraIssue);
            }
        } catch (MojoFailureException ex) {
            log.warn("No issues found.");
        }
        return issues;
    }

    public void setClient(JiraClient client) {
        this.client = client;
    }

    public void setJqlTemplate(String jqlTemplate) {
        this.jqlTemplate = jqlTemplate;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public void setJiraProjectKey(String jiraProjectKey) {
        this.jiraProjectKey = jiraProjectKey;
    }

    /**
     * Sets the maximum number of Issues to show.
     *
     * @param maxIssues The maximum number of Issues
     */
    public void setMaxIssues(final int maxIssues) {
        this.maxIssues = maxIssues;
    }

    /**
     * Sets the password to log into a secured JIRA.
     *
     * @param thisJiraPassword The password for JIRA
     */
    public void setJiraPassword(final String thisJiraPassword) {
        this.jiraPassword = thisJiraPassword;
    }

    /**
     * Sets the username to log into a secured JIRA.
     *
     * @param thisJiraUser The username for JIRA
     */
    public void setJiraUser(String thisJiraUser) {
        this.jiraUser = thisJiraUser;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    protected Log getLog() {
        return log;
    }

    protected void fillIssue(JiraIssue issue, Issue remoteIssue) throws MojoFailureException {
        // reporter
        User reporter = remoteIssue.getReporter();
        if (reporter != null) {
            issue.setReporter(reporter.getDisplayName());
        }
        // type
        issue.setType(remoteIssue.getIssueType().getName());
        // key
        issue.setKey(remoteIssue.getKey());
        issue.setLink(String.format("%s/browse/%s", client.getJiraURL(), remoteIssue.getKey()));
        User assignee = remoteIssue.getAssignee();
        if (assignee != null) {
            issue.setAssignee(assignee.getDisplayName());
        }
        issue.setCreated(remoteIssue.getCreationDate().toDate());
        issue.setId(remoteIssue.getId().toString());
        BasicPriority priority = remoteIssue.getPriority();
        if (priority != null) {
            issue.setPriority(priority.getName());
        }
        Resolution resolution = remoteIssue.getResolution();
        if (resolution != null) {
            issue.setResolution(resolution.getName());
        }
        issue.setStatus(remoteIssue.getStatus().getName());

        issue.setSummary(remoteIssue.getSummary());
        issue.setUpdated(remoteIssue.getUpdateDate().toDate());

        for (Comment remoteComment : remoteIssue.getComments()) {
            issue.addComment(remoteComment.getBody());
        }
        for (BasicComponent remoteComponent : remoteIssue.getComponents()) {
            issue.addComponent(remoteComponent.getName());
        }
        Iterable<Version> fixVersions = remoteIssue.getFixVersions();
        if (fixVersions != null) {
            for (Version remoteVersion : fixVersions) {
                issue.addFixVersion(remoteVersion.getName());
            }
        }
    }

    protected MojoFailureException fail(String message, Exception e) {
        getLog().error(message, e);
        return new MojoFailureException(e, message, e.getMessage());
    }
}
