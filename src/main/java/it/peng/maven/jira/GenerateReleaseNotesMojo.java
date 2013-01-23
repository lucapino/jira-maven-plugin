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

import edu.emory.mathcs.backport.java.util.Collections;
import it.peng.maven.jira.helpers.IssuesDownloader;
import it.peng.maven.jira.helpers.JiraIssueComparator;
import it.peng.maven.jira.model.JiraIssue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * Goal that generates release notes based on a version in a JIRA project.
 *
 * NOTE: SOAP access must be enabled in your JIRA installation. Check JIRA docs
 * for more info.
 *
 * @goal generate-release-notes
 * @phase deploy
 *
 * @author George Gastaldi
 */
public class GenerateReleaseNotesMojo extends AbstractJiraMojo {

    /**
     * JQL Template to generate release notes. Parameter 0 = Project Key
     * Parameter 1 = Fix version
     *
     * @parameter parameter="jqlTemplate" default-value="project = ''{0}'' AND status in (Resolved, Closed) AND fixVersion = ''{1}''"
     * @required
     */
    String jqlTemplate = "project = ''{0}'' AND status in (Resolved, Closed) AND fixVersion = ''{1}''";
    /**
     * Max number of issues to return
     *
     * @parameter parameter="maxIssues" default-value="100"
     * @required
     */
    int maxIssues = 100;
    /**
     * Released Version
     *
     * @parameter parameter="releaseVersion" default-value="${project.version}"
     * @required
     */
    String releaseVersion;
    /**
     * Map of custom parameters for the announcement. This Map will be passed to
     * the template.
     *
     * @parameter
     */
    private Map announceParameters;
    /**
     * Template file
     *
     * @parameter parameter="templateFile" property="templateFile"
     */
    File templateFile;
    /**
     * Target file
     *
     * @parameter parameter="targetFile" property="targetFile"
     * default-value="${project.build.directory}/releaseNotes.vm"
     * @required
     */
    File targetFile;
    /**
     * Text to be appended BEFORE all issues details.
     *
     * @parameter parameter="beforeText"
     */
    String beforeText;
    /**
     * Text to be appended AFTER all issues details.
     *
     * @parameter parameter="afterText"
     */
    String afterText;

    @Override
    public void doExecute() throws Exception {
        IssuesDownloader issuesDownloader = new IssuesDownloader();
        configureIssueDownloader(issuesDownloader);
        List<JiraIssue> issues = issuesDownloader.getIssueList();
        output(issues);
    }

    /**
     * Writes issues to output
     *
     * @param issues
     */
    void output(List<JiraIssue> issues) throws IOException, MojoFailureException {

        Log log = getLog();
        if (targetFile == null) {
            log.warn("No targetFile specified. Ignoring");
            return;
        }
        if (issues == null) {
            log.warn("No issues found. File will not be generated.");
            return;
        }
        HashMap<Object, Object> parameters = new HashMap<Object, Object>();
        HashMap<String, List<JiraIssue>> jiraIssues = processIssues(issues);
        List<JiraIssue> jiraIssuesList = new ArrayList<JiraIssue>();
        for (List<JiraIssue> list : jiraIssues.values()) {
            jiraIssuesList.addAll(list);
        }
        parameters.put("issues", jiraIssuesList);
        parameters.put("issuesMap", jiraIssues);
        parameters.put("jiraURL", url);
        parameters.put("jiraProjectKey", jiraProjectKey);
        parameters.put("releaseVersion", releaseVersion);
        if (announceParameters == null) {
            // empty Map to prevent NPE in velocity execution
            parameters.put("announceParameters", java.util.Collections.EMPTY_MAP);
        } else {
            parameters.put("announceParameters", announceParameters);
        }

        boolean useDefault = false;
        if (templateFile == null || !templateFile.exists()) {
            useDefault = true;
            // let's use the default one
            // it/peng/maven/jira/releaseNotes.vm
            InputStream defaultTemplate = this.getClass().getClassLoader().getResourceAsStream("it/peng/maven/jira/releaseNotes.vm");
            templateFile = File.createTempFile("releaseNotes.vm", null);
            FileOutputStream fos = new FileOutputStream(templateFile);
            IOUtils.copy(defaultTemplate, fos);
            IOUtils.closeQuietly(defaultTemplate);
            IOUtils.closeQuietly(fos);
        }

        String content = getEvaluator().evaluate(templateFile, parameters);

        if (useDefault) {
            // remove the temp file
            templateFile.delete();
        }

        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(targetFile, true), "UTF-8");
        PrintWriter ps = new PrintWriter(writer);

        try {
            if (beforeText != null) {
                ps.println(beforeText);
            }
            ps.println(content);
            if (afterText != null) {
                ps.println(afterText);
            }
        } finally {
            ps.flush();
            IOUtils.closeQuietly(ps);
        }
    }

    public void setAfterText(String afterText) {
        this.afterText = afterText;
    }

    public void setBeforeText(String beforeText) {
        this.beforeText = beforeText;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public void setJqlTemplate(String jqlTemplate) {
        this.jqlTemplate = jqlTemplate;
    }

    private HashMap<String, List<JiraIssue>> processIssues(List<JiraIssue> issues) throws MojoFailureException {
        HashMap<String, List<JiraIssue>> jiraIssues = new HashMap<String, List<JiraIssue>>();
        jiraIssues.put("add", new ArrayList<JiraIssue>());
        jiraIssues.put("fix", new ArrayList<JiraIssue>());
        jiraIssues.put("update", new ArrayList<JiraIssue>());
        for (JiraIssue issue : issues) {
            String issueCategory;
            String issueType = issue.getType();
            if (issueType.equalsIgnoreCase("new feature") || issueType.equalsIgnoreCase("task") || issueType.equalsIgnoreCase("internaltask") || issueType.equalsIgnoreCase("sub-task")) {
                // add
                issueCategory = "add";
            } else if (issueType.equalsIgnoreCase("bug") || issueType.equalsIgnoreCase("internalbug")) {
                // fix
                issueCategory = "fix";
            } else {
                // update
                issueCategory = "update";
            }
            List<JiraIssue> currentList = jiraIssues.get(issueCategory);
            currentList.add(issue);
            jiraIssues.put(issueCategory, currentList);
        }
        for (List<JiraIssue> list : jiraIssues.values()) {
            Collections.sort(list, new JiraIssueComparator());
        }
        return jiraIssues;
    }

    private void configureIssueDownloader(IssuesDownloader issueDownloader) {
        issueDownloader.setLog(getLog());
        issueDownloader.setMavenProject(project);
        issueDownloader.setMaxIssues(maxIssues);
        issueDownloader.setJiraUser(username);
        issueDownloader.setJiraPassword(password);
        issueDownloader.setSettings(settings);
        issueDownloader.setJqlTemplate(jqlTemplate);
        issueDownloader.setReleaseVersion(releaseVersion);
        issueDownloader.setServerId(serverId);
        issueDownloader.setWagonManager(wagonManager);
        issueDownloader.setJiraProjectKey(jiraProjectKey);
        issueDownloader.setUrl(url);
    }
}
