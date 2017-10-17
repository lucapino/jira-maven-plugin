/*
 * Copyright 2011 Tomasz Maciejewski
 * Copyright 2012 George Gastaldi
 * Copyright 2013-2017 Luca Tagliani
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
package com.github.lucapino.jira.helpers;

import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import java.net.URI;
import java.net.URL;

public class JiraClient {

    private final JiraRestClient jiraRestClient;

    private final URL url;

    public JiraClient(String username, String password, String url) throws Exception {
        JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        URI jiraServerUri = new URI(url);
        this.url = jiraServerUri.toURL();
        if (username != null && password != null) {
            jiraRestClient = factory.createWithBasicHttpAuthentication(jiraServerUri, username, password);
        } else {
            jiraRestClient = factory.create(jiraServerUri, (AuthenticationHandler) null);
        }

    }

    public JiraRestClient getRestClient() {
        return jiraRestClient;
    }

    public URL getJiraURL() {
        return url;
    }
}
