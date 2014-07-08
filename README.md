Available goals:
================
* **create-new-jira-version** - creates a new JIRA version
* **generate-release-notes** - generates a release notes file based on a velocity template
* **release-jira-version** - releases a JIRA version
* **mail-release-notes** -  send announce mail with release note.
* **transition-issues** - transitions issue based on a JQL query 

Example plugin definition:
==========================
    <plugin>
        <groupId>it.peng.maven.plugin</groupId>
        <artifactId>jira-maven-plugin</artifactId>
        <version>1.0.1</version>
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

Example _mail-release-notes_ goal configuration:
----------------------------------------
    <configuration>
        <smtpHost>localhost</smtpHost>
		<smtpPort>25</smtpPort>
		<smtpUsername>user</smtpUsername>
		<smtpPassword>passwd</smtpPassword>
		<toAddresses>
			<toAddress>foo@bar.com</toAddress>
		</toAddresses>
		<ccAddresses>
			<ccAddress>bar@foo.com</ccAddress>
		</ccAddresses>
		<bccAddresses>
			<bccAddress>bar.foo@foo-bar.com</bccAddress>
		</bccAddresses>
		<fromDeveloperId>foo.bar</fromDeveloperId>
    </configuration>

    <developers>
		<developer>
			<id>foo.bar</id>
			<name>Foo Bar</name>
			<email>foo@bar.com</email>
		</developer>
	</developers>

Example _transition-issues_ goal configuration:
-------------------------------------------
    <configuration>
        <jiraProjectKey>JRA</jiraProjectKey>
        <releaseVersion>${project.version}</releaseVersion>
        <!-- Parameter 0 = Project Key, Parameter 1 = Fix version -->
        <jqlTemplate>project = ''{0}'' AND status in (Resolved) AND fixVersion = ''{1}''</jqlTemplate>
        <transition>Closed</transition>
    </configuration>
