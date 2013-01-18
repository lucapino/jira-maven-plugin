/*
 * Copyright 2011 Tomasz Maciejewski
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
package it.peng.maven.jira.helpers;

import com.atlassian.jira.rpc.soap.beans.JiraSoapService;
import com.atlassian.jira.rpc.soap.beans.JiraSoapServiceServiceLocator;
import java.net.URL;

public class JiraClient {

    private final JiraSoapService service;
    private final String token;
    /**
     * This is the JIRA SOAP Suffix for accessing the webservice
     */
    private final String jiraSoapSuffix = "/rpc/soap/jirasoapservice-v2";

    public JiraClient(String username, String password, URL url) throws Exception {
        JiraSoapServiceServiceLocator locator = new JiraSoapServiceServiceLocator();
        url = new URL(url.toExternalForm() + jiraSoapSuffix);
        service = locator.getJirasoapserviceV2(url);
        token = service.login(username, password);
    }

    public JiraSoapService getService() {
        return service;
    }

    public String getToken() {
        return token;
    }
}
