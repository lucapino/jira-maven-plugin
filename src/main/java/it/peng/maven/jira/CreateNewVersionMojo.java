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

import com.atlassian.jira.rpc.soap.beans.RemoteVersion;
import it.peng.maven.jira.helpers.RemoteVersionComparator;
import java.util.Comparator;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;

/**
 * Goal that creates a version in a JIRA project . NOTE: SOAP access must be
 * enabled in your JIRA installation. Check JIRA docs for more info.
 *
 * @goal create-new-jira-version
 * @phase deploy
 *
 * @author George Gastaldi
 */
public class CreateNewVersionMojo extends AbstractJiraMojo {

    /**
     * Next Development Version
     *
     * @parameter parameter="developmentVersion"
     * default-value="${project.version}"
     * @required
     */
    String developmentVersion;
    /**
     * @parameter default-value="${project.build.finalName}"
     */
    String finalName;
    /**
     * Whether the final name is to be used for the version; defaults to false.
     *
     * @parameter parameter="finalNameUsedForVersion"
     */
    boolean finalNameUsedForVersion;
    /**
     * Comparator for discovering the latest release
     *
     * @parameter
     * implementation="it.peng.maven.jira.helpers.RemoteVersionComparator"
     */
    Comparator<RemoteVersion> remoteVersionComparator = new RemoteVersionComparator();

    @Override
    public void doExecute() throws Exception {
        Log log = getLog();
        try {
            RemoteVersion[] versions = getClient().getService()
                    .getVersions(getClient().getToken(),
                    jiraProjectKey);
            String newDevVersion;

            if (finalNameUsedForVersion) {
                newDevVersion = finalName;
            } else {
                newDevVersion = developmentVersion;
            }

            // Removing -SNAPSHOT suffix for safety and sensible formatting
            newDevVersion = StringUtils.capitaliseAllWords(newDevVersion.replace(
                    "-SNAPSHOT", "").replace("-", " "));

            boolean versionExists = isVersionAlreadyPresent(versions, newDevVersion);

            if (!versionExists) {
                RemoteVersion newVersion = new RemoteVersion();
                log.debug("New Development version in JIRA is: " + newDevVersion);
                newVersion.setName(newDevVersion);
                getClient().getService()
                        .addVersion(getClient().getToken(), jiraProjectKey, newVersion);
                log.info("Version created in JIRA for project key "
                        + jiraProjectKey + " : " + newDevVersion);
            } else {
                log.warn(String.format(
                        "Version %s is already created in JIRA. Nothing to do.",
                        newDevVersion));
            }
        } finally {
            if (client != null) {
                getClient().getService().logout(getClient().getToken());
            }
        }
    }

    /**
     * Check if version is already present on array
     *
     * @param versions
     * @param newDevVersion
     * @return
     */
    boolean isVersionAlreadyPresent(RemoteVersion[] versions,
            String newDevVersion) {
        boolean versionExists = false;

        if (versions != null) {
            // Creating new Version (if not already created)
            for (RemoteVersion remoteVersion : versions) {
                if (remoteVersion.getName().equalsIgnoreCase(newDevVersion)) {
                    versionExists = true;

                    break;
                }
            }
        }

        // existant
        return versionExists;
    }
}
