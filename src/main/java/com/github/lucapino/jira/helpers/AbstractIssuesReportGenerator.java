/*
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

import java.util.ResourceBundle;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkEventAttributes;
import org.apache.maven.doxia.sink.impl.SinkEventAttributeSet;
import org.apache.maven.doxia.util.HtmlTools;
import org.codehaus.plexus.util.StringUtils;

/**
 * An abstract super class that helps when generating a report on issues.
 *
 * @author Dennis Lundberg
 * @version $Id: AbstractIssuesReportGenerator.java 1423355 2012-12-18 09:03:16Z
 * ltheussl $
 * @since 2.4
 */
public abstract class AbstractIssuesReportGenerator {

    protected String author;
    protected String title;

    public AbstractIssuesReportGenerator() {
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    protected void sinkBeginReport(Sink sink, ResourceBundle bundle) {
        sink.head();

        String reportTitle;
        if (this.title != null) {
            reportTitle = this.title;
        } else {
            reportTitle = bundle.getString("report.issues.header");
        }
        sink.title();
        sink.text(reportTitle);
        sink.title_();

        if (StringUtils.isNotEmpty(author)) {
            sink.author();
            sink.text(author);
            sink.author_();
        }

        sink.head_();

        sink.body();

        sink.section1();

        sinkSectionTitle1Anchor(sink, reportTitle, reportTitle);
    }

    protected void sinkCell(Sink sink, String text) {
        sink.tableCell();

        if (text != null) {
            sink.text(text);
        } else {
            sink.nonBreakingSpace();
        }

        sink.tableCell_();
    }

    protected void sinkCellLink(Sink sink, String text, String link) {
        sink.tableCell();

        sinkLink(sink, text, link);

        sink.tableCell_();
    }

    protected void sinkEndReport(Sink sink) {
        sink.section1_();

        sink.body_();

        sink.flush();

        sink.close();
    }

    protected void sinkFigure(Sink sink, String image, String altText) {
        SinkEventAttributes attributes = new SinkEventAttributeSet();
        attributes.addAttribute("alt", altText);
        attributes.addAttribute("title", altText);

        sink.figureGraphics(image, attributes);
    }

    protected void sinkHeader(Sink sink, String header) {
        sink.tableHeaderCell();

        sink.text(header);

        sink.tableHeaderCell_();
    }

    protected void sinkLink(Sink sink, String text, String link) {
        sink.link(link);

        sink.text(text);

        sink.link_();
    }

    protected void sinkSectionTitle1Anchor(Sink sink, String text, String anchor) {
        sink.sectionTitle1();

        sink.text(text);

        sink.sectionTitle1_();

        sink.anchor(HtmlTools.encodeId(anchor));
        sink.anchor_();
    }

    protected void sinkSectionTitle2Anchor(Sink sink, String text, String anchor) {
        sink.sectionTitle2();
        sink.text(text);
        sink.sectionTitle2_();

        sink.anchor(HtmlTools.encodeId(anchor));
        sink.anchor_();
    }

    protected void sinkShowTypeIcon(Sink sink, String type) {
        String image = "";
        String altText = "";

        if (null == type) {
            image = "images/icon_help_sml.gif";
            altText = "?";
        } else {
            switch (type) {
                case "fix":
                    image = "images/fix.gif";
                    altText = "fix";
                    break;
                case "update":
                    image = "images/update.gif";
                    altText = "update";
                    break;
                case "add":
                    image = "images/add.gif";
                    altText = "add";
                    break;
                case "remove":
                    image = "images/remove.gif";
                    altText = "remove";
                    break;
                default:
                    break;
            }
        }

        sink.tableCell();

        sinkFigure(sink, image, altText);

        sink.tableCell_();
    }
}
