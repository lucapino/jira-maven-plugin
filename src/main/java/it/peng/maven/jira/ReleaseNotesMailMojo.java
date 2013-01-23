/*
 * Copyright 2013 tagliani.
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
package it.peng.maven.jira;

import it.peng.maven.jira.helpers.ProjectJavamailMailSender;
import it.peng.maven.jira.model.MailSender;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.apache.maven.model.Developer;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.mailsender.MailMessage;
import org.codehaus.plexus.mailsender.MailSenderException;
import org.codehaus.plexus.util.IOUtil;

/**
 * Goal that generates release notes based on a version in a JIRA project.
 *
 * NOTE: SOAP access must be enabled in your JIRA installation. Check JIRA docs
 * for more info.
 *
 * @goal mail-release-notes
 * @execute goal="generate-release-notes"
 *
 * @author Luca Tagliani
 */
public class ReleaseNotesMailMojo extends AbstractJiraMojo {

    /**
     * Possible senders.
     *
     * @parameter property="project.developers"
     * @required
     * @readonly
     */
    private List from;
    /**
     * The id of the developer sending the announcement mail. Only used if the
     * <tt>mailSender</tt>
     * attribute is not set. In this case, this should match the id of one of
     * the developers in the pom. If a matching developer is not found, then the
     * first developer in the pom will be used.
     *
     * @parameter
     */
    private String fromDeveloperId;
    /**
     * Mail content type to use.
     *
     * @parameter default-value ="text/plain"
     * @required
     */
    private String mailContentType;
    /**
     * Defines the sender of the announcement email. This takes precedence over
     * the list of developers specified in the POM. if the sender is not a
     * member of the development team. Note that since this is a bean type, you
     * cannot specify it from command level with
     * <pre>-D</pre>. Use
     * <pre>-Dchanges.sender='Your Name &lt;you@domain>'</pre> instead.
     * @parameter
     */
    private MailSender mailSender;
    /**
     * Defines the sender of the announcement. This takes precedence over both
     * ${changes.mailSender} and the list of developers in the POM.
     * <p/>
     * This parameter parses an email address in standard RFC822 format, e.g.
     * <pre>-Dchanges.sender='Your Name &lt;you@domain>'</pre>.
     *
     * @parameter
     */
    private String senderString;
    /**
     * The password used to send the email.
     *
     * @parameter
     */
    private String smtpPassword;
    /**
     * Smtp Server.
     *
     * @parameter
     * @required
     */
    private String smtpHost;
    /**
     * Port.
     *
     * @parameter default-value="25"
     * @required
     */
    private int smtpPort;
    /**
     * If the email should be sent in SSL mode.
     *
     * @parameter default-value="false"
     */
    private boolean sslMode;
    /**
     * Subject for the email.
     *
     * @parameter default-value="[ANNOUNCEMENT] - ${project.name}
     * ${project.version} released"
     * @required
     */
    private String subject;
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
     * Recipient email address.
     *
     * @parameter
     */
    private List toAddresses;
    /**
     * Recipient cc email address.
     *
     * @parameter
     */
    private List ccAddresses;
    /**
     * Recipient bcc email address.
     *
     * @parameter
     */
    private List bccAddresses;
    /**
     * The username used to send the email.
     *
     * @parameter
     */
    private String smtpUsername;
    private ProjectJavamailMailSender mailer = new ProjectJavamailMailSender();

    @Override
    public void doExecute() throws Exception {
        // retrieve the announcement crated at the generate-release-notes goal

        ConsoleLogger logger = new ConsoleLogger(Logger.LEVEL_INFO, "base");

        if (getLog().isDebugEnabled()) {
            logger.setThreshold(Logger.LEVEL_DEBUG);
        }

        mailer.enableLogging(logger);

        mailer.setSmtpHost(smtpHost);

        mailer.setSmtpPort(smtpPort);

        mailer.setSslMode(sslMode);

        if (smtpUsername != null) {
            mailer.setUsername(smtpUsername);
        }

        if (smtpPassword != null) {
            mailer.setPassword(smtpPassword);
        }

        mailer.initialize();

        if (getLog().isDebugEnabled()) {
            getLog().debug("fromDeveloperId: " + fromDeveloperId);
        }

        if (targetFile.isFile()) {
            getLog().info("Connecting to Host: " + smtpHost + ":" + smtpPort);

            sendMessage();
        } else {
            throw new MojoExecutionException("Announcement template " + targetFile + " not found...");
        }
        // create the mail
        // send the mail
    }

    /**
     * Send the email.
     *
     * @throws MojoExecutionException if the mail could not be sent
     */
    protected void sendMessage()
            throws MojoExecutionException {
        String email = "";
        final MailSender ms = getActualMailSender();
        final String fromName = ms.getName();
        final String fromAddress = ms.getEmail();
        if (fromAddress == null || fromAddress.equals("")) {
            throw new MojoExecutionException("Invalid mail sender: name and email is mandatory (" + ms + ").");
        }
        if (toAddresses == null && bccAddresses == null && ccAddresses == null) {
            throw new MojoExecutionException("Invalid mail recipients: a recipients (to, cc or bcc) is mandatory (" + ms + ").");
        }

        getLog().info("Using this sender for email announcement: " + fromAddress + " < " + fromName + " > ");
        try {
            MailMessage mailMsg = new MailMessage();
            mailMsg.setSubject(subject);
            mailMsg.setContent(IOUtil.toString(new FileInputStream(targetFile), "UTF-8"));
            mailMsg.setContentType(this.mailContentType);
            mailMsg.setHeader("Content-Transfer-Encoding", "quoted-printable");
            mailMsg.setFrom(fromAddress, fromName);

            if (ccAddresses != null) {
                final Iterator it = toAddresses.iterator();
                while (it.hasNext()) {
                    email = it.next().toString();
                    getLog().info("Sending mail to " + email + "...");
                    mailMsg.addTo(email, "");
                }
            }
            if (ccAddresses != null) {
                final Iterator it2 = ccAddresses.iterator();
                while (it2.hasNext()) {
                    email = it2.next().toString();
                    getLog().info("Sending cc mail to " + email + "...");
                    mailMsg.addCc(email, "");
                }
            }

            if (bccAddresses != null) {
                final Iterator it3 = bccAddresses.iterator();
                while (it3.hasNext()) {
                    email = it3.next().toString();
                    getLog().info("Sending bcc mail to " + email + "...");
                    mailMsg.addBcc(email, "");
                }
            }

            mailer.send(mailMsg);
            getLog().info("Sent...");
        } catch (IOException ioe) {
            throw new MojoExecutionException("Failed to send email.", ioe);
        } catch (MailSenderException e) {
            throw new MojoExecutionException("Failed to send email < " + email + " >", e);
        }
    }

    /**
     * Returns the identify of the mail sender according to the plugin's
     * configuration:
     * <ul>
     * <li>if the <tt>mailSender</tt> parameter is set, it is returned</li>
     * <li>if no <tt>fromDeveloperId</tt> is set, the first developer in the
     * list is returned</li>
     * <li>if a <tt>fromDeveloperId</tt> is set, the developer with that id is
     * returned</li>
     * <li>if the developers list is empty or if the specified id does not
     * exist, an exception is thrown</li>
     * </ul>
     *
     * @return the mail sender to use
     * @throws MojoExecutionException if the mail sender could not be retrieved
     */
    protected MailSender getActualMailSender()
            throws MojoExecutionException {
        if (senderString != null) {
            try {
                InternetAddress ia = new InternetAddress(senderString, true);
                return new MailSender(ia.getPersonal(), ia.getAddress());
            } catch (AddressException e) {
                throw new MojoExecutionException("Invalid value for change.sender: ", e);
            }
        }
        if (mailSender != null && mailSender.getEmail() != null) {
            return mailSender;
        } else if (from == null || from.isEmpty()) {
            throw new MojoExecutionException(
                    "The <developers> section in your pom should not be empty. Add a <developer> entry or set the "
                    + "mailSender parameter.");
        } else if (fromDeveloperId == null) {
            final Developer dev = (Developer) from.get(0);
            return new MailSender(dev.getName(), dev.getEmail());
        } else {
            final Iterator it = from.iterator();
            while (it.hasNext()) {
                Developer developer = (Developer) it.next();

                if (fromDeveloperId.equals(developer.getId())) {
                    return new MailSender(developer.getName(), developer.getEmail());
                }
            }
            throw new MojoExecutionException(
                    "Missing developer with id '" + fromDeveloperId + "' in the <developers> section in your pom.");
        }
    }
}
