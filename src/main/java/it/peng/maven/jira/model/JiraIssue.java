/*
 * Copyright 2013 Luca Tagliani.
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
package it.peng.maven.jira.model;

import com.atlassian.jira.rpc.soap.beans.JiraSoapService;
import com.atlassian.jira.rpc.soap.beans.RemoteIssue;
import com.atlassian.jira.rpc.soap.beans.RemoteIssueType;
import com.atlassian.jira.rpc.soap.beans.RemoteProject;
import java.rmi.RemoteException;

/**
 *
 * @author tagliani
 */
public class JiraIssue {

    private RemoteIssue remoteIssue;
    private JiraSoapService jiraService;
    private String loginToken;

    public JiraIssue(RemoteIssue remoteIssue, JiraSoapService jiraService, String loginToken) {
        this.jiraService = jiraService;
        this.loginToken = loginToken;
        this.remoteIssue = remoteIssue;
    }

    public String getReporter() {
        String result;
        try {
            result = jiraService.getUser(loginToken, remoteIssue.getReporter()).getFullname();
        } catch (RemoteException ex) {
            result = "";
        }
        return result;
    }
    
    public String getKey() {
        return remoteIssue.getKey();
    }
    
    public String getSummary() {
        return remoteIssue.getSummary();
    }
    
    public String getType() {
        String result = "";
        try {
            RemoteProject project = jiraService.getProjectByKey(loginToken, remoteIssue.getProject());
            RemoteIssueType[] issueTypes =  jiraService.getIssueTypesForProject(loginToken, project.getId());
            for (RemoteIssueType remoteIssueType : issueTypes) {
                if (remoteIssueType.getId().equals(remoteIssue.getType())) {
                    result = remoteIssueType.getName();
                    break;
                }
            }
        } catch (RemoteException ex) {
            result = "";
        }
        return result;
    }
}
