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
package com.github.lucapino.jira.model;

import com.atlassian.jira.rest.client.api.domain.Issue;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Luca Tagliani
 */
public class JiraIssue {

    private String assignee;
    private List<String> comments;
    private List<String> components;
    private Date created;
    private List<String> fixVersions;
    private String id;
    private String key;
    private String link;
    private String priority;
    private String reporter;
    private String resolution;
    private String status;
    private String summary;
    private String type;
    private Date updated;
    private Issue remoteIssue;

    public JiraIssue() {
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public List<String> getComments() {
        return comments;
    }

    public void addComment(String comment) {
        if (comments == null) {
            comments = new ArrayList<>();
        }
        comments.add(comment);
    }

    public List<String> getComponents() {
        return components;
    }

    public void addComponent(String component) {
        if (components == null) {
            components = new ArrayList<>();
        }
        components.add(component);
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public List<String> getFixVersions() {
        return fixVersions;
    }

    public void addFixVersion(String fixVersion) {
        if (fixVersions == null) {
            fixVersions = new ArrayList<>();
        }
        fixVersions.add(fixVersion);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Issue getRemoteIssue() {
        return remoteIssue;
    }

    public void setRemoteIssue(Issue remoteIssue) {
        this.remoteIssue = remoteIssue;
    }

}
