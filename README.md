Available goals:
================
* **create-new-jira-version** - creates a new JIRA version
* **generate-release-notes** - generates a release notes file based on a velocity template
* **release-jira-version** - releases a JIRA version
* **transition-issues** - transitions issue based on a JQL query 

Example plugin definition:
==========================
    <plugin>
        <groupId>it.peng.maven.plugin</groupId>
        <artifactId>jira-maven-plugin</artifactId>
        <version>1.0-SNAPSHOT</version>
        <configuration>
            <serverId>jira-server</serverId>
            <url>https://jira.example.org/jira/ </url>
        </configuration>
    </plugin>

Example _create-new-jira-version_ goal configuration:
-------------------------------------
    <configuration>
        <jiraProjectKey>JRA</jiraProjectKey>
        <developmentVersion>${project.version}</developmentVersion>
    </configuration>

Example _generate-release-notes_ goal configuration:
------------------------------------------
    <configuration>
        <jiraProjectKey>JRA</jiraProjectKey>
        <releaseVersion>${project.version}</releaseVersion>
    </configuration>

Example _release-jira-version_ goal configuration:
----------------------------------------
    <configuration>
        <jiraProjectKey>JRA</jiraProjectKey>
        <releaseVersion>${project.version}</releaseVersion>
    </configuration>

Example _transition-issues_ goal configuration:
-------------------------------------------
    <configuration>
        <jiraProjectKey>JRA</jiraProjectKey>
        <releaseVersion>${project.version}</releaseVersion>
        <!-- Parameter 0 = Project Key, Parameter 1 = Fix version -->
        <jqlTemplate>project = ''{0}'' AND status in (Resolved) AND fixVersion = ''{1}''</jqlTemplate>
        <transition>Closed</transition>
    </configuration>
