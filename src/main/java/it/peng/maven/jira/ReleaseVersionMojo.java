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

import com.atlassian.jira.rpc.soap.beans.RemoteAuthenticationException;
import com.atlassian.jira.rpc.soap.beans.RemotePermissionException;
import com.atlassian.jira.rpc.soap.beans.RemoteVersion;
import it.peng.maven.jira.helpers.RemoteVersionComparator;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * Goal that creates a version in a JIRA project . NOTE: SOAP access must be
 * enabled in your JIRA installation. Check JIRA docs for more info.
 *
 * @goal release-jira-version
 * @phase deploy
 *
 * @author George Gastaldi
 */
public class ReleaseVersionMojo extends AbstractJiraMojo {

    /**
     * Released Version
     *
     * @parameter parameter="releaseVersion" default-value="${project.version}"
     */
    String releaseVersion;
    /**
     * Auto Discover latest release and release it.
     *
     * @parameter parameter="autoDiscoverLatestRelease" default-value="true"
     */
    boolean autoDiscoverLatestRelease;
    /**
     * Comparator for discovering the latest release
     *
     * @parameter
     * implementation="it.peng.maven.jira.helpers.RemoteVersionComparator"
     */
    Comparator<RemoteVersion> remoteVersionComparator = new RemoteVersionComparator();

    @Override
    public void doExecute()
            throws Exception {
        Log log = getLog();
        try {
            RemoteVersion[] versions = getClient().getService().getVersions(getClient().getToken(),
                    jiraProjectKey);
            String thisReleaseVersion = (autoDiscoverLatestRelease) ? calculateLatestReleaseVersion(versions)
                    : releaseVersion;
            if (thisReleaseVersion != null) {
                log.info("Releasing Version " + this.releaseVersion);
                markVersionAsReleased(versions,
                        thisReleaseVersion);
            }
        } finally {
            if (client != null) {
                getClient().getService().logout(getClient().getToken());
            }
        }
    }

    /**
     * Returns the latest unreleased version
     *
     * @param versions
     * @return
     */
    String calculateLatestReleaseVersion(RemoteVersion[] versions) {
        Arrays.sort(versions, remoteVersionComparator);

        for (RemoteVersion remoteVersion : versions) {
            if (!remoteVersion.isReleased()) {
                return remoteVersion.getName();
            }
        }
        return null;
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

    /**
     * Release Version
     *
     * @param log
     * @param jiraService
     * @param loginToken
     * @throws RemoteException
     * @throws RemotePermissionException
     * @throws RemoteAuthenticationException
     * @throws com.atlassian.jira.rpc.soap.client.RemoteException
     */
    RemoteVersion markVersionAsReleased(RemoteVersion[] versions, String releaseVersion)
            throws RemoteException, RemotePermissionException,
            RemoteAuthenticationException, RemoteException, MojoFailureException,
            com.atlassian.jira.rpc.soap.beans.RemoteException {
        RemoteVersion ret = null;
        if (versions != null) {
            for (RemoteVersion remoteReleasedVersion : versions) {
                if (releaseVersion.equalsIgnoreCase(remoteReleasedVersion
                        .getName())
                        && !remoteReleasedVersion.isReleased()) {
                    // Mark as released
                    remoteReleasedVersion.setReleased(true);
                    remoteReleasedVersion
                            .setReleaseDate(Calendar.getInstance());
                    getClient().getService().releaseVersion(getClient().getToken(), jiraProjectKey,
                            remoteReleasedVersion);
                    getLog().info(
                            "Version " + remoteReleasedVersion.getName()
                            + " was released in JIRA.");
                    ret = remoteReleasedVersion;
                    break;
                }
            }
        }
        return ret;
    }
}
