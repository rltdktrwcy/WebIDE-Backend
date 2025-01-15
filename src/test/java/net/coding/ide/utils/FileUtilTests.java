package net.coding.ide.utils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FileUtilTests {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File gitignoreFile;
    private File textFile;
    private File emptyFile;
    private File directory;
    private File unknownFile;
    private File htmlFile;

    @Before
    public void setUp() throws IOException {
        gitignoreFile = temporaryFolder.newFile(".gitignore");
        textFile = temporaryFolder.newFile("test.txt");
        emptyFile = temporaryFolder.newFile("empty.txt");
        directory = temporaryFolder.newFolder("testDir");
        unknownFile = temporaryFolder.newFile("noextension");
        htmlFile = temporaryFolder.newFile("test.html");

        try (FileWriter writer = new FileWriter(gitignoreFile)) {
            writer.write("*.class\n");
        }

        try (FileWriter writer = new FileWriter(textFile)) {
            writer.write("Hello World");
        }

        try (FileWriter writer = new FileWriter(htmlFile)) {
            writer.write("<html><body>Test</body></html>");
        }

        try (FileWriter writer = new FileWriter(unknownFile)) {
            writer.write("Some content");
        }
    }

    @Test
    public void testGetContentTypeForKnownFile() {
        assertEquals("text/plain", FileUtil.getContentType(gitignoreFile));
    }

    @Test
    public void testGetContentTypeForTextFile() {
        assertEquals("text/plain", FileUtil.getContentType(textFile));
    }

    @Test
    public void testGetContentTypeForHtmlFile() {
        assertEquals("text/html", FileUtil.getContentType(htmlFile));
    }

    @Test
    public void testGetContentTypeForEmptyFile() {
        assertEquals("text/plain", FileUtil.getContentType(emptyFile));
    }

    @Test
    public void testGetContentTypeForDirectory() {
        assertNull(FileUtil.getContentType(directory));
    }

    @Test
    public void testGetContentTypeForUnknownFile() {
        String contentType = FileUtil.getContentType(unknownFile);
        assertEquals("text/plain", contentType);
    }

    @Test
    public void testGetContentTypeForGemfile() throws IOException {
        File gemfile = temporaryFolder.newFile("Gemfile");
        assertEquals("text/x-ruby-bundler-gemfile", FileUtil.getContentType(gemfile));
    }

    @Test
    public void testGetContentTypeForDockerfile() throws IOException {
        File dockerfile = temporaryFolder.newFile("Dockerfile");
        assertEquals("text/x-extension-docker", FileUtil.getContentType(dockerfile));
    }
}
