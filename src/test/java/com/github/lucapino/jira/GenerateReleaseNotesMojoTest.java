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
package com.github.lucapino.jira;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GenerateReleaseNotesMojoTest extends AbstractMojoTestCase {

    private GenerateReleaseNotesMojo mojo;

    @Override
    @BeforeClass
    public void setUp() throws Exception {
        super.setUp();
        mojo = (GenerateReleaseNotesMojo) lookupMojo("generate-release-notes",
                "src/test/resources/GenerateReleaseNotesMojoTest.xml");
    }

    @Test
    public void testDoExecute() throws Exception {
        mojo.execute();
        assertTrue("Release Notes not generated", new File(
                "target/releaseNotes.txt").exists());
    }

}
