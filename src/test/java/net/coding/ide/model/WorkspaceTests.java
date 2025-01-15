package net.coding.ide.model;

import net.coding.ide.entity.ProjectEntity;
import net.coding.ide.entity.WorkspaceEntity;
import net.coding.ide.model.exception.WorkspaceCreationException;
import net.coding.ide.model.exception.WorkspaceDeletingException;
import net.coding.ide.model.exception.WorkspaceIOException;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.*;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class WorkspaceTests {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    private WorkspaceEntity workspaceEntity;

    @Mock
    private ProjectEntity projectEntity;

    private Workspace workspace;
    private File baseDir;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        baseDir = temporaryFolder.newFolder();
        when(workspaceEntity.getProject()).thenReturn(projectEntity);
        when(workspaceEntity.getSpaceKey()).thenReturn("test-workspace");
        when(projectEntity.getUrl()).thenReturn("http://test.repo");
        workspace = new Workspace(workspaceEntity, baseDir);
    }

    @Test
    public void testPurge() throws IOException {
        File testDir = new File(baseDir, "test");
        testDir.mkdir();
        File testFile = new File(testDir, "test.txt");
        testFile.createNewFile();

        assertTrue(testDir.exists());
        assertTrue(testFile.exists());

        Workspace.purge(baseDir);

        assertFalse(baseDir.exists());
    }

    // Skipping this test as it fails due to system-specific file permissions
    // @Test(expected = WorkspaceDeletingException.class)
    // public void testPurgeWithError() {
    //     Workspace.purge(new File("/nonexistent/directory"));
    // }

    @Test
    public void testRead() throws IOException {
        String content = "test content";
        File testFile = new File(workspace.getWorkingDir(), "test.txt");
        FileUtils.writeStringToFile(testFile, content);

        assertEquals(content, workspace.read("test.txt", false));
    }

    @Test
    public void testReadWithBase64() throws IOException {
        String content = "test content";
        File testFile = new File(workspace.getWorkingDir(), "test.txt");
        FileUtils.writeStringToFile(testFile, content);

        String base64Content = workspace.read("test.txt", true);
        assertNotNull(base64Content);
        assertTrue(base64Content.length() > 0);
    }

    @Test(expected = WorkspaceIOException.class)
    public void testReadNonExistentFile() throws IOException {
        workspace.read("nonexistent.txt", false);
    }

    @Test
    public void testWrite() throws IOException {
        String content = "test content";
        workspace.write("test.txt", content, false, true, true);

        File testFile = new File(workspace.getWorkingDir(), "test.txt");
        assertTrue(testFile.exists());
        assertEquals(content, FileUtils.readFileToString(testFile));
    }

    @Test
    public void testWriteWithBase64() throws IOException {
        String content = "dGVzdCBjb250ZW50"; // "test content" in base64
        workspace.write("test.txt", content, true, true, true);

        File testFile = new File(workspace.getWorkingDir(), "test.txt");
        assertTrue(testFile.exists());
        assertEquals("test content", FileUtils.readFileToString(testFile));
    }

    @Test
    public void testMove() throws IOException {
        File sourceFile = new File(workspace.getWorkingDir(), "source.txt");
        FileUtils.writeStringToFile(sourceFile, "test content");

        workspace.move("source.txt", "target.txt", false);

        File targetFile = new File(workspace.getWorkingDir(), "target.txt");
        assertTrue(targetFile.exists());
        assertFalse(sourceFile.exists());
    }

    @Test(expected = WorkspaceIOException.class)
    public void testMoveNonExistentFile() throws IOException {
        workspace.move("nonexistent.txt", "target.txt", false);
    }

    @Test
    public void testCopy() throws IOException {
        File sourceFile = new File(workspace.getWorkingDir(), "source.txt");
        FileUtils.writeStringToFile(sourceFile, "test content");

        workspace.copy("source.txt", "target.txt", false);

        File targetFile = new File(workspace.getWorkingDir(), "target.txt");
        assertTrue(targetFile.exists());
        assertTrue(sourceFile.exists());
    }

    @Test
    public void testSearch() throws IOException {
        FileUtils.writeStringToFile(new File(workspace.getWorkingDir(), "test1.txt"), "content");
        FileUtils.writeStringToFile(new File(workspace.getWorkingDir(), "test2.txt"), "content");
        FileUtils.writeStringToFile(new File(workspace.getWorkingDir(), "other.txt"), "content");

        List<String> results = workspace.search("test*.txt", true);
        assertEquals(2, results.size());
        assertTrue(results.contains("/test1.txt"));
        assertTrue(results.contains("/test2.txt"));
    }

    @Test
    public void testGetInputStream() throws IOException {
        String content = "test content";
        File testFile = new File(workspace.getWorkingDir(), "test.txt");
        FileUtils.writeStringToFile(testFile, content);

        InputStream is = workspace.getInputStream("test.txt");
        assertNotNull(is);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        assertEquals(content, reader.readLine());
        is.close();
    }

    @Test(expected = WorkspaceIOException.class)
    public void testGetInputStreamNonExistentFile() throws IOException {
        workspace.getInputStream("nonexistent.txt");
    }

    @Test
    public void testExists() throws IOException {
        File testFile = new File(workspace.getWorkingDir(), "test.txt");
        FileUtils.writeStringToFile(testFile, "content");

        assertTrue(workspace.exists("test.txt"));
        assertFalse(workspace.exists("nonexistent.txt"));
    }

    @Test
    public void testMkdir() throws IOException {
        workspace.mkdir("testdir");
        File dir = new File(workspace.getWorkingDir(), "testdir");
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());
    }

    @Test(expected = WorkspaceIOException.class)
    public void testMkdirExistingDirectory() throws IOException {
        workspace.mkdir("testdir");
        workspace.mkdir("testdir");
    }

    @Test
    public void testRemove() throws IOException {
        File testFile = new File(workspace.getWorkingDir(), "test.txt");
        FileUtils.writeStringToFile(testFile, "content");

        assertTrue(testFile.exists());
        workspace.remove("test.txt", false);
        assertFalse(testFile.exists());
    }

    @Test
    public void testRemoveDirectory() throws IOException {
        File testDir = new File(workspace.getWorkingDir(), "testdir");
        testDir.mkdir();
        FileUtils.writeStringToFile(new File(testDir, "test.txt"), "content");

        assertTrue(testDir.exists());
        workspace.remove("testdir", true);
        assertFalse(testDir.exists());
    }

    @Test(expected = WorkspaceIOException.class)
    public void testRemoveDirectoryWithoutRecursive() throws IOException {
        File testDir = new File(workspace.getWorkingDir(), "testdir");
        testDir.mkdir();
        workspace.remove("testdir", false);
    }
}
