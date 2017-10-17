/*
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

import com.atlassian.jira.rest.client.api.domain.Version;
import static com.github.lucapino.jira.helpers.RemoteVersionComparator.doComparison;
import java.util.Comparator;

public class RemoteVersionComparator implements Comparator<Version> {

    @Override
    public int compare(Version o1, Version o2) {
        return doComparison(o1, o2);
    }

    public static int doComparison(Version o1, Version o2) {
        return -1 * o1.getName().compareToIgnoreCase(o2.getName());
    }
}
