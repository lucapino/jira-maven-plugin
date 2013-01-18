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
package it.peng.maven.jira;

import com.atlassian.jira.rpc.soap.beans.RemoteIssue;
import java.rmi.RemoteException;
import static java.text.MessageFormat.format;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * Goal that made a transition on the given issues.
 *
 * NOTE: SOAP access must be enabled in your JIRA installation. Check JIRA docs
 * for more info.
 *
 * @goal transition-issues
 *
 * @author Luca Tagliani
 */
public class TransitionIssuesMojo extends AbstractJiraMojo {

    /**
     * JQL Template to retrieve Resolved issues. Parameter 0 = Project Key
     * Parameter 1 = Fix version
     *
     * @parameter parameter="jqlTemplate" default-value="project = ''{0}'' AND status in (Resolved) AND fixVersion = ''{1}''"
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
    public void doExecute()
            throws Exception {
        Log log = getLog();
        if (transition == null) {
            log.info("Transition not specified. Nothing to do");
        }
        RemoteIssue[] issues = getIssues();
        transitionIssues(issues, transition);
    }

    /**
     * Recover issues from JIRA based on JQL Filter
     *
     * @param jiraService
     * @param loginToken
     * @return
     * @throws RemoteException
     * @throws com.atlassian.jira.rpc.soap.client.RemoteException
     */
    RemoteIssue[] getIssues()
            throws RemoteException, MojoFailureException,
            com.atlassian.jira.rpc.soap.beans.RemoteException {
        Log log = getLog();
        String jql = format(jqlTemplate, jiraProjectKey, releaseVersion);
        if (log.isInfoEnabled()) {
            log.info("JQL: " + jql);
        }
        RemoteIssue[] issues = getClient().getService().getIssuesFromJqlSearch(getClient().getToken(), jql, maxIssues);
        if (log.isInfoEnabled()) {
            log.info("Issues: " + issues.length);
        }
        return issues;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public void setJqlTemplate(String jqlTemplate) {
        this.jqlTemplate = jqlTemplate;
    }

    private void transitionIssues(RemoteIssue[] issues, String transition) throws RemoteException, MojoFailureException,
            com.atlassian.jira.rpc.soap.beans.RemoteException {
        for (RemoteIssue remoteIssue : issues) {
            getClient().getService().progressWorkflowAction(getClient().getToken(), remoteIssue.getKey(), transition, null);
        }
    }
}
