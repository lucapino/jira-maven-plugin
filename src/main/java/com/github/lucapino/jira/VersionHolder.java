/*
 * Copyright (C) 2006-2017 Hitachi Systems CBT S.p.A. All rights reserved.
 */
package com.github.lucapino.jira;

import com.atlassian.jira.rest.client.api.domain.input.VersionInput;
import java.net.URI;

/**
 *
 * @author Tagliani
 */
public class VersionHolder {

    private final VersionInput version;
    private final URI versionURI;

    public VersionHolder(VersionInput version, URI versionURI) {
        this.version = version;
        this.versionURI = versionURI;
    }

    public VersionInput getVersion() {
        return version;
    }

    public URI getVersionURI() {
        return versionURI;
    }

}
