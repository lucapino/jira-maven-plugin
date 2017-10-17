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
package com.github.lucapino.jira;

import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.input.VersionInput;
import com.github.lucapino.jira.helpers.RemoteVersionComparator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.collections.IteratorUtils;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.joda.time.DateTime;

/**
 * Goal that creates a version in a JIRA project.
 *
 * @author George Gastaldi
 * @author Luca Tagliani
 */
@Mojo(name = "release-jira-version")
@Execute(goal = "release-jira-version", phase = LifecyclePhase.DEPLOY)
public class ReleaseVersionMojo extends AbstractJiraMojo {

    /**
     * Released Version
     */
    @Parameter(defaultValue = "${project.version}")
    String releaseVersion;
    /**
     * Auto Discover latest release and release it.
     */
    @Parameter(defaultValue = "true")
    boolean autoDiscoverLatestRelease;
    /**
     * Comparator for discovering the latest release
     */
    Comparator<Version> remoteVersionComparator = new RemoteVersionComparator();

    @Override
    public void doExecute() throws Exception {
        Log log = getLog();
        VersionHolder thisReleaseVersion = calculateReleaseVersion();
        if (thisReleaseVersion != null) {
            log.info("Releasing Version " + thisReleaseVersion.getVersion().getName());
            markVersionAsReleased(thisReleaseVersion);
        }
    }

    /**
     * Returns the latest unreleased version
     *
     * @return the new version to release.
     */
    private VersionHolder calculateReleaseVersion() {
        Project jiraProject = jiraClient.getRestClient().getProjectClient().getProject(jiraProjectKey).claim();
        Iterable<Version> versions = jiraProject.getVersions();

        List<Version> versionList = IteratorUtils.toList(versions.iterator());
        Collections.sort(versionList, remoteVersionComparator);

        VersionHolder holder = null;
        VersionInput version;
        for (Version remoteVersion : versions) {
            // if we don't want auto discover -> we use provided releaseVersion
            if (!autoDiscoverLatestRelease && !remoteVersion.isReleased() && remoteVersion.getName().equals(releaseVersion)) {
                version = new VersionInput(jiraProjectKey, releaseVersion, null, new DateTime(), false, true);
                holder = new VersionHolder(version, remoteVersion.getSelf());
                break;
            } else {
                // else get first unreleased version ??
                if (autoDiscoverLatestRelease && !remoteVersion.isReleased()) {
                    version = new VersionInput(jiraProjectKey, remoteVersion.getName(), null, new DateTime(), false, true);
                    holder = new VersionHolder(version, remoteVersion.getSelf());
                    break;
                }
            }
        }
        return holder;
    }

    void markVersionAsReleased(VersionHolder versionHolder) throws MojoFailureException {
        jiraClient.getRestClient().getVersionRestClient().updateVersion(versionHolder.getVersionURI(), versionHolder.getVersion()).claim();
        getLog().info("Version " + versionHolder.getVersion().getName() + " was released in JIRA.");
    }
}
