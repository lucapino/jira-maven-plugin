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
package it.peng.maven.jira.helpers;

import it.peng.maven.jira.model.JiraIssue;
import java.util.Comparator;

/**
 *
 * @author tagliani
 */
public class JiraIssueComparator implements Comparator<JiraIssue> {

    @Override
    public int compare(JiraIssue issue1, JiraIssue issue2) {
        return Long.valueOf(issue1.getKey().split("-")[1]).compareTo(Long.valueOf(issue2.getKey().split("-")[1]));
    }
}
