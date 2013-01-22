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

import com.atlassian.jira.rpc.soap.beans.RemoteIssue;
import edu.emory.mathcs.backport.java.util.Collections;
import it.peng.maven.jira.helpers.JiraIssueComparator;
import it.peng.maven.jira.model.JiraIssue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import static java.text.MessageFormat.format;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;

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
     * Template file
     *
     * @parameter parameter="templateFile"
     */
    File templateFile;
    /**
     * Target file
     *
     * @parameter parameter="targetFile"
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
    public void doExecute()
            throws Exception {
        RemoteIssue[] issues = getIssues();
        output(issues);
    }

    /**
     * Recover issues from JIRA based on JQL Filter
     *
     * @param jiraService
     * @param loginToken
     * @return
     * @throws RemoteException
     * @throws com.atlassian.jira.rpc.soap.client.RemoteException
     */
    RemoteIssue[] getIssues()
            throws RemoteException, MojoFailureException,
            com.atlassian.jira.rpc.soap.beans.RemoteException {
        Log log = getLog();
        // strip out -SNAPSHOT from releaseVersion
        releaseVersion = StringUtils.capitaliseAllWords(releaseVersion.replace("-SNAPSHOT", "").replace("-", " "));
        String jql = format(jqlTemplate, jiraProjectKey, releaseVersion);
        if (log.isInfoEnabled()) {
            log.info("JQL: " + jql);
        }
        RemoteIssue[] issues = null;
        try {
            issues = getClient().getService().getIssuesFromJqlSearch(getClient().getToken(), jql, maxIssues);
            if (log.isInfoEnabled()) {
                log.info("Issues: " + issues.length);
            }
        } catch (Exception ex) {
            log.warn("No issues found.");
        }
        return issues;
    }

    /**
     * Writes issues to output
     *
     * @param issues
     */
    void output(RemoteIssue[] issues) throws IOException, MojoFailureException {
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
        List<JiraIssue> jiraIssues = new ArrayList<JiraIssue>();
        for (RemoteIssue remoteIssue : issues) {
            jiraIssues.add(new JiraIssue(remoteIssue, getClient().getService(), getClient().getToken()));
        }
        Collections.sort(jiraIssues, new JiraIssueComparator());
        parameters.put("issues", jiraIssues);
        parameters.put("jiraURL", url);
        parameters.put("jiraProjectKey", jiraProjectKey);
        parameters.put("releaseVersion", releaseVersion);

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
}
