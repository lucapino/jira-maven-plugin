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
package it.peng.maven.jira.helpers;

import com.atlassian.jira.rpc.soap.beans.JiraSoapService;
import com.atlassian.jira.rpc.soap.beans.RemoteComment;
import com.atlassian.jira.rpc.soap.beans.RemoteComponent;
import com.atlassian.jira.rpc.soap.beans.RemoteIssue;
import com.atlassian.jira.rpc.soap.beans.RemoteIssueType;
import com.atlassian.jira.rpc.soap.beans.RemotePriority;
import com.atlassian.jira.rpc.soap.beans.RemoteProject;
import com.atlassian.jira.rpc.soap.beans.RemoteResolution;
import com.atlassian.jira.rpc.soap.beans.RemoteStatus;
import com.atlassian.jira.rpc.soap.beans.RemoteVersion;
import it.peng.maven.jira.model.JiraIssue;
import java.net.URL;
import java.rmi.RemoteException;
import static java.text.MessageFormat.format;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.WordUtils;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.wagon.authentication.AuthenticationInfo;

/**
 *
 * @author tagliani
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
     * The maven project.
     */
    protected MavenProject project;
    /**
     * The maven settings.
     */
    protected Settings settings;
    protected String jiraProjectKey;
    protected String releaseVersion;
    protected String jqlTemplate;

    public void setJqlTemplate(String jqlTemplate) {
        this.jqlTemplate = jqlTemplate;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }
    protected WagonManager wagonManager;

    public void setWagonManager(WagonManager wagonManager) {
        this.wagonManager = wagonManager;
    }
    private JiraClient client;

    public List<JiraIssue> getIssueList() throws MojoFailureException {
        List<JiraIssue> issues = new ArrayList<JiraIssue>();
        // strip out -SNAPSHOT from releaseVersion
        releaseVersion = WordUtils.capitalize(releaseVersion.replace("-SNAPSHOT", "").replace("-", " "));
        String jql = format(jqlTemplate, jiraProjectKey, releaseVersion);
        if (log.isInfoEnabled()) {
            log.info("JQL: " + jql);
        }
        RemoteIssue[] remoteIssues = null;
        try {
            remoteIssues = getClient().getService().getIssuesFromJqlSearch(getClient().getToken(), jql, maxIssues);
            if (log.isInfoEnabled()) {
                log.info("Issues: " + remoteIssues.length);
            }
            for (RemoteIssue remoteIssue : remoteIssues) {
                JiraIssue jiraIssue = new JiraIssue();
                fillIssue(jiraIssue, remoteIssue);
                issues.add(jiraIssue);
            }
        } catch (Exception ex) {
            log.warn("No issues found.");
        }
        return issues;
    }

    public void setJiraProjectKey(String jiraProjectKey) {
        this.jiraProjectKey = jiraProjectKey;
    }

    /**
     * Sets the project.
     *
     * @param thisProject The project to set
     */
    public void setMavenProject(Object thisProject) {
        this.project = (MavenProject) thisProject;
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
    private String serverId;

    /**
     * Set the value of serverId
     *
     * @param serverId new value of serverId
     */
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
    private URL url;

    /**
     * Set the value of url
     *
     * @param url new value of url
     */
    public void setUrl(URL url) {
        this.url = url;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
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

    protected void fillIssue(JiraIssue issue, RemoteIssue remoteIssue) throws MojoFailureException {

        try {
            JiraSoapService jiraService = getClient().getService();
            String loginToken = getClient().getToken();
            RemoteProject remoteProject = jiraService.getProjectByKey(loginToken, remoteIssue.getProject());

            // reporter
            issue.setReporter(jiraService.getUser(loginToken, remoteIssue.getReporter()).getFullname());
            // type
            RemoteIssueType[] issueTypes = jiraService.getIssueTypesForProject(loginToken, remoteProject.getId());
            for (RemoteIssueType remoteIssueType : issueTypes) {
                if (remoteIssueType.getId().equals(remoteIssue.getType())) {
                    issue.setType(remoteIssueType.getName());
                    break;
                }
            }
            issue.setKey(remoteIssue.getKey());
            issue.setLink(String.format("%s/browse/%s", url, remoteIssue.getKey()));
            issue.setAssignee(jiraService.getUser(loginToken, remoteIssue.getAssignee()).getFullname());
            issue.setCreated(remoteIssue.getCreated().getTime());
            issue.setId(remoteIssue.getId());
            RemotePriority[] priorities = jiraService.getPriorities(loginToken);
            for (RemotePriority remotePriority : priorities) {
                if (remotePriority.getId().equals(remoteIssue.getPriority())) {
                    issue.setPriority(remotePriority.getName());
                    break;
                }
            }
            RemoteResolution[] resolutions = jiraService.getResolutions(loginToken);
            for (RemoteResolution remoteResolution : resolutions) {
                if (remoteResolution.getId().equals(remoteIssue.getResolution())) {
                    issue.setResolution(remoteResolution.getName());
                    break;
                }
            }
            RemoteStatus[] statuses = jiraService.getStatuses(loginToken);
            for (RemoteStatus remoteStatus : statuses) {
                if (remoteStatus.getId().equals(remoteIssue.getStatus())) {
                    issue.setStatus(remoteStatus.getName());
                    break;
                }
            }
            issue.setSummary(remoteIssue.getSummary());
            issue.setUpdated(remoteIssue.getUpdated().getTime());
            RemoteComment[] comments = jiraService.getComments(loginToken, remoteIssue.getKey());
            for (RemoteComment remoteComment : comments) {
                issue.addComment(remoteComment.getBody());
            }
            RemoteComponent[] components = remoteIssue.getComponents();
            for (RemoteComponent remoteComponent : components) {
                issue.addComponent(remoteComponent.getName());
            }
            RemoteVersion[] fixVersions = remoteIssue.getFixVersions();
            for (RemoteVersion remoteVersion : fixVersions) {
                issue.addFixVersion(remoteVersion.getName());
            }
        } catch (RemoteException ex) {
            getLog().error("Error retrieving values from remote issue");
            throw new MojoFailureException("Error retrieving values from remote issue", ex.getCause());
        }
    }

    protected MojoFailureException fail(String message, Exception e) {
        getLog().error(message, e);
        return new MojoFailureException(e, message, e.getMessage());
    }
}
