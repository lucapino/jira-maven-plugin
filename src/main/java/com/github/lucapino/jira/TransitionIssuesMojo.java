/*
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

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.github.lucapino.jira.helpers.IssuesDownloader;
import com.github.lucapino.jira.model.JiraIssue;
import java.rmi.RemoteException;
import java.util.List;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Goal that made a transition on the given issues.
 *
 * @author Luca Tagliani
 */
@Mojo(name = "transition-issues")
public class TransitionIssuesMojo extends AbstractJiraMojo {

    /**
     * JQL Template to retrieve Resolved issues. Parameter 0 = Project Key
     * Parameter 1 = Fix version
     *
     * @parameter parameter="jqlTemplate" default-value="project = ''{0}'' AND
     * status in (Resolved) AND fixVersion = ''{1}''"
     * @required
     */
    String jqlTemplate = "project = ''{0}'' AND status in (Resolved) AND fixVersion = ''{1}''";
    /**
     * Max number of issues to return
     *
     * @parameter parameter="maxIssues" default-value="100"
     * @required
     */
    int maxIssues = 100;
    /**
     * Released Version
     *
     * @parameter parameter="releaseVersion" default-value="${project.version}"
     * @required
     */
    String releaseVersion;
    /**
     * Transition to take
     *
     * @parameter parameter="transition"
     * @required
     */
    String transition;

    @Override
    public void doExecute() throws Exception {
        Log log = getLog();
        if (transition == null) {
            log.info("Transition not specified. Nothing to do");
        }
        IssuesDownloader issuesDownloader = new IssuesDownloader();
        configureIssueDownloader(issuesDownloader);
        List<JiraIssue> issues = issuesDownloader.getIssueList();
        transitionIssues(issues, transition);
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public void setJqlTemplate(String jqlTemplate) {
        this.jqlTemplate = jqlTemplate;
    }

    private void transitionIssues(List<JiraIssue> issues, String transition) throws RemoteException, MojoFailureException {
        for (JiraIssue issue : issues) {
            Issue remoteIssue = issue.getRemoteIssue();
            Iterable<Transition> transitions = jiraClient.getRestClient().getIssueClient().getTransitions(remoteIssue).claim();
            if (transitions == null || !transitions.iterator().hasNext()) {
                getLog().warn("No transitions found for issue " + issue.getKey());
            } else {
                boolean found = false;
                for (Transition remoteTransition : transitions) {
                    if (remoteTransition.getName().equals(transition)) {
                        TransitionInput transitionToTake = new TransitionInput(remoteTransition.getId());
                        jiraClient.getRestClient().getIssueClient().transition(remoteIssue, transitionToTake).claim();
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    getLog().warn("No transition with name '" + transition + "' found for issue " + issue.getKey());
                }
            }
        }
    }

    private void configureIssueDownloader(IssuesDownloader issueDownloader) {
        issueDownloader.setLog(getLog());
        issueDownloader.setMaxIssues(maxIssues);
        issueDownloader.setJiraUser(jiraUser);
        issueDownloader.setJiraPassword(jiraPassword);
        issueDownloader.setJqlTemplate(jqlTemplate);
        issueDownloader.setReleaseVersion(releaseVersion);
        issueDownloader.setJiraProjectKey(jiraProjectKey);
        issueDownloader.setClient(jiraClient);
    }
}
