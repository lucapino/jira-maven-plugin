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
